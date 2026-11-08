# Cashiogram

Cashiogram is a personal finance management application built with Java, Spring Boot and Angular.

The project started from a personal need for better control over transaction categorization than what is typically available in banking applications. It is also used as a practical project for experimenting with backend architecture, persistence, testing and frontend development.

> The backend and persistence layers are implemented. The Angular frontend includes a responsive dashboard, account management, balance integration, categories, subcategories, transactions, transaction split management and interactive financial reports.

All core V1.0 features are implemented. The project is currently undergoing final code cleanup, UI/UX polishing and release validation.

---

## Core Features

- Single local account with an initial balance
- Income and expense transactions
- Custom categories and subcategories
- Transaction splits across multiple subcategories
- Dynamically calculated account balance
- Interactive financial reports with advanced filtering and dynamic pie charts
- Financial breakdown by category and subcategory, including income, expenses and unallocated amounts
- Persistent light and dark themes with application-wide switching
- Dashboard transaction filtering by date range and transaction type
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

### Financial reporting

Reports provide interactive financial analysis through dynamic filtering, aggregation and visualizations.

Available filters include:

- Date ranges
- Multiple categories
- Case-insensitive partial subcategory name search
- Minimum and maximum transaction amounts
- Minimum and maximum transaction split amounts

Reports support three visualization modes:

- **Transaction Type:** income versus expenses
- **Category:** distribution of income or expenses across categories
- **Subcategory:** distribution across subcategories, including unallocated amounts when analyzing the complete allocation

A dynamic pie chart displays the percentage and amount represented by each group.

Without split-related filters, transaction-based reports use the full transaction amounts.

When filtering by subcategory name or split amount, aggregations use only matching splits rather than the original transaction amounts.

Subcategories remain distinct by ID and parent category, even when they share the same name.

The **Financial Breakdown** provides a complete view of income, expenses and net results for the selected period.

Categories can be expanded to display their subcategories and any unallocated amounts. The breakdown is independent of the chart's analytical filters, ensuring that the complete financial activity remains visible.

Unallocated amounts represent the difference between transaction amounts and their actual split allocations. Splits excluded by analytical filters are never treated as unallocated.

Reporting is analytical and does not modify transactions or account balances.

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

For example, the Account service owns the current account state and the Balance service exposes the calculated balance as shared reactive state.

Creating, updating or deleting a transaction triggers a balance reload, allowing the Balance component to update automatically without requiring a page refresh.

Category management uses dedicated detail views where categories and their associated subcategories can be created, updated and deleted while preserving their relationship.

Transaction management follows the same detail-view approach. Each transaction has a dedicated view where it can be updated or deleted and where its amount can be analytically allocated across subcategories using transaction splits.

Split creation, updates and deletion immediately update the local transaction state without affecting the account balance, since splits only classify an existing financial movement.

Financial reports use Angular Signals and computed state to manage filters, chart data, percentages and category breakdowns.

The Reports feature consumes dedicated backend aggregation endpoints through an Angular HTTP service.

Analytical filters affect the chart and its summary, while the Financial Breakdown independently loads complete category and subcategory totals for the selected period.

Loading states, API errors and retry behaviour are handled independently for report visualizations and the financial breakdown.

This keeps components independent from each other while allowing related features to stay synchronized.

### Theming and UI

The Angular frontend supports persistent light and dark themes through a centralized theme service.

- Light theme uses warm gray and beige tones.
- Dark theme uses navy blue surfaces with orange accents.
- Theme preferences are stored locally and restored when the application loads.
- A global navigation header provides access to the Options menu and theme switching from any page.
- Shared CSS variables define colors, surfaces, borders and interactive states across the application.
- Financial report charts use theme-specific color palettes.

Dashboard transactions support client-side filtering by date range and transaction type without additional API requests.

### Service and database validation

Business rules are primarily enforced in the service layer, while PostgreSQL also protects important data integrity constraints.

Examples include:

- Case-insensitive category uniqueness
- Subcategory uniqueness within a category
- Positive transaction and split amounts
- Foreign-key relationships
- One split per subcategory within the same transaction

Rules involving multiple records, such as ensuring that the total split amount does not exceed the transaction amount, remain service-level business logic.
A transaction amount cannot be reduced below the amount already allocated through its splits.
A transaction category cannot be changed while the transaction contains splits, preventing existing subcategory allocations from becoming inconsistent with the transaction category.
Categories referenced by existing transactions cannot be deleted. When an unused category is deleted, its subcategories are removed with it.
Similarly, subcategories referenced by existing transaction splits cannot be deleted.

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
- CSS custom properties for application-wide theming
- SVG-based financial charts

### Testing

Backend:

- JUnit 5
- Mockito
- Spring MockMvc
- `@WebMvcTest`
- `@DataJpaTest`
- Testcontainers

Repository integration tests run against a temporary real PostgreSQL instance rather than an in-memory database.
These tests also validate persistence behaviour such as Hibernate-managed creation and update timestamps.

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
├── Transactions              ✅
├── Categories                ✅
├── Subcategories             ✅
├── Transaction Splits        ✅
└── Reports                   ✅
```

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
├── Transactions              ✅
├── Categories                ✅
├── Subcategories             ✅
├── Transaction Splits        ✅
└── Reports                   ✅
```

### Remaining before V1.0 release

- Final code cleanup and refactoring
- UI/UX polishing and visual consistency
- Responsive layout improvements
- Final test suite and production build validation
- Docker environment verification
- V1.0 release preparation

---

## Author

**Mauro Fernandes**  
Full Stack Developer

Built as a personal project for practice, portfolio development and real-world personal finance tracking.
