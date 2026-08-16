package rs.ac.metropolitan.salon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import rs.ac.metropolitan.salon.controller.AdminController;
import rs.ac.metropolitan.salon.controller.AuthController;
import rs.ac.metropolitan.salon.controller.TerminController;
import rs.ac.metropolitan.salon.service.TerminService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "Smoke" test: proverava da se Spring kontekst uopste podize i da su
 * svi kljucni bean-ovi kreirani. Ako negde nedostaje anotacija ili
 * postoji kruzna zavisnost, ovaj test ce prvi da pukne.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Podizanje Spring konteksta")
class SalonApplicationTests {

    @Autowired private AuthController authController;
    @Autowired private TerminController terminController;
    @Autowired private AdminController adminController;
    @Autowired private TerminService terminService;

    @Test
    @DisplayName("Kontekst se uspesno ucitava sa svim kljucnim komponentama")
    void kontekstSeUcitava() {
        assertThat(authController).isNotNull();
        assertThat(terminController).isNotNull();
        assertThat(adminController).isNotNull();
        assertThat(terminService).isNotNull();
    }
}
