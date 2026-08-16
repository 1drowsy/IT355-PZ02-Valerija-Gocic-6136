package rs.ac.metropolitan.salon.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.metropolitan.salon.config.SalonPodesavanja;
import rs.ac.metropolitan.salon.dto.SlobodanTerminOdgovor;
import rs.ac.metropolitan.salon.dto.TerminOdgovor;
import rs.ac.metropolitan.salon.dto.TerminZahtev;
import rs.ac.metropolitan.salon.exception.PoslovnaGreskaException;
import rs.ac.metropolitan.salon.exception.ResursNijePronadjenException;
import rs.ac.metropolitan.salon.exception.ZabranjenPristupException;
import rs.ac.metropolitan.salon.mapper.SalonMapper;
import rs.ac.metropolitan.salon.model.*;
import rs.ac.metropolitan.salon.repository.KorisnikRepository;
import rs.ac.metropolitan.salon.repository.KozmeticarRepository;
import rs.ac.metropolitan.salon.repository.TerminRepository;
import rs.ac.metropolitan.salon.repository.UslugaRepository;
import rs.ac.metropolitan.salon.service.TerminService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * POSEBNA FUNKCIONALNOST PROJEKTA
 * ================================
 * 1) Validacija preklapanja termina - kozmeticar ne moze imati dva termina
 *    koja se vremenski seku.
 * 2) Automatski obracun vremena zavrsetka i konacne cene sa popustima.
 *
 * Sva pravila su namerno u SERVISNOM sloju (ne u kontroleru i ne u bazi),
 * jer je servis jedino mesto koje vidi celu poslovnu operaciju.
 */
@Service
public class TerminServiceImpl implements TerminService {

    private static final Logger log = LoggerFactory.getLogger(TerminServiceImpl.class);

    /** Najveci ukupni popust koji klijent moze da ostvari (u procentima). */
    static final int MAKSIMALAN_POPUST = 15;

    /** Korak mreze slobodnih termina - termini pocinju svakih 15 minuta. */
    private static final int KORAK_MINUTA = 15;

    private static final DateTimeFormatter FORMAT_PRIKAZA =
            DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");

    private final TerminRepository terminRepository;
    private final KorisnikRepository korisnikRepository;
    private final UslugaRepository uslugaRepository;
    private final KozmeticarRepository kozmeticarRepository;
    private final SalonPodesavanja podesavanja;

    public TerminServiceImpl(TerminRepository terminRepository,
                             KorisnikRepository korisnikRepository,
                             UslugaRepository uslugaRepository,
                             KozmeticarRepository kozmeticarRepository,
                             SalonPodesavanja podesavanja) {
        this.terminRepository = terminRepository;
        this.korisnikRepository = korisnikRepository;
        this.uslugaRepository = uslugaRepository;
        this.kozmeticarRepository = kozmeticarRepository;
        this.podesavanja = podesavanja;
    }

    // ==================================================================
    //  ZAKAZIVANJE
    // ==================================================================

    /**
     * @Transactional otvara transakciju pre ulaska u metodu i zatvara je
     * (commit) tek kada se metoda uspesno zavrsi. Ako se baci
     * RuntimeException (npr. PoslovnaGreskaException zbog zauzetog termina),
     * transakcija se ponistava (rollback) i u bazi ne ostaje nista.
     */
    @Override
    @Transactional
    public TerminOdgovor zakazi(String emailKlijenta, TerminZahtev zahtev) {

        // --- 1. Ucitavanje entiteta ------------------------------------
        Korisnik klijent = korisnikRepository.findByEmail(emailKlijenta)
                .orElseThrow(() -> new ResursNijePronadjenException(
                        "Korisnik sa emailom " + emailKlijenta + " ne postoji."));

        Usluga usluga = uslugaRepository.findById(zahtev.uslugaId())
                .orElseThrow(() -> new ResursNijePronadjenException("Usluga", zahtev.uslugaId()));

        Kozmeticar kozmeticar = kozmeticarRepository.findById(zahtev.kozmeticarId())
                .orElseThrow(() -> new ResursNijePronadjenException("Kozmeticar", zahtev.kozmeticarId()));

        // --- 2. Provera dostupnosti resursa ----------------------------
        if (!usluga.isAktivna()) {
            throw new PoslovnaGreskaException(
                    "Usluga '" + usluga.getNaziv() + "' trenutno nije u ponudi.");
        }
        if (!kozmeticar.isAktivan()) {
            throw new PoslovnaGreskaException(
                    "Kozmeticar " + kozmeticar.getPunoIme() + " trenutno ne prima klijente.");
        }
        if (!kozmeticar.pruzaUslugu(usluga.getId())) {
            throw new PoslovnaGreskaException(
                    "Kozmeticar " + kozmeticar.getPunoIme() +
                    " ne pruza uslugu '" + usluga.getNaziv() + "'.");
        }

        // --- 3. Racunanje intervala ------------------------------------
        LocalDateTime pocetak = zahtev.datumVremePocetka();
        LocalDateTime kraj = pocetak.plusMinutes(usluga.getTrajanjeMinuta());

        if (!pocetak.isAfter(LocalDateTime.now())) {
            throw new PoslovnaGreskaException("Termin se moze zakazati samo u buducnosti.");
        }
        proveriRadnoVreme(pocetak, kraj);

        // --- 4. KLJUCNA PROVERA: preklapanje sa postojecim terminima ---
        boolean zauzeto = terminRepository.postojiPreklapanje(
                kozmeticar.getId(), pocetak, kraj, StatusTermina.OTKAZAN);

        if (zauzeto) {
            throw new PoslovnaGreskaException(
                    "Kozmeticar " + kozmeticar.getPunoIme() + " je zauzet u intervalu " +
                    pocetak.format(FORMAT_PRIKAZA) + " - " + kraj.toLocalTime() +
                    ". Izaberite drugo vreme ili drugog kozmeticara.");
        }

        // --- 5. Automatski obracun cene --------------------------------
        long brojZavrsenih = terminRepository.countByKorisnikIdAndStatus(
                klijent.getId(), StatusTermina.ZAVRSEN);

        int popust = izracunajPopust(klijent.isStudent(), brojZavrsenih);
        BigDecimal konacnaCena = primeniPopust(usluga.getCena(), popust);

        // --- 6. Cuvanje -------------------------------------------------
        Termin termin = new Termin();
        termin.setKorisnik(klijent);
        termin.setKozmeticar(kozmeticar);
        termin.setUsluga(usluga);
        termin.setDatumVremePocetka(pocetak);
        termin.setDatumVremeKraja(kraj);
        termin.setStatus(StatusTermina.ZAKAZAN);
        termin.setUkupnaCena(konacnaCena);
        termin.setPrimenjenPopust(popust);
        termin.setNapomena(zahtev.napomena());
        termin.setDatumKreiranja(LocalDateTime.now());

        Termin sacuvan = terminRepository.save(termin);

        log.info("Zakazan termin #{}: klijent={}, kozmeticar={}, {} -> {}, cena={} (popust {}%)",
                sacuvan.getId(), klijent.getEmail(), kozmeticar.getPunoIme(),
                pocetak, kraj, konacnaCena, popust);

        return SalonMapper.uTerminOdgovor(sacuvan);
    }

    // ==================================================================
    //  OBRACUN CENE  (izdvojeno da bi bilo lako testirati)
    // ==================================================================

    /**
     * Ukupan popust = studentski + lojaliti, ograniceno na MAKSIMALAN_POPUST.
     *
     * @param student       da li je klijent oznacen kao student
     * @param brojZavrsenih broj vec zavrsenih termina tog klijenta
     */
    int izracunajPopust(boolean student, long brojZavrsenih) {
        int popust = 0;

        if (student) {
            popust += podesavanja.getPopustStudent();
        }
        if (brojZavrsenih >= podesavanja.getLojalnostPrag()) {
            popust += podesavanja.getPopustLojalnost();
        }
        return Math.min(popust, MAKSIMALAN_POPUST);
    }

    /**
     * Primenjuje popust na osnovnu cenu i zaokruzuje na 2 decimale.
     * BigDecimal se koristi zbog tacnosti - kod novca se double NE koristi.
     */
    BigDecimal primeniPopust(BigDecimal osnovnaCena, int popustProcenat) {
        BigDecimal faktor = BigDecimal.valueOf(100L - popustProcenat)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        return osnovnaCena.multiply(faktor).setScale(2, RoundingMode.HALF_UP);
    }

    /** Termin mora u celosti stati u radno vreme salona i u jedan dan. */
    private void proveriRadnoVreme(LocalDateTime pocetak, LocalDateTime kraj) {

        boolean istiDan = pocetak.toLocalDate().equals(kraj.toLocalDate());
        boolean prerano = pocetak.toLocalTime().isBefore(podesavanja.getRadnoVremeOd());
        boolean prekasno = kraj.toLocalTime().isAfter(podesavanja.getRadnoVremeDo());

        if (!istiDan || prerano || prekasno) {
            throw new PoslovnaGreskaException(
                    "Termin mora u celosti biti u radnom vremenu salona (" +
                    podesavanja.getRadnoVremeOd() + " - " + podesavanja.getRadnoVremeDo() + ").");
        }
    }

    // ==================================================================
    //  PREGLED I OTKAZIVANJE (klijent)
    // ==================================================================

    @Override
    @Transactional(readOnly = true)
    public List<TerminOdgovor> mojiTermini(String emailKlijenta) {
        Korisnik klijent = nadjiKorisnikaIliBaci(emailKlijenta);
        return SalonMapper.uTerminOdgovore(
                terminRepository.findByKorisnikIdOrderByDatumVremePocetkaDesc(klijent.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public TerminOdgovor jedan(String email, Long terminId) {
        Termin termin = nadjiTerminIliBaci(terminId);
        Korisnik korisnik = nadjiKorisnikaIliBaci(email);

        // Admin sme da vidi svaki termin, klijent samo svoj
        boolean jeAdmin = korisnik.getUloga() == Uloga.ROLE_ADMIN;
        boolean jeVlasnik = termin.getKorisnik().getId().equals(korisnik.getId());

        if (!jeAdmin && !jeVlasnik) {
            throw new ZabranjenPristupException("Ovaj termin ne pripada vama.");
        }
        return SalonMapper.uTerminOdgovor(termin);
    }

    @Override
    @Transactional
    public TerminOdgovor otkazi(String emailKlijenta, Long terminId) {
        Termin termin = nadjiTerminIliBaci(terminId);

        // Provera vlasnistva - klijent ne sme da otkaze tudji termin
        if (!termin.getKorisnik().getEmail().equalsIgnoreCase(emailKlijenta)) {
            throw new ZabranjenPristupException(
                    "Mozete otkazati samo sopstvene termine.");
        }

        if (termin.getStatus() == StatusTermina.OTKAZAN) {
            throw new PoslovnaGreskaException("Termin je vec otkazan.");
        }
        if (termin.getStatus() == StatusTermina.ZAVRSEN) {
            throw new PoslovnaGreskaException("Zavrsen termin ne moze biti otkazan.");
        }

        int minSati = podesavanja.getMinSatiZaOtkazivanje();
        if (LocalDateTime.now().plusHours(minSati).isAfter(termin.getDatumVremePocetka())) {
            throw new PoslovnaGreskaException(
                    "Termin se moze otkazati najkasnije " + minSati + "h pre pocetka.");
        }

        termin.setStatus(StatusTermina.OTKAZAN);
        Termin sacuvan = terminRepository.save(termin);

        log.info("Klijent {} je otkazao termin #{}", emailKlijenta, terminId);
        return SalonMapper.uTerminOdgovor(sacuvan);
    }

    // ==================================================================
    //  SLOBODNI TERMINI
    // ==================================================================

    /**
     * Racuna slobodne slotove tako sto prolazi kroz radno vreme u koracima od
     * 15 minuta i odbacuje svaki slot koji se preklapa sa nekim vec zauzetim
     * terminom (ili je u proslosti).
     */
    @Override
    @Transactional(readOnly = true)
    public List<SlobodanTerminOdgovor> dostupnost(Long kozmeticarId, Long uslugaId, LocalDate datum) {

        Usluga usluga = uslugaRepository.findById(uslugaId)
                .orElseThrow(() -> new ResursNijePronadjenException("Usluga", uslugaId));

        if (!kozmeticarRepository.existsById(kozmeticarId)) {
            throw new ResursNijePronadjenException("Kozmeticar", kozmeticarId);
        }

        List<Termin> zauzeti = terminRepository.findZauzeteTermine(
                kozmeticarId,
                datum.atStartOfDay(),
                datum.plusDays(1).atStartOfDay(),
                StatusTermina.OTKAZAN);

        List<SlobodanTerminOdgovor> slobodni = new ArrayList<>();

        LocalDateTime kursor = datum.atTime(podesavanja.getRadnoVremeOd());
        LocalDateTime krajRadnogVremena = datum.atTime(podesavanja.getRadnoVremeDo());
        LocalDateTime sada = LocalDateTime.now();

        while (true) {
            LocalDateTime krajSlota = kursor.plusMinutes(usluga.getTrajanjeMinuta());
            if (krajSlota.isAfter(krajRadnogVremena)) {
                break;
            }

            LocalDateTime pocetakSlota = kursor;
            boolean uProslosti = !pocetakSlota.isAfter(sada);
            boolean preklapa = zauzeti.stream()
                    .anyMatch(t -> t.sePreklapaSa(pocetakSlota, krajSlota));

            if (!uProslosti && !preklapa) {
                slobodni.add(new SlobodanTerminOdgovor(pocetakSlota, krajSlota));
            }
            kursor = kursor.plusMinutes(KORAK_MINUTA);
        }

        return slobodni;
    }

    // ==================================================================
    //  ADMINISTRACIJA TERMINA
    // ==================================================================

    @Override
    @Transactional(readOnly = true)
    public List<TerminOdgovor> svi(StatusTermina status) {
        List<Termin> termini = (status == null)
                ? terminRepository.findAllByOrderByDatumVremePocetkaDesc()
                : terminRepository.findByStatusOrderByDatumVremePocetkaDesc(status);

        return SalonMapper.uTerminOdgovore(termini);
    }

    @Override
    @Transactional
    public TerminOdgovor promeniStatus(Long terminId, StatusTermina noviStatus) {
        Termin termin = nadjiTerminIliBaci(terminId);
        StatusTermina trenutni = termin.getStatus();

        proveriPrelazStatusa(trenutni, noviStatus);

        termin.setStatus(noviStatus);
        Termin sacuvan = terminRepository.save(termin);

        log.info("Termin #{}: status {} -> {}", terminId, trenutni, noviStatus);
        return SalonMapper.uTerminOdgovor(sacuvan);
    }

    /**
     * Dozvoljeni prelazi statusa:
     *   ZAKAZAN   -> POTVRDJEN | OTKAZAN
     *   POTVRDJEN -> ZAVRSEN   | OTKAZAN
     *   ZAVRSEN / OTKAZAN -> nista (zavrsna stanja)
     */
    private void proveriPrelazStatusa(StatusTermina trenutni, StatusTermina novi) {
        if (trenutni == novi) {
            throw new PoslovnaGreskaException("Termin vec ima status " + novi + ".");
        }

        boolean dozvoljeno = switch (trenutni) {
            case ZAKAZAN -> novi == StatusTermina.POTVRDJEN || novi == StatusTermina.OTKAZAN;
            case POTVRDJEN -> novi == StatusTermina.ZAVRSEN || novi == StatusTermina.OTKAZAN;
            case ZAVRSEN, OTKAZAN -> false;
        };

        if (!dozvoljeno) {
            throw new PoslovnaGreskaException(
                    "Nedozvoljena promena statusa: " + trenutni + " -> " + novi + ".");
        }
    }

    // ---------------------------------------------------------- POMOCNE

    private Termin nadjiTerminIliBaci(Long id) {
        return terminRepository.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Termin", id));
    }

    private Korisnik nadjiKorisnikaIliBaci(String email) {
        return korisnikRepository.findByEmail(email)
                .orElseThrow(() -> new ResursNijePronadjenException(
                        "Korisnik sa emailom " + email + " ne postoji."));
    }
}
