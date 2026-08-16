package rs.ac.metropolitan.salon.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ac.metropolitan.salon.dto.RecenzijaOdgovor;
import rs.ac.metropolitan.salon.dto.RecenzijaZahtev;
import rs.ac.metropolitan.salon.service.RecenzijaService;

import java.util.List;

/** Ocenjivanje zavrsenih termina (samo prijavljeni klijenti). */
@RestController
@RequestMapping("/api/recenzije")
public class RecenzijaController {

    private final RecenzijaService recenzijaService;

    public RecenzijaController(RecenzijaService recenzijaService) {
        this.recenzijaService = recenzijaService;
    }

    /** POST /api/recenzije */
    @PostMapping
    public ResponseEntity<RecenzijaOdgovor> kreiraj(@Valid @RequestBody RecenzijaZahtev zahtev,
                                                    Authentication autentifikacija) {
        RecenzijaOdgovor kreirana = recenzijaService.kreiraj(autentifikacija.getName(), zahtev);
        return ResponseEntity.status(HttpStatus.CREATED).body(kreirana);
    }

    /** GET /api/recenzije/moje */
    @GetMapping("/moje")
    public ResponseEntity<List<RecenzijaOdgovor>> moje(Authentication autentifikacija) {
        return ResponseEntity.ok(recenzijaService.moje(autentifikacija.getName()));
    }
}
