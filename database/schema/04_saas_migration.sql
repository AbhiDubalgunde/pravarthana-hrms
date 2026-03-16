-- =============================================
-- Pravarthana HRMS — SaaS Multi-tenant Migration
-- Version: 2.0.0 — Companies + Subscriptions + company_id
-- Run AFTER 01_init.sql and 02_seed_data.sql
-- =============================================

-- =============================================
-- 1. COMPANIES (SaaS tenants)
-- =============================================

CREATE TABLE IF NOT EXISTS companies (
    id                  SERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    domain              VARCHAR(255) UNIQUE,
    subscription_plan   VARCHAR(50) NOT NULL DEFAULT 'FREE',  -- FREE, BASIC, PRO
    subscription_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, EXPIRED
    max_users           INT NOT NULL DEFAULT 10,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert the default demo company (for existing seed data)
INSERT INTO companies (id, name, domain, subscription_plan, subscription_status, max_users)
VALUES (1, 'Pravarthana Technologies', 'pravarthana.com', 'PRO', 'ACTIVE', -1)
ON CONFLICT (id) DO NOTHING;

-- =============================================
-- 2. SUBSCRIPTIONS (billing history)
-- =============================================

CREATE TABLE IF NOT EXISTS subscriptions (
    id              SERIAL PRIMARY KEY,
    company_id      INT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    plan            VARCHAR(50) NOT NULL,      -- FREE, BASIC, PRO
    price           DECIMAL(10, 2) DEFAULT 0,
    billing_cycle   VARCHAR(20) DEFAULT 'MONTHLY', -- MONTHLY, ANNUAL
    start_date      DATE NOT NULL,
    end_date        DATE,
    status          VARCHAR(20) DEFAULT 'ACTIVE',  -- ACTIVE, CANCELLED, EXPIRED
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO subscriptions (company_id, plan, price, billing_cycle, start_date, status)
VALUES (1, 'PRO', 0, 'ANNUAL', '2024-01-01', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- =============================================
-- 3. DESIGNATIONS (normalised from VARCHAR)
-- =============================================

CREATE TABLE IF NOT EXISTS designations (
    id          SERIAL PRIMARY KEY,
    company_id  INT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    title       VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, title)
);

-- =============================================
-- 4. ADD company_id TO EXISTING TABLES (safely)
-- =============================================

-- users
ALTER TABLE users ADD COLUMN IF NOT EXISTS company_id INT REFERENCES companies(id);
UPDATE users SET company_id = 1 WHERE company_id IS NULL;
ALTER TABLE users ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE users ALTER COLUMN company_id SET DEFAULT 1;

-- employees
ALTER TABLE employees ADD COLUMN IF NOT EXISTS company_id INT REFERENCES companies(id);
UPDATE employees SET company_id = 1 WHERE company_id IS NULL;
ALTER TABLE employees ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE employees ALTER COLUMN company_id SET DEFAULT 1;

-- departments
ALTER TABLE departments ADD COLUMN IF NOT EXISTS company_id INT REFERENCES companies(id);
UPDATE departments SET company_id = 1 WHERE company_id IS NULL;
ALTER TABLE departments ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE departments ALTER COLUMN company_id SET DEFAULT 1;

-- attendance
ALTER TABLE attendance ADD COLUMN IF NOT EXISTS company_id INT REFERENCES companies(id);
UPDATE attendance SET company_id = 1 WHERE company_id IS NULL;
ALTER TABLE attendance ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE attendance ALTER COLUMN company_id SET DEFAULT 1;

-- leaves
ALTER TABLE leaves ADD COLUMN IF NOT EXISTS company_id INT REFERENCES companies(id);
UPDATE leaves SET company_id = 1 WHERE company_id IS NULL;
ALTER TABLE leaves ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE leaves ALTER COLUMN company_id SET DEFAULT 1;

-- =============================================
-- 5. ACCOUNT LOCKOUT COLUMNS on users
-- =============================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INT DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_locked_until TIMESTAMP;

-- =============================================
-- 6. INDEXES FOR PERFORMANCE
-- =============================================

CREATE INDEX IF NOT EXISTS idx_users_company         ON users(company_id);
CREATE INDEX IF NOT EXISTS idx_employees_company     ON employees(company_id);
CREATE INDEX IF NOT EXISTS idx_departments_company   ON departments(company_id);
CREATE INDEX IF NOT EXISTS idx_attendance_company    ON attendance(company_id);
CREATE INDEX IF NOT EXISTS idx_leaves_company        ON leaves(company_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_company ON subscriptions(company_id);
CREATE INDEX IF NOT EXISTS idx_designations_company  ON designations(company_id);

-- =============================================
-- 7. AUDIT LOGS TABLE (structured logging)
-- =============================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    company_id      INT REFERENCES companies(id),
    user_id         INT REFERENCES users(id),
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(100),
    entity_id       BIGINT,
    request_id      VARCHAR(64),
    ip_address      VARCHAR(50),
    details         JSONB,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_company    ON audit_logs(company_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user       ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity     ON audit_logs(entity_type, entity_id);

-- =============================================
-- 8. TRIGGERS FOR NEW TABLES
-- =============================================

CREATE TRIGGER update_companies_updated_at BEFORE UPDATE ON companies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- END OF SAAS MIGRATION
-- =============================================
