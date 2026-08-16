package rs.ac.metropolitan.salon.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import rs.ac.metropolitan.salon.dto.GreskaOdgovor;

import java.io.IOException;

/**
 * Sta se desava kada neautentifikovan korisnik pokusa da otvori zasticenu rutu?
 *
 * Bez ove klase Spring Security bi vratio HTML stranicu za prijavu, sto je
 * beskorisno za REST API. Ovde umesto toga vracamo 401 i JSON u istom
 * formatu koji koristi i GlobalniExceptionHandler.
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();   // podrska za LocalDateTime

    @Override
    public void commence(HttpServletRequest zahtev,
                         HttpServletResponse odgovor,
                         AuthenticationException izuzetak) throws IOException {

        odgovor.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        odgovor.setContentType(MediaType.APPLICATION_JSON_VALUE);
        odgovor.setCharacterEncoding("UTF-8");

        GreskaOdgovor telo = GreskaOdgovor.of(401, "Unauthorized",
                "Niste prijavljeni ili je JWT token istekao.", zahtev.getRequestURI());

        objectMapper.writeValue(odgovor.getOutputStream(), telo);
    }
}
