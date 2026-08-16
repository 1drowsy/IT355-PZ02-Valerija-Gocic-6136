package rs.ac.metropolitan.salon.service;

import rs.ac.metropolitan.salon.dto.AuthOdgovor;
import rs.ac.metropolitan.salon.dto.KorisnikOdgovor;
import rs.ac.metropolitan.salon.dto.LoginZahtev;
import rs.ac.metropolitan.salon.dto.RegistracijaZahtev;

/** Registracija, prijava i podaci o trenutno prijavljenom korisniku. */
public interface AuthService {

    /** Kreira novog klijenta (ROLE_KLIJENT) i odmah vraca JWT token. */
    AuthOdgovor registruj(RegistracijaZahtev zahtev);

    /** Proverava email + lozinku i vraca JWT token. */
    AuthOdgovor prijavi(LoginZahtev zahtev);

    /** Podaci o korisniku na osnovu emaila iz tokena. */
    KorisnikOdgovor profil(String email);
}
