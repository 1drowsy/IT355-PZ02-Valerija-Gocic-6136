package rs.ac.metropolitan.salon.service;

import rs.ac.metropolitan.salon.dto.KorisnikOdgovor;
import rs.ac.metropolitan.salon.dto.StatistikaOdgovor;

import java.util.List;

/** Agregirani podaci za administratorski panel. */
public interface StatistikaService {

    StatistikaOdgovor izracunaj();

    List<KorisnikOdgovor> sviKorisnici();
}
