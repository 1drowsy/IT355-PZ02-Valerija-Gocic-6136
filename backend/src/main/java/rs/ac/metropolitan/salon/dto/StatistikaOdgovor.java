package rs.ac.metropolitan.salon.dto;

import java.math.BigDecimal;
import java.util.Map;

/** Agregirani podaci za admin panel (GET /api/admin/statistika). */
public record StatistikaOdgovor(
        long ukupnoTermina,
        Map<String, Long> terminiPoStatusu,
        BigDecimal ukupanPrihod,
        long brojKlijenata,
        long brojUsluga,
        long brojKozmeticara,
        String najtrazenijaUsluga,
        Double prosecnaOcenaSalona
) {
}
