-- Seed ~500 invoices with deterministic amounts and due dates spread across past and near future
SET search_path TO sales;

WITH nums AS (
  SELECT i FROM generate_series(1,500) AS s(i)
), base AS (
  SELECT 
    1000 + i AS id,
    format('INV-%04s', 1000 + i) AS invoice_no,
    (ARRAY[
      'Tata Motors','Infosys','Reliance Retail','Wipro','HDFC Bank','Mahindra & Mahindra','Flipkart','Paytm','Maruti Suzuki','Aditya Birla Group',
      'Larsen & Toubro','HCL Technologies','Bajaj Auto','Godrej','ITC Limited','Asian Paints','Hindustan Unilever','Tech Mahindra','TCS','Zomato'
    ])[((i - 1) % 20) + 1] AS customer,
    (ARRAY['OPEN','PAID','OVERDUE','CANCELLED','PAID'])[((i - 1) % 5) + 1] AS status,
    CASE 
      WHEN (ARRAY['OPEN','PAID','OVERDUE','CANCELLED','PAID'])[((i - 1) % 5) + 1] = 'PAID' THEN (DATE '2025-12-25' - (((i * 7) % 180)))
      WHEN (ARRAY['OPEN','PAID','OVERDUE','CANCELLED','PAID'])[((i - 1) % 5) + 1] = 'OVERDUE' THEN (DATE '2025-12-25' - (((i * 11) % 90) + 10))
      WHEN (ARRAY['OPEN','PAID','OVERDUE','CANCELLED','PAID'])[((i - 1) % 5) + 1] = 'OPEN' THEN (DATE '2025-12-25' + (((i * 3) % 60) + 5))
      WHEN (ARRAY['OPEN','PAID','OVERDUE','CANCELLED','PAID'])[((i - 1) % 5) + 1] = 'CANCELLED' THEN (DATE '2025-12-25' - (((i * 13) % 200)))
      ELSE (DATE '2025-12-25' + (((i * 2) % 30)))
    END AS due,
    ROUND(((50000 + ((i * 199) % 600000)) / 100.0)::numeric, 2) AS amount
  FROM nums
)
INSERT INTO invoices (id, invoice_no, customer, status, due, amount)
SELECT id, invoice_no, customer, status, due, amount FROM base
ON CONFLICT (invoice_no) DO NOTHING;
