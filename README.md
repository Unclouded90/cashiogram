# Cashiogram

Cashiogram is a personal finance management application built with Java and Spring Boot, with an Angular frontend planned for the next stage of development.

The project started from a personal need: I wanted a better way to keep track of my bank balance and expenses, but I was frustrated by how little control most banking applications provide over transaction categorization.

Instead of being limited to a fixed set of categories, Cashiogram is designed to provide more control over how transactions are organized, including custom categories, subcategories and transaction splits.

The application is intended to run locally and store personal financial data persistently.

> This project is currently under active development.

---

## Goals

The main goal of Cashiogram is to provide a simple personal finance dashboard where it will be possible to:

- Track the current account balance
- Register income and expenses
- Organize transactions using custom categories and subcategories
- Split a transaction across multiple subcategories
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

The current balance will **not** be stored directly in the database. Instead, it will be calculated from:

```text
Current Balance =
Initial Balance
+ Income
- Expense
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

Available operations:

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

Available operations:

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

### Transactions

Transactions represent actual movements of money in the account.

Each transaction contains:

```text
Amount
Type (INCOME / EXPENSE)
Category
Date
Description
Optional splits
```

Available operations:

```text
GET    /transactions
GET    /transactions/{transactionId}
POST   /transactions
PATCH  /transactions/{transactionId}
DELETE /transactions/{transactionId}
```

Transactions returned by the API are ordered by date, with the most recent transactions first.

A transaction can be created with or without splits.

Example:

```text
Transaction: €100
Category: Groceries

Splits:
├── Food              €50
├── Cat Products      €20
└── Cleaning Products €10

Unsplit amount:        €20
```

The transaction remains the actual bank movement.

Splits are used only for classification and reporting purposes and therefore do not affect the account balance independently.

The total value of all splits must never exceed the transaction amount:

```text
Sum of splits <= Transaction amount
```

Each split must reference a subcategory belonging to the same category as the transaction.

Duplicate subcategories inside the same transaction are not allowed.

---

### Transaction Splits

Splits can be created together with a transaction or added later.

Available operations:

```text
POST   /transactions/{transactionId}/splits
PATCH  /transactions/{transactionId}/splits/{splitId}
DELETE /transactions/{transactionId}/splits/{splitId}
```

When updating a transaction, existing splits are also taken into account.

For example, reducing a transaction amount below the current split total is rejected:

```text
Transaction amount: €100
Existing splits:     €80

New amount:          €60  ❌
```

Changing the transaction category is also rejected if its existing splits belong to subcategories from the previous category.

This keeps the transaction and its classifications consistent.

---

## Planned V1.0

With the main transaction system implemented, the next major step is the balance and reporting layer.

Transactions will be used together with the account's initial balance to calculate:

```text
Current balance
Income between dates
Expenses between dates
Expenses by category
Expenses by subcategory
Monthly totals
Transaction filtering
```

Because transaction splits are analytical classifications rather than separate financial movements, reports will distinguish between transaction totals and subcategory allocations to avoid double counting.

Persistent storage using PostgreSQL is also planned as part of the backend development.

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
│   ├── repository
│   ├── AccountController
│   └── AccountService
│
├── category
│   ├── dto
│   ├── entity
│   ├── repository
│   ├── CategoryController
│   └── CategoryService
│
├── subcategory
│   ├── dto
│   ├── entity
│   ├── repository
│   ├── SubcategoryController
│   └── SubcategoryService
│
├── transaction
│   ├── dto
│   ├── entity
│   ├── repository
│   ├── TransactionController
│   ├── TransactionService
│   └── TransactionSplitService
│
└── exception
```

Each feature contains its own controller, service, entities, repositories and DTOs where applicable.

The transaction feature separates transaction management from split management at the service level while exposing both through the same transaction API.

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

This includes scenarios such as:

```text
Missing resources
Duplicate resources
Invalid category/subcategory relationships
Transaction split limits
Invalid transaction updates
```

Controller tests focus on the HTTP contract, including:

```text
Request parameters
Path variables
JSON serialization/deserialization
Request validation
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

The application currently handles cases such as:

```text
Resources that do not exist
Invalid requests
Invalid business operations
```

Business validation is handled primarily at the service layer, while request validation is handled through Jakarta Validation at the controller boundary.

---

## Development Status

Cashiogram is currently a work in progress.

Completed:

```text
Account             ✅
Categories          ✅
Subcategories       ✅
Transactions        ✅
Transaction Splits  ✅
```

In progress / planned:

```text
Balance logic       ⏳
Reports             ⏳
Persistence         ⏳
Angular UI          ⏳
```

The project will continue to evolve as new requirements and ideas emerge.

---

## Author

**Mauro Fernandes**  
Full Stack Developer

Built as a personal project for practice, portfolio development, and real-world personal finance tracking.