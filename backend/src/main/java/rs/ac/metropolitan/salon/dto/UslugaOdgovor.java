package rs.ac.metropolitan.salon.dto;

import java.math.BigDecimal;

public record UslugaOdgovor(
        Long id,
        String naziv,
        String opis,
        Integer trajanjeMinuta,
        BigDecimal cena,
        boolean aktivna,
        Long kategorijaId,
        String kategorijaNaziv
) {
}
