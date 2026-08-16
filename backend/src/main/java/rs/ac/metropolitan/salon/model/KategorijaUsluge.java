package rs.ac.metropolitan.salon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * ENTITET 2: Kategorija usluge (npr. "Nega lica", "Manikir", "Depilacija").
 *
 * Sluzi za grupisanje usluga u katalogu na pocetnoj strani.
 */
@Entity
@Table(name = "kategorija_usluge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KategorijaUsluge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String naziv;

    @Column(length = 255)
    private String opis;

    /** Jedna kategorija sadrzi vise usluga. */
    @OneToMany(mappedBy = "kategorija")
    private List<Usluga> usluge = new ArrayList<>();

    public KategorijaUsluge(String naziv, String opis) {
        this.naziv = naziv;
        this.opis = opis;
    }
}
