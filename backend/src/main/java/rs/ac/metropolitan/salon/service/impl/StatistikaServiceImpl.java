package rs.ac.metropolitan.salon.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.metropolitan.salon.dto.KorisnikOdgovor;
import rs.ac.metropolitan.salon.dto.StatistikaOdgovor;
import rs.ac.metropolitan.salon.mapper.SalonMapper;
import rs.ac.metropolitan.salon.model.StatusTermina;
import rs.ac.metropolitan.salon.model.Uloga;
import rs.ac.metropolitan.salon.repository.*;
import rs.ac.metropolitan.salon.service.StatistikaService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatistikaServiceImpl implements StatistikaService {

    private final TerminRepository terminRepository;
    private final KorisnikRepository korisnikRepository;
    private final UslugaRepository uslugaRepository;
    private final KozmeticarRepository kozmeticarRepository;
    private final RecenzijaRepository recenzijaRepository;

    public StatistikaServiceImpl(TerminRepository terminRepository,
                                 KorisnikRepository korisnikRepository,
                                 UslugaRepository uslugaRepository,
                                 KozmeticarRepository kozmeticarRepository,
                                 RecenzijaRepository recenzijaRepository) {
        this.terminRepository = terminRepository;
        this.korisnikRepository = korisnikRepository;
        this.uslugaRepository = uslugaRepository;
        this.kozmeticarRepository = kozmeticarRepository;
        this.recenzijaRepository = recenzijaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public StatistikaOdgovor izracunaj() {

        // Broj termina po svakom statusu (LinkedHashMap cuva redosled enum-a)
        Map<String, Long> poStatusu = new LinkedHashMap<>();
        for (StatusTermina status : StatusTermina.values()) {
            poStatusu.put(status.name(), terminRepository.countByStatus(status));
        }

        BigDecimal prihod = terminRepository.saberiPrihodPoStatusu(StatusTermina.ZAVRSEN);
        if (prihod == null) {
            prihod = BigDecimal.ZERO;
        }

        String najtrazenija = terminRepository
                .statistikaPoUslugama(StatusTermina.OTKAZAN).stream()
                .findFirst()
                .map(red -> red.getNaziv() + " (" + red.getBroj() + "x)")
                .orElse("-");

        Double prosecnaOcena = recenzijaRepository.prosecnaOcenaSalona();
        Double zaokruzena = (prosecnaOcena == null) ? null
                : BigDecimal.valueOf(prosecnaOcena).setScale(2, RoundingMode.HALF_UP).doubleValue();

        return new StatistikaOdgovor(
                terminRepository.count(),
                poStatusu,
                prihod.setScale(2, RoundingMode.HALF_UP),
                korisnikRepository.countByUloga(Uloga.ROLE_KLIJENT),
                uslugaRepository.count(),
                kozmeticarRepository.count(),
                najtrazenija,
                zaokruzena
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<KorisnikOdgovor> sviKorisnici() {
        return korisnikRepository.findAll().stream()
                .map(SalonMapper::uKorisnikOdgovor)
                .toList();
    }
}
