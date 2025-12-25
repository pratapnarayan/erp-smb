-- Seed ~500 sales orders with deterministic amounts and dates across last ~11 months
SET search_path TO orders;

WITH nums AS (
  SELECT i FROM generate_series(1,500) AS s(i)
), base AS (
  SELECT 
    1000 + i AS id,
    format('SO-%04s', 1000 + i) AS code,
    (ARRAY[
      'Tata Motors','Infosys','Reliance Retail','Wipro','HDFC Bank','Mahindra & Mahindra','Flipkart','Paytm','Maruti Suzuki','Aditya Birla Group',
      'Larsen & Toubro','HCL Technologies','Bajaj Auto','Godrej','ITC Limited','Asian Paints','Hindustan Unilever','Tech Mahindra','TCS','Zomato'
    ])[((i - 1) % 20) + 1] AS customer,
    (ARRAY['OPEN','CONFIRMED','SHIPPED','DELIVERED','CANCELLED'])[((i - 1) % 5) + 1] AS status,
    ROUND(((50000 + ((i * 137) % 450000)) / 100.0)::numeric, 2) AS total,
    (DATE '2025-12-25' - (((i * 3) % 330))::int) AS order_date
  FROM nums
)
INSERT INTO sales_orders (id, code, customer, status, total, order_date)
SELECT id, code, customer, status, total, order_date FROM base
ON CONFLICT (code) DO NOTHING;
