-- =============================================
-- Pravarthana HRMS — Org Structure Fix
-- Version: 3.0.0
-- Run AFTER 01_init.sql, 02_seed_data.sql, 04_saas_migration.sql
-- =============================================

-- =============================================
-- 1. TEAMS TABLE (new)
-- =============================================

CREATE TABLE IF NOT EXISTS teams (
    id              SERIAL PRIMARY KEY,
    company_id      INT NOT NULL REFERENCES companies(id) ON DELETE CASCADE DEFAULT 1,
    department_id   INT NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    is_active       BOOLEAN DEFAULT true,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, department_id, name)
);

CREATE INDEX IF NOT EXISTS idx_teams_company      ON teams(company_id);
CREATE INDEX IF NOT EXISTS idx_teams_department   ON teams(department_id);

-- =============================================
-- 2. ADD team_id TO employees
-- =============================================

ALTER TABLE employees ADD COLUMN IF NOT EXISTS team_id INT REFERENCES teams(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_employees_team ON employees(team_id);

-- =============================================
-- 3. ENSURE employees.department_id IS INDEXED
-- =============================================

CREATE INDEX IF NOT EXISTS idx_employees_department ON employees(department_id);

-- =============================================
-- 4. BACKFILL department_id WHERE NULL
--    (for rows inserted via JPA that only stored the string).
--    Matches on name case-insensitively.
-- =============================================

UPDATE employees e
SET department_id = d.id
FROM departments d
WHERE e.company_id = d.company_id
  AND e.department_id IS NULL
  AND d.name ILIKE TRIM(e.department)
  AND e.department IS NOT NULL;

-- Also fix employees where department column holds the name but department_id = 0
UPDATE employees e
SET department_id = d.id
FROM departments d
WHERE e.company_id = d.company_id
  AND e.department_id = 0
  AND d.name ILIKE TRIM(e.department);

-- =============================================
-- 5. ENSURE departments.name IS UNIQUE PER COMPANY
-- =============================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_departments_company_name'
    ) THEN
        ALTER TABLE departments
        ADD CONSTRAINT uq_departments_company_name UNIQUE (company_id, name);
    END IF;
END $$;

-- =============================================
-- 6. ENSURE company_id DEFAULT ON departments
-- =============================================

ALTER TABLE departments ALTER COLUMN company_id SET DEFAULT 1;

-- =============================================
-- 7. SEED DEFAULT TEAMS for demo company
-- =============================================

INSERT INTO teams (company_id, department_id, name, description) VALUES
(1, (SELECT id FROM departments WHERE name = 'Engineering' AND company_id = 1 LIMIT 1),
 'Backend Team', 'Server-side development and APIs')
ON CONFLICT (company_id, department_id, name) DO NOTHING;

INSERT INTO teams (company_id, department_id, name, description) VALUES
(1, (SELECT id FROM departments WHERE name = 'Engineering' AND company_id = 1 LIMIT 1),
 'Frontend Team', 'UI/UX and client-side development')
ON CONFLICT (company_id, department_id, name) DO NOTHING;

INSERT INTO teams (company_id, department_id, name, description) VALUES
(1, (SELECT id FROM departments WHERE name = 'Human Resources' AND company_id = 1 LIMIT 1),
 'Recruitment Team', 'Talent acquisition and onboarding')
ON CONFLICT (company_id, department_id, name) DO NOTHING;

INSERT INTO teams (company_id, department_id, name, description) VALUES
(1, (SELECT id FROM departments WHERE name = 'Sales' AND company_id = 1 LIMIT 1),
 'Inside Sales', 'Inside sales and lead conversion')
ON CONFLICT (company_id, department_id, name) DO NOTHING;

-- =============================================
-- 8. TRIGGER FOR teams.updated_at
-- =============================================

CREATE TRIGGER update_teams_updated_at BEFORE UPDATE ON teams
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- 9. DROP AND RECREATE ORG STRUCTURE VIEW
-- =============================================

DROP VIEW IF EXISTS v_org_structure;

CREATE OR REPLACE VIEW v_org_structure AS
SELECT
    d.company_id,
    d.id                                                    AS department_id,
    d.name                                                  AS department_name,
    t.id                                                    AS team_id,
    t.name                                                  AS team_name,
    e.id                                                    AS employee_id,
    e.employee_id                                           AS employee_code,
    e.first_name,
    e.last_name,
    e.first_name || ' ' || COALESCE(e.last_name, '')       AS full_name,
    e.designation,
    e.reporting_manager_id                                  AS manager_id,
    e.status
FROM departments d
LEFT JOIN teams      t  ON t.department_id = d.id AND t.company_id = d.company_id
LEFT JOIN employees  e  ON e.department_id = d.id
                       AND e.company_id    = d.company_id
                       AND (e.team_id = t.id OR (e.team_id IS NULL AND t.id IS NULL));

-- =============================================
-- 10. VERIFY
-- =============================================

DO $$
DECLARE
    dept_count  INT;
    emp_count   INT;
    team_count  INT;
    unlinked    INT;
BEGIN
    SELECT COUNT(*) INTO dept_count FROM departments WHERE company_id = 1;
    SELECT COUNT(*) INTO emp_count  FROM employees  WHERE company_id = 1;
    SELECT COUNT(*) INTO team_count FROM teams       WHERE company_id = 1;
    SELECT COUNT(*) INTO unlinked
    FROM employees WHERE department_id IS NULL AND company_id = 1;

    RAISE NOTICE '=========================================';
    RAISE NOTICE 'Org Structure Fix Applied Successfully!';
    RAISE NOTICE '=========================================';
    RAISE NOTICE 'Departments : %', dept_count;
    RAISE NOTICE 'Teams       : %', team_count;
    RAISE NOTICE 'Employees   : %', emp_count;
    RAISE NOTICE 'Unlinked    : % (should be 0)', unlinked;
    RAISE NOTICE '=========================================';
END $$;
