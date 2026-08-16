package rs.ac.metropolitan.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.metropolitan.salon.model.Korisnik;
import rs.ac.metropolitan.salon.model.Uloga;

import java.util.Optional;

/**
 * Sloj perzistencije za korisnike.
 *
 * JpaRepository<Korisnik, Long> vec obezbedjuje save/findById/findAll/deleteById,
 * pa se ovde dopisuju samo dodatni upiti. Spring Data ih generise
 * automatski na osnovu naziva metode ("derived query methods").
 */
@Repository
public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {

    /** Koristi se pri prijavi i u JWT filteru za ucitavanje ulogovanog korisnika. */
    Optional<Korisnik> findByEmail(String email);

    /** Provera pri registraciji da email vec nije zauzet. */
    boolean existsByEmail(String email);

    long countByUloga(Uloga uloga);
}
