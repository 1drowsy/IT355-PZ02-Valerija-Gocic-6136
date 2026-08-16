package rs.ac.metropolitan.salon.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.metropolitan.salon.model.Korisnik;
import rs.ac.metropolitan.salon.repository.KorisnikRepository;

import java.util.List;

/**
 * Spona izmedju nase tabele "korisnik" i Spring Security-ja.
 *
 * Spring Security ne zna nista o nasem entitetu Korisnik - on radi sa
 * interfejsom UserDetails. Ova klasa ucitava korisnika iz baze po emailu
 * i pretvara ga u UserDetails objekat sa listom ovlascenja (authorities).
 *
 * Koristi se na dva mesta:
 *  1) pri prijavi - AuthenticationManager proverava lozinku,
 *  2) u JwtAuthenticationFilter-u - za svaki zahtev sa tokenom.
 */
@Service
public class KorisnikDetailsService implements UserDetailsService {

    private final KorisnikRepository korisnikRepository;

    public KorisnikDetailsService(KorisnikRepository korisnikRepository) {
        this.korisnikRepository = korisnikRepository;
    }

    /**
     * "username" je u nasem sistemu email adresa.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Korisnik korisnik = korisnikRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Korisnik sa emailom " + email + " ne postoji."));

        // uloga.name() vec sadrzi prefiks "ROLE_", pa hasRole("ADMIN") radi ispravno
        return User.builder()
                .username(korisnik.getEmail())
                .password(korisnik.getLozinka())   // BCrypt hes iz baze
                .authorities(List.of(new SimpleGrantedAuthority(korisnik.getUloga().name())))
                .build();
    }
}
