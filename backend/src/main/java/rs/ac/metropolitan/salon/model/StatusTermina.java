package rs.ac.metropolitan.salon.model;

/**
 * Zivotni ciklus jednog termina.
 *
 * Dozvoljeni prelazi (proverava se u TerminServiceImpl):
 *   ZAKAZAN   -> POTVRDJEN | OTKAZAN
 *   POTVRDJEN -> ZAVRSEN   | OTKAZAN
 *   ZAVRSEN   -> (kraj, nema daljih prelaza)
 *   OTKAZAN   -> (kraj, nema daljih prelaza)
 */
public enum StatusTermina {

    /** Klijent je zakazao termin, ceka se odobrenje administratora. */
    ZAKAZAN,

    /** Administrator je odobrio termin. */
    POTVRDJEN,

    /** Usluga je pruzena - tek tada klijent moze ostaviti recenziju. */
    ZAVRSEN,

    /** Termin je otkazan (od strane klijenta ili administratora). */
    OTKAZAN
}
