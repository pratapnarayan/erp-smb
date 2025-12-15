INSERT INTO auth.users (username, password, enabled, roles)
VALUES
  ('admin', crypt('admin', gen_salt('bf', 10)), TRUE, 'ROLE_ADMIN')
ON CONFLICT (username) DO UPDATE
  SET password = EXCLUDED.password,
      enabled = EXCLUDED.enabled,
      roles = EXCLUDED.roles;
