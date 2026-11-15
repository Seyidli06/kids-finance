# Kids Finance

Kids Finance is a financial education platform for children aged 10–14.

The MVP teaches financial decision-making through:

- practice challenges
- virtual money
- financial simulations
- consequences
- skill scoring
- XP and levels
- achievements
- streaks
- progress tracking
- parent dashboards and reports

> The application uses virtual money only. No real financial transactions are performed.

---

# MVP Learning Flow

The core experience is:

```text
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

The MVP focuses on four financial topics:

- Saving
- Budgeting
- Spending Decisions
- Investing Basics

The skill model tracks:

- Saving
- Budgeting
- Risk Management
- Investment
- Decision Making

---

# Current Backend Status

The backend MVP is feature-complete.

Implemented:

```text
Parent registration                ✅
Parent authentication              ✅
Child authentication               ✅
Parent-child relationship          ✅
Child profiles                     ✅

Virtual wallet                     ✅
Wallet transaction history         ✅
Parent credit/debit                ✅

Challenges / practice              ✅
Challenge attempt history          ✅

1,000 AZN financial simulation     ✅
Random financial events            ✅
Simulation history                 ✅
Simulation consequences            ✅

XP                                 ✅
Levels                             ✅
Financial skill scores             ✅
Overall score                      ✅

Achievements / badges              ✅
Daily activity streaks             ✅

Child progress                     ✅
Parent dashboard                   ✅
Weekly reports                     ✅
Monthly reports                    ✅

JWT role isolation                 ✅
Parent ownership validation        ✅
PostgreSQL / Flyway                ✅
Testcontainers integration tests   ✅
GitHub Actions CI                  ✅
OpenAPI / Swagger                  ✅
```

---

# Technology Stack

## Backend

- Java 21
- Spring Boot 4.1.1
- Spring MVC
- Spring Data JPA
- Spring Security
- JWT
- Hibernate
- Flyway
- PostgreSQL 17
- Maven

## Testing

- JUnit 5
- AssertJ
- Spring Boot Test
- MockMvc
- Testcontainers
- PostgreSQL Testcontainer

## Infrastructure

- Docker Compose
- GitHub Actions

## API Documentation

- OpenAPI
- Swagger UI

---

# Project Structure

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
│   │   │   │   ├── auth/
│   │   │   │   ├── challenge/
│   │   │   │   ├── child/
│   │   │   │   ├── dashboard/
│   │   │   │   ├── family/
│   │   │   │   ├── gamification/
│   │   │   │   ├── learning/
│   │   │   │   ├── parent/
│   │   │   │   ├── reporting/
│   │   │   │   ├── scoring/
│   │   │   │   ├── simulation/
│   │   │   │   ├── user/
│   │   │   │   └── wallet/
│   │   │   │
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

A separate Maven installation is not required because Maven Wrapper is included.

Check Java:

```powershell
java -version
```

Check Docker:

```powershell
docker version
```

---

# Environment Configuration

The backend reads its configuration from environment variables.

Example:

```env
DB_URL=jdbc:postgresql://127.0.0.1:5433/kids_finance
DB_USERNAME=kids_finance
DB_PASSWORD=kids_finance_dev

JWT_SECRET=REPLACE_WITH_BASE64_32_BYTE_SECRET

SERVER_PORT=8081
```

Do not commit real secrets.

## PowerShell Setup

From the repository root:

```powershell
$env:DB_URL="jdbc:postgresql://127.0.0.1:5433/kids_finance"
$env:DB_USERNAME="kids_finance"
$env:DB_PASSWORD="kids_finance_dev"
$env:SERVER_PORT="8081"

$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
```

These variables exist only for the current PowerShell session.

---

# Start PostgreSQL

From the repository root:

```powershell
docker compose up -d
```

Check:

```powershell
docker compose ps
```

PostgreSQL is exposed at:

```text
127.0.0.1:5433
```

Database:

```text
kids_finance
```

---

# Start Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Default API address:

```text
http://localhost:8081
```

Flyway automatically applies pending migrations during startup.

---

# Authentication

Both parents and children use:

```text
POST /api/auth/login
```

The JWT contains the authenticated user ID and role.

Supported roles:

```text
PARENT
CHILD
```

Access tokens currently expire after:

```text
15 minutes
```

---

# Parent API

## Registration

```text
POST /api/parents
```

## Children

```text
GET  /api/parents/me/children
POST /api/parents/me/children
```

## Wallet

```text
GET  /api/parents/me/children/{childId}/wallet
GET  /api/parents/me/children/{childId}/wallet/transactions

POST /api/parents/me/children/{childId}/wallet/credit
POST /api/parents/me/children/{childId}/wallet/debit
```

Every new child wallet starts with:

```text
1,000.00 AZN
```

All money is virtual.

## Progress

```text
GET /api/parents/me/children/{childId}/progress
```

## Simulation History

```text
GET /api/parents/me/children/{childId}/simulations/runs
```

## Achievements

```text
GET /api/parents/me/children/{childId}/achievements
```

## Streak

```text
GET /api/parents/me/children/{childId}/streak
```

## Dashboard

```text
GET /api/parents/me/children/{childId}/dashboard
```

The dashboard aggregates:

- XP
- level
- overall score
- five skill scores
- streak
- completed challenges
- completed simulations
- completed activities
- earned achievements
- areas for improvement

## Reports

Weekly:

```text
GET /api/parents/me/children/{childId}/reports?period=WEEKLY
```

Monthly:

```text
GET /api/parents/me/children/{childId}/reports?period=MONTHLY
```

Reports include:

- period range
- challenge attempts
- correct challenge answers
- simulation runs
- total activities
- XP earned
- active days
- achievements earned
- daily activity breakdown
- current score
- areas for improvement

---

# Child API

## Profile

```text
GET /api/children/me
```

## Wallet

```text
GET /api/children/me/wallet
GET /api/children/me/wallet/transactions
```

Children can view their wallet but cannot directly credit or debit it.

## Progress

```text
GET /api/children/me/progress
```

## Challenges

List:

```text
GET /api/children/me/challenges
```

Submit answer:

```text
POST /api/children/me/challenges/{challengeCode}/answers
```

Attempt history:

```text
GET /api/children/me/challenges/attempts
```

A correct first completion awards XP.

Replay is allowed, but the same challenge cannot be used to farm XP.

## Simulation

Run a simulation:

```text
POST /api/children/me/simulations/runs
```

History:

```text
GET /api/children/me/simulations/runs
```

Each simulation starts with:

```text
1,000 virtual AZN
```

The child allocates the money between:

- cash
- saving
- deposit
- bond
- investment
- spending
- business

The allocation must total exactly:

```text
1,000.00 AZN
```

The system then applies a financial event.

Current event examples:

```text
Stock market -15%
Unexpected expense -200 AZN
Deposit interest +6%
Business profit +20%
```

The result includes:

- allocation
- event
- event impact
- ending amount
- net change
- financial skill scores
- XP earned

Random market outcomes do not directly determine decision quality.

The decision itself is scored separately.

## Achievements

```text
GET /api/children/me/achievements
```

Current achievement examples:

```text
FIRST_CHALLENGE
FIRST_SIMULATION
XP_50
BALANCED_50
```

## Daily Streak

```text
GET /api/children/me/streak
```

The streak tracks:

- current streak
- longest streak
- last activity date
- whether the child was active today

Challenge attempts and simulation runs count as learning activity.

---

# Financial Skill Scoring

The application tracks five skills:

```text
SAVING
BUDGETING
RISK_MANAGEMENT
INVESTMENT
DECISION_MAKING
```

Each score ranges from:

```text
0–100
```

Activity results update skill scores using a running average.

The overall score is calculated from the five skill scores.

The parent dashboard identifies attempted skills below the MVP improvement threshold as areas for improvement.

---

# XP and Levels

Current progression:

```text
100 XP per level
```

Challenges may award XP on first correct completion.

The main financial simulation awards XP on first completion.

Replay remains available for practice but does not provide unlimited XP farming.

---

# Gamification

Implemented MVP gamification:

```text
XP             ✅
Levels         ✅
Challenges     ✅
Achievements   ✅
Badges         ✅
Daily Streaks  ✅
```

For the MVP, an earned achievement also represents a badge.

---

# Database and Flyway

Flyway owns the database schema.

Hibernate uses:

```text
ddl-auto: validate
```

Schema modifications must be performed through Flyway.

Current migrations:

```text
V1   Create user domain
V2   Create wallet
V3   Backfill child wallets
V4   Create wallet transactions

V5   Create simulation decisions
V6   Add simulation decision XP
V7   Create learning and simulation catalog
V8   Seed learning topics and simulations
V9   Introduce simulation runs

V10  Create challenge domain
V11  Seed MVP challenges
V12  Add financial skill scoring

V13  Create main financial simulation
V14  Add simulation scores

V15  Remove legacy simulation
V16  Rename main simulation to the final simulation domain

V17  Add achievement system
V18  Add streak system
```

V5–V9 are retained because already-applied Flyway migrations must remain immutable.

The legacy simulation tables they introduced are removed by V15.

The current application uses the consolidated simulation domain created through V13–V16.

---

# Seeded Topics and Challenges

Financial topics:

```text
Saving
Budgeting
Spending Decisions
Investing Basics
```

Initial challenge set covers:

- Saving
- Budgeting
- Spending decisions
- Investing
- Risk management

The MVP does not currently implement a separate lesson engine.

---

# API Documentation

When the backend is running:

Swagger UI:

```text
http://localhost:8081/swagger-ui.html
```

OpenAPI specification:

```text
http://localhost:8081/v3/api-docs
```

Protected endpoints require a Bearer JWT.

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

Tests use an isolated PostgreSQL container.

The test database is independent from the local development database.

Current integration coverage includes:

- authentication
- parent/child role isolation
- challenges
- simulation
- financial skill scoring
- achievements
- streaks
- parent dashboard
- weekly/monthly reporting

---

# Continuous Integration

GitHub Actions runs backend tests automatically for:

- pushes to `main`
- pull requests targeting `main`

Workflow:

```text
.github/workflows/backend-ci.yml
```

Pipeline:

```text
Checkout
→ Java 21
→ Maven
→ Testcontainers PostgreSQL
→ Flyway
→ Integration tests
```

---

# Security

Current security model:

- stateless authentication
- JWT Bearer authentication
- hashed passwords
- `PARENT` and `CHILD` roles
- parent-child ownership validation
- role-isolated APIs
- inactive accounts cannot log in
- invalid credentials return unauthorized
- unknown endpoints are denied by default
- secrets are supplied through environment variables

Real passwords, JWT secrets, and production credentials must never be committed.

---

# MVP Scope Status

Backend MVP:

```text
Authentication                  ✅
Parent / Child                  ✅
Wallet                          ✅
Challenges                      ✅
Simulation                      ✅
Consequences                    ✅
XP / Levels                     ✅
Skill Scoring                   ✅
Progress                        ✅
Achievements / Badges           ✅
Streaks                         ✅
Parent Dashboard                ✅
Weekly / Monthly Reports        ✅
Security Coverage               ✅
CI                              ✅
API Documentation               ✅
```

Next major phase:

```text
Frontend / PWA
```

After frontend integration:

```text
Final QA
Deployment
Production hardening
```

---

# Next Frontend Screens

The MVP frontend should implement:

1. Login / Registration
2. Child Dashboard
3. Virtual Wallet
4. Challenges
5. Simulation
6. Result / Score
7. Progress Dashboard
8. Parent Dashboard

The frontend should consume the existing backend contract rather than introducing new backend product scope.

---

## License

A license has not yet been selected.