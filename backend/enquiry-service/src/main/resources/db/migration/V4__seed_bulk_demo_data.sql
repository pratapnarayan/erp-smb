-- Seed ~500 enquiries with deterministic data across realistic channels and statuses
SET search_path TO enquiry;

WITH nums AS (
  SELECT i FROM generate_series(1,500) AS s(i)
), base AS (
  SELECT 
    1000 + i AS id,
    format('ENQ-%04s', 1000 + i) AS code,
    (ARRAY[
      'Aarav Sharma','Vivaan Gupta','Aditya Verma','Vihaan Iyer','Arjun Reddy',
      'Sai Raj','Krishna Nair','Manish Patel','Rohit Sharma','Rahul Khanna',
      'Priya Singh','Neha Agarwal','Ananya Rao','Ishita Mehta','Kavya Kapoor',
      'Riya Desai','Sneha Joshi','Pooja Menon','Nikita Bansal','Aditi Kulkarni'
    ])[((i - 1) % 20) + 1] AS customer,
    (ARRAY['Email','Phone','Web','WhatsApp'])[((i - 1) % 4) + 1] AS channel,
    (ARRAY[
      'Request for product info','Pricing and discount query','Bulk order enquiry','Delivery timeline question',
      'Post-sale support request','Invoice copy needed','Warranty claim','Installation assistance','Return policy question','Payment options'
    ])[((i - 1) % 10) + 1] AS subject,
    (ARRAY['OPEN','IN_PROGRESS','RESOLVED','CLOSED'])[((i - 1) % 4) + 1] AS status
  FROM nums
)
INSERT INTO enquiries (id, code, customer, channel, subject, status)
SELECT id, code, customer, channel, subject, status FROM base
ON CONFLICT (code) DO NOTHING;
