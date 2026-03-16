-- =============================================
-- Pravarthana HRMS - Seed Data
-- Demo users and sample data
-- =============================================

-- Passwords updated to per-user passwords (BCrypt, cost=10)
-- admin@pravarthana.com    → admin@123
-- hr@pravarthana.com       → headhr@123
-- manager@pravarthana.com  → teamlead@123
-- employee@pravarthana.com → employee@123
-- john.doe@pravarthana.com → employee@123

-- =============================================
-- 1. DEMO DEPARTMENTS
-- =============================================

INSERT INTO departments (name, description, is_active) VALUES
('Engineering', 'Software development and technology', true),
('Human Resources', 'HR and recruitment', true),
('Sales', 'Sales and business development', true),
('Marketing', 'Marketing and communications', true),
('Finance', 'Accounts and finance', true)
ON CONFLICT DO NOTHING;

-- =============================================
-- 2. DEMO USERS
-- =============================================

-- Super Admin
INSERT INTO users (email, password_hash, role_id, is_active) VALUES
('admin@pravarthana.com', '$2a$10$epOjs7w0vAqBzK6zXM7ac.vPsi9wGVN2RdKwouC4KK0Y6USnCxRAO',
 (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'), true);

-- HR Admin
INSERT INTO users (email, password_hash, role_id, is_active) VALUES
('hr@pravarthana.com', '$2a$10$0zNw3EpVjscAIJF1LmW2Juk/1MnslcasY0twOrTGkON1tdB3A0xdi',
 (SELECT id FROM roles WHERE name = 'HR_ADMIN'), true);

-- Team Lead
INSERT INTO users (email, password_hash, role_id, is_active) VALUES
('manager@pravarthana.com', '$2a$10$gpNv7HC6WyC37XUfgLguR.rQ/kRWKmUX6g.A7NdoLdJ/d0VtiF7Ru',
 (SELECT id FROM roles WHERE name = 'TEAM_LEAD'), true);

-- Employee 1
INSERT INTO users (email, password_hash, role_id, is_active) VALUES
('employee@pravarthana.com', '$2a$10$eVRCfw/iC/aHBCXDsIHnreGeTQHeGG7QRACyk4Zb8XW2Y9SwQSpiO',
 (SELECT id FROM roles WHERE name = 'EMPLOYEE'), true);

-- Employee 2
INSERT INTO users (email, password_hash, role_id, is_active) VALUES
('john.doe@pravarthana.com', '$2a$10$tqYV9AkSyjnKSXkVKA/9f.tqpGg7D8DN2F5EXChIkdM.m2S/TgtE6',
 (SELECT id FROM roles WHERE name = 'EMPLOYEE'), true);

-- =============================================
-- 3. DEMO EMPLOYEES
-- =============================================

-- Super Admin Employee Profile
INSERT INTO employees (
    user_id, employee_id, first_name, last_name, phone, 
    date_of_birth, gender, department_id, designation, 
    date_of_joining, employment_type, salary, status
) VALUES (
    (SELECT id FROM users WHERE email = 'admin@pravarthana.com'),
    'EMP001',
    'Admin',
    'User',
    '+91-9876543210',
    '1985-01-15',
    'Male',
    (SELECT id FROM departments WHERE name = 'Human Resources'),
    'System Administrator',
    '2020-01-01',
    'Full-time',
    150000.00,
    'ACTIVE'
);

-- HR Admin Employee Profile
INSERT INTO employees (
    user_id, employee_id, first_name, last_name, phone, 
    date_of_birth, gender, department_id, designation, 
    date_of_joining, employment_type, salary, status
) VALUES (
    (SELECT id FROM users WHERE email = 'hr@pravarthana.com'),
    'EMP002',
    'Priya',
    'Sharma',
    '+91-9876543211',
    '1990-03-20',
    'Female',
    (SELECT id FROM departments WHERE name = 'Human Resources'),
    'HR Manager',
    '2021-02-15',
    'Full-time',
    120000.00,
    'ACTIVE'
);

-- Manager Employee Profile
INSERT INTO employees (
    user_id, employee_id, first_name, last_name, phone, 
    date_of_birth, gender, department_id, designation, 
    date_of_joining, employment_type, salary, status
) VALUES (
    (SELECT id FROM users WHERE email = 'manager@pravarthana.com'),
    'EMP003',
    'Rajesh',
    'Kumar',
    '+91-9876543212',
    '1988-07-10',
    'Male',
    (SELECT id FROM departments WHERE name = 'Engineering'),
    'Engineering Manager',
    '2020-06-01',
    'Full-time',
    180000.00,
    'ACTIVE'
);

-- Employee 1 Profile
INSERT INTO employees (
    user_id, employee_id, first_name, last_name, phone, 
    date_of_birth, gender, department_id, designation, 
    reporting_manager_id, date_of_joining, employment_type, salary, status
) VALUES (
    (SELECT id FROM users WHERE email = 'employee@pravarthana.com'),
    'EMP004',
    'Amit',
    'Patel',
    '+91-9876543213',
    '1995-11-25',
    'Male',
    (SELECT id FROM departments WHERE name = 'Engineering'),
    'Software Engineer',
    (SELECT id FROM employees WHERE employee_id = 'EMP003'),
    '2022-08-01',
    'Full-time',
    80000.00,
    'ACTIVE'
);

-- Employee 2 Profile
INSERT INTO employees (
    user_id, employee_id, first_name, last_name, phone, 
    date_of_birth, gender, department_id, designation, 
    reporting_manager_id, date_of_joining, employment_type, salary, status
) VALUES (
    (SELECT id FROM users WHERE email = 'john.doe@pravarthana.com'),
    'EMP005',
    'John',
    'Doe',
    '+91-9876543214',
    '1993-05-18',
    'Male',
    (SELECT id FROM departments WHERE name = 'Engineering'),
    'Senior Software Engineer',
    (SELECT id FROM employees WHERE employee_id = 'EMP003'),
    '2021-03-15',
    'Full-time',
    120000.00,
    'ACTIVE'
);

-- =============================================
-- 4. LEAVE BALANCES (2024)
-- =============================================

-- Initialize leave balances for all employees for year 2024
INSERT INTO leave_balances (employee_id, leave_type_id, year, total_balance, used_balance, remaining_balance)
SELECT 
    e.id,
    lt.id,
    2024,
    lt.default_balance,
    0,
    lt.default_balance
FROM employees e
CROSS JOIN leave_types lt
ON CONFLICT (employee_id, leave_type_id, year) DO NOTHING;

-- =============================================
-- 5. SAMPLE ATTENDANCE DATA (Last 30 days)
-- =============================================

-- Generate attendance for last 30 days for all employees
DO $$
DECLARE
    emp_record RECORD;
    day_offset INT;
    attendance_date DATE;
    check_in_hour INT;
    is_late_flag BOOLEAN;
BEGIN
    FOR emp_record IN SELECT id FROM employees LOOP
        FOR day_offset IN 0..29 LOOP
            attendance_date := CURRENT_DATE - day_offset;
            
            -- Skip weekends
            IF EXTRACT(DOW FROM attendance_date) NOT IN (0, 6) THEN
                -- Random check-in time between 8:30 AM and 10:00 AM
                check_in_hour := 8 + floor(random() * 2)::INT;
                is_late_flag := check_in_hour >= 9 AND (random() > 0.7);
                
                INSERT INTO attendance (
                    employee_id, 
                    date, 
                    check_in_time, 
                    check_out_time,
                    status,
                    is_late,
                    total_hours
                ) VALUES (
                    emp_record.id,
                    attendance_date,
                    make_time(check_in_hour, (random() * 30)::INT, 0),
                    make_time(17 + (random() * 2)::INT, (random() * 30)::INT, 0),
                    CASE 
                        WHEN random() > 0.95 THEN 'ABSENT'
                        WHEN is_late_flag THEN 'LATE'
                        ELSE 'PRESENT'
                    END,
                    is_late_flag,
                    8.0 + (random() * 2)
                )
                ON CONFLICT (employee_id, date) DO NOTHING;
            END IF;
        END LOOP;
    END LOOP;
END $$;

-- =============================================
-- 6. SAMPLE LEAVE REQUESTS
-- =============================================

-- Sample approved leave
INSERT INTO leaves (
    employee_id, 
    leave_type_id, 
    start_date, 
    end_date, 
    total_days, 
    reason, 
    status,
    approved_by,
    approved_at
) VALUES (
    (SELECT id FROM employees WHERE employee_id = 'EMP004'),
    (SELECT id FROM leave_types WHERE code = 'CL'),
    CURRENT_DATE + 10,
    CURRENT_DATE + 12,
    3,
    'Family function',
    'APPROVED',
    (SELECT id FROM employees WHERE employee_id = 'EMP003'),
    CURRENT_TIMESTAMP
);

-- Sample pending leave
INSERT INTO leaves (
    employee_id, 
    leave_type_id, 
    start_date, 
    end_date, 
    total_days, 
    reason, 
    status
) VALUES (
    (SELECT id FROM employees WHERE employee_id = 'EMP005'),
    (SELECT id FROM leave_types WHERE code = 'SL'),
    CURRENT_DATE + 5,
    CURRENT_DATE + 6,
    2,
    'Medical appointment',
    'PENDING'
);

-- Update leave balances after approved leave
UPDATE leave_balances SET
    used_balance = 3,
    remaining_balance = total_balance - 3
WHERE employee_id = (SELECT id FROM employees WHERE employee_id = 'EMP004')
AND leave_type_id = (SELECT id FROM leave_types WHERE code = 'CL')
AND year = 2024;

-- =============================================
-- 7. SAMPLE DEMO REQUESTS
-- =============================================

INSERT INTO demo_requests (company_name, contact_name, email, phone, company_size, message, status) VALUES
('Tech Innovations Pvt Ltd', 'Suresh Reddy', 'suresh@techinnovations.com', '+91-9988776655', '50-100', 'Interested in HRMS demo for our company', 'NEW'),
('Global Solutions Inc', 'Meera Iyer', 'meera@globalsolutions.com', '+91-9876512345', '100-500', 'Looking for comprehensive HR management system', 'CONTACTED'),
('StartupHub', 'Vikram Singh', 'vikram@startuphub.com', '+91-8765432109', '10-50', 'Need HRMS for small team', 'NEW');

-- =============================================
-- 8. SAMPLE CHAT ROOMS
-- =============================================

-- Direct chat between Manager and Employee
INSERT INTO chat_rooms (name, type, created_by) VALUES
(NULL, 'DIRECT', (SELECT id FROM users WHERE email = 'manager@pravarthana.com'));

-- Add members to direct chat
INSERT INTO chat_room_members (room_id, user_id) VALUES
((SELECT MAX(id) FROM chat_rooms), (SELECT id FROM users WHERE email = 'manager@pravarthana.com')),
((SELECT MAX(id) FROM chat_rooms), (SELECT id FROM users WHERE email = 'employee@pravarthana.com'));

-- Group chat for Engineering team
INSERT INTO chat_rooms (name, type, created_by) VALUES
('Engineering Team', 'GROUP', (SELECT id FROM users WHERE email = 'manager@pravarthana.com'));

-- Add members to group chat
INSERT INTO chat_room_members (room_id, user_id) VALUES
((SELECT MAX(id) FROM chat_rooms), (SELECT id FROM users WHERE email = 'manager@pravarthana.com')),
((SELECT MAX(id) FROM chat_rooms), (SELECT id FROM users WHERE email = 'employee@pravarthana.com')),
((SELECT MAX(id) FROM chat_rooms), (SELECT id FROM users WHERE email = 'john.doe@pravarthana.com'));

-- Sample messages
INSERT INTO chat_messages (room_id, sender_id, message) VALUES
((SELECT MAX(id) FROM chat_rooms), 
 (SELECT id FROM users WHERE email = 'manager@pravarthana.com'),
 'Welcome to the Engineering team chat!');

-- =============================================
-- END OF SEED DATA
-- =============================================

-- Display summary
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Seed Data Loaded Successfully!';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Demo Accounts Created:';
    RAISE NOTICE '  - admin@pravarthana.com (Super Admin)';
    RAISE NOTICE '  - hr@pravarthana.com (HR Admin)';
    RAISE NOTICE '  - manager@pravarthana.com (Manager)';
    RAISE NOTICE '  - employee@pravarthana.com (Employee)';
    RAISE NOTICE '  - john.doe@pravarthana.com (Employee)';
    RAISE NOTICE '';
    RAISE NOTICE 'Password for all: password123';
    RAISE NOTICE '========================================';
END $$;
