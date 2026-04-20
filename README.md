# NNH Backend

REST API for **Nanda Nursing Home** — a full-featured clinic management system built with Spring Boot 3 and Java 21.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security 6 · Stateless JWT (JJWT 0.12) |
| Persistence | Spring Data JPA · Hibernate |
| Database (dev) | H2 file-backed |
| Database (prod) | PostgreSQL |
| Build | Maven |

---

## Features

- **Appointments** — public patient booking, staff ad-hoc booking, status lifecycle, full audit trail
- **Billing** — itemised bills (medicine, room, procedure, other), auto-completes appointment on bill creation
- **Doctor Absences** — care staff submit requests, admin approves/rejects; approved absences block booking slots
- **Employees** — `NNHE-xxxxxxx` ID format, role-based access (ADMIN / CARE_STAFF), forced first-login password change, deactivate & permanent delete
- **Contact** — public contact form submission, admin inbox with READ/RESPONDED status tracking
- **Auth** — login by email or Employee ID, JWT tokens, change-password endpoint

---

## Project Structure

```
src/main/java/com/nnh/backend/
├── config/          # DataSeeder, CorsConfig
├── controller/      # REST controllers
├── dto/
│   ├── request/     # Incoming request bodies
│   └── response/    # Outgoing response bodies
├── exception/       # GlobalExceptionHandler, custom exceptions
├── orchestration/   # Multi-service coordination layer
├── persistence/
│   ├── entity/      # JPA entities
│   └── repository/  # Spring Data repositories
├── security/        # JwtUtil, JwtAuthFilter, SecurityConfig
└── service/         # Business logic
```

---

## Getting Started (Local)

### Prerequisites
- Java 21
- Maven 3.9+

### Run

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. H2 console available at `http://localhost:8080/h2-console`.

### Default Seeded Accounts

| Email | Password | Role |
|---|---|---|
| `care@nandanursinghome.in` | `Care@NNH2025` | ADMIN |
| `drvaishnavipurohit@nandanursinghome.in` | `Admin@NNH2025` | ADMIN |

> Change both passwords after first login.

---

## API Reference

All responses follow a standard envelope:
```json
{ "success": true, "message": "...", "data": { ... } }
```

### Auth — `/api/auth`

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/login` | Public | Login with email or Employee ID |
| POST | `/verify` | Bearer | Re-authenticate before sensitive actions |
| POST | `/change-password` | Bearer | Change own password |

### Appointments — `/api/appointments`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/availability` | Public | Check booked slots for a doctor on a date |
| POST | `/` | Public | Patient self-booking (status: PENDING) |
| POST | `/staff` | Bearer | Staff ad-hoc booking (status: PENDING) |
| GET | `/` | Bearer | List all appointments |
| GET | `/{id}` | Bearer | Get appointment by ID |
| PATCH | `/{id}/status` | Bearer | Update appointment status |
| GET | `/{id}/audit` | ADMIN | Full audit history for an appointment |

**Appointment Status Lifecycle:**
```
PENDING → IN_CONSULTATION → COMPLETED  (auto-set when bill is generated)
       ↘ CANCELLED
```

### Billing — `/api/bills`

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/` | Bearer | Create a bill (auto-completes linked appointment) |
| GET | `/` | ADMIN | List all bills |
| GET | `/{id}` | Bearer | Get bill by ID |

### Doctor Absences — `/api/absences`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/active` | Public | Approved upcoming absences (used by booking form) |
| GET | `/` | Bearer | All absence records |
| POST | `/` | Bearer | Submit absence request (admin = auto-approved) |
| PATCH | `/{id}/approve` | ADMIN | Approve a pending request |
| PATCH | `/{id}/reject` | ADMIN | Reject a pending request |
| DELETE | `/{id}` | ADMIN | Delete an absence record |

### Employees — `/api/employees`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | ADMIN | List all employees |
| POST | `/` | ADMIN | Create employee (auto-generates NNHE-ID) |
| DELETE | `/{id}` | ADMIN | Deactivate employee |
| DELETE | `/{id}/permanent` | ADMIN | Permanently delete inactive employee |

### Contact — `/api/contact`

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/` | Public | Submit contact form |
| GET | `/` | ADMIN | List all messages |
| GET | `/{id}` | ADMIN | Get message by ID |
| PATCH | `/{id}/status` | ADMIN | Update status (READ / RESPONDED) |

### Doctors & Services — `/api/doctors`, `/api/services`

Public read-only endpoints used to populate the booking form.

---

## Configuration

### Development (`application.properties`)

Key properties already configured:

```properties
spring.datasource.url=jdbc:h2:file:./data/nnh-db;AUTO_SERVER=TRUE
app.jwt.secret=<base64-secret>
app.cors.allowed-origins=http://localhost:5173,http://localhost:4173
```

### Production (`application-prod.properties`)

Activated via `SPRING_PROFILES_ACTIVE=prod`. Reads the following environment variables:

| Variable | Description |
|---|---|
| `PGHOST` | PostgreSQL host |
| `PGPORT` | PostgreSQL port |
| `PGDATABASE` | Database name |
| `PGUSER` | Database user |
| `PGPASSWORD` | Database password |
| `JWT_SECRET` | Base64-encoded HS256 secret (≥ 256 bits) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins |

---

## Deployment (Railway)

1. Connect your repository to a Railway project
2. Add a **PostgreSQL** plugin — Railway injects `PG*` env vars automatically
3. Set the following environment variables in the Railway dashboard:

   | Variable | Value |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `JWT_SECRET` | *(secure base64 string, ≥ 256 bits)* |
   | `CORS_ALLOWED_ORIGINS` | `https://your-netlify-site.netlify.app` |

4. Railway builds with Maven and exposes a public HTTPS URL
5. Set that URL as `VITE_API_URL` in your Netlify environment variables

---

## Security Notes

- JWT tokens expire after 24 hours
- Passwords are hashed with BCrypt
- The `care@nandanursinghome.in` account is protected from deactivation and deletion
- New employees are forced to change their temporary password on first login
- H2 console is disabled in the production profile
