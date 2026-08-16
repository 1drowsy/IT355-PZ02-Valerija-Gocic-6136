package rs.ac.metropolitan.salon.dto;

import java.util.List;

public record KozmeticarOdgovor(
        Long id,
        String ime,
        String prezime,
        String punoIme,
        String biografija,
        Double ocena,
        boolean aktivan,
        List<UslugaOdgovor> usluge
) {
}
