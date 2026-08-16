package rs.ac.metropolitan.salon.model;

/**
 * Uloge u sistemu (RBAC - Role Based Access Control).
 *
 * Nazivi namerno pocinju sa "ROLE_" jer Spring Security po konvenciji
 * ocekuje taj prefiks kada se koristi hasRole("ADMIN") / @PreAuthorize.
 */
public enum Uloga {

    /** Obican korisnik salona - zakazuje i otkazuje svoje termine. */
    ROLE_KLIJENT,

    /** Administrator salona - upravlja uslugama, kozmeticarima i svim terminima. */
    ROLE_ADMIN
}
