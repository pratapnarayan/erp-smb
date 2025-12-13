-- Seed demo data for users.user_profiles
INSERT INTO users.user_profiles (username, full_name, role) VALUES
  ('admin','Administrator','ADMIN'),
  ('jdoe','Jane Doe','USER'),
  ('jsmith','John Smith','USER')
ON CONFLICT (username) DO NOTHING;
