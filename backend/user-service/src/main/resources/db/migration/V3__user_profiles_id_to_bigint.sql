SET search_path TO users;
ALTER TABLE IF EXISTS user_profiles
    ALTER COLUMN id TYPE bigint;
