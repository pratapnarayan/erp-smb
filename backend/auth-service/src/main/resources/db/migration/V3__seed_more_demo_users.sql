-- Additional demo users for role-based UI testing (password = username)
-- These map to platform-supported roles: ADMIN/OWNER/MANAGER/USER
--
-- Note: In some local/dev setups the auth schema/table may be missing (e.g., dropped manually)
-- while Flyway history still exists. To make this migration idempotent and more resilient,
-- we (re)create the schema/table if needed before inserting demo users.

CREATE SCHEMA IF NOT EXISTS auth;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS auth.users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(200) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  roles VARCHAR(200) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO auth.users (username, password, enabled, roles)
VALUES
  ('sales', crypt('sales', gen_salt('bf', 10)), TRUE, 'ROLE_USER'),
  ('warehouse', crypt('warehouse', gen_salt('bf', 10)), TRUE, 'ROLE_USER'),
  ('finance', crypt('finance', gen_salt('bf', 10)), TRUE, 'ROLE_USER'),
  ('hr', crypt('hr', gen_salt('bf', 10)), TRUE, 'ROLE_USER')
ON CONFLICT (username) DO UPDATE
  SET password = EXCLUDED.password,
      enabled = EXCLUDED.enabled,
      roles = EXCLUDED.roles;
