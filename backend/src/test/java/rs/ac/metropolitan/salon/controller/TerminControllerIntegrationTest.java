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
import rs.ac.metropolitan.salon.dto.PromenaStatusaZahtev;
import rs.ac.metropolitan.salon.dto.TerminZahtev;
import rs.ac.metropolitan.salon.model.*;
import rs.ac.metropolitan.salon.repository.*;
import rs.ac.metropolitan.salon.security.JwtUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * INTEGRACIONI TESTOVI ZASTICENIH RUTA - @SpringBootTest + MockMvc
 * ================================================================
 *
 * Ovde se proverava kombinacija JWT autentifikacije, RBAC autorizacije i
 * poslovne logike zakazivanja - dakle sve sto se trazi u zahtevima projekta:
 *   - kreiranje resursa BEZ tokena          -> 401
 *   - kreiranje resursa SA validnim tokenom -> 201
 *   - klijent na admin ruti                 -> 403
 *   - admin na admin ruti                   -> 200
 *   - preklapanje termina                   -> 400
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("TerminController i AdminController - integracioni testovi sa JWT-om")
class TerminControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private KorisnikRepository korisnikRepository;
    @Autowired private KategorijaUslugeRepository kategorijaRepository;
    @Autowired private UslugaRepository uslugaRepository;
    @Autowired private KozmeticarRepository kozmeticarRepository;
    @Autowired private TerminRepository terminRepository;
    @Autowired private RecenzijaRepository recenzijaRepository;

    private String tokenKlijenta;
    private String tokenAdmina;
    private Long uslugaId;
    private Long kozmeticarId;
    private LocalDateTime sutraU10;

    @BeforeEach
    void pripremiPodatke() {
        // ciscenje redosledom koji postuje strane kljuceve
        recenzijaRepository.deleteAll();
        terminRepository.deleteAll();
        kozmeticarRepository.deleteAll();
        uslugaRepository.deleteAll();
        kategorijaRepository.deleteAll();
        korisnikRepository.deleteAll();

        KategorijaUsluge kategorija = kategorijaRepository.save(
                new KategorijaUsluge("Nega lica", "Tretmani lica"));

        Usluga usluga = uslugaRepository.save(new Usluga(
                "Dubinsko ciscenje lica", "Opis usluge",
                60, new BigDecimal("3000.00"), kategorija));
        uslugaId = usluga.getId();

        Kozmeticar kozmeticar = new Kozmeticar("Milica", "Jovanovic", "Biografija");
        kozmeticar.setUsluge(Set.of(usluga));
        kozmeticarId = kozmeticarRepository.save(kozmeticar).getId();

        Korisnik klijent = korisnikRepository.save(new Korisnik(
                "Ana", "Markovic", "ana@primer.rs",
                passwordEncoder.encode("klijent123"),
                "0601112233", Uloga.ROLE_KLIJENT, false));

        Korisnik admin = korisnikRepository.save(new Korisnik(
                "Valerija", "Gocic", "admin@salon.rs",
                passwordEncoder.encode("admin123"),
                "0641234567", Uloga.ROLE_ADMIN, false));

        tokenKlijenta = jwtUtil.generisiToken(klijent);
        tokenAdmina = jwtUtil.generisiToken(admin);

        sutraU10 = LocalDate.now().plusDays(1).atTime(10, 0);
    }

    // ==================================================================
    //  AUTENTIFIKACIJA NA ZASTICENOJ RUTI
    // ==================================================================

    @Test
    @DisplayName("POST /api/termini bez JWT tokena vraca 401 Unauthorized")
    void zakazivanje_bezTokena_vraca401() throws Exception {
        TerminZahtev zahtev = new TerminZahtev(uslugaId, kozmeticarId, sutraU10, null);

        mockMvc.perform(post("/api/termini")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zahtev)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/termini sa neispravnim tokenom vraca 401 Unauthorized")
    void zakazivanje_saNeispravnimTokenom_vraca401() throws Exception {
        TerminZahtev zahtev = new TerminZahtev(uslugaId, kozmeticarId, sutraU10, null);

        mockMvc.perform(post("/api/termini")
                        .header("Authorization", "Bearer neispravan.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zahtev)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/termini sa validnim tokenom vraca 201 i automatski obracunate podatke")
    void zakazivanje_saValidnimTokenom_vraca201() throws Exception {
        TerminZahtev zahtev = new TerminZahtev(
                uslugaId, kozmeticarId, sutraU10, "Osetljiva koza");

        mockMvc.perform(post("/api/termini")
                        .header("Authorization", "Bearer " + tokenKlijenta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zahtev)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("ZAKAZAN"))
                // backend je sam izracunao kraj (10:00 + 60 min) i cenu
                .andExpect(jsonPath("$.datumVremeKraja").value(
                        org.hamcrest.Matchers.startsWith(
                                sutraU10.plusMinutes(60).toLocalDate() + "T11:00")))
                .andExpect(jsonPath("$.ukupnaCena").value(3000.00))
                .andExpect(jsonPath("$.primenjenPopust").value(0))
                .andExpect(jsonPath("$.korisnikEmail").value("ana@primer.rs"));
    }

    // ==================================================================
    //  POSLOVNA PRAVILA KROZ HTTP SLOJ
    // ==================================================================

    @Test
    @DisplayName("Drugi termin koji se preklapa sa postojecim vraca 400 Bad Request")
    void zakazivanje_preklapanje_vraca400() throws Exception {
        TerminZahtev prvi = new TerminZahtev(uslugaId, kozmeticarId, sutraU10, null);

        mockMvc.perform(post("/api/termini")
                        .header("Authorization", "Bearer " + tokenKlijenta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prvi)))
                .andExpect(status().isCreated());

        // drugi termin pocinje 30 min kasnije - upada u prvi (10:00-11:00)
        TerminZahtev drugi = new TerminZahtev(
                uslugaId, kozmeticarId, sutraU10.plusMinutes(30), null);

        mockMvc.perform(post("/api/termini")
                        .header("Authorization", "Bearer " + tokenKlijenta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drugi)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.poruka").value(
                        org.hamcrest.Matchers.containsString("zauzet")));
    }

    @Test
    @DisplayName("Termin u proslosti pada na @Valid validaciji i vraca 400")
    void zakazivanje_uProslosti_vraca400() throws Exception {
        TerminZahtev zahtev = new TerminZahtev(
                uslugaId, kozmeticarId, LocalDateTime.now().minusDays(1), null);

        mockMvc.perform(post("/api/termini")
                        .header("Authorization", "Bearer " + tokenKlijenta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zahtev)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.greskeValidacije.datumVremePocetka").exists());
    }

    @Test
    @DisplayName("GET /api/termini/moji vraca samo termine prijavljenog klijenta")
    void mojiTermini_vracaSamoSopstvene() throws Exception {
        TerminZahtev zahtev = new TerminZahtev(uslugaId, kozmeticarId, sutraU10, null);

        mockMvc.perform(post("/api/termini")
                        .header("Authorization", "Bearer " + tokenKlijenta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zahtev)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/termini/moji")
                        .header("Authorization", "Bearer " + tokenKlijenta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].korisnikEmail").value("ana@primer.rs"));

        // admin nema svojih termina
        mockMvc.perform(get("/api/termini/moji")
                        .header("Authorization", "Bearer " + tokenAdmina))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================================================================
    //  RBAC - AUTORIZACIJA PO ULOGAMA
    // ==================================================================

    @Test
    @DisplayName("Klijent na /api/admin/termini dobija 403 Forbidden")
    void adminRuta_saKlijentskimTokenom_vraca403() throws Exception {
        mockMvc.perform(get("/api/admin/termini")
                        .header("Authorization", "Bearer " + tokenKlijenta))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Admin na /api/admin/termini dobija 200 OK")
    void adminRuta_saAdminTokenom_vraca200() throws Exception {
        mockMvc.perform(get("/api/admin/termini")
                        .header("Authorization", "Bearer " + tokenAdmina))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin menja status termina ZAKAZAN -> POTVRDJEN")
    void adminMenjaStatusTermina() throws Exception {
        // klijent zakazuje
        String odgovor = mockMvc.perform(post("/api/termini")
                        .header("Authorization", "Bearer " + tokenKlijenta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TerminZahtev(uslugaId, kozmeticarId, sutraU10, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long terminId = objectMapper.readTree(odgovor).get("id").asLong();

        // admin potvrdjuje
        mockMvc.perform(put("/api/admin/termini/" + terminId + "/status")
                        .header("Authorization", "Bearer " + tokenAdmina)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PromenaStatusaZahtev(StatusTermina.POTVRDJEN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POTVRDJEN"));
    }

    @Test
    @DisplayName("Klijent ne moze da promeni status termina (403)")
    void klijentNeMozeDaMenjaStatus() throws Exception {
        String odgovor = mockMvc.perform(post("/api/termini")
                        .header("Authorization", "Bearer " + tokenKlijenta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TerminZahtev(uslugaId, kozmeticarId, sutraU10, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long terminId = objectMapper.readTree(odgovor).get("id").asLong();

        mockMvc.perform(put("/api/admin/termini/" + terminId + "/status")
                        .header("Authorization", "Bearer " + tokenKlijenta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PromenaStatusaZahtev(StatusTermina.ZAVRSEN))))
                .andExpect(status().isForbidden());
    }

    // ==================================================================
    //  JAVNE RUTE
    // ==================================================================

    @Test
    @DisplayName("GET /api/javno/usluge je dostupno bez prijave")
    void javnaRuta_bezTokena_vraca200() throws Exception {
        mockMvc.perform(get("/api/javno/usluge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].naziv").value("Dubinsko ciscenje lica"));
    }
}
