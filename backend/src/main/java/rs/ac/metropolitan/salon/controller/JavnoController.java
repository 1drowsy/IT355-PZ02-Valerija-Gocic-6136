package rs.ac.metropolitan.salon.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.metropolitan.salon.dto.*;
import rs.ac.metropolitan.salon.service.KozmeticarService;
import rs.ac.metropolitan.salon.service.RecenzijaService;
import rs.ac.metropolitan.salon.service.UslugaService;

import java.util.List;

/**
 * Rute dostupne BEZ prijave (permitAll u SecurityConfig-u).
 * Sluze za pocetnu stranu: katalog usluga, tim salona i recenzije.
 */
@RestController
@RequestMapping("/api/javno")
public class JavnoController {

    private final UslugaService uslugaService;
    private final KozmeticarService kozmeticarService;
    private final RecenzijaService recenzijaService;

    public JavnoController(UslugaService uslugaService,
                           KozmeticarService kozmeticarService,
                           RecenzijaService recenzijaService) {
        this.uslugaService = uslugaService;
        this.kozmeticarService = kozmeticarService;
        this.recenzijaService = recenzijaService;
    }

    // ------------------------------------------------------------ USLUGE

    /**
     * GET /api/javno/usluge
     * GET /api/javno/usluge?kategorijaId=2
     * GET /api/javno/usluge?naziv=manikir
     *
     * @RequestParam(required = false) - oba parametra su opciona.
     */
    @GetMapping("/usluge")
    public ResponseEntity<List<UslugaOdgovor>> usluge(
            @RequestParam(required = false) Long kategorijaId,
            @RequestParam(required = false) String naziv) {

        if (kategorijaId != null) {
            return ResponseEntity.ok(uslugaService.poKategoriji(kategorijaId));
        }
        if (naziv != null && !naziv.isBlank()) {
            return ResponseEntity.ok(uslugaService.pretrazi(naziv));
        }
        return ResponseEntity.ok(uslugaService.sveAktivne());
    }

    /** GET /api/javno/usluge/{id} */
    @GetMapping("/usluge/{id}")
    public ResponseEntity<UslugaOdgovor> usluga(@PathVariable Long id) {
        return ResponseEntity.ok(uslugaService.jedna(id));
    }

    /** GET /api/javno/kategorije */
    @GetMapping("/kategorije")
    public ResponseEntity<List<KategorijaOdgovor>> kategorije() {
        return ResponseEntity.ok(uslugaService.sveKategorije());
    }

    // -------------------------------------------------------- KOZMETICARI

    /**
     * GET /api/javno/kozmeticari
     * GET /api/javno/kozmeticari?uslugaId=3   (samo oni koji pruzaju tu uslugu)
     */
    @GetMapping("/kozmeticari")
    public ResponseEntity<List<KozmeticarOdgovor>> kozmeticari(
            @RequestParam(required = false) Long uslugaId) {

        return ResponseEntity.ok(uslugaId == null
                ? kozmeticarService.sviAktivni()
                : kozmeticarService.zaUslugu(uslugaId));
    }

    /** GET /api/javno/kozmeticari/{id} */
    @GetMapping("/kozmeticari/{id}")
    public ResponseEntity<KozmeticarOdgovor> kozmeticar(@PathVariable Long id) {
        return ResponseEntity.ok(kozmeticarService.jedan(id));
    }

    /** GET /api/javno/kozmeticari/{id}/recenzije */
    @GetMapping("/kozmeticari/{id}/recenzije")
    public ResponseEntity<List<RecenzijaOdgovor>> recenzijeKozmeticara(@PathVariable Long id) {
        return ResponseEntity.ok(recenzijaService.zaKozmeticara(id));
    }
}
