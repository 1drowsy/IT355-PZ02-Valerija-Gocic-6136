package rs.ac.metropolitan.salon.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.metropolitan.salon.dto.*;
import rs.ac.metropolitan.salon.model.StatusTermina;
import rs.ac.metropolitan.salon.service.KozmeticarService;
import rs.ac.metropolitan.salon.service.StatistikaService;
import rs.ac.metropolitan.salon.service.TerminService;
import rs.ac.metropolitan.salon.service.UslugaService;

import java.util.List;

/**
 * Administratorske rute.
 *
 * Zastita je postavljena na dva nivoa (pojas i tregeri):
 *  1) u SecurityConfig-u: .requestMatchers("/api/admin/**").hasRole("ADMIN")
 *  2) ovde: @PreAuthorize("hasRole('ADMIN')") na nivou cele klase
 *
 * hasRole('ADMIN') interno trazi ovlascenje "ROLE_ADMIN" - prefiks se dodaje
 * automatski, zato se u enum-u Uloga vec nalazi ROLE_ prefiks.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TerminService terminService;
    private final UslugaService uslugaService;
    private final KozmeticarService kozmeticarService;
    private final StatistikaService statistikaService;

    public AdminController(TerminService terminService,
                           UslugaService uslugaService,
                           KozmeticarService kozmeticarService,
                           StatistikaService statistikaService) {
        this.terminService = terminService;
        this.uslugaService = uslugaService;
        this.kozmeticarService = kozmeticarService;
        this.statistikaService = statistikaService;
    }

    // ============================== TERMINI ==============================

    /**
     * GET /api/admin/termini
     * GET /api/admin/termini?status=ZAKAZAN
     */
    @GetMapping("/termini")
    public ResponseEntity<List<TerminOdgovor>> sviTermini(
            @RequestParam(required = false) StatusTermina status) {
        return ResponseEntity.ok(terminService.svi(status));
    }

    /**
     * PUT /api/admin/termini/{id}/status
     * Telo: { "status": "POTVRDJEN" }
     */
    @PutMapping("/termini/{id}/status")
    public ResponseEntity<TerminOdgovor> promeniStatus(
            @PathVariable Long id,
            @Valid @RequestBody PromenaStatusaZahtev zahtev) {
        return ResponseEntity.ok(terminService.promeniStatus(id, zahtev.status()));
    }

    // ============================== USLUGE ===============================

    /** GET /api/admin/usluge - ukljucujuci i neaktivne. */
    @GetMapping("/usluge")
    public ResponseEntity<List<UslugaOdgovor>> sveUsluge() {
        return ResponseEntity.ok(uslugaService.sve());
    }

    /** POST /api/admin/usluge */
    @PostMapping("/usluge")
    public ResponseEntity<UslugaOdgovor> kreirajUslugu(@Valid @RequestBody UslugaZahtev zahtev) {
        return ResponseEntity.status(HttpStatus.CREATED).body(uslugaService.kreiraj(zahtev));
    }

    /** PUT /api/admin/usluge/{id} */
    @PutMapping("/usluge/{id}")
    public ResponseEntity<UslugaOdgovor> izmeniUslugu(@PathVariable Long id,
                                                      @Valid @RequestBody UslugaZahtev zahtev) {
        return ResponseEntity.ok(uslugaService.izmeni(id, zahtev));
    }

    /** DELETE /api/admin/usluge/{id} */
    @DeleteMapping("/usluge/{id}")
    public ResponseEntity<PorukaOdgovor> obrisiUslugu(@PathVariable Long id) {
        return ResponseEntity.ok(new PorukaOdgovor(uslugaService.obrisi(id)));
    }

    // ============================ KOZMETICARI ============================

    /** GET /api/admin/kozmeticari */
    @GetMapping("/kozmeticari")
    public ResponseEntity<List<KozmeticarOdgovor>> sviKozmeticari() {
        return ResponseEntity.ok(kozmeticarService.svi());
    }

    /** POST /api/admin/kozmeticari */
    @PostMapping("/kozmeticari")
    public ResponseEntity<KozmeticarOdgovor> kreirajKozmeticara(
            @Valid @RequestBody KozmeticarZahtev zahtev) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kozmeticarService.kreiraj(zahtev));
    }

    /** PUT /api/admin/kozmeticari/{id} */
    @PutMapping("/kozmeticari/{id}")
    public ResponseEntity<KozmeticarOdgovor> izmeniKozmeticara(
            @PathVariable Long id,
            @Valid @RequestBody KozmeticarZahtev zahtev) {
        return ResponseEntity.ok(kozmeticarService.izmeni(id, zahtev));
    }

    /** DELETE /api/admin/kozmeticari/{id} */
    @DeleteMapping("/kozmeticari/{id}")
    public ResponseEntity<PorukaOdgovor> obrisiKozmeticara(@PathVariable Long id) {
        return ResponseEntity.ok(new PorukaOdgovor(kozmeticarService.obrisi(id)));
    }

    // ============================ STATISTIKA =============================

    /** GET /api/admin/statistika */
    @GetMapping("/statistika")
    public ResponseEntity<StatistikaOdgovor> statistika() {
        return ResponseEntity.ok(statistikaService.izracunaj());
    }

    /** GET /api/admin/korisnici */
    @GetMapping("/korisnici")
    public ResponseEntity<List<KorisnikOdgovor>> korisnici() {
        return ResponseEntity.ok(statistikaService.sviKorisnici());
    }
}
