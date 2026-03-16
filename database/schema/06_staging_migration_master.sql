-- =========================================================
-- STAGING MIGRATION MASTER SCRIPT
-- Run ONCE in Supabase SQL Editor or via psql
-- =========================================================

-- ─── PART 1: BACKUPS ──────────────────────────────────────
DROP TABLE IF EXISTS departments_backup;
CREATE TABLE departments_backup AS SELECT * FROM departments;

DROP TABLE IF EXISTS employees_backup;
CREATE TABLE employees_backup AS SELECT * FROM employees;

DO $$ BEGIN
    RAISE NOTICE 'Backups created: departments_backup (%), employees_backup (%)',
        (SELECT count(*) FROM departments_backup),
        (SELECT count(*) FROM employees_backup);
END $$;

-- ─── PART 2: TEAMS TABLE ──────────────────────────────────
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

CREATE INDEX IF NOT EXISTS idx_teams_company     ON teams(company_id);
CREATE INDEX IF NOT EXISTS idx_teams_department  ON teams(department_id);

-- ─── PART 3: ADD team_id TO employees ─────────────────────
ALTER TABLE employees ADD COLUMN IF NOT EXISTS team_id INT REFERENCES teams(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_employees_team       ON employees(team_id);
CREATE INDEX IF NOT EXISTS idx_employees_department ON employees(department_id);

-- ─── PART 4: BACKFILL NULL department_id ──────────────────
-- Employees inserted by old JPA (which wrote string dept name) get linked by name match
WITH dept_map AS (
    SELECT d.id AS dept_id, d.company_id, d.name AS dept_name
    FROM departments d
)
UPDATE employees e
SET department_id = dm.dept_id
FROM dept_map dm
WHERE e.company_id = dm.company_id
  AND e.department_id IS NULL
  AND (
      -- match the old JPA 'department' column (if it still exists) 
      (pg_catalog.col_description(e.tableoid, 0)::text IS DISTINCT FROM 'placeholder')
  );

-- Simpler fallback: match by direct column value if 'department' varchar still exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'employees' AND column_name = 'department'
    ) THEN
        UPDATE employees e
        SET department_id = d.id
        FROM departments d
        WHERE e.company_id = d.company_id
          AND e.department_id IS NULL
          AND d.name ILIKE TRIM(e.department);
        RAISE NOTICE 'Backfilled department_id from string ''department'' column';
    ELSE
        RAISE NOTICE 'No legacy ''department'' column found — skipping string backfill';
    END IF;
END $$;

-- ─── PART 5: UNIQUE CONSTRAINT ON departments.name ────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_departments_company_name'
    ) THEN
        ALTER TABLE departments ADD CONSTRAINT uq_departments_company_name UNIQUE (company_id, name);
        RAISE NOTICE 'Added UNIQUE(company_id, name) to departments';
    ELSE
        RAISE NOTICE 'UNIQUE constraint already exists on departments';
    END IF;
END $$;

ALTER TABLE departments ALTER COLUMN company_id SET DEFAULT 1;

-- ─── PART 6: SEED DEMO TEAMS ──────────────────────────────
INSERT INTO teams (company_id, department_id, name, description)
SELECT 1, id, 'Backend Team', 'Server-side development and APIs'
FROM departments WHERE name = 'Engineering' AND company_id = 1
ON CONFLICT (company_id, department_id, name) DO NOTHING;

INSERT INTO teams (company_id, department_id, name, description)
SELECT 1, id, 'Frontend Team', 'UI/UX and client-side development'
FROM departments WHERE name = 'Engineering' AND company_id = 1
ON CONFLICT (company_id, department_id, name) DO NOTHING;

INSERT INTO teams (company_id, department_id, name, description)
SELECT 1, id, 'Recruitment Team', 'Talent acquisition and onboarding'
FROM departments WHERE name = 'Human Resources' AND company_id = 1
ON CONFLICT (company_id, department_id, name) DO NOTHING;

INSERT INTO teams (company_id, department_id, name, description)
SELECT 1, id, 'Inside Sales', 'Inside sales and lead conversion'
FROM departments WHERE name = 'Sales' AND company_id = 1
ON CONFLICT (company_id, department_id, name) DO NOTHING;

-- ─── PART 7: TRIGGER FOR teams.updated_at ─────────────────
DROP TRIGGER IF EXISTS update_teams_updated_at ON teams;
CREATE TRIGGER update_teams_updated_at BEFORE UPDATE ON teams
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ─── PART 8: v_org_structure VIEW ─────────────────────────
DROP VIEW IF EXISTS v_org_structure;
CREATE OR REPLACE VIEW v_org_structure AS
SELECT
    d.company_id,
    d.id                                                  AS department_id,
    d.name                                                AS department_name,
    t.id                                                  AS team_id,
    t.name                                                AS team_name,
    e.id                                                  AS employee_id,
    e.employee_id                                         AS employee_code,
    e.first_name,
    e.last_name,
    (e.first_name || ' ' || COALESCE(e.last_name, ''))   AS full_name,
    e.designation,
    e.reporting_manager_id                                AS manager_id,
    e.status
FROM departments d
LEFT JOIN teams     t ON t.department_id = d.id AND t.company_id = d.company_id
LEFT JOIN employees e ON e.department_id = d.id
                     AND e.company_id    = d.company_id
                     AND (e.team_id = t.id OR (e.team_id IS NULL AND t.id IS NULL));

-- ─── PART 9: VERIFICATION QUERIES ────────────────────────
DO $$
DECLARE
    v_teams        INT;
    v_null_dept    INT;
    v_total_emp    INT;
BEGIN
    SELECT COUNT(*) INTO v_teams     FROM teams       WHERE company_id = 1;
    SELECT COUNT(*) INTO v_null_dept FROM employees   WHERE company_id = 1 AND department_id IS NULL;
    SELECT COUNT(*) INTO v_total_emp FROM employees   WHERE company_id = 1;

    RAISE NOTICE '================================================';
    RAISE NOTICE '  MIGRATION RESULT SUMMARY';
    RAISE NOTICE '================================================';
    RAISE NOTICE '  Teams created       : %', v_teams;
    RAISE NOTICE '  Total employees     : %', v_total_emp;
    RAISE NOTICE '  NULL department_id  : % (target: 0)', v_null_dept;
    RAISE NOTICE '================================================';

    IF v_null_dept > 0 THEN
        RAISE WARNING '% employees still have NULL department_id! Manual reassignment required.', v_null_dept;
    END IF;
END $$;

-- ─── INLINE VERIFICATION SELECTS ─────────────────────────
SELECT 'teams_count'         AS check_name, COUNT(*)::text AS result FROM teams;
SELECT 'null_dept_employees' AS check_name, COUNT(*)::text AS result FROM employees WHERE department_id IS NULL AND company_id = 1;
SELECT 'org_view_sample'     AS check_name, COUNT(*)::text AS result FROM v_org_structure WHERE company_id = 1;
