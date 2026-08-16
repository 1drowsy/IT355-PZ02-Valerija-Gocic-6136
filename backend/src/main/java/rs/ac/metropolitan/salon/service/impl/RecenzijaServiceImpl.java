package rs.ac.metropolitan.salon.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.metropolitan.salon.dto.RecenzijaOdgovor;
import rs.ac.metropolitan.salon.dto.RecenzijaZahtev;
import rs.ac.metropolitan.salon.exception.PoslovnaGreskaException;
import rs.ac.metropolitan.salon.exception.ResursNijePronadjenException;
import rs.ac.metropolitan.salon.exception.ZabranjenPristupException;
import rs.ac.metropolitan.salon.mapper.SalonMapper;
import rs.ac.metropolitan.salon.model.Korisnik;
import rs.ac.metropolitan.salon.model.Kozmeticar;
import rs.ac.metropolitan.salon.model.Recenzija;
import rs.ac.metropolitan.salon.model.StatusTermina;
import rs.ac.metropolitan.salon.model.Termin;
import rs.ac.metropolitan.salon.repository.KorisnikRepository;
import rs.ac.metropolitan.salon.repository.KozmeticarRepository;
import rs.ac.metropolitan.salon.repository.RecenzijaRepository;
import rs.ac.metropolitan.salon.repository.TerminRepository;
import rs.ac.metropolitan.salon.service.RecenzijaService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RecenzijaServiceImpl implements RecenzijaService {

    private final RecenzijaRepository recenzijaRepository;
    private final TerminRepository terminRepository;
    private final KorisnikRepository korisnikRepository;
    private final KozmeticarRepository kozmeticarRepository;

    public RecenzijaServiceImpl(RecenzijaRepository recenzijaRepository,
                                TerminRepository terminRepository,
                                KorisnikRepository korisnikRepository,
                                KozmeticarRepository kozmeticarRepository) {
        this.recenzijaRepository = recenzijaRepository;
        this.terminRepository = terminRepository;
        this.korisnikRepository = korisnikRepository;
        this.kozmeticarRepository = kozmeticarRepository;
    }

    /**
     * Kreiranje recenzije i azuriranje prosecne ocene kozmeticara moraju
     * biti u ISTOJ transakciji - zato @Transactional. Ako azuriranje ocene
     * pukne, recenzija se nece upisati i podaci ostaju konzistentni.
     */
    @Override
    @Transactional
    public RecenzijaOdgovor kreiraj(String emailKlijenta, RecenzijaZahtev zahtev) {

        Termin termin = terminRepository.findById(zahtev.terminId())
                .orElseThrow(() -> new ResursNijePronadjenException("Termin", zahtev.terminId()));

        if (!termin.getKorisnik().getEmail().equalsIgnoreCase(emailKlijenta)) {
            throw new ZabranjenPristupException("Mozete oceniti samo sopstvene termine.");
        }
        if (termin.getStatus() != StatusTermina.ZAVRSEN) {
            throw new PoslovnaGreskaException(
                    "Recenzija se moze ostaviti tek kada termin dobije status ZAVRSEN.");
        }
        if (recenzijaRepository.existsByTerminId(termin.getId())) {
            throw new PoslovnaGreskaException("Ovaj termin je vec ocenjen.");
        }

        Recenzija recenzija = new Recenzija(zahtev.ocena(), zahtev.komentar(), termin);
        Recenzija sacuvana = recenzijaRepository.save(recenzija);

        azurirajProsecnuOcenu(termin.getKozmeticar());

        return SalonMapper.uRecenzijaOdgovor(sacuvana);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecenzijaOdgovor> moje(String emailKlijenta) {
        Korisnik klijent = korisnikRepository.findByEmail(emailKlijenta)
                .orElseThrow(() -> new ResursNijePronadjenException(
                        "Korisnik sa emailom " + emailKlijenta + " ne postoji."));

        return SalonMapper.uRecenzijaOdgovore(
                recenzijaRepository.findByKorisnikIdOrderByDatumKreiranjaDesc(klijent.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecenzijaOdgovor> zaKozmeticara(Long kozmeticarId) {
        if (!kozmeticarRepository.existsById(kozmeticarId)) {
            throw new ResursNijePronadjenException("Kozmeticar", kozmeticarId);
        }
        return SalonMapper.uRecenzijaOdgovore(
                recenzijaRepository.findByKozmeticarIdOrderByDatumKreiranjaDesc(kozmeticarId));
    }

    /** Prepisuje prosecnu ocenu kozmeticara, zaokruzenu na jednu decimalu. */
    private void azurirajProsecnuOcenu(Kozmeticar kozmeticar) {
        Double prosek = recenzijaRepository.prosecnaOcenaKozmeticara(kozmeticar.getId());

        double zaokruzeno = (prosek == null) ? 0.0
                : BigDecimal.valueOf(prosek).setScale(1, RoundingMode.HALF_UP).doubleValue();

        kozmeticar.setOcena(zaokruzeno);
        kozmeticarRepository.save(kozmeticar);
    }
}
