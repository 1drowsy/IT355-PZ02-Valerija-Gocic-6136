package rs.ac.metropolitan.salon.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * "Spljosten" (flat) prikaz termina - umesto ugnjezdenih entiteta salju se
 * samo ID-jevi i nazivi. Time se izbegava beskonacna rekurzija pri
 * serijalizaciji (Termin -> Korisnik -> termini -> Termin -> ...).
 */
public record TerminOdgovor(
        Long id,
        LocalDateTime datumVremePocetka,
        LocalDateTime datumVremeKraja,
        String status,
        BigDecimal ukupnaCena,
        Integer primenjenPopust,
        String napomena,
        LocalDateTime datumKreiranja,

        Long korisnikId,
        String korisnikIme,
        String korisnikEmail,
        String korisnikTelefon,

        Long kozmeticarId,
        String kozmeticarIme,

        Long uslugaId,
        String uslugaNaziv,
        Integer trajanjeMinuta,
        BigDecimal osnovnaCena,

        boolean imaRecenziju
) {
}
