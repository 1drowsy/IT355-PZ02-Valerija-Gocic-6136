package rs.ac.metropolitan.salon.service;

import rs.ac.metropolitan.salon.dto.SlobodanTerminOdgovor;
import rs.ac.metropolitan.salon.dto.TerminOdgovor;
import rs.ac.metropolitan.salon.dto.TerminZahtev;
import rs.ac.metropolitan.salon.model.StatusTermina;

import java.time.LocalDate;
import java.util.List;

/**
 * Najvazniji servis u sistemu - sadrzi posebnu funkcionalnost projekta
 * (provera preklapanja termina i automatski obracun cene).
 */
public interface TerminService {

    // --- klijent ---

    /**
     * Zakazuje termin za prijavljenog klijenta.
     * Servis sam racuna vreme zavrsetka i konacnu cenu sa popustom,
     * i odbija zahtev ako je kozmeticar zauzet.
     */
    TerminOdgovor zakazi(String emailKlijenta, TerminZahtev zahtev);

    List<TerminOdgovor> mojiTermini(String emailKlijenta);

    TerminOdgovor jedan(String email, Long terminId);

    /** Klijent otkazuje SVOJ termin (najkasnije N sati pre pocetka). */
    TerminOdgovor otkazi(String emailKlijenta, Long terminId);

    /** Lista slobodnih slotova kozmeticara za zadatu uslugu i datum. */
    List<SlobodanTerminOdgovor> dostupnost(Long kozmeticarId, Long uslugaId, LocalDate datum);

    // --- admin ---

    List<TerminOdgovor> svi(StatusTermina status);

    TerminOdgovor promeniStatus(Long terminId, StatusTermina noviStatus);
}
