package rs.ac.metropolitan.salon.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rs.ac.metropolitan.salon.model.Korisnik;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Token provider - generise i proverava JWT tokene.
 *
 * JWT se sastoji iz tri dela odvojena tackom:
 *   HEADER.PAYLOAD.POTPIS
 *   - HEADER  : algoritam potpisa (HMAC-SHA)
 *   - PAYLOAD : podaci (subject = email, uloga, id, vreme izdavanja i isteka)
 *   - POTPIS  : HMAC-SHA heder-a i payload-a tajnim kljucem servera
 *
 * Konkretan algoritam bira jjwt na osnovu duzine kljuca: minimum je 256 bita
 * (HS256), a nas kljuc iz application.properties je duzi od 512 bita pa se
 * koristi HS512.
 *
 * Token NIJE sifrovan - svako moze da procita payload. Zbog toga u njega
 * nikada ne stavljamo lozinku niti poverljive podatke. Potpis sluzi samo
 * da niko ne moze da IZMENI sadrzaj (npr. da sebi upise ROLE_ADMIN).
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private static final String CLAIM_ULOGA = "uloga";
    private static final String CLAIM_ID = "korisnikId";

    private final SecretKey kljuc;
    private final long trajanjeMs;

    public JwtUtil(@Value("${jwt.secret}") String tajniKljuc,
                   @Value("${jwt.expiration-ms}") long trajanjeMs) {
        // HMAC-SHA zahteva kljuc duzine bar 256 bita (32 karaktera)
        this.kljuc = Keys.hmacShaKeyFor(tajniKljuc.getBytes(StandardCharsets.UTF_8));
        this.trajanjeMs = trajanjeMs;
    }

    /** Generise token za prijavljenog korisnika. */
    public String generisiToken(Korisnik korisnik) {
        Date sada = new Date();
        Date istice = new Date(sada.getTime() + trajanjeMs);

        return Jwts.builder()
                .subject(korisnik.getEmail())            // "sub" - koga token predstavlja
                .claim(CLAIM_ULOGA, korisnik.getUloga().name())
                .claim(CLAIM_ID, korisnik.getId())
                .issuedAt(sada)                          // "iat"
                .expiration(istice)                      // "exp"
                .signWith(kljuc)                         // potpis tajnim kljucem
                .compact();
    }

    /** Iz tokena vraca email korisnika (subject). */
    public String izvuciEmail(String token) {
        return procitajClaims(token).getSubject();
    }

    /** Iz tokena vraca ulogu (npr. ROLE_ADMIN). */
    public String izvuciUlogu(String token) {
        return procitajClaims(token).get(CLAIM_ULOGA, String.class);
    }

    /** Iz tokena vraca ID korisnika. */
    public Long izvuciKorisnikId(String token) {
        Number id = procitajClaims(token).get(CLAIM_ID, Number.class);
        return id == null ? null : id.longValue();
    }

    /**
     * Provera ispravnosti tokena: potpis mora odgovarati tajnom kljucu
     * i token ne sme biti istekao.
     */
    public boolean jeIspravan(String token) {
        try {
            procitajClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token je istekao: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT token nije ispravan: {}", e.getMessage());
        }
        return false;
    }

    /** Dekodira i verifikuje token; baca JwtException ako nesto nije u redu. */
    private Claims procitajClaims(String token) {
        return Jwts.parser()
                .verifyWith(kljuc)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
