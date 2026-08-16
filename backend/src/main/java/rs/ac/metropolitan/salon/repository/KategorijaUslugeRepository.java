package rs.ac.metropolitan.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.metropolitan.salon.model.KategorijaUsluge;

import java.util.Optional;

@Repository
public interface KategorijaUslugeRepository extends JpaRepository<KategorijaUsluge, Long> {

    Optional<KategorijaUsluge> findByNaziv(String naziv);

    boolean existsByNaziv(String naziv);
}
