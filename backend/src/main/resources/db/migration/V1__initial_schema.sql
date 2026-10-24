CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    initial_balance NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uq_categories_name_ci
    ON categories (LOWER(name));

CREATE TABLE subcategories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    category_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_subcategories_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
);

CREATE UNIQUE INDEX uq_subcategories_category_name_ci
    ON subcategories (category_id, LOWER(name));

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    amount NUMERIC(19, 2) NOT NULL,
    transaction_type VARCHAR(255) NOT NULL,
    category_id UUID NOT NULL,
    date DATE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_transactions_amount_positive
        CHECK (amount > 0),

    CONSTRAINT fk_transactions_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
);

CREATE TABLE transaction_splits (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    subcategory_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,

    CONSTRAINT chk_transaction_splits_amount_positive
        CHECK (amount > 0),

    CONSTRAINT fk_transaction_splits_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id),

    CONSTRAINT fk_transaction_splits_subcategory
        FOREIGN KEY (subcategory_id)
        REFERENCES subcategories(id),

    CONSTRAINT uq_transaction_splits_transaction_subcategory
        UNIQUE (transaction_id, subcategory_id)
);