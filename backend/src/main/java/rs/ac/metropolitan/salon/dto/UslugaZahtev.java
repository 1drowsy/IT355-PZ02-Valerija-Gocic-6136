package rs.ac.metropolitan.salon.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Telo zahteva za kreiranje i izmenu usluge (samo ADMIN). */
public record UslugaZahtev(

        @NotBlank(message = "Naziv usluge je obavezan")
        @Size(max = 100, message = "Naziv moze imati najvise 100 karaktera")
        String naziv,

        @Size(max = 500, message = "Opis moze imati najvise 500 karaktera")
        String opis,

        @NotNull(message = "Trajanje usluge je obavezno")
        @Min(value = 15, message = "Usluga mora trajati najmanje 15 minuta")
        @Max(value = 480, message = "Usluga moze trajati najvise 480 minuta")
        Integer trajanjeMinuta,

        @NotNull(message = "Cena je obavezna")
        @DecimalMin(value = "0.0", inclusive = false, message = "Cena mora biti veca od nule")
        @Digits(integer = 8, fraction = 2, message = "Cena moze imati najvise 2 decimale")
        BigDecimal cena,

        @NotNull(message = "Kategorija je obavezna")
        Long kategorijaId,

        Boolean aktivna
) {
}
