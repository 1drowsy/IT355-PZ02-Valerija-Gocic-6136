package rs.ac.metropolitan.salon.dto;

import jakarta.validation.constraints.NotNull;
import rs.ac.metropolitan.salon.model.StatusTermina;

/** Telo zahteva za PUT /api/admin/termini/{id}/status. */
public record PromenaStatusaZahtev(

        @NotNull(message = "Novi status je obavezan")
        StatusTermina status
) {
}
