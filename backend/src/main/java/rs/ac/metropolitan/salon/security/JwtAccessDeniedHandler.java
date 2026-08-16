package rs.ac.metropolitan.salon.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import rs.ac.metropolitan.salon.dto.GreskaOdgovor;

import java.io.IOException;

/**
 * Korisnik JESTE prijavljen, ali nema odgovarajucu ulogu
 * (npr. ROLE_KLIJENT pokusava da otvori /api/admin/statistika).
 * Vraca 403 Forbidden u JSON formatu.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void handle(HttpServletRequest zahtev,
                       HttpServletResponse odgovor,
                       AccessDeniedException izuzetak) throws IOException {

        odgovor.setStatus(HttpServletResponse.SC_FORBIDDEN);
        odgovor.setContentType(MediaType.APPLICATION_JSON_VALUE);
        odgovor.setCharacterEncoding("UTF-8");

        GreskaOdgovor telo = GreskaOdgovor.of(403, "Forbidden",
                "Nemate potrebnu ulogu za pristup ovom resursu.", zahtev.getRequestURI());

        objectMapper.writeValue(odgovor.getOutputStream(), telo);
    }
}
