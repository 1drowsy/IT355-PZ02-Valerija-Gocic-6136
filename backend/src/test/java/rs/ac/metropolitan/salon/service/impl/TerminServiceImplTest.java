package rs.ac.metropolitan.salon.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.ac.metropolitan.salon.config.SalonPodesavanja;
import rs.ac.metropolitan.salon.dto.TerminOdgovor;
import rs.ac.metropolitan.salon.dto.TerminZahtev;
import rs.ac.metropolitan.salon.exception.PoslovnaGreskaException;
import rs.ac.metropolitan.salon.exception.ResursNijePronadjenException;
import rs.ac.metropolitan.salon.exception.ZabranjenPristupException;
import rs.ac.metropolitan.salon.model.*;
import rs.ac.metropolitan.salon.repository.KorisnikRepository;
import rs.ac.metropolitan.salon.repository.KozmeticarRepository;
import rs.ac.metropolitan.salon.repository.TerminRepository;
import rs.ac.metropolitan.salon.repository.UslugaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * JEDINICNI (UNIT) TESTOVI SERVISNOG SLOJA - JUnit 5 + Mockito
 * =============================================================
 *
 * Testira se ISKLJUCIVO poslovna logika klase TerminServiceImpl.
 * Baza podataka, Spring kontekst i HTTP sloj se NE podizu - repozitorijumi
 * su zamenjeni mock objektima (@Mock), pa su testovi brzi i deterministicki.
 *
 * Obrazac svakog testa je "Given - When - Then":
 *   given (priprema)  -> namestimo sta mock objekti vracaju
 *   when  (akcija)    -> pozovemo metodu koja se testira
 *   then  (provera)   -> proverimo rezultat / izuzetak / interakcije
 */
@ExtendWith(MockitoExtension.class)   // ukljucuje Mockito u JUnit 5 zivotni ciklus
@DisplayName("TerminService - jedinicni testovi poslovne logike")
class TerminServiceImplTest {

    // --- laznjaci (mock objekti) umesto pravih repozitorijuma ---
    @Mock private TerminRepository terminRepository;
    @Mock private KorisnikRepository korisnikRepository;
    @Mock private UslugaRepository uslugaRepository;
    @Mock private KozmeticarRepository kozmeticarRepository;

    /** Podesavanja su obican objekat (nije mock) jer sadrze samo vrednosti. */
    private final SalonPodesavanja podesavanja =
            new SalonPodesavanja("08:00", "20:00", 10, 5, 5, 2);

    private TerminServiceImpl terminService;

    // --- test podaci ---
    private Korisnik klijent;
    private Kozmeticar kozmeticar;
    private Usluga usluga;
    private LocalDateTime sutraU10;

    @BeforeEach
    void pripremi() {
        // Servis se pravi rucno - konstruktorska injekcija to omogucava bez Spring-a
        terminService = new TerminServiceImpl(
                terminRepository, korisnikRepository, uslugaRepository,
                kozmeticarRepository, podesavanja);

        KategorijaUsluge kategorija = new KategorijaUsluge("Nega lica", "Tretmani lica");
        kategorija.setId(1L);

        usluga = new Usluga("Dubinsko ciscenje lica", "Opis",
                60, new BigDecimal("3000.00"), kategorija);
        usluga.setId(10L);

        kozmeticar = new Kozmeticar("Milica", "Jovanovic", "Biografija");
        kozmeticar.setId(20L);
        Set<Usluga> usluge = new HashSet<>();
        usluge.add(usluga);
        kozmeticar.setUsluge(usluge);

        klijent = new Korisnik("Ana", "Markovic", "ana@primer.rs", "hes",
                "0601112233", Uloga.ROLE_KLIJENT, false);
        klijent.setId(30L);

        // Sutra u 10:00 - uvek u buducnosti i unutar radnog vremena (08-20)
        sutraU10 = LocalDate.now().plusDays(1).atTime(10, 0);
    }

    // ==================================================================
    //  1. USPESNO ZAKAZIVANJE
    // ==================================================================

    @Test
    @DisplayName("Uspesno zakazivanje racuna kraj termina i cuva termin sa statusom ZAKAZAN")
    void zakazi_uspesno_racunaKrajTerminaIStatus() {
        // given
        pripremiOsnovneMockove();
        when(terminRepository.postojiPreklapanje(eq(20L), any(), any(), eq(StatusTermina.OTKAZAN)))
                .thenReturn(false);
        when(terminRepository.countByKorisnikIdAndStatus(30L, StatusTermina.ZAVRSEN))
                .thenReturn(0L);
        when(terminRepository.save(any(Termin.class)))
                .thenAnswer(poziv -> {
                    Termin t = poziv.getArgument(0);
                    t.setId(100L);
                    return t;
                });

        TerminZahtev zahtev = new TerminZahtev(10L, 20L, sutraU10, "Osetljiva koza");

        // when
        TerminOdgovor odgovor = terminService.zakazi("ana@primer.rs", zahtev);

        // then - vreme kraja je pocetak + trajanje usluge (60 min)
        assertThat(odgovor.datumVremePocetka()).isEqualTo(sutraU10);
        assertThat(odgovor.datumVremeKraja()).isEqualTo(sutraU10.plusMinutes(60));
        assertThat(odgovor.status()).isEqualTo("ZAKAZAN");
        assertThat(odgovor.uslugaNaziv()).isEqualTo("Dubinsko ciscenje lica");
        assertThat(odgovor.kozmeticarIme()).isEqualTo("Milica Jovanovic");

        // provera da je termin zaista prosledjen repozitorijumu na cuvanje
        ArgumentCaptor<Termin> hvatac = ArgumentCaptor.forClass(Termin.class);
        verify(terminRepository).save(hvatac.capture());
        assertThat(hvatac.getValue().getStatus()).isEqualTo(StatusTermina.ZAKAZAN);
        assertThat(hvatac.getValue().getKorisnik().getEmail()).isEqualTo("ana@primer.rs");
    }

    // ==================================================================
    //  2. ZAUZET TERMIN (preklapanje)
    // ==================================================================

    @Test
    @DisplayName("Kada je kozmeticar zauzet, baca se PoslovnaGreskaException i nista se ne cuva")
    void zakazi_kadaJeKozmeticarZauzet_bacaIzuzetak() {
        // given - repozitorijum javlja da postoji preklapanje
        pripremiOsnovneMockove();
        when(terminRepository.postojiPreklapanje(eq(20L), any(), any(), eq(StatusTermina.OTKAZAN)))
                .thenReturn(true);

        TerminZahtev zahtev = new TerminZahtev(10L, 20L, sutraU10, null);

        // when + then
        assertThatThrownBy(() -> terminService.zakazi("ana@primer.rs", zahtev))
                .isInstanceOf(PoslovnaGreskaException.class)
                .hasMessageContaining("zauzet");

        // termin NE sme biti sacuvan
        verify(terminRepository, never()).save(any(Termin.class));
    }

    // ==================================================================
    //  3. OBRACUN CENE SA POPUSTIMA
    // ==================================================================

    @Test
    @DisplayName("Klijent bez popusta placa punu cenu usluge")
    void zakazi_bezPopusta_punaCena() {
        pripremiOsnovneMockove();
        when(terminRepository.postojiPreklapanje(anyLong(), any(), any(), any())).thenReturn(false);
        when(terminRepository.countByKorisnikIdAndStatus(30L, StatusTermina.ZAVRSEN)).thenReturn(2L);
        when(terminRepository.save(any(Termin.class))).thenAnswer(p -> p.getArgument(0));

        TerminOdgovor odgovor = terminService.zakazi(
                "ana@primer.rs", new TerminZahtev(10L, 20L, sutraU10, null));

        assertThat(odgovor.primenjenPopust()).isZero();
        assertThat(odgovor.ukupnaCena()).isEqualByComparingTo("3000.00");
    }

    @Test
    @DisplayName("Student sa 5 zavrsenih termina dobija 15% popusta (10% student + 5% lojalnost)")
    void zakazi_studentSaLojalnoscu_dobijaMaksimalanPopust() {
        // given - klijent je student i ima 5 zavrsenih termina (prag lojalnosti)
        klijent.setStudent(true);
        pripremiOsnovneMockove();
        when(terminRepository.postojiPreklapanje(anyLong(), any(), any(), any())).thenReturn(false);
        when(terminRepository.countByKorisnikIdAndStatus(30L, StatusTermina.ZAVRSEN)).thenReturn(5L);
        when(terminRepository.save(any(Termin.class))).thenAnswer(p -> p.getArgument(0));

        // when
        TerminOdgovor odgovor = terminService.zakazi(
                "ana@primer.rs", new TerminZahtev(10L, 20L, sutraU10, null));

        // then - 3000 * 0.85 = 2550.00
        assertThat(odgovor.primenjenPopust()).isEqualTo(15);
        assertThat(odgovor.ukupnaCena()).isEqualByComparingTo("2550.00");
    }

    @Test
    @DisplayName("Student bez lojaliti praga dobija samo 10% popusta")
    void zakazi_samoStudentskiPopust() {
        klijent.setStudent(true);
        pripremiOsnovneMockove();
        when(terminRepository.postojiPreklapanje(anyLong(), any(), any(), any())).thenReturn(false);
        when(terminRepository.countByKorisnikIdAndStatus(30L, StatusTermina.ZAVRSEN)).thenReturn(1L);
        when(terminRepository.save(any(Termin.class))).thenAnswer(p -> p.getArgument(0));

        TerminOdgovor odgovor = terminService.zakazi(
                "ana@primer.rs", new TerminZahtev(10L, 20L, sutraU10, null));

        // 3000 * 0.90 = 2700.00
        assertThat(odgovor.primenjenPopust()).isEqualTo(10);
        assertThat(odgovor.ukupnaCena()).isEqualByComparingTo("2700.00");
    }

    @Test
    @DisplayName("Metoda za obracun popusta ispravno kombinuje pravila")
    void izracunajPopust_kombinacijePravila() {
        assertThat(terminService.izracunajPopust(false, 0)).isZero();
        assertThat(terminService.izracunajPopust(true, 0)).isEqualTo(10);
        assertThat(terminService.izracunajPopust(false, 5)).isEqualTo(5);
        assertThat(terminService.izracunajPopust(true, 10)).isEqualTo(15);
    }

    @Test
    @DisplayName("Primena popusta zaokruzuje cenu na dve decimale")
    void primeniPopust_zaokruzujeNaDveDecimale() {
        assertThat(terminService.primeniPopust(new BigDecimal("1999.99"), 10))
                .isEqualByComparingTo("1799.99");   // 1799.991 -> 1799.99
        assertThat(terminService.primeniPopust(new BigDecimal("1500.00"), 0))
                .isEqualByComparingTo("1500.00");
    }

    // ==================================================================
    //  4. OSTALA POSLOVNA PRAVILA PRI ZAKAZIVANJU
    // ==================================================================

    @Test
    @DisplayName("Zakazivanje kod kozmeticara koji ne pruza izabranu uslugu nije dozvoljeno")
    void zakazi_kozmeticarNePruzaUslugu_bacaIzuzetak() {
        kozmeticar.setUsluge(new HashSet<>());   // kozmeticar vise ne pruza nista
        pripremiOsnovneMockove();

        TerminZahtev zahtev = new TerminZahtev(10L, 20L, sutraU10, null);

        assertThatThrownBy(() -> terminService.zakazi("ana@primer.rs", zahtev))
                .isInstanceOf(PoslovnaGreskaException.class)
                .hasMessageContaining("ne pruza uslugu");

        verify(terminRepository, never()).save(any(Termin.class));
    }

    @Test
    @DisplayName("Termin koji izlazi iz radnog vremena salona se odbija")
    void zakazi_vanRadnogVremena_bacaIzuzetak() {
        pripremiOsnovneMockove();

        // 19:30 + 60 min = 20:30, a salon radi do 20:00
        LocalDateTime kasno = LocalDate.now().plusDays(1).atTime(19, 30);
        TerminZahtev zahtev = new TerminZahtev(10L, 20L, kasno, null);

        assertThatThrownBy(() -> terminService.zakazi("ana@primer.rs", zahtev))
                .isInstanceOf(PoslovnaGreskaException.class)
                .hasMessageContaining("radnom vremenu");

        verify(terminRepository, never()).save(any(Termin.class));
    }

    @Test
    @DisplayName("Zakazivanje na nepostojecu uslugu vraca ResursNijePronadjenException")
    void zakazi_nepostojecaUsluga_bacaIzuzetak() {
        when(korisnikRepository.findByEmail("ana@primer.rs")).thenReturn(Optional.of(klijent));
        when(uslugaRepository.findById(999L)).thenReturn(Optional.empty());

        TerminZahtev zahtev = new TerminZahtev(999L, 20L, sutraU10, null);

        assertThatThrownBy(() -> terminService.zakazi("ana@primer.rs", zahtev))
                .isInstanceOf(ResursNijePronadjenException.class)
                .hasMessageContaining("Usluga");
    }

    @Test
    @DisplayName("Neaktivna usluga ne moze biti zakazana")
    void zakazi_neaktivnaUsluga_bacaIzuzetak() {
        usluga.setAktivna(false);
        when(korisnikRepository.findByEmail("ana@primer.rs")).thenReturn(Optional.of(klijent));
        when(uslugaRepository.findById(10L)).thenReturn(Optional.of(usluga));
        when(kozmeticarRepository.findById(20L)).thenReturn(Optional.of(kozmeticar));

        TerminZahtev zahtev = new TerminZahtev(10L, 20L, sutraU10, null);

        assertThatThrownBy(() -> terminService.zakazi("ana@primer.rs", zahtev))
                .isInstanceOf(PoslovnaGreskaException.class)
                .hasMessageContaining("nije u ponudi");
    }

    // ==================================================================
    //  5. OTKAZIVANJE
    // ==================================================================

    @Test
    @DisplayName("Klijent uspesno otkazuje svoj termin (vise od 2h pre pocetka)")
    void otkazi_sopstveniTermin_menjaStatusUOtkazan() {
        Termin termin = napraviTermin(sutraU10, StatusTermina.ZAKAZAN);
        when(terminRepository.findById(100L)).thenReturn(Optional.of(termin));
        when(terminRepository.save(any(Termin.class))).thenAnswer(p -> p.getArgument(0));

        TerminOdgovor odgovor = terminService.otkazi("ana@primer.rs", 100L);

        assertThat(odgovor.status()).isEqualTo("OTKAZAN");
        verify(terminRepository).save(termin);
    }

    @Test
    @DisplayName("Klijent ne moze otkazati tudji termin - ZabranjenPristupException")
    void otkazi_tudjiTermin_bacaIzuzetak() {
        Termin termin = napraviTermin(sutraU10, StatusTermina.ZAKAZAN);
        when(terminRepository.findById(100L)).thenReturn(Optional.of(termin));

        assertThatThrownBy(() -> terminService.otkazi("neko.drugi@primer.rs", 100L))
                .isInstanceOf(ZabranjenPristupException.class)
                .hasMessageContaining("sopstvene");

        verify(terminRepository, never()).save(any(Termin.class));
    }

    @Test
    @DisplayName("Termin koji pocinje za manje od 2 sata ne moze se otkazati")
    void otkazi_prekasno_bacaIzuzetak() {
        Termin termin = napraviTermin(LocalDateTime.now().plusMinutes(30), StatusTermina.POTVRDJEN);
        when(terminRepository.findById(100L)).thenReturn(Optional.of(termin));

        assertThatThrownBy(() -> terminService.otkazi("ana@primer.rs", 100L))
                .isInstanceOf(PoslovnaGreskaException.class)
                .hasMessageContaining("najkasnije");
    }

    @Test
    @DisplayName("Zavrsen termin se ne moze otkazati")
    void otkazi_zavrsenTermin_bacaIzuzetak() {
        Termin termin = napraviTermin(sutraU10, StatusTermina.ZAVRSEN);
        when(terminRepository.findById(100L)).thenReturn(Optional.of(termin));

        assertThatThrownBy(() -> terminService.otkazi("ana@primer.rs", 100L))
                .isInstanceOf(PoslovnaGreskaException.class)
                .hasMessageContaining("Zavrsen");
    }

    // ==================================================================
    //  6. PROMENA STATUSA (admin)
    // ==================================================================

    @Test
    @DisplayName("Admin moze da prebaci termin iz ZAKAZAN u POTVRDJEN")
    void promeniStatus_dozvoljenPrelaz() {
        Termin termin = napraviTermin(sutraU10, StatusTermina.ZAKAZAN);
        when(terminRepository.findById(100L)).thenReturn(Optional.of(termin));
        when(terminRepository.save(any(Termin.class))).thenAnswer(p -> p.getArgument(0));

        TerminOdgovor odgovor = terminService.promeniStatus(100L, StatusTermina.POTVRDJEN);

        assertThat(odgovor.status()).isEqualTo("POTVRDJEN");
    }

    @Test
    @DisplayName("Zavrsen termin je zavrsno stanje - prelaz u POTVRDJEN nije dozvoljen")
    void promeniStatus_nedozvoljenPrelaz_bacaIzuzetak() {
        Termin termin = napraviTermin(sutraU10, StatusTermina.ZAVRSEN);
        when(terminRepository.findById(100L)).thenReturn(Optional.of(termin));

        assertThatThrownBy(() -> terminService.promeniStatus(100L, StatusTermina.POTVRDJEN))
                .isInstanceOf(PoslovnaGreskaException.class)
                .hasMessageContaining("Nedozvoljena promena statusa");

        verify(terminRepository, never()).save(any(Termin.class));
    }

    // ==================================================================
    //  POMOCNE METODE
    // ==================================================================

    /** Stubuje tri ucitavanja koja se dese na pocetku metode zakazi(). */
    private void pripremiOsnovneMockove() {
        when(korisnikRepository.findByEmail("ana@primer.rs")).thenReturn(Optional.of(klijent));
        when(uslugaRepository.findById(10L)).thenReturn(Optional.of(usluga));
        when(kozmeticarRepository.findById(20L)).thenReturn(Optional.of(kozmeticar));
    }

    private Termin napraviTermin(LocalDateTime pocetak, StatusTermina status) {
        Termin termin = new Termin();
        termin.setId(100L);
        termin.setKorisnik(klijent);
        termin.setKozmeticar(kozmeticar);
        termin.setUsluga(usluga);
        termin.setDatumVremePocetka(pocetak);
        termin.setDatumVremeKraja(pocetak.plusMinutes(60));
        termin.setStatus(status);
        termin.setUkupnaCena(new BigDecimal("3000.00"));
        termin.setPrimenjenPopust(0);
        termin.setDatumKreiranja(LocalDateTime.now());
        return termin;
    }
}
