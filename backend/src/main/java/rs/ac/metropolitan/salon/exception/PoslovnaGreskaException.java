package rs.ac.metropolitan.salon.exception;

/**
 * Baca se kada je zahtev tehnicki ispravan, ali krsi poslovno pravilo
 * (npr. kozmeticar je zauzet, termin je van radnog vremena, email je zauzet).
 *
 * GlobalniExceptionHandler ga prevodi u HTTP 400 Bad Request.
 */
public class PoslovnaGreskaException extends RuntimeException {

    public PoslovnaGreskaException(String poruka) {
        super(poruka);
    }
}
