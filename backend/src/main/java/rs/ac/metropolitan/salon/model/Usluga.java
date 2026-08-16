package rs.ac.metropolitan.salon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * ENTITET 3: Usluga koju salon nudi (npr. "Tretman lica hijaluronom").
 *
 * Polje trajanjeMinuta je kljucno za posebnu funkcionalnost projekta:
 * na osnovu njega se racuna vreme zavrsetka termina i provera preklapanja.
 */
@Entity
@Table(name = "usluga")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usluga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String naziv;

    @Column(length = 500)
    private String opis;

    /** Trajanje usluge u minutima (npr. 45). */
    @Column(name = "trajanje_minuta", nullable = false)
    private Integer trajanjeMinuta;

    /**
     * Osnovna cena bez popusta.
     * BigDecimal se koristi umesto double da ne bi doslo do
     * gubitka preciznosti pri radu sa novcem.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cena;

    /** Neaktivna usluga se ne prikazuje klijentima i ne moze se zakazati. */
    @Column(nullable = false)
    private boolean aktivna = true;

    /**
     * Vise usluga pripada jednoj kategoriji.
     * FetchType.LAZY - kategorija se ucitava tek kada se stvarno zatrazi.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kategorija_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_usluga_kategorija"))
    private KategorijaUsluge kategorija;

    public Usluga(String naziv, String opis, Integer trajanjeMinuta,
                  BigDecimal cena, KategorijaUsluge kategorija) {
        this.naziv = naziv;
        this.opis = opis;
        this.trajanjeMinuta = trajanjeMinuta;
        this.cena = cena;
        this.kategorija = kategorija;
        this.aktivna = true;
    }
}
