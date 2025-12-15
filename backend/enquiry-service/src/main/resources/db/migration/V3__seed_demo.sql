-- Seed demo data for enquiry.enquiries
-- Uses ON CONFLICT on unique(code) to avoid duplicate inserts if rerun

INSERT INTO enquiry.enquiries (code, customer, channel, subject, status)
VALUES
  ('ENQ-1001', 'Acme Corp', 'Email', 'Request for product info', 'OPEN'),
  ('ENQ-1002', 'Globex Inc', 'Phone', 'Pricing and discount query', 'IN_PROGRESS'),
  ('ENQ-1003', 'Initech', 'Web', 'Bulk order enquiry', 'OPEN'),
  ('ENQ-1004', 'Umbrella Co', 'Email', 'Delivery timeline question', 'RESOLVED'),
  ('ENQ-1005', 'Soylent Corp', 'Phone', 'Post-sale support request', 'OPEN')
ON CONFLICT (code) DO NOTHING;
