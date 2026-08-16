package rs.ac.metropolitan.salon.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Telo zahteva za POST /api/recenzije. */
public record RecenzijaZahtev(

        @NotNull(message = "Termin je obavezan")
        Long terminId,

        @NotNull(message = "Ocena je obavezna")
        @Min(value = 1, message = "Ocena mora biti izmedju 1 i 5")
        @Max(value = 5, message = "Ocena mora biti izmedju 1 i 5")
        Integer ocena,

        @Size(max = 500, message = "Komentar moze imati najvise 500 karaktera")
        String komentar
) {
}
