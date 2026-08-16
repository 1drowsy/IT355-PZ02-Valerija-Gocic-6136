# Sistem za zakazivanje i upravljanje kozmetičkim salonom

Full-stack web aplikacija za online zakazivanje termina u kozmetičkom salonu.
Backend je REST API napisan u Spring Boot-u sa JWT autentifikacijom i RBAC
autorizacijom, a frontend je React SPA koji komunicira isključivo preko REST-a.

> **Kompletna dokumentacija je u [dokumentacija/DOKUMENTACIJA.md](dokumentacija/DOKUMENTACIJA.md)**
> (opis sistema, model baze, spisak svih endpointa, priprema za odbranu).

---

## Brzo pokretanje

### 1. Backend (port 8080)

```bash
cd backend
mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run        # Linux / macOS
```

> Projekat sadrži **Maven wrapper** (`mvnw`), pa Maven ne mora biti instaliran —
> wrapper ga sam preuzme pri prvom pokretanju. Ako imate Maven na sistemu,
> može i običan `mvn spring-boot:run`.

Baza je H2 u memoriji i puni se demo podacima pri svakom pokretanju.
Konzola baze: <http://localhost:8080/h2-console>
(JDBC URL: `jdbc:h2:mem:salondb`, korisnik `sa`, bez lozinke)

### 2. Frontend (port 5173)

```bash
cd frontend
npm install
npm run dev
```

Aplikacija se otvara na <http://localhost:5173>.

### 3. Nalozi za demonstraciju

| Uloga | Email | Lozinka |
|---|---|---|
| Administrator | `admin@salon.rs` | `admin123` |
| Klijent (student) | `ana@primer.rs` | `klijent123` |
| Klijent | `marko@primer.rs` | `klijent123` |

### 4. Pokretanje testova

```bash
cd backend
mvnw.cmd test                 # Windows  (ili: mvn test)
```

Rezultat: **42 testa, sve prolazi** (17 unit + 21 integracioni + 3 slice + 1 smoke).

---

## Tehnologije

**Backend:** Java 21, Spring Boot 3.3.5, Spring Web, Spring Data JPA (Hibernate),
Spring Security 6 + JWT (jjwt 0.12.6) + BCrypt, Bean Validation, H2 / MySQL,
Lombok, JUnit 5, Mockito, MockMvc.

**Frontend:** React 18, React Router 6, Axios, Vite, čist CSS.

---

## Struktura projekta

```
IT355-PZ02-Valerija-Gocic-6136/
│
├── README.md
├── dokumentacija/
│   └── DOKUMENTACIJA.md            ← kompletna dokumentacija projekta
│
├── backend/
│   ├── pom.xml
│   ├── mvnw, mvnw.cmd, .mvn/       ← Maven wrapper (Maven ne mora biti instaliran)
│   └── src/
│       ├── main/
│       │   ├── java/rs/ac/metropolitan/salon/
│       │   │   ├── SalonApplication.java        (ulazna tačka)
│       │   │   ├── config/
│       │   │   │   ├── DataSeeder.java          (početni podaci)
│       │   │   │   └── SalonPodesavanja.java    (radno vreme, popusti)
│       │   │   ├── model/                       (6 JPA entiteta + 2 enum-a)
│       │   │   │   ├── Korisnik.java
│       │   │   │   ├── KategorijaUsluge.java
│       │   │   │   ├── Usluga.java
│       │   │   │   ├── Kozmeticar.java
│       │   │   │   ├── Termin.java
│       │   │   │   ├── Recenzija.java
│       │   │   │   ├── Uloga.java
│       │   │   │   └── StatusTermina.java
│       │   │   ├── repository/                  (6 JpaRepository interfejsa)
│       │   │   ├── dto/                         (Zahtev/Odgovor record-i)
│       │   │   ├── mapper/SalonMapper.java      (entitet → DTO)
│       │   │   ├── service/                     (interfejsi servisa)
│       │   │   │   └── impl/                    (@Service + @Transactional)
│       │   │   ├── controller/                  (5 @RestController klasa)
│       │   │   ├── security/                    (JWT + SecurityConfig)
│       │   │   │   ├── JwtUtil.java
│       │   │   │   ├── JwtAuthenticationFilter.java
│       │   │   │   ├── JwtAuthEntryPoint.java
│       │   │   │   ├── JwtAccessDeniedHandler.java
│       │   │   │   ├── KorisnikDetailsService.java
│       │   │   │   └── SecurityConfig.java
│       │   │   └── exception/                   (@RestControllerAdvice)
│       │   └── resources/
│       │       ├── application.properties
│       │       └── application-mysql.properties
│       └── test/
│           ├── java/rs/ac/metropolitan/salon/
│           │   ├── SalonApplicationTests.java
│           │   ├── service/impl/
│           │   │   └── TerminServiceImplTest.java          (JUnit 5 + Mockito)
│           │   └── controller/
│           │       ├── AuthControllerIntegrationTest.java  (@SpringBootTest)
│           │       ├── TerminControllerIntegrationTest.java(@SpringBootTest)
│           │       └── JavnoControllerTest.java            (@WebMvcTest)
│           └── resources/application-test.properties
│
└── frontend/
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src/
        ├── main.jsx
        ├── App.jsx                  (rute + ProtectedRoute)
        ├── index.css
        ├── api/api.js               (Axios instanca + interceptori)
        ├── context/AuthContext.jsx  (globalno stanje prijave)
        ├── components/
        │   ├── Navbar.jsx
        │   ├── ProtectedRoute.jsx
        │   └── Poruka.jsx
        ├── pages/
        │   ├── Pocetna.jsx          (katalog usluga i tim)
        │   ├── Login.jsx
        │   ├── Registracija.jsx
        │   ├── Zakazivanje.jsx      (izbor usluge/kozmetičara/slota)
        │   ├── MojiTermini.jsx      (tabela + otkazivanje + ocena)
        │   ├── AdminPanel.jsx
        │   └── admin/
        │       ├── AdminTermini.jsx
        │       ├── AdminUsluge.jsx
        │       └── AdminStatistika.jsx
        └── utils/format.js
```
