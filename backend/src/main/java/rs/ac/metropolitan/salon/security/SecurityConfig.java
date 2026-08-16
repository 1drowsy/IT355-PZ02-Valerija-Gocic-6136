package rs.ac.metropolitan.salon.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Centralna konfiguracija Spring Security-ja.
 *
 * Kljucne odluke:
 *  - STATELESS sesija: server ne pamti prijavljene korisnike, identitet
 *    se u svakom zahtevu dokazuje JWT tokenom,
 *  - CSRF iskljucen: CSRF napad se oslanja na kolacice sesije kojih ovde nema,
 *  - autorizacija po ulogama (RBAC) definisana je u authorizeHttpRequests.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // omogucava @PreAuthorize na metodama kontrolera/servisa
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final KorisnikDetailsService korisnikDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthEntryPoint jwtAuthEntryPoint,
                          JwtAccessDeniedHandler jwtAccessDeniedHandler,
                          KorisnikDetailsService korisnikDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.korisnikDetailsService = korisnikDetailsService;
    }

    /**
     * BCrypt je algoritam za hesovanje lozinki sa ugradjenim "salt"-om.
     * Isti tekst daje razlicit hes svaki put, a provera se radi metodom
     * matches(sirovaLozinka, hes). Hes se NE moze "odhesovati".
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DaoAuthenticationProvider spaja ucitavanje korisnika iz baze
     * (KorisnikDetailsService) i proveru lozinke (BCryptPasswordEncoder).
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(korisnikDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /** Koristi ga AuthService prilikom prijave (authenticate(...)). */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Dozvoljava React razvojnom serveru (Vite: 5173, CRA: 3000) da poziva API.
     * Bez ovoga bi pregledac blokirao zahteve zbog CORS politike.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration konfiguracija = new CorsConfiguration();
        konfiguracija.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"
        ));
        konfiguracija.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        konfiguracija.setAllowedHeaders(List.of("*"));
        konfiguracija.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource izvor = new UrlBasedCorsConfigurationSource();
        izvor.registerCorsConfiguration("/**", konfiguracija);
        return izvor;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())

            // Server ne cuva sesiju - svaki zahtev nosi svoj JWT
            .sessionManagement(sesija ->
                    sesija.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Nasi JSON odgovori umesto podrazumevanih HTML stranica
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(jwtAuthEntryPoint)   // 401
                    .accessDeniedHandler(jwtAccessDeniedHandler))  // 403

            // ---------------- PRAVILA PRISTUPA (RBAC) ----------------
            .authorizeHttpRequests(zahtevi -> zahtevi

                    // CORS preflight mora proci bez tokena
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // Javno dostupno: prijava, registracija, katalog usluga, H2 konzola
                    .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                    .requestMatchers("/api/javno/**").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()

                    // /api/auth/me zahteva vazeci token
                    .requestMatchers("/api/auth/me").authenticated()

                    // Samo administrator
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    // Svaki prijavljen korisnik (klijent ili admin)
                    .requestMatchers("/api/termini/**").authenticated()
                    .requestMatchers("/api/recenzije/**").authenticated()

                    // Sve ostalo zahteva prijavu
                    .anyRequest().authenticated()
            )

            .authenticationProvider(authenticationProvider())

            // Nas JWT filter ide PRE standardnog filtera za formu za prijavu
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        // H2 konzola se prikazuje u <frame>, pa mora biti dozvoljen isti izvor
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
