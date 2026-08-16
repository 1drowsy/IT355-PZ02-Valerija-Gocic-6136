package rs.ac.metropolitan.salon.dto;

import java.time.LocalDateTime;

/**
 * Podaci o korisniku koji se salju klijentu.
 * Napomena: polje "lozinka" se NIKADA ne nalazi u odgovoru -
 * to je glavni razlog zasto se koriste DTO objekti umesto entiteta.
 */
public record KorisnikOdgovor(
        Long id,
        String ime,
        String prezime,
        String punoIme,
        String email,
        String telefon,
        String uloga,
        boolean student,
        LocalDateTime datumRegistracije
) {
}
