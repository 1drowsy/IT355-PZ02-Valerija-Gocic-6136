package rs.ac.metropolitan.salon.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.metropolitan.salon.model.*;
import rs.ac.metropolitan.salon.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * Puni bazu pocetnim podacima pri svakom pokretanju aplikacije.
 *
 * CommandLineRunner#run se izvrsava odmah nakon podizanja Spring konteksta.
 * Profil "!test" znaci: NE pokretaj se u testovima - integracioni testovi
 * sami prave podatke koji su im potrebni.
 *
 * NALOZI ZA DEMONSTRACIJU:
 *   admin@salon.rs / admin123      (ROLE_ADMIN)
 *   ana@primer.rs  / klijent123    (ROLE_KLIJENT, student)
 *   marko@primer.rs/ klijent123    (ROLE_KLIJENT)
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final KorisnikRepository korisnikRepository;
    private final KategorijaUslugeRepository kategorijaRepository;
    private final UslugaRepository uslugaRepository;
    private final KozmeticarRepository kozmeticarRepository;
    private final TerminRepository terminRepository;
    private final RecenzijaRepository recenzijaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(KorisnikRepository korisnikRepository,
                      KategorijaUslugeRepository kategorijaRepository,
                      UslugaRepository uslugaRepository,
                      KozmeticarRepository kozmeticarRepository,
                      TerminRepository terminRepository,
                      RecenzijaRepository recenzijaRepository,
                      PasswordEncoder passwordEncoder) {
        this.korisnikRepository = korisnikRepository;
        this.kategorijaRepository = kategorijaRepository;
        this.uslugaRepository = uslugaRepository;
        this.kozmeticarRepository = kozmeticarRepository;
        this.terminRepository = terminRepository;
        this.recenzijaRepository = recenzijaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // Ako podaci vec postoje (npr. MySQL profil sa ddl-auto=update), ne diramo bazu
        if (korisnikRepository.count() > 0) {
            log.info("Baza vec sadrzi podatke - preskacem inicijalno punjenje.");
            return;
        }

        log.info("Punjenje baze pocetnim podacima...");

        // ----------------------------------------------------- KATEGORIJE
        KategorijaUsluge negaLica = kategorijaRepository.save(new KategorijaUsluge(
                "Nega lica", "Tretmani ciscenja, hidratacije i pomladjivanja lica"));
        KategorijaUsluge manikir = kategorijaRepository.save(new KategorijaUsluge(
                "Manikir i pedikir", "Nega noktiju ruku i stopala"));
        KategorijaUsluge depilacija = kategorijaRepository.save(new KategorijaUsluge(
                "Depilacija", "Uklanjanje dlacica voskom i secernom pastom"));
        KategorijaUsluge tretmaniTela = kategorijaRepository.save(new KategorijaUsluge(
                "Tretmani tela", "Masaze, piling i anticelulit tretmani"));

        // --------------------------------------------------------- USLUGE
        Usluga cisceLica = uslugaRepository.save(new Usluga(
                "Dubinsko ciscenje lica",
                "Ultrazvucno ciscenje, piling i zavrsna maska prema tipu koze.",
                60, new BigDecimal("3500.00"), negaLica));

        Usluga hijaluron = uslugaRepository.save(new Usluga(
                "Hijaluronski tretman lica",
                "Intenzivna hidratacija hijaluronskim serumom i mezoterapijom.",
                75, new BigDecimal("5200.00"), negaLica));

        Usluga klasicanManikir = uslugaRepository.save(new Usluga(
                "Klasican manikir",
                "Oblikovanje noktiju, nega zanoktica i lakiranje.",
                45, new BigDecimal("1500.00"), manikir));

        Usluga gelLak = uslugaRepository.save(new Usluga(
                "Manikir sa gel lakom",
                "Trajni gel lak sa pripremom nokta i UV susenjem.",
                90, new BigDecimal("2800.00"), manikir));

        Usluga pedikir = uslugaRepository.save(new Usluga(
                "Medicinski pedikir",
                "Nega stopala, uklanjanje zadebljanja i oblikovanje noktiju.",
                60, new BigDecimal("2600.00"), manikir));

        Usluga depilacijaNogu = uslugaRepository.save(new Usluga(
                "Depilacija celih nogu",
                "Depilacija toplim voskom uz umirujucu njegu nakon tretmana.",
                45, new BigDecimal("2200.00"), depilacija));

        Usluga masaza = uslugaRepository.save(new Usluga(
                "Relax masaza celog tela",
                "Opustajuca masaza aromaticnim uljima u trajanju od jednog sata.",
                60, new BigDecimal("3800.00"), tretmaniTela));

        Usluga anticelulit = uslugaRepository.save(new Usluga(
                "Anticelulit tretman",
                "Kombinacija limfne drenaze i anticelulit preparata.",
                50, new BigDecimal("3200.00"), tretmaniTela));

        // ---------------------------------------------------- KOZMETICARI
        Kozmeticar milica = new Kozmeticar("Milica", "Jovanovic",
                "Kozmeticar sa 8 godina iskustva, specijalizovana za tretmane lica.");
        milica.setUsluge(Set.of(cisceLica, hijaluron, anticelulit));

        Kozmeticar jelena = new Kozmeticar("Jelena", "Petrovic",
                "Nail art tehnicar i pedikir specijalista sa 5 godina iskustva.");
        jelena.setUsluge(Set.of(klasicanManikir, gelLak, pedikir));

        Kozmeticar sara = new Kozmeticar("Sara", "Nikolic",
                "Diplomirani kozmeticar, radi depilacije i tretmane tela.");
        sara.setUsluge(Set.of(depilacijaNogu, masaza, anticelulit, cisceLica));

        kozmeticarRepository.saveAll(List.of(milica, jelena, sara));

        // -------------------------------------------------------- KORISNICI
        korisnikRepository.save(new Korisnik(
                "Valerija", "Gocic", "admin@salon.rs",
                passwordEncoder.encode("admin123"),
                "0641234567", Uloga.ROLE_ADMIN, false));

        Korisnik ana = korisnikRepository.save(new Korisnik(
                "Ana", "Markovic", "ana@primer.rs",
                passwordEncoder.encode("klijent123"),
                "0631112233", Uloga.ROLE_KLIJENT, true));   // student -> ima popust

        Korisnik marko = korisnikRepository.save(new Korisnik(
                "Marko", "Ilic", "marko@primer.rs",
                passwordEncoder.encode("klijent123"),
                "0654445566", Uloga.ROLE_KLIJENT, false));

        // ---------------------------------------------------------- TERMINI
        LocalDateTime sutra = LocalDateTime.now().plusDays(1).with(LocalTime.of(10, 0));
        LocalDateTime prekosutra = LocalDateTime.now().plusDays(2).with(LocalTime.of(13, 30));
        LocalDateTime prosliMesec = LocalDateTime.now().minusDays(20).with(LocalTime.of(11, 0));

        // Buduci termin - ceka odobrenje administratora
        terminRepository.save(napraviTermin(ana, milica, cisceLica, sutra,
                new BigDecimal("3150.00"), 10, StatusTermina.ZAKAZAN,
                "Osetljiva koza, molim blaziji piling."));

        // Buduci termin - vec odobren
        terminRepository.save(napraviTermin(marko, jelena, pedikir, prekosutra,
                new BigDecimal("2600.00"), 0, StatusTermina.POTVRDJEN, null));

        // Zavrsen termin iz proslosti - na njega se moze ostaviti recenzija
        Termin zavrsen = terminRepository.save(napraviTermin(ana, jelena, gelLak, prosliMesec,
                new BigDecimal("2520.00"), 10, StatusTermina.ZAVRSEN, null));

        // -------------------------------------------------------- RECENZIJA
        Recenzija recenzija = new Recenzija(5,
                "Preporucujem! Gel lak je izdrzao pun mesec dana.", zavrsen);
        recenzijaRepository.save(recenzija);

        jelena.setOcena(5.0);
        kozmeticarRepository.save(jelena);

        log.info("Baza je popunjena: {} korisnika, {} usluga, {} kozmeticara, {} termina.",
                korisnikRepository.count(), uslugaRepository.count(),
                kozmeticarRepository.count(), terminRepository.count());
        log.info("Prijava za admina:  admin@salon.rs / admin123");
        log.info("Prijava za klijenta: ana@primer.rs / klijent123");
    }

    /** Pomocna metoda da se izbegne ponavljanje pri kreiranju demo termina. */
    private Termin napraviTermin(Korisnik korisnik, Kozmeticar kozmeticar, Usluga usluga,
                                 LocalDateTime pocetak, BigDecimal cena, int popust,
                                 StatusTermina status, String napomena) {
        Termin termin = new Termin();
        termin.setKorisnik(korisnik);
        termin.setKozmeticar(kozmeticar);
        termin.setUsluga(usluga);
        termin.setDatumVremePocetka(pocetak);
        termin.setDatumVremeKraja(pocetak.plusMinutes(usluga.getTrajanjeMinuta()));
        termin.setUkupnaCena(cena);
        termin.setPrimenjenPopust(popust);
        termin.setStatus(status);
        termin.setNapomena(napomena);
        termin.setDatumKreiranja(LocalDateTime.now());
        return termin;
    }
}
