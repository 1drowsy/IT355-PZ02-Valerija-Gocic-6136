package rs.ac.metropolitan.salon.dto;

import jakarta.validation.constraints.NotBlank;

/** Telo zahteva za POST /api/auth/login. */
public record LoginZahtev(

        @NotBlank(message = "Email je obavezan")
        String email,

        @NotBlank(message = "Lozinka je obavezna")
        String lozinka
) {
}
