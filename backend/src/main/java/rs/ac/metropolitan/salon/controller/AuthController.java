package rs.ac.metropolitan.salon.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.metropolitan.salon.dto.AuthOdgovor;
import rs.ac.metropolitan.salon.dto.KorisnikOdgovor;
import rs.ac.metropolitan.salon.dto.LoginZahtev;
import rs.ac.metropolitan.salon.dto.RegistracijaZahtev;
import rs.ac.metropolitan.salon.service.AuthService;

/**
 * Javne rute za registraciju i prijavu.
 *
 * @RestController = @Controller + @ResponseBody, tj. povratne vrednosti metoda
 * se automatski serijalizuju u JSON (bez trazenja HTML sablona).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/register
     * Kreira novog klijenta i odmah vraca JWT token (korisnik ne mora
     * posebno da se prijavljuje posle registracije).
     *
     * @Valid pokrece Bean Validation nad DTO-om; ako ne prodje,
     * GlobalniExceptionHandler vraca 400 sa spiskom gresaka po poljima.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthOdgovor> registracija(@Valid @RequestBody RegistracijaZahtev zahtev) {
        AuthOdgovor odgovor = authService.registruj(zahtev);
        return ResponseEntity.status(HttpStatus.CREATED).body(odgovor);
    }

    /**
     * POST /api/auth/login
     * Vraca JWT token koji frontend cuva u localStorage.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthOdgovor> prijava(@Valid @RequestBody LoginZahtev zahtev) {
        return ResponseEntity.ok(authService.prijavi(zahtev));
    }

    /**
     * GET /api/auth/me
     * Podaci o trenutno prijavljenom korisniku.
     *
     * Objekat Authentication popunjava JwtAuthenticationFilter,
     * a getName() vraca email iz "sub" polja tokena.
     */
    @GetMapping("/me")
    public ResponseEntity<KorisnikOdgovor> mojProfil(Authentication autentifikacija) {
        return ResponseEntity.ok(authService.profil(autentifikacija.getName()));
    }
}
