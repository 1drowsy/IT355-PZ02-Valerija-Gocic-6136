package rs.ac.metropolitan.salon.dto;

/**
 * Odgovor na uspesnu prijavu/registraciju.
 * Frontend cuva "token" u localStorage i salje ga u Authorization headeru.
 */
public record AuthOdgovor(
        String token,
        String tip,          // uvek "Bearer"
        Long korisnikId,
        String email,
        String punoIme,
        String uloga,        // ROLE_KLIJENT ili ROLE_ADMIN
        boolean student
) {
    public static AuthOdgovor bearer(String token, KorisnikOdgovor k) {
        return new AuthOdgovor(token, "Bearer", k.id(), k.email(),
                k.punoIme(), k.uloga(), k.student());
    }
}
