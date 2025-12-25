-- Seed ~50 user profiles with deterministic usernames and roles
SET search_path TO users;

WITH nums AS (
  SELECT i FROM generate_series(1,50) AS s(i)
), base AS (
  SELECT 
    1000 + i AS id,
    (
      (ARRAY['Aarav','Vivaan','Aditya','Vihaan','Arjun','Reyansh','Ishaan','Shaurya','Atharv','Krishna',
             'Ananya','Diya','Aadhya','Myra','Ira','Aarohi','Siya','Kiara','Anika','Nisha'])[((i - 1) % 20) + 1]
      || '.' || lower((ARRAY['sharma','gupta','verma','iyer','reddy','patel','nair','khanna','kapoor','mehta',
                             'rao','agarwal','desai','joshi','menon','bansal','kulkarni','singh','jain','chopra'])[(((i - 1) / 5) % 20) + 1])
    ) AS username,
    (
      (ARRAY['Aarav','Vivaan','Aditya','Vihaan','Arjun','Reyansh','Ishaan','Shaurya','Atharv','Krishna',
             'Ananya','Diya','Aadhya','Myra','Ira','Aarohi','Siya','Kiara','Anika','Nisha'])[((i - 1) % 20) + 1]
      || ' ' || (ARRAY['Sharma','Gupta','Verma','Iyer','Reddy','Patel','Nair','Khanna','Kapoor','Mehta',
                        'Rao','Agarwal','Desai','Joshi','Menon','Bansal','Kulkarni','Singh','Jain','Chopra'])[(((i - 1) / 5) % 20) + 1]
    ) AS full_name,
    (ARRAY['USER','USER','MANAGER','USER','ADMIN'])[((i - 1) % 5) + 1] AS role
  FROM nums
)
INSERT INTO user_profiles (id, username, full_name, role)
SELECT id, username, full_name, role FROM base
ON CONFLICT (username) DO NOTHING;
