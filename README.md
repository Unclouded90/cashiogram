# Cashiogram

Cashiogram is a personal finance management application built with Java and Spring Boot, with an Angular frontend planned for the next stage of development.

The project started from a personal need: I wanted a better way to keep track of my bank balance and expenses, but I was frustrated by how little control most banking applications provide over transaction categorization.

Instead of being limited to a fixed set of categories, Cashiogram is designed to provide more control over how transactions are organized, including custom categories and optional subcategories.

The application is intended to run locally and store personal financial data persistently.

> This project is currently under active development.

---

## Goals

The main goal of Cashiogram is to provide a simple personal finance dashboard where it will be possible to:

- Track the current account balance
- Register income and expenses
- Organize transactions using custom categories and subcategories
- Filter transactions by date and category
- View totals for specific periods
- Generate summaries and reports based on transaction data

The project is also being developed as a practical exercise in backend and frontend development, focusing on Spring Boot, REST APIs, persistence, testing and Angular.

---

## Current Features

### Account

Cashiogram currently supports a single local account.

The account stores the initial balance that will later be used together with transactions to calculate the current balance.

Available operations:

```text
GET  /account
POST /account
PUT  /account
```

The application only allows one account to be initialized.

The current balance will **not** be stored directly in the database. Instead, it will eventually be calculated from:

```text
Current Balance =
Initial Balance
+ Income
- Expenses
```

This keeps transactions as the source of truth and avoids inconsistencies between transaction data and the stored balance.

---

### Categories

Transactions can be organized using custom categories.

Examples:

```text
Groceries
Bills
Restaurants
Cats
Salary
Fuel
```

Categories support:

```text
GET    /categories
GET    /categories/{categoryId}
POST   /categories
PATCH  /categories/{categoryId}
DELETE /categories/{categoryId}
```

Categories can also be filtered by name:

```text
GET /categories?name=groceries
```

Duplicate category names are prevented using case-insensitive validation.

---

### Subcategories

Categories can contain optional subcategories, allowing transactions to be classified with more detail.

For example:

```text
Groceries
├── Food
├── Cleaning Products
└── Cat Products

Bills
├── Electricity
├── Water
└── Internet
```

A subcategory always belongs to a category.

Available endpoints include:

```text
GET    /subcategories
GET    /subcategories/{subcategoryId}

POST   /categories/{categoryId}/subcategories

PATCH  /subcategories/{subcategoryId}
DELETE /subcategories/{subcategoryId}
```

Subcategories can be filtered globally:

```text
GET /subcategories?name=food
```

or by category:

```text
GET /subcategories?categoryId={categoryId}
```

Both filters can also be combined:

```text
GET /subcategories?categoryId={categoryId}&name=food
```

Subcategory names only need to be unique inside their respective category.

This means that the following is valid:

```text
Groceries → Other
Bills     → Other
```

while this is not:

```text
Groceries → Food
Groceries → Food
```

---

## Planned V1.0

The next major feature is the transaction system.

A transaction will represent a movement of money and will contain information such as:

```text
Amount
Type (INCOME / EXPENSE)
Category
Optional Subcategory
Date
Description
```

Transactions will be the basis for calculating the current account balance.

After transactions are implemented, the reporting layer will provide functionality such as:

```text
Current balance
Income between dates
Expenses between dates
Expenses by category
Expenses by subcategory
Monthly totals
Transaction filtering
```

A later stage of development will introduce the Angular frontend.

---

## Backend Structure

The backend is organized by feature rather than by global technical layer.

```text
com.mauro.cashiogram
│
├── account
│   ├── dto
│   ├── entity
│   └── repository
│
├── category
│   ├── dto
│   ├── entity
│   └── repository
│
├── subcategory
│   ├── dto
│   ├── entity
│   └── repository
│
├── transaction
│
└── exception
```

Each feature contains its own controller, service, entities, repositories and DTOs where applicable.

---

## Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Jakarta Validation
- Hibernate
- Lombok
- Gradle

### Database

PostgreSQL is planned as the persistent database for the application.

The application is intended to run locally, so no external hosting or cloud infrastructure is required for the initial version.

### Frontend

- Angular
- TypeScript

The frontend will be implemented after the main backend functionality is complete.

---

## Testing

The backend includes unit and web-layer tests using:

- JUnit 5
- Mockito
- Spring MockMvc
- `@WebMvcTest`

Service tests focus on business rules and repository interactions.

Controller tests focus on the HTTP contract, including:

```text
Request parameters
Path variables
JSON serialization/deserialization
Validation
HTTP status codes
Error responses
```

Tests can be executed with:

```bash
./gradlew clean test
```

---

## API Error Handling

The API uses centralized exception handling and structured error responses.

Example:

```json
{
  "status": 404,
  "message": "Category not found"
}
```

Custom exceptions currently include cases such as:

```text
Resource not found
Invalid requests
Duplicate resources
```

---

## Development Status

Cashiogram is currently a work in progress.

Completed:

```text
Account        ✅
Categories     ✅
Subcategories  ✅
```

In progress / planned:

```text
Transactions   ⏳
Balance logic  ⏳
Reports        ⏳
Angular UI     ⏳
Persistence    ⏳
```

The project will continue to evolve as new requirements and ideas emerge.

---

## Author

**Mauro Fernandes**  
Full Stack Developer

Built as a personal project for practice, portfolio development, and real-world personal finance tracking.