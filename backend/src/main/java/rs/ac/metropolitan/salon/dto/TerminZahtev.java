package rs.ac.metropolitan.salon.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Telo zahteva za zakazivanje termina (POST /api/termini).
 *
 * Klijent salje SAMO uslugu, kozmeticara i pocetno vreme.
 * Vreme zavrsetka i konacnu cenu racuna backend - tako klijent
 * ne moze da "podmetne" nizu cenu ili krace trajanje.
 *
 * Datum se salje u ISO formatu, npr. "2026-09-15T10:30:00"
 * (sekunde su opcione: "2026-09-15T10:30" je takodje ispravno).
 */
public record TerminZahtev(

        @NotNull(message = "Usluga je obavezna")
        Long uslugaId,

        @NotNull(message = "Kozmeticar je obavezan")
        Long kozmeticarId,

        @NotNull(message = "Datum i vreme pocetka su obavezni")
        @Future(message = "Termin se moze zakazati samo u buducnosti")
        LocalDateTime datumVremePocetka,

        @Size(max = 300, message = "Napomena moze imati najvise 300 karaktera")
        String napomena
) {
}
