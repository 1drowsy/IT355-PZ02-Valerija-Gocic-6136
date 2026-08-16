package rs.ac.metropolitan.salon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ENTITET 4: Kozmeticar (zaposleni u salonu).
 *
 * Kozmeticar pruza odredjeni skup usluga - veza je @ManyToMany jer
 * jednu uslugu moze pruzati vise kozmeticara, a jedan kozmeticar
 * moze pruzati vise usluga.
 */
@Entity
@Table(name = "kozmeticar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Kozmeticar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String ime;

    @Column(nullable = false, length = 50)
    private String prezime;

    @Column(length = 500)
    private String biografija;

    /** Prosecna ocena izracunata iz svih recenzija (0.0 - 5.0). */
    @Column(nullable = false)
    private Double ocena = 0.0;

    @Column(nullable = false)
    private boolean aktivan = true;

    /**
     * Spojna tabela kozmeticar_usluga sa dva strana kljuca.
     * Vlasnik veze je Kozmeticar (on ima @JoinTable).
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "kozmeticar_usluga",
            joinColumns = @JoinColumn(name = "kozmeticar_id"),
            inverseJoinColumns = @JoinColumn(name = "usluga_id")
    )
    private Set<Usluga> usluge = new HashSet<>();

    @OneToMany(mappedBy = "kozmeticar")
    private List<Termin> termini = new ArrayList<>();

    public Kozmeticar(String ime, String prezime, String biografija) {
        this.ime = ime;
        this.prezime = prezime;
        this.biografija = biografija;
        this.ocena = 0.0;
        this.aktivan = true;
    }

    /**
     * Namerno rucno pisan setter (Lombok ga zato ne generise).
     *
     * Pravi se defanzivna kopija jer Hibernate mora da dobije IZMENLJIVU
     * kolekciju - ako bi se prosledio npr. Set.of(...), Hibernate bi pri
     * sinhronizaciji kolekcije bacio UnsupportedOperationException.
     */
    public void setUsluge(Set<Usluga> usluge) {
        this.usluge = (usluge == null) ? new HashSet<>() : new HashSet<>(usluge);
    }

    public String getPunoIme() {
        return ime + " " + prezime;
    }

    /**
     * Provera da li kozmeticar uopste pruza trazenu uslugu.
     * Poredi se po ID-u jer Usluga ne redefinise equals/hashCode.
     */
    public boolean pruzaUslugu(Long uslugaId) {
        return usluge.stream()
                .anyMatch(u -> u.getId() != null && u.getId().equals(uslugaId));
    }
}
