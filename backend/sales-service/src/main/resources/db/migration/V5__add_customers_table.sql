-- Add customers table for data import feature
CREATE TABLE sales.customers (
  id BIGSERIAL PRIMARY KEY,
  customer_name VARCHAR(255) NOT NULL,
  phone VARCHAR(50),
  email VARCHAR(255),
  address VARCHAR(500),
  gst_number VARCHAR(50),
  opening_balance DECIMAL(15,2),
  balance_type VARCHAR(10) CHECK (balance_type IN ('Credit', 'Debit')),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_customers_email ON sales.customers(email);
CREATE INDEX idx_customers_name ON sales.customers(customer_name);
