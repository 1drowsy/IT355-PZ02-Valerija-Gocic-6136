package rs.ac.metropolitan.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.metropolitan.salon.model.Usluga;

import java.util.List;

@Repository
public interface UslugaRepository extends JpaRepository<Usluga, Long> {

    /** Katalog koji vidi klijent - samo aktivne usluge. */
    List<Usluga> findByAktivnaTrue();

    List<Usluga> findByKategorijaIdAndAktivnaTrue(Long kategorijaId);

    /** Pretraga kataloga po nazivu (case-insensitive). */
    List<Usluga> findByNazivContainingIgnoreCaseAndAktivnaTrue(String naziv);
}
