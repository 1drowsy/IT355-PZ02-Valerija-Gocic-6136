package rs.ac.metropolitan.salon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ENTITET 6: Recenzija koju klijent ostavlja nakon ZAVRSENOG termina.
 *
 * Veza sa terminom je @OneToOne (jedan termin = najvise jedna recenzija),
 * a veza sa kozmeticarom je @ManyToOne kako bi se lako racunala
 * prosecna ocena kozmeticara.
 */
@Entity
@Table(
        name = "recenzija",
        uniqueConstraints = @UniqueConstraint(name = "uk_recenzija_termin", columnNames = "termin_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recenzija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ocena od 1 do 5. */
    @Column(nullable = false)
    private Integer ocena;

    @Column(length = 500)
    private String komentar;

    @Column(name = "datum_kreiranja", nullable = false)
    private LocalDateTime datumKreiranja = LocalDateTime.now();

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "termin_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_recenzija_termin"))
    private Termin termin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kozmeticar_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_recenzija_kozmeticar"))
    private Kozmeticar kozmeticar;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "korisnik_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_recenzija_korisnik"))
    private Korisnik korisnik;

    public Recenzija(Integer ocena, String komentar, Termin termin) {
        this.ocena = ocena;
        this.komentar = komentar;
        this.termin = termin;
        this.kozmeticar = termin.getKozmeticar();
        this.korisnik = termin.getKorisnik();
        this.datumKreiranja = LocalDateTime.now();
    }
}
