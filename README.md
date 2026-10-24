# Cashiogram

Cashiogram is a personal finance management application built with Java and Spring Boot.

The project started from a personal need for better control over transaction categorization than what is typically available in banking applications. It is also used as a practical project for experimenting with backend architecture, persistence, testing and, later, Angular.

> The backend, persistence layer and frontend infrastructure are implemented. The Angular UI is the remaining major part of V1.0.

---

## Core Features

- Single local account with an initial balance
- Income and expense transactions
- Custom categories and subcategories
- Transaction splits across multiple subcategories
- Dynamically calculated account balance
- Period, category and subcategory reports
- PostgreSQL persistence
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

The application currently runs as three Docker containers:

```text
Cashiogram
├── backend    → Spring Boot
├── frontend   → Angular (Nginx)
└── postgres   → PostgreSQL
```

PostgreSQL data is stored in a Docker volume and survives container restarts.

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

### Testing

- JUnit 5
- Mockito
- Spring MockMvc
- `@WebMvcTest`
- `@DataJpaTest`
- Testcontainers

Repository integration tests run against a temporary real PostgreSQL instance rather than an in-memory database.

Tests are also executed automatically through GitHub Actions on every push and pull request.

### Infrastructure

- Docker
- Docker Compose

### Frontend

- Angular
- TypeScript
- SCSS
- Vitest
- Prettier
- Nginx

---

## Running the Application

Create the local environment file:

```bash
cp .env.example .env
```

Then start the application:

```bash
docker compose up -d --build
```

The backend becomes available on:

```text
http://localhost:8080
```

PostgreSQL schema migrations are applied automatically by Flyway during application startup.

The frontend is available on:

```text
http://localhost:4200
```

API requests from the frontend use the `/api` path, which is proxied to the Spring Boot backend by Nginx.

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

The frontend includes component tests using Vitest.

---

## V1.0 Status

```text
Account                   ✅
Categories                ✅
Subcategories             ✅
Transactions              ✅
Transaction Splits        ✅
Balance                   ✅
Reports                   ✅
PostgreSQL Persistence    ✅
Docker Environment        ✅
Angular Infrastructure    ✅
Angular UI                ⏳
```

---

## Author

**Mauro Fernandes**  
Full Stack Developer

Built as a personal project for practice, portfolio development and real-world personal finance tracking.
