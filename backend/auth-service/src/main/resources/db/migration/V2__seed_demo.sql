INSERT INTO auth.users(username,password,role) VALUES
  ('admin', '$2a$10$0bNL7RZ0Eo8hRzW0zS3vUO0n4m5c3Q1l7o0Lbn7m1D9vP3mFqvK8W', 'ADMIN')
ON CONFLICT DO NOTHING;
-- password hash here is for 'admin' (bcrypt). For demo roles, sign up via API.
