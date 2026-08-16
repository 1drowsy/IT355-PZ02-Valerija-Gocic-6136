package rs.ac.metropolitan.salon.dto;

import java.time.LocalDateTime;

/**
 * Jedan slobodan "slot" u rasporedu kozmeticara.
 * Vraca ga GET /api/termini/dostupnost i koristi se na formi za zakazivanje.
 */
public record SlobodanTerminOdgovor(
        LocalDateTime pocetak,
        LocalDateTime kraj
) {
}
