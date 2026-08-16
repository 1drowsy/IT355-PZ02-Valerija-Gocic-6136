package rs.ac.metropolitan.salon.mapper;

import rs.ac.metropolitan.salon.dto.*;
import rs.ac.metropolitan.salon.model.*;

import java.util.Comparator;
import java.util.List;

/**
 * Pretvara JPA entitete u DTO objekte (i obrnuto, gde ima smisla).
 *
 * Zasto uopste mapiranje?
 *  1) entitet sadrzi polja koja klijent ne sme da vidi (npr. hes lozinke),
 *  2) entiteti imaju kruzne reference koje bi razbile JSON serijalizaciju,
 *  3) API ostaje stabilan i kada se sema baze promeni.
 *
 * Metode su staticke jer mapiranje nema stanje - nema potrebe za bean-om.
 * VAZNO: pozivati ih iz metoda oznacenih sa @Transactional, jer se ovde
 * dodiruju LAZY veze (korisnik, kozmeticar, usluga).
 */
public final class SalonMapper {

    private SalonMapper() {
        // utility klasa - ne instancira se
    }

    // ---------------------------------------------------------- KORISNIK

    public static KorisnikOdgovor uKorisnikOdgovor(Korisnik k) {
        return new KorisnikOdgovor(
                k.getId(),
                k.getIme(),
                k.getPrezime(),
                k.getPunoIme(),
                k.getEmail(),
                k.getTelefon(),
                k.getUloga().name(),
                k.isStudent(),
                k.getDatumRegistracije()
        );
    }

    // -------------------------------------------------------- KATEGORIJA

    public static KategorijaOdgovor uKategorijaOdgovor(KategorijaUsluge kat) {
        return new KategorijaOdgovor(kat.getId(), kat.getNaziv(), kat.getOpis());
    }

    // ------------------------------------------------------------ USLUGA

    public static UslugaOdgovor uUslugaOdgovor(Usluga u) {
        return new UslugaOdgovor(
                u.getId(),
                u.getNaziv(),
                u.getOpis(),
                u.getTrajanjeMinuta(),
                u.getCena(),
                u.isAktivna(),
                u.getKategorija().getId(),
                u.getKategorija().getNaziv()
        );
    }

    public static List<UslugaOdgovor> uUslugaOdgovore(List<Usluga> usluge) {
        return usluge.stream().map(SalonMapper::uUslugaOdgovor).toList();
    }

    // -------------------------------------------------------- KOZMETICAR

    public static KozmeticarOdgovor uKozmeticarOdgovor(Kozmeticar k) {
        List<UslugaOdgovor> usluge = k.getUsluge().stream()
                .sorted(Comparator.comparing(Usluga::getNaziv))
                .map(SalonMapper::uUslugaOdgovor)
                .toList();

        return new KozmeticarOdgovor(
                k.getId(),
                k.getIme(),
                k.getPrezime(),
                k.getPunoIme(),
                k.getBiografija(),
                k.getOcena(),
                k.isAktivan(),
                usluge
        );
    }

    // ------------------------------------------------------------ TERMIN

    public static TerminOdgovor uTerminOdgovor(Termin t) {
        return new TerminOdgovor(
                t.getId(),
                t.getDatumVremePocetka(),
                t.getDatumVremeKraja(),
                t.getStatus().name(),
                t.getUkupnaCena(),
                t.getPrimenjenPopust(),
                t.getNapomena(),
                t.getDatumKreiranja(),

                t.getKorisnik().getId(),
                t.getKorisnik().getPunoIme(),
                t.getKorisnik().getEmail(),
                t.getKorisnik().getTelefon(),

                t.getKozmeticar().getId(),
                t.getKozmeticar().getPunoIme(),

                t.getUsluga().getId(),
                t.getUsluga().getNaziv(),
                t.getUsluga().getTrajanjeMinuta(),
                t.getUsluga().getCena(),

                t.getRecenzija() != null
        );
    }

    public static List<TerminOdgovor> uTerminOdgovore(List<Termin> termini) {
        return termini.stream().map(SalonMapper::uTerminOdgovor).toList();
    }

    // ---------------------------------------------------------- RECENZIJA

    public static RecenzijaOdgovor uRecenzijaOdgovor(Recenzija r) {
        return new RecenzijaOdgovor(
                r.getId(),
                r.getOcena(),
                r.getKomentar(),
                r.getDatumKreiranja(),
                r.getTermin().getId(),
                r.getKozmeticar().getId(),
                r.getKozmeticar().getPunoIme(),
                r.getKorisnik().getPunoIme(),
                r.getTermin().getUsluga().getNaziv()
        );
    }

    public static List<RecenzijaOdgovor> uRecenzijaOdgovore(List<Recenzija> recenzije) {
        return recenzije.stream().map(SalonMapper::uRecenzijaOdgovor).toList();
    }
}
