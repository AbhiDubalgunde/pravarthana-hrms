-- =============================================
-- Pravarthana HRMS - Database Schema
-- PostgreSQL (Supabase)
-- Version: 1.0.0
-- =============================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================
-- 1. ROLES & PERMISSIONS
-- =============================================

CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO roles (name, description) VALUES
('SUPER_ADMIN', 'Full system access'),
('HR_ADMIN', 'HR management access'),
('TEAM_LEAD', 'Team lead access - manage their team'),
('EMPLOYEE', 'Basic employee access')
ON CONFLICT (name) DO NOTHING;

-- =============================================
-- 2. DEPARTMENTS
-- =============================================

CREATE TABLE IF NOT EXISTS departments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    head_employee_id INT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 3. USERS & EMPLOYEES
-- =============================================

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL REFERENCES roles(id),
    is_active BOOLEAN DEFAULT true,
    last_login TIMESTAMP,
    password_reset_token VARCHAR(255),
    password_reset_expires TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employees (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    employee_id VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100) DEFAULT 'India',
    postal_code VARCHAR(20),
    
    -- Employment details
    department_id INT REFERENCES departments(id),
    designation VARCHAR(100),
    reporting_manager_id INT REFERENCES employees(id),
    date_of_joining DATE NOT NULL,
    employment_type VARCHAR(50), -- Full-time, Part-time, Contract
    
    -- Salary (optional for MVP)
    salary DECIMAL(10, 2),
    
    -- Status
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, TERMINATED
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint after employees table is created
ALTER TABLE departments 
ADD CONSTRAINT fk_dept_head 
FOREIGN KEY (head_employee_id) 
REFERENCES employees(id);

-- =============================================
-- 4. ATTENDANCE
-- =============================================

CREATE TABLE IF NOT EXISTS attendance (
    id SERIAL PRIMARY KEY,
    employee_id INT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    
    -- Manual check-in/out
    check_in_time TIME,
    check_out_time TIME,
    
    -- Login tracking (for reporting)
    first_login_time TIMESTAMP,
    last_logout_time TIMESTAMP,
    
    -- Status
    status VARCHAR(20) NOT NULL, -- PRESENT, ABSENT, LATE, HALF_DAY, LEAVE
    is_late BOOLEAN DEFAULT false,
    
    -- Working hours
    total_hours DECIMAL(4, 2),
    
    -- Notes
    notes TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(employee_id, date)
);

CREATE INDEX idx_attendance_employee ON attendance(employee_id);
CREATE INDEX idx_attendance_date ON attendance(date);

-- =============================================
-- 5. LEAVE MANAGEMENT
-- =============================================

CREATE TABLE IF NOT EXISTS leave_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    code VARCHAR(10) UNIQUE NOT NULL,
    default_balance INT NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true
);

INSERT INTO leave_types (name, code, default_balance, description) VALUES
('Casual Leave', 'CL', 12, 'Casual or planned leave'),
('Sick Leave', 'SL', 12, 'Medical or health-related leave'),
('Earned Leave', 'EL', 18, 'Earned/privilege leave'),
('Unpaid Leave', 'UL', 9999, 'Leave without pay')
ON CONFLICT (code) DO NOTHING;

CREATE TABLE IF NOT EXISTS leave_balances (
    id SERIAL PRIMARY KEY,
    employee_id INT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    leave_type_id INT NOT NULL REFERENCES leave_types(id),
    year INT NOT NULL,
    total_balance INT NOT NULL,
    used_balance INT DEFAULT 0,
    remaining_balance INT NOT NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(employee_id, leave_type_id, year)
);

CREATE TABLE IF NOT EXISTS leaves (
    id SERIAL PRIMARY KEY,
    employee_id INT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    leave_type_id INT NOT NULL REFERENCES leave_types(id),
    
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT NOT NULL,
    
    reason TEXT NOT NULL,
    
    -- Approval
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    approved_by INT REFERENCES employees(id),
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_leaves_employee ON leaves(employee_id);
CREATE INDEX idx_leaves_status ON leaves(status);

-- =============================================
-- 6. OFFER LETTERS
-- =============================================

CREATE TABLE IF NOT EXISTS offer_letters (
    id SERIAL PRIMARY KEY,
    employee_id INT REFERENCES employees(id) ON DELETE SET NULL,
    
    -- Letter details
    letter_number VARCHAR(50) UNIQUE NOT NULL,
    candidate_name VARCHAR(255) NOT NULL,
    candidate_email VARCHAR(255) NOT NULL,
    
    designation VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    salary DECIMAL(10, 2) NOT NULL,
    joining_date DATE NOT NULL,
    location VARCHAR(100),
    
    -- Template
    template_content TEXT,
    
    -- PDF
    pdf_url TEXT,
    
    -- Status
    status VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, SENT, ACCEPTED, REJECTED
    
    -- Tracking
    generated_by INT REFERENCES users(id),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 7. CHAT SYSTEM
-- =============================================

CREATE TABLE IF NOT EXISTS chat_rooms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    type VARCHAR(20) NOT NULL, -- DIRECT, GROUP
    
    created_by INT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_room_members (
    id SERIAL PRIMARY KEY,
    room_id INT NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_read_at TIMESTAMP,
    
    UNIQUE(room_id, user_id)
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id SERIAL PRIMARY KEY,
    room_id INT NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    message TEXT NOT NULL,
    
    -- Status
    is_read BOOLEAN DEFAULT false,
    is_deleted BOOLEAN DEFAULT false,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_messages_room ON chat_messages(room_id);
CREATE INDEX idx_chat_messages_sender ON chat_messages(sender_id);

-- =============================================
-- 8. DEMO REQUEST (Marketing Website)
-- =============================================

CREATE TABLE IF NOT EXISTS demo_requests (
    id SERIAL PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    company_size VARCHAR(50),
    message TEXT,
    
    status VARCHAR(20) DEFAULT 'NEW', -- NEW, CONTACTED, QUALIFIED, CONVERTED
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 9. AUDIT LOG (Optional)
-- =============================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id INT,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(50),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- TRIGGERS FOR UPDATED_AT
-- =============================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to all tables with updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_employees_updated_at BEFORE UPDATE ON employees
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_attendance_updated_at BEFORE UPDATE ON attendance
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_leaves_updated_at BEFORE UPDATE ON leaves
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_departments_updated_at BEFORE UPDATE ON departments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- VIEWS FOR REPORTING
-- =============================================

-- Monthly attendance summary
CREATE OR REPLACE VIEW monthly_attendance_summary AS
SELECT 
    e.id,
    e.employee_id,
    e.first_name || ' ' || e.last_name as employee_name,
    DATE_TRUNC('month', a.date) as month,
    COUNT(*) FILTER (WHERE a.status = 'PRESENT') as present_days,
    COUNT(*) FILTER (WHERE a.status = 'ABSENT') as absent_days,
    COUNT(*) FILTER (WHERE a.is_late = true) as late_days,
    COUNT(*) FILTER (WHERE a.status = 'LEAVE') as leave_days
FROM employees e
LEFT JOIN attendance a ON e.id = a.employee_id
GROUP BY e.id, e.employee_id, employee_name, month;

-- Leave balance summary
CREATE OR REPLACE VIEW leave_balance_summary AS
SELECT 
    e.id,
    e.employee_id,
    e.first_name || ' ' || e.last_name as employee_name,
    lt.name as leave_type,
    lb.year,
    lb.total_balance,
    lb.used_balance,
    lb.remaining_balance
FROM employees e
JOIN leave_balances lb ON e.id = lb.employee_id
JOIN leave_types lt ON lb.leave_type_id = lt.id;

-- =============================================
-- END OF SCHEMA
-- =============================================
