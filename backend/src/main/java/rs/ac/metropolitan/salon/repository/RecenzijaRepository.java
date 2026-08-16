package rs.ac.metropolitan.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.metropolitan.salon.model.Recenzija;

import java.util.List;

@Repository
public interface RecenzijaRepository extends JpaRepository<Recenzija, Long> {

    boolean existsByTerminId(Long terminId);

    List<Recenzija> findByKozmeticarIdOrderByDatumKreiranjaDesc(Long kozmeticarId);

    List<Recenzija> findByKorisnikIdOrderByDatumKreiranjaDesc(Long korisnikId);

    /** Prosecna ocena jednog kozmeticara - upisuje se u Kozmeticar.ocena. */
    @Query("SELECT AVG(r.ocena) FROM Recenzija r WHERE r.kozmeticar.id = :kozmeticarId")
    Double prosecnaOcenaKozmeticara(@Param("kozmeticarId") Long kozmeticarId);

    /** Prosecna ocena celog salona - prikazuje se u statistici. */
    @Query("SELECT AVG(r.ocena) FROM Recenzija r")
    Double prosecnaOcenaSalona();
}
