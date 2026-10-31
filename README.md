# Cashiogram

Cashiogram is a personal finance management application built with Java, Spring Boot and Angular.

The project started from a personal need for better control over transaction categorization than what is typically available in banking applications. It is also used as a practical project for experimenting with backend architecture, persistence, testing and frontend development.

> The backend and persistence layers are implemented. The Angular frontend is currently being developed, with the dashboard, account management and balance integration already available.

---

## Core Features

- Single local account with an initial balance
- Income and expense transactions
- Custom categories and subcategories
- Transaction splits across multiple subcategories
- Dynamically calculated account balance
- Period, category and subcategory reports
- PostgreSQL persistence
- Responsive Angular dashboard
- Docker-based local environment

---

## Technical Decisions

### Transactions as the source of truth

The current balance is not stored in the database.

Instead, it is calculated from:

```text
Current Balance =
Initial Balance
+ Income
- Expenses
```

This avoids keeping a mutable balance synchronized with transaction data.

### Transaction splits are analytical

A transaction can be split across multiple subcategories for reporting purposes.

For example:

```text
Transaction: €100
Category: Groceries

Food          €60
Cat Products  €20
Unallocated   €20
```

The account balance is still affected by exactly €100.

Splits are classifications and do not represent additional financial movements.

The sum of all splits cannot exceed the original transaction amount.

### Package-by-feature architecture

The backend is organized by domain feature instead of global technical layers.

```text
com.mauro.cashiogram
├── account
├── balance
├── category
├── subcategory
├── transaction
├── report
└── exception
```

Each feature owns its controller, service, DTOs, repositories and entities where applicable.

Derived features such as `balance` and `report` do not have their own entities because their data is calculated from existing persisted data.

The Angular frontend follows the same feature-oriented approach, with reusable visual components kept under `shared`.

### Frontend state and API integration

Angular services handle communication with the backend through `HttpClient`.

Feature state is exposed using Angular Signals where shared reactive state is useful.

For example, the Account service owns the current account state. When the initial balance is updated, the Balance component reacts to that change and reloads the calculated balance from the backend.

This keeps components independent from each other while allowing related features to stay synchronized.

### Service and database validation

Business rules are primarily enforced in the service layer, while PostgreSQL also protects important data integrity constraints.

Examples include:

- Case-insensitive category uniqueness
- Subcategory uniqueness within a category
- Positive transaction and split amounts
- Foreign-key relationships
- One split per subcategory within the same transaction

Rules involving multiple records, such as ensuring that the total split amount does not exceed the transaction amount, remain service-level business logic.

### Flyway owns the database schema

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Hibernate validates the mappings but does not create or modify the schema.

Database changes are versioned through Flyway migrations:

```text
V1__initial_schema.sql
V2__...
V3__...
```

### Dockerized environment

The application runs as three Docker containers:

```text
Cashiogram
├── frontend  → Angular (Nginx)
├── backend   → Spring Boot
└── postgres  → PostgreSQL
```

PostgreSQL data is stored in a Docker volume and survives container restarts.

The frontend uses `/api` for backend requests. Nginx proxies these requests to the backend when running the full Docker stack.

During frontend development, the Angular development server uses a local proxy with the same `/api` path.

---

## Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Jakarta Validation
- Lombok
- Flyway
- Gradle

### Database

- PostgreSQL 18

### Frontend

- Angular
- TypeScript
- Angular Signals
- RxJS
- SCSS
- Vitest
- Prettier
- Nginx

### Testing

Backend:

- JUnit 5
- Mockito
- Spring MockMvc
- `@WebMvcTest`
- `@DataJpaTest`
- Testcontainers

Repository integration tests run against a temporary real PostgreSQL instance rather than an in-memory database.

Frontend:

- Vitest
- Angular TestBed
- Angular HTTP testing utilities
- Component and service tests

Tests are executed automatically through GitHub Actions on every push and pull request.

### Infrastructure

- Docker
- Docker Compose
- GitHub Actions

---

## Running the Application

Create the local environment file:

```bash
cp .env.example .env
```

Then start the full application:

```bash
docker compose up -d --build
```

The backend is available at `http://localhost:8081`.

The frontend is available at `http://localhost:4200`.

PostgreSQL schema migrations are applied automatically by Flyway during application startup.

API requests from the frontend use the `/api` path and are proxied to the Spring Boot backend.

### Frontend development

The backend and PostgreSQL can be started through Docker:

```bash
docker compose up -d postgres backend
```

Then run the Angular development server:

```bash
cd frontend
npm start
```

The development proxy forwards `/api` requests to the backend.

---

## Testing

From the `backend` directory:

```bash
./gradlew clean test
```

From the `frontend` directory:

```bash
npm test -- --no-watch --no-progress
npm run format:check
npm run build
```

The backend test suite includes:

- Service unit tests
- Controller web-layer tests
- Repository integration tests against PostgreSQL using Testcontainers

The frontend test suite includes:

- Component tests
- Service tests
- HTTP request contract tests
- Reactive state behaviour tests

---

## V1.0 Status

```text
Backend
├── Account                   ✅
├── Categories                ✅
├── Subcategories             ✅
├── Transactions              ✅
├── Transaction Splits        ✅
├── Balance                   ✅
└── Reports                   ✅

Persistence
├── PostgreSQL                ✅
├── Flyway                    ✅
└── Repository Tests          ✅

Infrastructure
├── Docker Environment        ✅
├── Backend CI                ✅
└── Frontend CI               ✅

Frontend
├── Angular Infrastructure    ✅
├── Responsive Dashboard      ✅
├── Account Management        ✅
├── Balance Integration       ✅
├── Transactions              ⏳
├── Categories                ⏳
├── Subcategories             ⏳
└── Reports                   ⏳
```

---

## Author

**Mauro Fernandes**  
Full Stack Developer

Built as a personal project for practice, portfolio development and real-world personal finance tracking.
