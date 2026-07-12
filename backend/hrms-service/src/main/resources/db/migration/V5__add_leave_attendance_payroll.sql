-- V5: Add leave_requests, attendance, and payroll tables with seed data
SET search_path TO hrms;

-- ── Leave Requests ───────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS hrms.leave_requests (
  id        BIGSERIAL    PRIMARY KEY,
  employee  VARCHAR(255) NOT NULL,
  type      VARCHAR(64)  NOT NULL,
  from_date DATE         NOT NULL,
  to_date   DATE         NOT NULL,
  days      INT          NOT NULL,
  status    VARCHAR(32)  NOT NULL DEFAULT 'Pending'
);

-- ── Attendance ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS hrms.attendance (
  id         BIGSERIAL    PRIMARY KEY,
  employee   VARCHAR(255) NOT NULL,
  att_date   DATE         NOT NULL,
  check_in   VARCHAR(10),
  check_out  VARCHAR(10),
  hours      VARCHAR(20),
  status     VARCHAR(32)  NOT NULL
);

-- ── Payroll ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS hrms.payroll (
  id          BIGSERIAL      PRIMARY KEY,
  employee    VARCHAR(255)   NOT NULL,
  role        VARCHAR(128)   NOT NULL,
  gross       NUMERIC(12,2)  NOT NULL,
  deductions  NUMERIC(12,2)  NOT NULL,
  net         NUMERIC(12,2)  NOT NULL,
  pay_status  VARCHAR(32)    NOT NULL DEFAULT 'Pending'
);

-- ── Seed: Leave Requests ─────────────────────────────────────────────────────
INSERT INTO hrms.leave_requests (employee, type, from_date, to_date, days, status) VALUES
  ('Ananya Sharma',  'Annual Leave',    current_date - 12, current_date - 10, 3,  'Approved'),
  ('Aditya Gupta',   'Sick Leave',      current_date - 16, current_date - 15, 2,  'Approved'),
  ('Myra Verma',     'Annual Leave',    current_date + 6,  current_date + 8,  3,  'Pending'),
  ('Vivaan Iyer',    'Work From Home',  current_date - 17, current_date - 17, 1,  'Approved'),
  ('Ira Reddy',      'Maternity Leave', current_date + 8,  current_date + 98, 91, 'Approved'),
  ('Ishaan Iyer',    'Casual Leave',    current_date - 5,  current_date - 4,  2,  'Approved'),
  ('Siya Reddy',     'Sick Leave',      current_date - 3,  current_date - 3,  1,  'Rejected'),
  ('Aarav Sharma',   'Paternity Leave', current_date + 14, current_date + 20, 7,  'Pending'),
  ('Diya Gupta',     'Annual Leave',    current_date + 20, current_date + 27, 8,  'Pending'),
  ('Arjun Verma',    'Annual Leave',    current_date - 30, current_date - 25, 6,  'Approved'),
  ('Kiara Patel',    'Casual Leave',    current_date - 2,  current_date - 1,  2,  'Approved'),
  ('Vihaan Nair',    'Sick Leave',      current_date - 7,  current_date - 6,  2,  'Approved'),
  ('Reyansh Khanna', 'Casual Leave',    current_date + 3,  current_date + 3,  1,  'Pending'),
  ('Aarohi Kapoor',  'Annual Leave',    current_date + 30, current_date + 37, 8,  'Pending'),
  ('Anika Mehta',    'Sick Leave',      current_date - 9,  current_date - 8,  2,  'Approved');

-- ── Seed: Attendance (current day + 6 preceding working days × 12 employees) ─
INSERT INTO hrms.attendance (employee, att_date, check_in, check_out, hours, status) VALUES
  -- Day 0 (today)
  ('Aarav Sharma',   current_date, '09:02', '18:14', '9h 12m', 'Present'),
  ('Diya Gupta',     current_date, '09:45', '18:30', '8h 45m', 'Late'),
  ('Arjun Verma',    current_date, NULL,    NULL,    NULL,      'Absent'),
  ('Ananya Sharma',  current_date, NULL,    NULL,    NULL,      'On Leave'),
  ('Ishaan Iyer',    current_date, '08:58', '17:55', '8h 57m', 'Present'),
  ('Siya Reddy',     current_date, '10:00', '19:02', '9h 02m', 'Present'),
  ('Myra Verma',     current_date, '09:10', '18:05', '8h 55m', 'Present'),
  ('Vivaan Iyer',    current_date, '09:30', '18:30', '9h 00m', 'Present'),
  ('Aditya Gupta',   current_date, '08:45', '17:50', '9h 05m', 'Present'),
  ('Kiara Patel',    current_date, '09:55', '18:50', '8h 55m', 'Late'),
  ('Vihaan Nair',    current_date, '09:00', '18:00', '9h 00m', 'Present'),
  ('Reyansh Khanna', current_date, NULL,    NULL,    NULL,      'Absent'),
  -- Day -1
  ('Aarav Sharma',   current_date - 1, '09:00', '18:00', '9h 00m', 'Present'),
  ('Diya Gupta',     current_date - 1, '09:15', '18:15', '9h 00m', 'Present'),
  ('Arjun Verma',    current_date - 1, '09:05', '18:10', '9h 05m', 'Present'),
  ('Ananya Sharma',  current_date - 1, NULL,    NULL,    NULL,      'On Leave'),
  ('Ishaan Iyer',    current_date - 1, '08:55', '17:55', '9h 00m', 'Present'),
  ('Siya Reddy',     current_date - 1, '09:48', '18:45', '8h 57m', 'Late'),
  ('Myra Verma',     current_date - 1, '09:00', '18:00', '9h 00m', 'Present'),
  ('Vivaan Iyer',    current_date - 1, '09:10', '18:05', '8h 55m', 'Present'),
  -- Day -2
  ('Aarav Sharma',   current_date - 2, '09:03', '18:10', '9h 07m', 'Present'),
  ('Diya Gupta',     current_date - 2, '10:02', '19:00', '8h 58m', 'Late'),
  ('Arjun Verma',    current_date - 2, NULL,    NULL,    NULL,      'Absent'),
  ('Ishaan Iyer',    current_date - 2, '08:50', '17:50', '9h 00m', 'Present'),
  ('Siya Reddy',     current_date - 2, '09:00', '18:00', '9h 00m', 'Present'),
  ('Vivaan Iyer',    current_date - 2, '09:20', '18:15', '8h 55m', 'Present'),
  -- Day -3
  ('Aarav Sharma',   current_date - 3, '09:01', '18:05', '9h 04m', 'Present'),
  ('Diya Gupta',     current_date - 3, '09:30', '18:25', '8h 55m', 'Present'),
  ('Ananya Sharma',  current_date - 3, NULL,    NULL,    NULL,      'On Leave'),
  ('Ishaan Iyer',    current_date - 3, '08:58', '17:55', '8h 57m', 'Present'),
  ('Siya Reddy',     current_date - 3, NULL,    NULL,    NULL,      'Absent');

-- ── Seed: Payroll (current month) ────────────────────────────────────────────
INSERT INTO hrms.payroll (employee, role, gross, deductions, net, pay_status) VALUES
  ('Aarav Sharma',   'Senior Engineer',    120000.00, 18360.00, 101640.00, 'Processed'),
  ('Diya Gupta',     'HR Partner',          75000.00, 11475.00,  63525.00, 'Processed'),
  ('Arjun Verma',    'QA Analyst',          68000.00, 10404.00,  57596.00, 'Processed'),
  ('Ananya Sharma',  'Finance Analyst',     82000.00, 12546.00,  69454.00, 'On Hold'),
  ('Ishaan Iyer',    'DevOps Engineer',     95000.00, 14535.00,  80465.00, 'Processed'),
  ('Siya Reddy',     'Sales Executive',     55000.00,  8415.00,  46585.00, 'Pending'),
  ('Myra Verma',     'Product Manager',    110000.00, 16830.00,  93170.00, 'Processed'),
  ('Vivaan Iyer',    'Software Engineer',   88000.00, 13464.00,  74536.00, 'Processed'),
  ('Aditya Gupta',   'Software Engineer',   85000.00, 13005.00,  71995.00, 'Processed'),
  ('Kiara Patel',    'Designer',            72000.00, 11016.00,  60984.00, 'Processed'),
  ('Vihaan Nair',    'Support Engineer',    62000.00,  9486.00,  52514.00, 'Processed'),
  ('Reyansh Khanna', 'Operations Manager',  98000.00, 14994.00,  83006.00, 'Pending'),
  ('Aarohi Kapoor',  'Business Analyst',    78000.00, 11934.00,  66066.00, 'Processed'),
  ('Anika Mehta',    'Sales Executive',     60000.00,  9180.00,  50820.00, 'Processed'),
  ('Ira Reddy',      'Marketing Manager',   92000.00, 14076.00,  77924.00, 'On Hold');
