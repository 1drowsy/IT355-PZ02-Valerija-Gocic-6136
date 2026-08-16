package rs.ac.metropolitan.salon.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.metropolitan.salon.dto.KozmeticarOdgovor;
import rs.ac.metropolitan.salon.dto.KozmeticarZahtev;
import rs.ac.metropolitan.salon.exception.PoslovnaGreskaException;
import rs.ac.metropolitan.salon.exception.ResursNijePronadjenException;
import rs.ac.metropolitan.salon.mapper.SalonMapper;
import rs.ac.metropolitan.salon.model.Kozmeticar;
import rs.ac.metropolitan.salon.model.Usluga;
import rs.ac.metropolitan.salon.repository.KozmeticarRepository;
import rs.ac.metropolitan.salon.repository.TerminRepository;
import rs.ac.metropolitan.salon.repository.UslugaRepository;
import rs.ac.metropolitan.salon.service.KozmeticarService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class KozmeticarServiceImpl implements KozmeticarService {

    private final KozmeticarRepository kozmeticarRepository;
    private final UslugaRepository uslugaRepository;
    private final TerminRepository terminRepository;

    public KozmeticarServiceImpl(KozmeticarRepository kozmeticarRepository,
                                 UslugaRepository uslugaRepository,
                                 TerminRepository terminRepository) {
        this.kozmeticarRepository = kozmeticarRepository;
        this.uslugaRepository = uslugaRepository;
        this.terminRepository = terminRepository;
    }

    // ------------------------------------------------------------ JAVNO

    @Override
    @Transactional(readOnly = true)
    public List<KozmeticarOdgovor> sviAktivni() {
        return kozmeticarRepository.findByAktivanTrue().stream()
                .map(SalonMapper::uKozmeticarOdgovor)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KozmeticarOdgovor> zaUslugu(Long uslugaId) {
        return kozmeticarRepository.findAktivneByUslugaId(uslugaId).stream()
                .map(SalonMapper::uKozmeticarOdgovor)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public KozmeticarOdgovor jedan(Long id) {
        return SalonMapper.uKozmeticarOdgovor(nadjiIliBaci(id));
    }

    // ------------------------------------------------------------ ADMIN

    @Override
    @Transactional(readOnly = true)
    public List<KozmeticarOdgovor> svi() {
        return kozmeticarRepository.findAll().stream()
                .map(SalonMapper::uKozmeticarOdgovor)
                .toList();
    }

    @Override
    @Transactional
    public KozmeticarOdgovor kreiraj(KozmeticarZahtev zahtev) {
        Kozmeticar kozmeticar = new Kozmeticar(
                zahtev.ime().trim(),
                zahtev.prezime().trim(),
                zahtev.biografija()
        );
        kozmeticar.setUsluge(ucitajUsluge(zahtev.uslugeIds()));
        kozmeticar.setAktivan(zahtev.aktivan() == null || zahtev.aktivan());

        return SalonMapper.uKozmeticarOdgovor(kozmeticarRepository.save(kozmeticar));
    }

    @Override
    @Transactional
    public KozmeticarOdgovor izmeni(Long id, KozmeticarZahtev zahtev) {
        Kozmeticar kozmeticar = nadjiIliBaci(id);

        kozmeticar.setIme(zahtev.ime().trim());
        kozmeticar.setPrezime(zahtev.prezime().trim());
        kozmeticar.setBiografija(zahtev.biografija());
        kozmeticar.setUsluge(ucitajUsluge(zahtev.uslugeIds()));
        if (zahtev.aktivan() != null) {
            kozmeticar.setAktivan(zahtev.aktivan());
        }

        return SalonMapper.uKozmeticarOdgovor(kozmeticarRepository.save(kozmeticar));
    }

    @Override
    @Transactional
    public String obrisi(Long id) {
        Kozmeticar kozmeticar = nadjiIliBaci(id);

        if (terminRepository.existsByKozmeticarId(id)) {
            kozmeticar.setAktivan(false);
            kozmeticarRepository.save(kozmeticar);
            return "Kozmeticar " + kozmeticar.getPunoIme() + " ima zakazane termine, " +
                   "pa je deaktiviran umesto obrisan.";
        }

        kozmeticarRepository.delete(kozmeticar);
        return "Kozmeticar " + kozmeticar.getPunoIme() + " je obrisan.";
    }

    // ---------------------------------------------------------- POMOCNE

    private Kozmeticar nadjiIliBaci(Long id) {
        return kozmeticarRepository.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Kozmeticar", id));
    }

    /** Ucitava usluge po ID-jevima i proverava da svaki od njih zaista postoji. */
    private Set<Usluga> ucitajUsluge(Set<Long> uslugeIds) {
        List<Usluga> pronadjene = uslugaRepository.findAllById(uslugeIds);

        if (pronadjene.size() != uslugeIds.size()) {
            throw new PoslovnaGreskaException(
                    "Jedna ili vise prosledjenih usluga ne postoji u bazi.");
        }
        return new HashSet<>(pronadjene);
    }
}
