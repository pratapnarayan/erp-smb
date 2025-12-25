-- Seed ~200 employees with realistic Indian names, roles, departments, and varied status
SET search_path TO hrms;

WITH nums AS (
  SELECT i FROM generate_series(1,200) AS s(i)
), base AS (
  SELECT 
    1000 + i AS id,
    (
      (ARRAY['Aarav','Vivaan','Aditya','Vihaan','Arjun','Reyansh','Ishaan','Shaurya','Atharv','Krishna',
             'Ananya','Diya','Aadhya','Myra','Ira','Aarohi','Siya','Kiara','Anika','Nisha'])[((i - 1) % 20) + 1]
      || ' '
      || (ARRAY['Sharma','Gupta','Verma','Iyer','Reddy','Patel','Nair','Khanna','Kapoor','Mehta',
                'Rao','Agarwal','Desai','Joshi','Menon','Bansal','Kulkarni','Singh','Jain','Chopra'])[(((i - 1) / 10) % 20) + 1]
    ) AS name,
    (ARRAY['Software Engineer','Senior Engineer','QA Analyst','HR Partner','Finance Analyst','Sales Executive','Support Engineer','DevOps Engineer','Product Manager','Designer'])[((i - 1) % 10) + 1] AS role,
    (ARRAY['Engineering','HR','Finance','Sales','Support','Operations','IT','Admin'])[((i - 1) % 8) + 1] AS dept,
    (ARRAY['ACTIVE','ACTIVE','ACTIVE','ON_LEAVE','INACTIVE'])[((i - 1) % 5) + 1] AS status
  FROM nums
)
INSERT INTO employees (id, name, role, dept, status)
SELECT id, name, role, dept, status FROM base;
