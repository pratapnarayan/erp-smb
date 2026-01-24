-- Create search schema
CREATE SCHEMA IF NOT EXISTS search;

-- Enable PostgreSQL extensions for full-text search
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS btree_gin;

-- Create search_products table
CREATE TABLE search.search_products (
    id BIGSERIAL PRIMARY KEY,
    entity_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    
    -- Product fields
    name VARCHAR(500) NOT NULL,
    sku VARCHAR(100),
    category VARCHAR(200),
    description TEXT,
    
    -- Full-text search vector
    search_vector tsvector,
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_search_products_entity UNIQUE (tenant_id, entity_id)
);

-- Create search_customers table
CREATE TABLE search.search_customers (
    id BIGSERIAL PRIMARY KEY,
    entity_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    
    -- Customer fields
    customer_name VARCHAR(500) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    gst_number VARCHAR(50),
    address TEXT,
    
    -- Full-text search vector
    search_vector tsvector,
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_search_customers_entity UNIQUE (tenant_id, entity_id)
);

-- Create search_orders table
CREATE TABLE search.search_orders (
    id BIGSERIAL PRIMARY KEY,
    entity_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    
    -- Order fields
    order_number VARCHAR(100) NOT NULL,
    customer_name VARCHAR(500),
    status VARCHAR(50),
    total_amount DECIMAL(15, 2),
    order_date DATE,
    
    -- Full-text search vector
    search_vector tsvector,
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_search_orders_entity UNIQUE (tenant_id, entity_id)
);

-- Create indexes for search_products
CREATE INDEX idx_search_products_tenant ON search.search_products(tenant_id);
CREATE INDEX idx_search_products_name ON search.search_products USING gin(name gin_trgm_ops) WHERE similarity(name, '') > 0.3;
CREATE INDEX idx_search_products_sku ON search.search_products USING gin(sku gin_trgm_ops) WHERE similarity(sku, '') > 0.3;
CREATE INDEX idx_search_products_vector ON search.search_products USING gin(search_vector);
CREATE INDEX idx_search_products_composite ON search.search_products(tenant_id, entity_id);

-- Create indexes for search_customers
CREATE INDEX idx_search_customers_tenant ON search.search_customers(tenant_id);
CREATE INDEX idx_search_customers_name ON search.search_customers USING gin(customer_name gin_trgm_ops);
CREATE INDEX idx_search_customers_email ON search.search_customers USING gin(email gin_trgm_ops);
CREATE INDEX idx_search_customers_phone ON search.search_customers USING gin(phone gin_trgm_ops);
CREATE INDEX idx_search_customers_vector ON search.search_customers USING gin(search_vector);
CREATE INDEX idx_search_customers_composite ON search.search_customers(tenant_id, entity_id);

-- Create indexes for search_orders
CREATE INDEX idx_search_orders_tenant ON search.search_orders(tenant_id);
CREATE INDEX idx_search_orders_number ON search.search_orders USING gin(order_number gin_trgm_ops);
CREATE INDEX idx_search_orders_customer ON search.search_orders USING gin(customer_name gin_trgm_ops);
CREATE INDEX idx_search_orders_vector ON search.search_orders USING gin(search_vector);
CREATE INDEX idx_search_orders_composite ON search.search_orders(tenant_id, entity_id);

-- Create trigger function to automatically update search_vector
CREATE OR REPLACE FUNCTION search.update_search_vector_products()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector := 
        setweight(to_tsvector('english', coalesce(NEW.name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(NEW.sku, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(NEW.category, '')), 'C') ||
        setweight(to_tsvector('english', coalesce(NEW.description, '')), 'D');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION search.update_search_vector_customers()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector := 
        setweight(to_tsvector('english', coalesce(NEW.customer_name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(NEW.email, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(NEW.phone, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(NEW.gst_number, '')), 'C') ||
        setweight(to_tsvector('english', coalesce(NEW.address, '')), 'D');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION search.update_search_vector_orders()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector := 
        setweight(to_tsvector('english', coalesce(NEW.order_number, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(NEW.customer_name, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(NEW.status, '')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers
CREATE TRIGGER trg_search_products_vector
    BEFORE INSERT OR UPDATE ON search.search_products
    FOR EACH ROW
    EXECUTE FUNCTION search.update_search_vector_products();

CREATE TRIGGER trg_search_customers_vector
    BEFORE INSERT OR UPDATE ON search.search_customers
    FOR EACH ROW
    EXECUTE FUNCTION search.update_search_vector_customers();

CREATE TRIGGER trg_search_orders_vector
    BEFORE INSERT OR UPDATE ON search.search_orders
    FOR EACH ROW
    EXECUTE FUNCTION search.update_search_vector_orders();

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION search.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply updated_at trigger to all tables
CREATE TRIGGER trg_search_products_updated_at
    BEFORE UPDATE ON search.search_products
    FOR EACH ROW
    EXECUTE FUNCTION search.update_updated_at_column();

CREATE TRIGGER trg_search_customers_updated_at
    BEFORE UPDATE ON search.search_customers
    FOR EACH ROW
    EXECUTE FUNCTION search.update_updated_at_column();

CREATE TRIGGER trg_search_orders_updated_at
    BEFORE UPDATE ON search.search_orders
    FOR EACH ROW
    EXECUTE FUNCTION search.update_updated_at_column();
