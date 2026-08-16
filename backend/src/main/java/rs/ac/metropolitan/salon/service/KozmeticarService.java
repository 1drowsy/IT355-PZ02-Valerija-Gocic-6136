package rs.ac.metropolitan.salon.service;

import rs.ac.metropolitan.salon.dto.KozmeticarOdgovor;
import rs.ac.metropolitan.salon.dto.KozmeticarZahtev;

import java.util.List;

/** CRUD nad kozmeticarima + javni prikaz tima salona. */
public interface KozmeticarService {

    // --- javno ---
    List<KozmeticarOdgovor> sviAktivni();

    /** Kozmeticari koji pruzaju zadatu uslugu (filter na formi za zakazivanje). */
    List<KozmeticarOdgovor> zaUslugu(Long uslugaId);

    KozmeticarOdgovor jedan(Long id);

    // --- admin ---
    List<KozmeticarOdgovor> svi();

    KozmeticarOdgovor kreiraj(KozmeticarZahtev zahtev);

    KozmeticarOdgovor izmeni(Long id, KozmeticarZahtev zahtev);

    /**
     * Brise kozmeticara, a ako on ima zakazane termine samo ga deaktivira.
     * Vraca poruku o tome sta je uradjeno.
     */
    String obrisi(Long id);
}
