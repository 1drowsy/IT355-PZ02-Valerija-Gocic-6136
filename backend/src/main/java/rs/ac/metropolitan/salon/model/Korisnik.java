package rs.ac.metropolitan.salon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ENTITET 1: Korisnik sistema (klijent salona ili administrator).
 *
 * Lozinka se NIKADA ne cuva u citljivom obliku - u bazu ide BCrypt hes
 * (vidi AuthServiceImpl.registruj).
 */
@Entity
@Table(
        name = "korisnik",
        uniqueConstraints = @UniqueConstraint(name = "uk_korisnik_email", columnNames = "email")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Korisnik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String ime;

    @Column(nullable = false, length = 50)
    private String prezime;

    /** Email je istovremeno i korisnicko ime prilikom prijave. */
    @Column(nullable = false, length = 100)
    private String email;

    /** BCrypt hes lozinke (60 karaktera). */
    @Column(nullable = false, length = 100)
    private String lozinka;

    @Column(length = 20)
    private String telefon;

    /**
     * Uloga se u bazi cuva kao tekst (ROLE_KLIJENT / ROLE_ADMIN).
     * EnumType.STRING je bezbedniji od ORDINAL jer dodavanje nove
     * vrednosti u enum ne pokvari postojece redove u bazi.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Uloga uloga;

    /** Da li je klijent student - koristi se za obracun studentskog popusta. */
    @Column(nullable = false)
    private boolean student = false;

    @Column(nullable = false)
    private LocalDateTime datumRegistracije = LocalDateTime.now();

    /**
     * Jedan korisnik ima vise termina.
     * mappedBy = "korisnik" znaci da je vlasnik veze polje "korisnik" u klasi Termin,
     * odnosno da se strani kljuc nalazi u tabeli "termin".
     */
    @OneToMany(mappedBy = "korisnik", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Termin> termini = new ArrayList<>();

    public Korisnik(String ime, String prezime, String email, String lozinka,
                    String telefon, Uloga uloga, boolean student) {
        this.ime = ime;
        this.prezime = prezime;
        this.email = email;
        this.lozinka = lozinka;
        this.telefon = telefon;
        this.uloga = uloga;
        this.student = student;
        this.datumRegistracije = LocalDateTime.now();
    }

    /** Pomocna metoda za prikaz punog imena u odgovorima API-ja. */
    public String getPunoIme() {
        return ime + " " + prezime;
    }
}
