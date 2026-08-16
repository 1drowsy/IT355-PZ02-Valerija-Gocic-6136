package rs.ac.metropolitan.salon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** Telo zahteva za kreiranje i izmenu kozmeticara (samo ADMIN). */
public record KozmeticarZahtev(

        @NotBlank(message = "Ime je obavezno")
        @Size(max = 50)
        String ime,

        @NotBlank(message = "Prezime je obavezno")
        @Size(max = 50)
        String prezime,

        @Size(max = 500, message = "Biografija moze imati najvise 500 karaktera")
        String biografija,

        /** ID-jevi usluga koje kozmeticar pruza. */
        @NotEmpty(message = "Kozmeticar mora pruzati bar jednu uslugu")
        Set<Long> uslugeIds,

        Boolean aktivan
) {
}
