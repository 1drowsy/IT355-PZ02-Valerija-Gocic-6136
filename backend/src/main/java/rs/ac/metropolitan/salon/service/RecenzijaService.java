package rs.ac.metropolitan.salon.service;

import rs.ac.metropolitan.salon.dto.RecenzijaOdgovor;
import rs.ac.metropolitan.salon.dto.RecenzijaZahtev;

import java.util.List;

public interface RecenzijaService {

    /** Klijent ocenjuje SVOJ zavrsen termin (najvise jedna recenzija po terminu). */
    RecenzijaOdgovor kreiraj(String emailKlijenta, RecenzijaZahtev zahtev);

    List<RecenzijaOdgovor> moje(String emailKlijenta);

    List<RecenzijaOdgovor> zaKozmeticara(Long kozmeticarId);
}
