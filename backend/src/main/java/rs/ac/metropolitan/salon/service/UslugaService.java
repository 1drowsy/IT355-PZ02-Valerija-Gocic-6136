package rs.ac.metropolitan.salon.service;

import rs.ac.metropolitan.salon.dto.KategorijaOdgovor;
import rs.ac.metropolitan.salon.dto.UslugaOdgovor;
import rs.ac.metropolitan.salon.dto.UslugaZahtev;

import java.util.List;

/** CRUD nad uslugama + javni katalog. */
public interface UslugaService {

    // --- javno ---
    List<UslugaOdgovor> sveAktivne();

    List<UslugaOdgovor> poKategoriji(Long kategorijaId);

    List<UslugaOdgovor> pretrazi(String naziv);

    UslugaOdgovor jedna(Long id);

    List<KategorijaOdgovor> sveKategorije();

    // --- admin ---
    List<UslugaOdgovor> sve();

    UslugaOdgovor kreiraj(UslugaZahtev zahtev);

    UslugaOdgovor izmeni(Long id, UslugaZahtev zahtev);

    /**
     * Brise uslugu, a ako ona ima zakazane termine samo je deaktivira
     * (da se ne prekrsi strani kljuc). Vraca poruku o tome sta je uradjeno.
     */
    String obrisi(Long id);
}
