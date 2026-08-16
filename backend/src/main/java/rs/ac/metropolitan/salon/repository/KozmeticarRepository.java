package rs.ac.metropolitan.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.metropolitan.salon.model.Kozmeticar;

import java.util.List;

@Repository
public interface KozmeticarRepository extends JpaRepository<Kozmeticar, Long> {

    List<Kozmeticar> findByAktivanTrue();

    /**
     * Vraca sve aktivne kozmeticare koji pruzaju zadatu uslugu.
     * Koristi se na formi za zakazivanje - kada klijent izabere uslugu,
     * lista kozmeticara se filtrira.
     *
     * JOIN po @ManyToMany kolekciji "usluge" prolazi kroz spojnu tabelu.
     */
    @Query("SELECT k FROM Kozmeticar k JOIN k.usluge u " +
           "WHERE u.id = :uslugaId AND k.aktivan = true")
    List<Kozmeticar> findAktivneByUslugaId(@Param("uslugaId") Long uslugaId);
}
