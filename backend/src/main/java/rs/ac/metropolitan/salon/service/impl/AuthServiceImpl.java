package rs.ac.metropolitan.salon.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.metropolitan.salon.dto.*;
import rs.ac.metropolitan.salon.exception.PoslovnaGreskaException;
import rs.ac.metropolitan.salon.exception.ResursNijePronadjenException;
import rs.ac.metropolitan.salon.mapper.SalonMapper;
import rs.ac.metropolitan.salon.model.Korisnik;
import rs.ac.metropolitan.salon.model.Uloga;
import rs.ac.metropolitan.salon.repository.KorisnikRepository;
import rs.ac.metropolitan.salon.security.JwtUtil;
import rs.ac.metropolitan.salon.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final KorisnikRepository korisnikRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(KorisnikRepository korisnikRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil) {
        this.korisnikRepository = korisnikRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registracija novog klijenta.
     *
     * @Transactional garantuje da se provera zauzetosti emaila i upis
     * korisnika izvrse u jednoj transakciji - ako bilo sta pukne,
     * u bazi nece ostati polovicno upisan korisnik.
     */
    @Override
    @Transactional
    public AuthOdgovor registruj(RegistracijaZahtev zahtev) {

        String email = zahtev.email().trim().toLowerCase();

        if (korisnikRepository.existsByEmail(email)) {
            throw new PoslovnaGreskaException(
                    "Korisnik sa emailom " + email + " je vec registrovan.");
        }

        Korisnik korisnik = new Korisnik(
                zahtev.ime().trim(),
                zahtev.prezime().trim(),
                email,
                // lozinka se hesuje BCrypt algoritmom - u bazu nikada ne ide otvoreni tekst
                passwordEncoder.encode(zahtev.lozinka()),
                zahtev.telefon(),
                Uloga.ROLE_KLIJENT,          // preko API-ja se moze registrovati samo klijent
                zahtev.student()
        );

        Korisnik sacuvan = korisnikRepository.save(korisnik);
        log.info("Registrovan novi korisnik: {}", sacuvan.getEmail());

        String token = jwtUtil.generisiToken(sacuvan);
        return AuthOdgovor.bearer(token, SalonMapper.uKorisnikOdgovor(sacuvan));
    }

    /**
     * Prijava korisnika.
     *
     * AuthenticationManager interno poziva KorisnikDetailsService (ucitavanje
     * korisnika) i BCryptPasswordEncoder.matches(...) (provera lozinke).
     * Ako lozinka ne odgovara, baca BadCredentialsException koji
     * GlobalniExceptionHandler pretvara u HTTP 401.
     */
    @Override
    @Transactional(readOnly = true)
    public AuthOdgovor prijavi(LoginZahtev zahtev) {

        String email = zahtev.email().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, zahtev.lozinka()));

        Korisnik korisnik = korisnikRepository.findByEmail(email)
                .orElseThrow(() -> new ResursNijePronadjenException(
                        "Korisnik sa emailom " + email + " ne postoji."));

        log.info("Prijavljen korisnik: {} ({})", korisnik.getEmail(), korisnik.getUloga());

        String token = jwtUtil.generisiToken(korisnik);
        return AuthOdgovor.bearer(token, SalonMapper.uKorisnikOdgovor(korisnik));
    }

    @Override
    @Transactional(readOnly = true)
    public KorisnikOdgovor profil(String email) {
        Korisnik korisnik = korisnikRepository.findByEmail(email)
                .orElseThrow(() -> new ResursNijePronadjenException(
                        "Korisnik sa emailom " + email + " ne postoji."));
        return SalonMapper.uKorisnikOdgovor(korisnik);
    }
}
