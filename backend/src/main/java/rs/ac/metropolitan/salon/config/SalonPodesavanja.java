package rs.ac.metropolitan.salon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * Poslovna pravila salona ucitana iz application.properties.
 *
 * Zasto zaseban bean, a ne @Value direktno u servisu?
 *  - servis se u unit testu moze napraviti obicnim `new` pozivom, bez
 *    podizanja Spring konteksta (dovoljno je proslediti nova podesavanja),
 *  - sva "magicna" pravila salona su na jednom mestu.
 */
@Component
public class SalonPodesavanja {

    private final LocalTime radnoVremeOd;
    private final LocalTime radnoVremeDo;
    private final int popustStudent;
    private final int popustLojalnost;
    private final int lojalnostPrag;
    private final int minSatiZaOtkazivanje;

    public SalonPodesavanja(
            @Value("${salon.radno-vreme.pocetak}") String radnoVremeOd,
            @Value("${salon.radno-vreme.kraj}") String radnoVremeDo,
            @Value("${salon.popust.student}") int popustStudent,
            @Value("${salon.popust.lojalnost}") int popustLojalnost,
            @Value("${salon.popust.lojalnost-prag}") int lojalnostPrag,
            @Value("${salon.otkazivanje.min-sati-ranije}") int minSatiZaOtkazivanje) {

        this.radnoVremeOd = LocalTime.parse(radnoVremeOd);
        this.radnoVremeDo = LocalTime.parse(radnoVremeDo);
        this.popustStudent = popustStudent;
        this.popustLojalnost = popustLojalnost;
        this.lojalnostPrag = lojalnostPrag;
        this.minSatiZaOtkazivanje = minSatiZaOtkazivanje;
    }

    public LocalTime getRadnoVremeOd() {
        return radnoVremeOd;
    }

    public LocalTime getRadnoVremeDo() {
        return radnoVremeDo;
    }

    public int getPopustStudent() {
        return popustStudent;
    }

    public int getPopustLojalnost() {
        return popustLojalnost;
    }

    public int getLojalnostPrag() {
        return lojalnostPrag;
    }

    public int getMinSatiZaOtkazivanje() {
        return minSatiZaOtkazivanje;
    }
}
