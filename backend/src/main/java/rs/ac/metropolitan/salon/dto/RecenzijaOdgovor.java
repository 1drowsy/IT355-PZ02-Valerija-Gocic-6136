package rs.ac.metropolitan.salon.dto;

import java.time.LocalDateTime;

public record RecenzijaOdgovor(
        Long id,
        Integer ocena,
        String komentar,
        LocalDateTime datumKreiranja,
        Long terminId,
        Long kozmeticarId,
        String kozmeticarIme,
        String korisnikIme,
        String uslugaNaziv
) {
}
