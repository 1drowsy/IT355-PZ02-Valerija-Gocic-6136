package rs.ac.metropolitan.salon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ENTITET 5: Termin - centralni entitet sistema.
 *
 * Povezuje klijenta, kozmeticara i uslugu u konkretnom vremenskom intervalu.
 * Polja datumVremeKraja i ukupnaCena se NE salju sa fronta - servis ih
 * automatski izracunava (posebna funkcionalnost projekta).
 */
@Entity
@Table(
        name = "termin",
        indexes = {
                @Index(name = "idx_termin_kozmeticar_pocetak",
                        columnList = "kozmeticar_id, datum_vreme_pocetka")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Termin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "datum_vreme_pocetka", nullable = false)
    private LocalDateTime datumVremePocetka;

    /** Racuna se kao: datumVremePocetka + usluga.trajanjeMinuta. */
    @Column(name = "datum_vreme_kraja", nullable = false)
    private LocalDateTime datumVremeKraja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTermina status = StatusTermina.ZAKAZAN;

    /** Konacna cena nakon primenjenih popusta. */
    @Column(name = "ukupna_cena", nullable = false, precision = 10, scale = 2)
    private BigDecimal ukupnaCena;

    /** Procenat popusta koji je primenjen (radi transparentnosti prema klijentu). */
    @Column(name = "primenjen_popust", nullable = false)
    private Integer primenjenPopust = 0;

    @Column(length = 300)
    private String napomena;

    @Column(name = "datum_kreiranja", nullable = false)
    private LocalDateTime datumKreiranja = LocalDateTime.now();

    /** Vise termina pripada jednom korisniku (klijentu). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "korisnik_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_termin_korisnik"))
    private Korisnik korisnik;

    /** Vise termina pripada jednom kozmeticaru. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kozmeticar_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_termin_kozmeticar"))
    private Kozmeticar kozmeticar;

    /** Vise termina moze biti zakazano za istu uslugu. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usluga_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_termin_usluga"))
    private Usluga usluga;

    /** Jedan termin moze imati najvise jednu recenziju. */
    @OneToOne(mappedBy = "termin", cascade = CascadeType.ALL)
    private Recenzija recenzija;

    /**
     * Provera preklapanja dva intervala:
     * intervali se preklapaju ako pocetak jednog pada pre kraja drugog
     * i obrnuto (klasican "overlap" uslov: A.pocetak < B.kraj && A.kraj > B.pocetak).
     */
    public boolean sePreklapaSa(LocalDateTime pocetak, LocalDateTime kraj) {
        return this.datumVremePocetka.isBefore(kraj) && this.datumVremeKraja.isAfter(pocetak);
    }

    /** Termin je "aktivan" ako nije otkazan - samo takvi zauzimaju kozmeticara. */
    public boolean jeAktivan() {
        return status != StatusTermina.OTKAZAN;
    }
}
