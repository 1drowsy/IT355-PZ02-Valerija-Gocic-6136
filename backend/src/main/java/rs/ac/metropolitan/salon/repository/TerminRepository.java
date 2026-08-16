package rs.ac.metropolitan.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.metropolitan.salon.model.StatusTermina;
import rs.ac.metropolitan.salon.model.Termin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TerminRepository extends JpaRepository<Termin, Long> {

    // ------------------------------------------------------------------
    //  POSEBNA FUNKCIONALNOST: provera preklapanja termina
    // ------------------------------------------------------------------

    /**
     * Vraca true ako kozmeticar vec ima termin koji se preklapa sa intervalom
     * [pocetak, kraj).
     *
     * Uslov preklapanja dva intervala A i B je:
     *      A.pocetak < B.kraj  AND  A.kraj > B.pocetak
     *
     * Otkazani termini se ignorisu (status <> OTKAZAN) jer oni vise ne
     * zauzimaju kozmeticara.
     */
    @Query("""
            SELECT COUNT(t) > 0 FROM Termin t
            WHERE t.kozmeticar.id = :kozmeticarId
              AND t.status <> :otkazan
              AND t.datumVremePocetka < :kraj
              AND t.datumVremeKraja   > :pocetak
            """)
    boolean postojiPreklapanje(@Param("kozmeticarId") Long kozmeticarId,
                               @Param("pocetak") LocalDateTime pocetak,
                               @Param("kraj") LocalDateTime kraj,
                               @Param("otkazan") StatusTermina otkazan);

    /**
     * Isti upit kao gore, ali sa izuzimanjem jednog termina (koristi se pri
     * izmeni/pomeranju postojeceg termina da termin ne bi "sudario sam sa sobom").
     */
    @Query("""
            SELECT COUNT(t) > 0 FROM Termin t
            WHERE t.kozmeticar.id = :kozmeticarId
              AND t.id <> :terminId
              AND t.status <> :otkazan
              AND t.datumVremePocetka < :kraj
              AND t.datumVremeKraja   > :pocetak
            """)
    boolean postojiPreklapanjeOsim(@Param("kozmeticarId") Long kozmeticarId,
                                   @Param("terminId") Long terminId,
                                   @Param("pocetak") LocalDateTime pocetak,
                                   @Param("kraj") LocalDateTime kraj,
                                   @Param("otkazan") StatusTermina otkazan);

    /**
     * Svi zauzeti (neotkazani) termini jednog kozmeticara u zadatom danu.
     * Sluzi za racunanje slobodnih termina na frontendu.
     */
    @Query("""
            SELECT t FROM Termin t
            WHERE t.kozmeticar.id = :kozmeticarId
              AND t.status <> :otkazan
              AND t.datumVremePocetka >= :odDatuma
              AND t.datumVremePocetka <  :doDatuma
            ORDER BY t.datumVremePocetka
            """)
    List<Termin> findZauzeteTermine(@Param("kozmeticarId") Long kozmeticarId,
                                    @Param("odDatuma") LocalDateTime odDatuma,
                                    @Param("doDatuma") LocalDateTime doDatuma,
                                    @Param("otkazan") StatusTermina otkazan);

    // ------------------------------------------------------------------
    //  Upiti za klijenta i administratora
    // ------------------------------------------------------------------

    List<Termin> findByKorisnikIdOrderByDatumVremePocetkaDesc(Long korisnikId);

    List<Termin> findAllByOrderByDatumVremePocetkaDesc();

    List<Termin> findByStatusOrderByDatumVremePocetkaDesc(StatusTermina status);

    /** Broj zavrsenih termina klijenta - ulaz u obracun lojaliti popusta. */
    long countByKorisnikIdAndStatus(Long korisnikId, StatusTermina status);

    long countByStatus(StatusTermina status);

    /** Provere pre brisanja usluge/kozmeticara (strani kljucevi u tabeli termin). */
    boolean existsByUslugaId(Long uslugaId);

    boolean existsByKozmeticarId(Long kozmeticarId);

    // ------------------------------------------------------------------
    //  Statistika (admin panel)
    // ------------------------------------------------------------------

    @Query("SELECT COALESCE(SUM(t.ukupnaCena), 0) FROM Termin t WHERE t.status = :status")
    BigDecimal saberiPrihodPoStatusu(@Param("status") StatusTermina status);

    /** Projekcija: naziv usluge + broj puta koliko je zakazana. */
    interface BrojTerminaPoUsluzi {
        String getNaziv();
        Long getBroj();
    }

    @Query("""
            SELECT u.naziv AS naziv, COUNT(t) AS broj
            FROM Termin t JOIN t.usluga u
            WHERE t.status <> :otkazan
            GROUP BY u.id, u.naziv
            ORDER BY COUNT(t) DESC
            """)
    List<BrojTerminaPoUsluzi> statistikaPoUslugama(@Param("otkazan") StatusTermina otkazan);
}
