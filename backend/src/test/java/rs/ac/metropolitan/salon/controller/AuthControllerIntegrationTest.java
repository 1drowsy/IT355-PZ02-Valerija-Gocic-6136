package rs.ac.metropolitan.salon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.metropolitan.salon.dto.LoginZahtev;
import rs.ac.metropolitan.salon.dto.RegistracijaZahtev;
import rs.ac.metropolitan.salon.model.Korisnik;
import rs.ac.metropolitan.salon.model.Uloga;
import rs.ac.metropolitan.salon.repository.KorisnikRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * INTEGRACIONI TESTOVI AUTENTIFIKACIJE - @SpringBootTest + MockMvc
 * ================================================================
 *
 * Za razliku od jedinicnih testova, ovde se podize CEO Spring kontekst
 * (kontroleri, servisi, repozitorijumi, Spring Security, H2 baza), pa se
 * testira ceo lanac: HTTP zahtev -> filter -> kontroler -> servis -> baza.
 *
 * @AutoConfigureMockMvc pravi MockMvc objekat koji salje "lazne" HTTP
 * zahteve bez pokretanja pravog Tomcat servera (brze i stabilnije).
 *
 * @Transactional na test klasi znaci da se svaka test metoda na kraju
 * ponistava (rollback), pa testovi ne uticu jedan na drugi.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController - integracioni testovi (/api/auth)")
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private KorisnikRepository korisnikRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String POSTOJECI_EMAIL = "postojeci@primer.rs";
    private static final String LOZINKA = "lozinka123";

    @BeforeEach
    void pripremiKorisnika() {
        korisnikRepository.deleteAll();
        korisnikRepository.save(new Korisnik(
                "Petar", "Petrovic", POSTOJECI_EMAIL,
                passwordEncoder.encode(LOZINKA),   // u bazi je BCrypt hes
                "0601234567", Uloga.ROLE_KLIJENT, false));
    }

    // ==================================================================
    //  REGISTRACIJA
    // ==================================================================

    @Test
    @DisplayName("POST /api/auth/register - uspesna registracija vraca 201 i JWT token")
    void registracija_uspesna() throws Exception {
        RegistracijaZahtev zahtev = new RegistracijaZahtev(
                "Ana", "Markovic", "ana.nova@primer.rs", "tajna123", "0641112233", true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zahtev)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tip").value("Bearer"))
                .andExpect(jsonPath("$.email").value("ana.nova@primer.rs"))
                .andExpect(jsonPath("$.uloga").value("ROLE_KLIJENT"))
                .andExpect(jsonPath("$.student").value(true));
    }

    @Test
    @DisplayName("POST /api/auth/register - lozinka se u bazi cuva kao BCrypt hes")
    void registracija_lozinkaSeHesuje() throws Exception {
        RegistracijaZahtev zahtev = new RegistracijaZahtev(
                "Ivan", "Ivic", "ivan@primer.rs", "mojaLozinka", "0601111111", false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zahtev)))
                .andExpect(status().isCreated());

        Korisnik sacuvan = korisnikRepository.findByEmail("ivan@primer.rs").orElseThrow();

        // u bazi NE sme biti otvoreni tekst lozinke
        org.assertj.core.api.Assertions.assertThat(sacuvan.getLozinka())
                .isNotEqualTo("mojaLozinka")
                .startsWith("$2a$");   // prepoznatljiv prefiks BCrypt hesa
        org.assertj.core.api.Assertions.assertThat(
                passwordEncoder.matches("mojaLozinka", sacuvan.getLozinka())).isTrue();
    }

    @Test
    @DisplayName("POST /api/auth/register - vec zauzet email vraca 400")
    void registracija_zauzetEmail_vraca400() throws Exception {
        RegistracijaZahtev zahtev = new RegistracijaZahtev(
                "Petar", "Petrovic", POSTOJECI_EMAIL, "lozinka123", "0601234567", false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zahtev)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.poruka").value(
                        org.hamcrest.Matchers.containsString("vec registrovan")));
    }

    @Test
    @DisplayName("POST /api/auth/register - nevalidni podaci vracaju 400 sa greskama po poljima")
    void registracija_nevalidniPodaci_vraca400() throws Exception {
        // prazno ime, neispravan email, prekratka lozinka
        RegistracijaZahtev zahtev = new RegistracijaZahtev(
                "", "Markovic", "ovo-nije-email", "123", "0641112233", false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zahtev)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greskeValidacije.ime").exists())
                .andExpect(jsonPath("$.greskeValidacije.email").exists())
                .andExpect(jsonPath("$.greskeValidacije.lozinka").exists());
    }

    // ==================================================================
    //  PRIJAVA
    // ==================================================================

    @Test
    @DisplayName("POST /api/auth/login - ispravni podaci vracaju 200 i JWT token")
    void login_ispravniPodaci_vracaToken() throws Exception {
        LoginZahtev zahtev = new LoginZahtev(POSTOJECI_EMAIL, LOZINKA);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zahtev)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(POSTOJECI_EMAIL))
                .andExpect(jsonPath("$.punoIme").value("Petar Petrovic"))
                .andExpect(jsonPath("$.uloga").value("ROLE_KLIJENT"));
    }

    @Test
    @DisplayName("POST /api/auth/login - pogresna lozinka vraca 401")
    void login_pogresnaLozinka_vraca401() throws Exception {
        LoginZahtev zahtev = new LoginZahtev(POSTOJECI_EMAIL, "pogresna-lozinka");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zahtev)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/auth/login - nepostojeci korisnik vraca 401")
    void login_nepostojeciKorisnik_vraca401() throws Exception {
        LoginZahtev zahtev = new LoginZahtev("ne.postoji@primer.rs", "bilosta");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zahtev)))
                .andExpect(status().isUnauthorized());
    }

    // ==================================================================
    //  ZASTICENA RUTA /api/auth/me
    // ==================================================================

    @Test
    @DisplayName("GET /api/auth/me - bez tokena vraca 401")
    void me_bezTokena_vraca401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.poruka").value(
                        org.hamcrest.Matchers.containsString("Niste prijavljeni")));
    }

    @Test
    @DisplayName("GET /api/auth/me - sa tokenom dobijenim pri prijavi vraca 200 i profil")
    void me_saTokenom_vracaProfil() throws Exception {
        // 1) prijavimo se i procitamo token iz odgovora
        String odgovor = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginZahtev(POSTOJECI_EMAIL, LOZINKA))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(odgovor).get("token").asText();

        // 2) token saljemo u Authorization headeru
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(POSTOJECI_EMAIL))
                .andExpect(jsonPath("$.uloga").value("ROLE_KLIJENT"));
    }

    @Test
    @DisplayName("GET /api/auth/me - falsifikovan token vraca 401")
    void me_neispravanToken_vraca401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer ovo.nije.validan-token"))
                .andExpect(status().isUnauthorized());
    }
}
