package rs.ac.metropolitan.salon.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter koji se izvrsava JEDNOM za svaki HTTP zahtev (OncePerRequestFilter).
 *
 * Postupak:
 *  1. procita header  ->  Authorization: Bearer eyJhbGciOi...
 *  2. proveri potpis i rok trajanja tokena (JwtUtil),
 *  3. ucita korisnika iz baze (KorisnikDetailsService),
 *  4. upise Authentication objekat u SecurityContextHolder.
 *
 * Tek nakon koraka 4 Spring Security zna KO je poslao zahtev, pa moze da
 * primeni pravila iz SecurityConfig (npr. /api/admin/** samo za ROLE_ADMIN).
 *
 * Ako tokena nema ili nije ispravan, filter NE baca gresku - samo propusta
 * zahtev dalje kao neautentifikovan; odluku donosi Spring Security kasnije.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String HEADER = "Authorization";
    private static final String PREFIKS = "Bearer ";

    private final JwtUtil jwtUtil;
    private final KorisnikDetailsService korisnikDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   KorisnikDetailsService korisnikDetailsService) {
        this.jwtUtil = jwtUtil;
        this.korisnikDetailsService = korisnikDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest zahtev,
                                    @NonNull HttpServletResponse odgovor,
                                    @NonNull FilterChain lanacFiltera)
            throws ServletException, IOException {

        String token = izvuciToken(zahtev);

        // Radimo samo ako token postoji i ako korisnik jos nije autentifikovan
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.jeIspravan(token)) {
                String email = jwtUtil.izvuciEmail(token);
                UserDetails korisnik = korisnikDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken autentifikacija =
                        new UsernamePasswordAuthenticationToken(
                                korisnik,
                                null,                       // lozinka nam vise ne treba
                                korisnik.getAuthorities()); // uloge iz baze

                autentifikacija.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(zahtev));

                SecurityContextHolder.getContext().setAuthentication(autentifikacija);
                log.debug("Autentifikovan korisnik '{}' za putanju {}", email, zahtev.getRequestURI());
            }
        }

        lanacFiltera.doFilter(zahtev, odgovor);
    }

    /** Vraca sam token bez prefiksa "Bearer ", ili null ako header ne postoji. */
    private String izvuciToken(HttpServletRequest zahtev) {
        String header = zahtev.getHeader(HEADER);
        if (StringUtils.hasText(header) && header.startsWith(PREFIKS)) {
            return header.substring(PREFIKS.length()).trim();
        }
        return null;
    }
}
