# Kids Finance

Kids Finance is a financial education platform for children aged 10–14.

The project is designed around practical financial learning through virtual money, simulations, decision-making, gamification, and parent-visible progress.

> All money used by the application is virtual. No real financial transactions are performed.

---

## Current Backend Capabilities

The backend currently supports:

- Parent registration and authentication
- Parent-child relationships
- Child profile creation
- Virtual child wallet
- Wallet credit and debit operations
- Wallet transaction history
- Database-driven learning topics
- Database-driven simulation scenarios and options
- Replayable simulation runs
- Simulation decision history
- XP and level progression
- Child progress endpoint
- Parent ownership validation
- JWT-based authorization
- Flyway database migrations
- PostgreSQL Testcontainers integration tests
- GitHub Actions CI
- OpenAPI / Swagger UI

---

## Technology Stack

### Backend

- Java 21
- Spring Boot 4.1.1
- Spring MVC
- Spring Data JPA
- Spring Security
- JWT authentication
- Hibernate
- Flyway
- PostgreSQL 17
- Maven

### Testing

- JUnit 5
- AssertJ
- Spring Boot Test
- Testcontainers
- PostgreSQL Testcontainer

### Infrastructure

- Docker Compose
- GitHub Actions

### API Documentation

- OpenAPI
- Swagger UI

---

## Project Structure

```text
kids-finance/
├── .github/
│   └── workflows/
│       └── backend-ci.yml
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/kidsfinance/
│   │   │   └── resources/
│   │   │       └── db/migration/
│   │   │
│   │   └── test/
│   │
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
│
├── docker-compose.yml
├── .env.example
└── README.md
```

---

# Local Development

## Requirements

Install:

- Java 21
- Docker Desktop
- Git

The project includes Maven Wrapper, so a separate Maven installation is not required.

Check Java:

```powershell
java -version
```

Check Docker:

```powershell
docker version
```

---

## 1. Clone the Repository

```powershell
git clone https://github.com/Seyidli06/kids-finance.git
cd kids-finance
```

---

## 2. Environment Configuration

The repository contains:

```text
.env.example
```

Create your local environment file:

```powershell
Copy-Item .env.example .env
```

Example configuration:

```env
DB_URL=jdbc:postgresql://127.0.0.1:5433/kids_finance
DB_USERNAME=kids_finance
DB_PASSWORD=kids_finance_dev

JWT_SECRET=REPLACE_WITH_BASE64_32_BYTE_SECRET

SERVER_PORT=8081
```

Do not commit real secrets.

### Generate JWT Secret on PowerShell

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
[Convert]::ToBase64String($bytes)
$rng.Dispose()
```

Copy the generated Base64 value.

---

## 3. Start PostgreSQL

From the repository root:

```powershell
docker compose up -d
```

Check status:

```powershell
docker compose ps
```

The local PostgreSQL instance is exposed on:

```text
localhost:5433
```

Database:

```text
kids_finance
```

---

## 4. Configure Backend Environment

Before starting the backend in PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://127.0.0.1:5433/kids_finance"
$env:DB_USERNAME="kids_finance"
$env:DB_PASSWORD="kids_finance_dev"
$env:SERVER_PORT="8081"
```

Generate a JWT secret:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
```

Environment variables set this way are valid only for the current PowerShell session.

---

## 5. Start the Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The API will start at:

```text
http://localhost:8081
```

Flyway automatically applies pending database migrations during startup.

---

# API Documentation

With the backend running:

### Swagger UI

```text
http://localhost:8081/swagger-ui.html
```

### OpenAPI JSON

```text
http://localhost:8081/v3/api-docs
```

Protected endpoints use JWT Bearer authentication.

To use Swagger:

1. Create a parent.
2. Call `POST /api/auth/login`.
3. Copy `accessToken`.
4. Click **Authorize** in Swagger.
5. Paste the token.
6. Call protected endpoints.

---

# Current API

## Authentication

```text
POST /api/auth/login
```

Currently parent login is supported.

---

## Parents

```text
POST /api/parents
GET  /api/parents/me/children
POST /api/parents/me/children
```

---

## Wallet

```text
GET  /api/parents/me/children/{childId}/wallet
GET  /api/parents/me/children/{childId}/wallet/transactions

POST /api/parents/me/children/{childId}/wallet/credit
POST /api/parents/me/children/{childId}/wallet/debit
```

New child wallets start with:

```text
1000.00 AZN
```

---

## Simulations

```text
GET /api/parents/me/children/{childId}/simulations
```

Submit a decision:

```text
POST /api/parents/me/children/{childId}/simulations/{scenarioCode}/decisions
```

Decision history:

```text
GET /api/parents/me/children/{childId}/simulations/decisions
```

Simulation run history:

```text
GET /api/parents/me/children/{childId}/simulations/runs
```

A scenario may be replayed multiple times.

Each play is represented by a separate `SimulationRun`.

Run statuses currently include:

```text
IN_PROGRESS
COMPLETED
ABANDONED
```

---

## Progress

```text
GET /api/parents/me/children/{childId}/progress
```

The current scoring model uses:

```text
10 XP per completed simulation
100 XP per level
```

---

# Database Migrations

Flyway owns the database schema.

Current migrations:

```text
V1  Create user domain
V2  Create wallet
V3  Backfill child wallets
V4  Create wallet transactions
V5  Create simulation decisions
V6  Add simulation decision XP
V7  Create learning and simulation catalog
V8  Seed learning topics and simulations
V9  Introduce simulation runs
```

Hibernate is configured with:

```text
ddl-auto: validate
```

Schema changes should therefore be made through Flyway migrations rather than Hibernate schema generation.

---

# Seeded Learning Content

Current topics:

```text
Saving
Budgeting
Spending Decisions
Investing Basics
```

Current simulation scenarios:

```text
SAVE_OR_SPEND_01
PLAN_PURCHASE_02
```

Simulation content is stored in PostgreSQL rather than hardcoded in the service layer.

---

# Running Tests

Docker Desktop must be running because integration tests use Testcontainers.

From:

```text
backend/
```

run:

```powershell
.\mvnw.cmd clean test
```

Tests start an isolated PostgreSQL container and apply Flyway migrations automatically.

The test database is independent from the local development database.

---

# Continuous Integration

GitHub Actions runs backend tests automatically for:

- pushes to `main`
- pull requests targeting `main`

Workflow:

```text
.github/workflows/backend-ci.yml
```

The CI pipeline:

```text
Checkout
→ Java 21
→ Maven
→ Testcontainers PostgreSQL
→ Flyway migrations
→ Backend tests
```

---

# Security

Current security model:

- Stateless authentication
- JWT Bearer tokens
- Password hashing
- Parent-only protected APIs
- Parent-child ownership validation
- Unauthorized child access is rejected
- Secrets are provided through environment variables
- Unknown endpoints are denied by default

Real passwords, JWT secrets, and production credentials must never be committed.

---

# Current Development Status

The backend foundation currently includes:

```text
Authentication foundation      ✅
Parent domain                  ✅
Child profile foundation       ✅
Wallet                         ✅
Simulation catalog             ✅
Simulation decisions           ✅
Replayable simulation runs     ✅
XP / level foundation          ✅
Progress API                   ✅
PostgreSQL migrations          ✅
Integration testing            ✅
CI                             ✅
Swagger / OpenAPI              ✅
```

Still planned:

```text
Child authentication
Learning / lesson engine
Practice / question engine
Advanced simulation consequences
Financial skill scoring
Badges
Achievements
Streaks
Challenges
Parent reports and analytics
Frontend / PWA
Production deployment
Observability and production hardening
```

---

# Planned Product Flow

The target learning experience is:

```text
Learn
  ↓
Practice
  ↓
Make a financial decision
  ↓
See the consequence
  ↓
Receive score / XP
  ↓
Track progress
```

The long-term skill model will evaluate areas such as:

- Saving
- Budgeting
- Risk Management
- Investment
- Decision Making

---

## License

A license has not yet been selected.