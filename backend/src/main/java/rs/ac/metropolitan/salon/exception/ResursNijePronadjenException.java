package rs.ac.metropolitan.salon.exception;

/**
 * Baca se kada trazeni zapis ne postoji u bazi.
 * GlobalniExceptionHandler ga prevodi u HTTP 404 Not Found.
 */
public class ResursNijePronadjenException extends RuntimeException {

    public ResursNijePronadjenException(String poruka) {
        super(poruka);
    }

    /** Pomocni konstruktor: new ResursNijePronadjenException("Usluga", 5) */
    public ResursNijePronadjenException(String resurs, Long id) {
        super(resurs + " sa ID-em " + id + " ne postoji.");
    }
}
