package rs.ac.metropolitan.salon.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import rs.ac.metropolitan.salon.dto.UslugaOdgovor;
import rs.ac.metropolitan.salon.exception.ResursNijePronadjenException;
import rs.ac.metropolitan.salon.security.JwtUtil;
import rs.ac.metropolitan.salon.security.KorisnikDetailsService;
import rs.ac.metropolitan.salon.service.KozmeticarService;
import rs.ac.metropolitan.salon.service.RecenzijaService;
import rs.ac.metropolitan.salon.service.UslugaService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * "SLICE" TEST WEB SLOJA - @WebMvcTest
 * =====================================
 *
 * Za razliku od @SpringBootTest, @WebMvcTest podize SAMO web sloj
 * (kontroler, Jackson, @RestControllerAdvice). Servisi se ne instanciraju
 * vec se ubacuju kao mock objekti (@MockBean), a baza se uopste ne dira.
 *
 * Zbog toga je ovaj test veoma brz i proverava tacno tri stvari:
 *   1) da li je ruta ispravno mapirana,
 *   2) da li kontroler prosledjuje parametre servisu,
 *   3) da li se odgovor (i greska) serijalizuje u ocekivani JSON.
 *
 * addFilters = false iskljucuje sigurnosne filtere, jer se JWT i uloge
 * testiraju u integracionim testovima (AuthControllerIT, TerminControllerIT).
 */
@WebMvcTest(controllers = JavnoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("JavnoController - @WebMvcTest slice test")
class JavnoControllerTest {

    @Autowired private MockMvc mockMvc;

    // Servisi koje kontroler koristi - zamenjeni mock objektima
    @MockBean private UslugaService uslugaService;
    @MockBean private KozmeticarService kozmeticarService;
    @MockBean private RecenzijaService recenzijaService;

    // Zavisnosti JWT filtera koji @WebMvcTest automatski ucitava (svaki Filter bean)
    @MockBean private JwtUtil jwtUtil;
    @MockBean private KorisnikDetailsService korisnikDetailsService;

    private static final UslugaOdgovor MANIKIR = new UslugaOdgovor(
            1L, "Klasican manikir", "Oblikovanje i lakiranje noktiju",
            45, new BigDecimal("1500.00"), true, 2L, "Manikir i pedikir");

    @Test
    @DisplayName("GET /api/javno/usluge vraca listu aktivnih usluga")
    void usluge_vracaKatalog() throws Exception {
        given(uslugaService.sveAktivne()).willReturn(List.of(MANIKIR));

        mockMvc.perform(get("/api/javno/usluge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].naziv").value("Klasican manikir"))
                .andExpect(jsonPath("$[0].trajanjeMinuta").value(45))
                .andExpect(jsonPath("$[0].kategorijaNaziv").value("Manikir i pedikir"));

        verify(uslugaService).sveAktivne();
    }

    @Test
    @DisplayName("GET /api/javno/usluge?kategorijaId=2 poziva filtriranje po kategoriji")
    void usluge_saKategorijom_pozivaFilter() throws Exception {
        given(uslugaService.poKategoriji(2L)).willReturn(List.of(MANIKIR));

        mockMvc.perform(get("/api/javno/usluge").param("kategorijaId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].kategorijaId").value(2));

        verify(uslugaService).poKategoriji(2L);
    }

    @Test
    @DisplayName("GET /api/javno/usluge/{id} za nepostojecu uslugu vraca 404 u nasem JSON formatu")
    void usluga_nepostojeca_vraca404() throws Exception {
        given(uslugaService.jedna(anyLong()))
                .willThrow(new ResursNijePronadjenException("Usluga", 99L));

        mockMvc.perform(get("/api/javno/usluge/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.greska").value("Not Found"))
                .andExpect(jsonPath("$.poruka").value("Usluga sa ID-em 99 ne postoji."))
                .andExpect(jsonPath("$.putanja").value("/api/javno/usluge/99"));
    }
}
