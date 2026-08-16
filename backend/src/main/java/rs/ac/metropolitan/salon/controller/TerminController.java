package rs.ac.metropolitan.salon.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.metropolitan.salon.dto.SlobodanTerminOdgovor;
import rs.ac.metropolitan.salon.dto.TerminOdgovor;
import rs.ac.metropolitan.salon.dto.TerminZahtev;
import rs.ac.metropolitan.salon.service.TerminService;

import java.time.LocalDate;
import java.util.List;

/**
 * Rute za prijavljene korisnike (klijent ili admin).
 * Zasticene pravilom: .requestMatchers("/api/termini/**").authenticated()
 *
 * Email prijavljenog korisnika se NIKADA ne uzima iz tela zahteva, vec
 * iskljucivo iz JWT tokena (Authentication#getName). Time se sprecava da
 * jedan klijent zakaze ili otkaze termin u ime drugog.
 */
@RestController
@RequestMapping("/api/termini")
public class TerminController {

    private final TerminService terminService;

    public TerminController(TerminService terminService) {
        this.terminService = terminService;
    }

    /**
     * POST /api/termini
     * Zakazivanje novog termina. Vraca 201 Created sa kreiranim terminom.
     */
    @PostMapping
    public ResponseEntity<TerminOdgovor> zakazi(@Valid @RequestBody TerminZahtev zahtev,
                                                Authentication autentifikacija) {
        TerminOdgovor kreiran = terminService.zakazi(autentifikacija.getName(), zahtev);
        return ResponseEntity.status(HttpStatus.CREATED).body(kreiran);
    }

    /** GET /api/termini/moji - svi termini prijavljenog klijenta. */
    @GetMapping("/moji")
    public ResponseEntity<List<TerminOdgovor>> mojiTermini(Authentication autentifikacija) {
        return ResponseEntity.ok(terminService.mojiTermini(autentifikacija.getName()));
    }

    /** GET /api/termini/{id} - detalji jednog termina (samo vlasnik ili admin). */
    @GetMapping("/{id}")
    public ResponseEntity<TerminOdgovor> jedan(@PathVariable Long id,
                                               Authentication autentifikacija) {
        return ResponseEntity.ok(terminService.jedan(autentifikacija.getName(), id));
    }

    /**
     * PUT /api/termini/{id}/otkazi
     * Klijent otkazuje svoj termin (najkasnije N sati pre pocetka).
     */
    @PutMapping("/{id}/otkazi")
    public ResponseEntity<TerminOdgovor> otkazi(@PathVariable Long id,
                                                Authentication autentifikacija) {
        return ResponseEntity.ok(terminService.otkazi(autentifikacija.getName(), id));
    }

    /**
     * GET /api/termini/dostupnost?kozmeticarId=1&uslugaId=2&datum=2026-09-15
     * Vraca listu slobodnih slotova - koristi je forma za zakazivanje.
     *
     * @DateTimeFormat kaze Spring-u kako da string iz URL-a pretvori u LocalDate.
     */
    @GetMapping("/dostupnost")
    public ResponseEntity<List<SlobodanTerminOdgovor>> dostupnost(
            @RequestParam Long kozmeticarId,
            @RequestParam Long uslugaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datum) {

        return ResponseEntity.ok(terminService.dostupnost(kozmeticarId, uslugaId, datum));
    }
}
