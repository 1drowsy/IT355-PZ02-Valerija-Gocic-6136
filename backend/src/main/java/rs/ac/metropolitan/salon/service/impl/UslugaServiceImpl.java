package rs.ac.metropolitan.salon.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.metropolitan.salon.dto.KategorijaOdgovor;
import rs.ac.metropolitan.salon.dto.UslugaOdgovor;
import rs.ac.metropolitan.salon.dto.UslugaZahtev;
import rs.ac.metropolitan.salon.exception.ResursNijePronadjenException;
import rs.ac.metropolitan.salon.mapper.SalonMapper;
import rs.ac.metropolitan.salon.model.KategorijaUsluge;
import rs.ac.metropolitan.salon.model.Usluga;
import rs.ac.metropolitan.salon.repository.KategorijaUslugeRepository;
import rs.ac.metropolitan.salon.repository.TerminRepository;
import rs.ac.metropolitan.salon.repository.UslugaRepository;
import rs.ac.metropolitan.salon.service.UslugaService;

import java.util.List;

@Service
public class UslugaServiceImpl implements UslugaService {

    private final UslugaRepository uslugaRepository;
    private final KategorijaUslugeRepository kategorijaRepository;
    private final TerminRepository terminRepository;

    /**
     * Konstruktorska injekcija zavisnosti (preporucen nacin u Spring-u):
     * polja mogu biti final, a klasa se u testu lako instancira sa mock objektima.
     */
    public UslugaServiceImpl(UslugaRepository uslugaRepository,
                             KategorijaUslugeRepository kategorijaRepository,
                             TerminRepository terminRepository) {
        this.uslugaRepository = uslugaRepository;
        this.kategorijaRepository = kategorijaRepository;
        this.terminRepository = terminRepository;
    }

    // ------------------------------------------------------------ JAVNO

    /**
     * readOnly = true je optimizacija: Hibernate ne pravi snimke objekata
     * za proveru izmena (dirty checking), pa je citanje brze.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UslugaOdgovor> sveAktivne() {
        return SalonMapper.uUslugaOdgovore(uslugaRepository.findByAktivnaTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UslugaOdgovor> poKategoriji(Long kategorijaId) {
        return SalonMapper.uUslugaOdgovore(
                uslugaRepository.findByKategorijaIdAndAktivnaTrue(kategorijaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UslugaOdgovor> pretrazi(String naziv) {
        if (naziv == null || naziv.isBlank()) {
            return sveAktivne();
        }
        return SalonMapper.uUslugaOdgovore(
                uslugaRepository.findByNazivContainingIgnoreCaseAndAktivnaTrue(naziv.trim()));
    }

    @Override
    @Transactional(readOnly = true)
    public UslugaOdgovor jedna(Long id) {
        return SalonMapper.uUslugaOdgovor(nadjiUsluguIliBaci(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KategorijaOdgovor> sveKategorije() {
        return kategorijaRepository.findAll().stream()
                .map(SalonMapper::uKategorijaOdgovor)
                .toList();
    }

    // ------------------------------------------------------------ ADMIN

    @Override
    @Transactional(readOnly = true)
    public List<UslugaOdgovor> sve() {
        return SalonMapper.uUslugaOdgovore(uslugaRepository.findAll());
    }

    @Override
    @Transactional
    public UslugaOdgovor kreiraj(UslugaZahtev zahtev) {
        KategorijaUsluge kategorija = nadjiKategorijuIliBaci(zahtev.kategorijaId());

        Usluga usluga = new Usluga(
                zahtev.naziv().trim(),
                zahtev.opis(),
                zahtev.trajanjeMinuta(),
                zahtev.cena(),
                kategorija
        );
        usluga.setAktivna(zahtev.aktivna() == null || zahtev.aktivna());

        return SalonMapper.uUslugaOdgovor(uslugaRepository.save(usluga));
    }

    @Override
    @Transactional
    public UslugaOdgovor izmeni(Long id, UslugaZahtev zahtev) {
        Usluga usluga = nadjiUsluguIliBaci(id);
        KategorijaUsluge kategorija = nadjiKategorijuIliBaci(zahtev.kategorijaId());

        usluga.setNaziv(zahtev.naziv().trim());
        usluga.setOpis(zahtev.opis());
        usluga.setTrajanjeMinuta(zahtev.trajanjeMinuta());
        usluga.setCena(zahtev.cena());
        usluga.setKategorija(kategorija);
        if (zahtev.aktivna() != null) {
            usluga.setAktivna(zahtev.aktivna());
        }

        return SalonMapper.uUslugaOdgovor(uslugaRepository.save(usluga));
    }

    /**
     * "Meko" brisanje: ako usluga ima zakazane termine, fizicko brisanje bi
     * prekrsilo strani kljuc u tabeli termin. Zato se takva usluga samo
     * deaktivira - nestaje iz kataloga, a istorija termina ostaje netaknuta.
     */
    @Override
    @Transactional
    public String obrisi(Long id) {
        Usluga usluga = nadjiUsluguIliBaci(id);

        if (terminRepository.existsByUslugaId(id)) {
            usluga.setAktivna(false);
            uslugaRepository.save(usluga);
            return "Usluga '" + usluga.getNaziv() + "' ima zakazane termine, " +
                   "pa je deaktivirana umesto obrisana.";
        }

        uslugaRepository.delete(usluga);
        return "Usluga '" + usluga.getNaziv() + "' je obrisana.";
    }

    // ---------------------------------------------------------- POMOCNE

    private Usluga nadjiUsluguIliBaci(Long id) {
        return uslugaRepository.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Usluga", id));
    }

    private KategorijaUsluge nadjiKategorijuIliBaci(Long id) {
        return kategorijaRepository.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Kategorija usluge", id));
    }
}
