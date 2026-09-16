# Spring Boot Backend Lab

[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)](#)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](#)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](#)
[![Security](https://img.shields.io/badge/Security-JWT-000000?logo=jsonwebtokens&logoColor=white)](#)

> English version: [README.md](README.md)

Backend REST realizzato con **Java 17** e **Spring Boot**, pensato come progetto portfolio e laboratorio pratico di backend engineering. Il progetto raccoglie in un'unica applicazione REST, persistenza relazionale, validazione, sicurezza JWT, testing, osservabilità, documentazione OpenAPI e containerizzazione con Docker.

## Stack tecnologico

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- JWT con JJWT
- BCrypt per l'hashing delle password
- PostgreSQL 16
- Jakarta Bean Validation
- JUnit 5 / Mockito / Spring Boot Test
- Spring Boot Actuator
- OpenAPI / Swagger UI
- Maven
- Docker e Docker Compose

## Architettura

```text
Client
  |
  v
Controller
  |
  v
Service
  |
  v
Repository
  |
  v
Spring Data JPA / Hibernate
  |
  v
PostgreSQL
```

L'autenticazione è stateless e basata su JWT:

```text
POST /auth/login
      |
      v
Spring Security
      |
      v
AuthenticationManager
      |
      v
Validazione utente su database
      |
      v
JWT
      |
      v
Authorization: Bearer <token>
```

## Funzionalità principali

- CRUD dei clienti
- gestione ordini
- paginazione e ordinamento
- ricerca per nome ed email
- DTO per request e response
- Bean Validation
- gestione centralizzata delle eccezioni
- relazioni JPA
- autorizzazione per ruoli (`USER`, `ADMIN`)
- autenticazione JWT
- password cifrate con BCrypt
- endpoint Actuator per health e info
- Swagger UI / OpenAPI
- build Docker multi-stage
- persistenza PostgreSQL tramite volume Docker

## Sicurezza

Endpoint pubblici:

```text
POST /auth/registrazione
POST /auth/login
GET  /actuator/health
GET  /actuator/info
GET  /swagger-ui/**
GET  /v3/api-docs/**
```

Gli altri endpoint applicativi richiedono un JWT valido. La cancellazione dei clienti è riservata agli utenti con ruolo `ADMIN`.

### Come vengono risolti identità e ruoli

In questo progetto il JWT contiene lo **username** nel claim `sub`, ma non contiene direttamente il ruolo dell'utente. A ogni richiesta autenticata il flusso è:

```text
Authorization: Bearer <token>
        |
        v
JwtAuthenticationFilter
        |
        | verifica il JWT ed estrae lo username (sub)
        v
CustomUserDetailsService
        |
        | carica l'utente dal database
        v
ruolo USER / ADMIN dal database
        |
        v
SecurityFilterChain / @PreAuthorize
```

Questo significa che un token generato per un utente `USER` identifica quello username; quando Spring ricarica l'utente dal database, recupera il ruolo `USER` e una richiesta amministrativa viene negata. Un token generato per l'account `ADMIN`, invece, porta allo username dell'admin e il ruolo `ADMIN` viene recuperato dal database, quindi gli endpoint amministrativi possono essere eseguiti.

Il server, però, **non conosce la persona fisica che sta usando il token**. Un JWT Bearer è una credenziale: chi possiede un token `ADMIN` valido viene autenticato come l'utente admin indicato dal token. Quindi un utente normale non può ottenere privilegi admin usando il proprio token, ma se entrasse realmente in possesso di un token admin valido avrebbe gli stessi privilegi fino alla scadenza o finché quel token non diventasse invalido. Per questo i token devono essere trattati come credenziali sensibili.

Se un token admin precedentemente funzionante smette di essere accettato dopo la modifica di `JWT_SECRET`, è normale: i token firmati con la vecchia secret non possono più essere verificati con la nuova chiave.

La cancellazione dei clienti è protetta su due livelli:

```text
DELETE /clienti/**
      |
      | SecurityFilterChain -> hasRole("ADMIN")
      v
ClienteService.elimina(...)
      |
      | @PreAuthorize("hasRole('ADMIN')")
      v
Repository / database
```

Secret JWT e password del database non vengono salvate nel repository: la configurazione runtime passa tramite variabili d'ambiente.

## Avvio con Docker Compose

### Requisiti

Servono solamente:

- Docker
- Docker Compose

Con questo setup non è necessario installare localmente Java, Maven o PostgreSQL.

### 1. Clonare il repository

```bash
git clone git@github.com:acervellera/springboot-backend-lab.git
cd springboot-backend-lab
```

### 2. Creare il file delle variabili d'ambiente

```bash
cp .env.example .env
```

Impostare almeno:

```env
POSTGRES_PASSWORD=scegli-una-password-locale
JWT_SECRET=la-tua-secret-base64
```

Per generare una secret JWT:

```bash
openssl rand -base64 32
```

Seeder admin opzionale per l'ambiente di sviluppo:

```env
ADMIN_USERNAME=admin
ADMIN_PASSWORD=scegli-una-password-admin
```

Se questi due valori restano vuoti, nessun account admin viene creato automaticamente.

### 3. Avviare l'applicazione

```bash
docker compose up --build -d
```

L'applicazione sarà disponibile su:

```text
http://localhost:8080
```

PostgreSQL resta nella rete interna di Docker Compose e, di default, non espone una porta verso l'host.

### 4. Verificare lo stato dell'applicazione

```bash
curl http://localhost:8080/actuator/health
```

Risposta attesa:

```json
{"status":"UP"}
```

### 5. Visualizzare i log

```bash
docker compose logs -f
```

Solo i log dell'applicazione:

```bash
docker compose logs -f app
```

### 6. Fermare lo stack

```bash
docker compose down
```

Il volume PostgreSQL viene mantenuto.

Per cancellare anche il volume e i dati del database:

```bash
docker compose down -v
```

## Esempio di autenticazione

Registrazione:

```bash
curl -i -X POST http://localhost:8080/auth/registrazione \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo-user",
    "password": "Password123!"
  }'
```

Login:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo-user",
    "password": "Password123!"
  }'
```

Uso del token ricevuto:

```bash
curl http://localhost:8080/clienti/trovatutti \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Documentazione API

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Specifica OpenAPI in JSON:

```text
http://localhost:8080/v3/api-docs
```

## Actuator

```text
GET /actuator/health
GET /actuator/info
```

## Build Docker

Il `Dockerfile` utilizza una build multi-stage:

```text
Maven + Java 17
      |
      | mvn package
      v
Spring Boot JAR
      |
      | copia
      v
Immagine runtime Java 17 JRE
```

In questo modo la build è riproducibile e Maven e i sorgenti non vengono inclusi nell'immagine runtime finale.

## Struttura del progetto

```text
src/
├── main/
│   ├── java/com/example/backendmid/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   └── resources/
└── test/
    └── java/com/example/backendmid/

Dockerfile
compose.yaml
.env.example
pom.xml
```

## Testing

Il repository contiene esempi di controller test, repository test e integration test con JUnit 5, Mockito, MockMvc e Spring Boot Test.

```bash
./mvnw test
```

## Avvio locale senza Docker

Configurare le variabili d'ambiente per database e JWT, quindi eseguire:

```bash
./mvnw spring-boot:run
```

## Obiettivo del progetto

Il progetto nasce per consolidare in modo pratico concetti tipici dello sviluppo backend Java/Spring: architettura a livelli, REST API, persistenza relazionale, autenticazione, autorizzazione, validazione, gestione errori, testing, osservabilità, documentazione API e containerizzazione.

## Autore

**Andrea Cervellera**  
GitHub: [@acervellera](https://github.com/acervellera)

---

Possibili evoluzioni: CI/CD, migrazioni database con Flyway/Liquibase, profilo production dedicato e maggiore copertura con test di integrazione.
