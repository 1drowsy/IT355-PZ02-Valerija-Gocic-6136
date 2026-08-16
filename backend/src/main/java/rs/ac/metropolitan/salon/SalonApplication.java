package rs.ac.metropolitan.salon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ulazna tacka aplikacije.
 *
 * @SpringBootApplication objedinjuje tri anotacije:
 *  - @Configuration        (klasa moze da definise bean-ove)
 *  - @EnableAutoConfiguration (Spring Boot sam podesava Tomcat, JPA, Security...)
 *  - @ComponentScan        (skenira paket rs.ac.metropolitan.salon i podpakete)
 */
@SpringBootApplication
public class SalonApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalonApplication.class, args);
    }
}
