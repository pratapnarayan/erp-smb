-- Seed ~700 finance transactions with deterministic dates, accounts, types, and amounts
SET search_path TO finance;

WITH nums AS (
  SELECT i FROM generate_series(1,700) AS s(i)
), base AS (
  SELECT 
    1000 + i AS id,
    (DATE '2025-12-25' - (((i * 2) % 330))::int) AS tx_date,
    (ARRAY['AR','AP','Cash','Bank','Sales','Expenses','GST Input','GST Output'])[((i - 1) % 8) + 1] AS account,
    (ARRAY['CREDIT','DEBIT'])[((i - 1) % 2) + 1] AS tx_type,
    ROUND(((10000 + ((i * 157) % 900000)) / 100.0)::numeric, 2) AS amount,
    CASE WHEN ((i % 2) = 0) 
      THEN format('Invoice INV-%04s', 1000 + ((i * 7) % 500) + 1)
      ELSE format('Sales Order SO-%04s', 1000 + ((i * 11) % 500) + 1)
    END AS memo
  FROM nums
)
INSERT INTO transactions (id, tx_date, account, tx_type, amount, memo)
SELECT id, tx_date, account, tx_type, amount, memo FROM base;
