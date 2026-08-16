package rs.ac.metropolitan.salon.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Telo zahteva za POST /api/auth/register.
 *
 * DTO je Java record - nepromenljiv objekat bez boilerplate koda.
 * Anotacije se proveravaju kada kontroler oznaci parametar sa @Valid;
 * ako validacija padne, GlobalniExceptionHandler vraca 400 sa listom gresaka.
 */
public record RegistracijaZahtev(

        @NotBlank(message = "Ime je obavezno")
        @Size(max = 50, message = "Ime moze imati najvise 50 karaktera")
        String ime,

        @NotBlank(message = "Prezime je obavezno")
        @Size(max = 50, message = "Prezime moze imati najvise 50 karaktera")
        String prezime,

        @NotBlank(message = "Email je obavezan")
        @Email(message = "Email nije u ispravnom formatu")
        String email,

        @NotBlank(message = "Lozinka je obavezna")
        @Size(min = 6, max = 60, message = "Lozinka mora imati izmedju 6 i 60 karaktera")
        String lozinka,

        @Pattern(regexp = "^$|^[0-9+/ -]{6,20}$", message = "Telefon nije u ispravnom formatu")
        String telefon,

        /** Ako je true, klijent ostvaruje studentski popust na svaki termin. */
        boolean student
) {
}
