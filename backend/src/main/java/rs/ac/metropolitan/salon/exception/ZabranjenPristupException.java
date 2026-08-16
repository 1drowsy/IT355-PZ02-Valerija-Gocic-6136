package rs.ac.metropolitan.salon.exception;

/**
 * Baca se kada je korisnik ulogovan, ali pokusava da pristupi tudjem resursu
 * (npr. klijent A hoce da otkaze termin klijenta B).
 *
 * GlobalniExceptionHandler ga prevodi u HTTP 403 Forbidden.
 */
public class ZabranjenPristupException extends RuntimeException {

    public ZabranjenPristupException(String poruka) {
        super(poruka);
    }
}
