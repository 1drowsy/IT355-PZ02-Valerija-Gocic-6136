package rs.ac.metropolitan.salon.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rs.ac.metropolitan.salon.dto.GreskaOdgovor;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralno mesto za obradu svih izuzetaka u aplikaciji.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody, tj. presrece
 * izuzetke iz SVIH @RestController klasa i vraca JSON odgovor.
 * Zahvaljujuci ovome kontroleri i servisi nemaju try/catch blokove.
 */
@RestControllerAdvice
public class GlobalniExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalniExceptionHandler.class);

    /** 404 - trazeni resurs ne postoji. */
    @ExceptionHandler(ResursNijePronadjenException.class)
    public ResponseEntity<GreskaOdgovor> obradiNijePronadjen(ResursNijePronadjenException ex,
                                                             HttpServletRequest zahtev) {
        log.warn("404: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                GreskaOdgovor.of(404, "Not Found", ex.getMessage(), zahtev.getRequestURI()));
    }

    /** 400 - prekrseno poslovno pravilo (zauzet termin, van radnog vremena...). */
    @ExceptionHandler(PoslovnaGreskaException.class)
    public ResponseEntity<GreskaOdgovor> obradiPoslovnuGresku(PoslovnaGreskaException ex,
                                                              HttpServletRequest zahtev) {
        log.warn("400: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                GreskaOdgovor.of(400, "Bad Request", ex.getMessage(), zahtev.getRequestURI()));
    }

    /** 403 - korisnik je ulogovan, ali nema pravo na ovaj resurs. */
    @ExceptionHandler({ZabranjenPristupException.class, AccessDeniedException.class})
    public ResponseEntity<GreskaOdgovor> obradiZabranjenPristup(Exception ex,
                                                                HttpServletRequest zahtev) {
        log.warn("403: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                GreskaOdgovor.of(403, "Forbidden",
                        "Nemate dozvolu za ovu akciju.", zahtev.getRequestURI()));
    }

    /** 401 - pogresan email ili lozinka. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GreskaOdgovor> obradiAutentifikaciju(AuthenticationException ex,
                                                               HttpServletRequest zahtev) {
        log.warn("401: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                GreskaOdgovor.of(401, "Unauthorized",
                        "Pogresan email ili lozinka.", zahtev.getRequestURI()));
    }

    /**
     * 400 - pala je @Valid validacija DTO-a.
     * Vraca mapu: naziv polja -> poruka, sto frontend prikazuje uz svako polje forme.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GreskaOdgovor> obradiValidaciju(MethodArgumentNotValidException ex,
                                                          HttpServletRequest zahtev) {
        Map<String, String> greske = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            greske.put(fe.getField(), fe.getDefaultMessage());
        }
        log.warn("400 validacija: {}", greske);
        return ResponseEntity.badRequest()
                .body(GreskaOdgovor.validaciona(zahtev.getRequestURI(), greske));
    }

    /** 500 - sve ostalo (neocekivane greske). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GreskaOdgovor> obradiOstalo(Exception ex, HttpServletRequest zahtev) {
        log.error("500 - neocekivana greska", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                GreskaOdgovor.of(500, "Internal Server Error",
                        "Doslo je do greske na serveru.", zahtev.getRequestURI()));
    }
}
