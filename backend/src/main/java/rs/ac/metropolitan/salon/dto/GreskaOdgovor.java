package rs.ac.metropolitan.salon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Jedinstven format greske koji vraca GlobalniExceptionHandler.
 * Zahvaljujuci njemu frontend uvek zna gde da procita poruku (polje "poruka").
 *
 * @JsonInclude(NON_NULL) - polje "greskeValidacije" se izostavlja iz JSON-a
 * kada je null, tj. kada greska nije validaciona.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GreskaOdgovor(
        LocalDateTime vreme,
        int status,
        String greska,
        String poruka,
        String putanja,
        Map<String, String> greskeValidacije
) {
    public static GreskaOdgovor of(int status, String greska, String poruka, String putanja) {
        return new GreskaOdgovor(LocalDateTime.now(), status, greska, poruka, putanja, null);
    }

    public static GreskaOdgovor validaciona(String putanja, Map<String, String> greske) {
        return new GreskaOdgovor(LocalDateTime.now(), 400, "Bad Request",
                "Validacija podataka nije prosla", putanja, greske);
    }
}
