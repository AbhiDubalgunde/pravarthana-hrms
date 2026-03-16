import java.sql.*;

/**
 * Complete HRMS Org Structure Migration Runner.
 * Handles the partial DB state: companies table missing, departments lacks
 * company_id.
 * Compiles with: javac -cp "lib/postgresql.jar" MigrationRunner.java
 * Runs with: java -cp ".;lib/postgresql.jar" MigrationRunner
 */
public class MigrationRunner {

    static final String URL = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
    static final String USER = "postgres.ubbnkuykcztnnqthtsuy";
    static final String PASS = "Pravarthana@2026!";

    // ── helpers ──────────────────────────────────────────────────────────────
    static void exec(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute(sql);
        }
    }

    static int upd(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement()) {
            return s.executeUpdate(sql);
        }
    }

    static int cnt(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getInt(1) : 0;
        }
    }

    static boolean tableExists(Connection c, String t) throws SQLException {
        return cnt(c, "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema='public' AND table_name='" + t
                + "'") > 0;
    }

    static boolean colExists(Connection c, String t, String col) throws SQLException {
        return cnt(c, "SELECT COUNT(1) FROM information_schema.columns WHERE table_name='" + t + "' AND column_name='"
                + col + "'") > 0;
    }

    static boolean constraintExists(Connection c, String name) throws SQLException {
        return cnt(c, "SELECT COUNT(1) FROM pg_constraint WHERE conname='" + name + "'") > 0;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("===========================================");
        System.out.println("  HRMS Org-Structure Migration Runner");
        System.out.println("===========================================\n");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            conn.setAutoCommit(false);
            System.out.println("✅ Connected to Supabase\n");

            // ──────────────────────────────────────────────────────────────
            // STEP 0 — Backups
            // ──────────────────────────────────────────────────────────────
            System.out.println("STEP 0 — Creating backups...");
            exec(conn, "DROP TABLE IF EXISTS departments_backup");
            exec(conn, "CREATE TABLE departments_backup AS SELECT * FROM departments");
            exec(conn, "DROP TABLE IF EXISTS employees_backup");
            exec(conn, "CREATE TABLE employees_backup AS SELECT * FROM employees");
            System.out.printf("  departments_backup: %d rows%n", cnt(conn, "SELECT COUNT(*) FROM departments_backup"));
            System.out.printf("  employees_backup  : %d rows%n", cnt(conn, "SELECT COUNT(*) FROM employees_backup"));

            // ──────────────────────────────────────────────────────────────
            // STEP 1 — companies table (prerequisite for multi-tenancy)
            // ──────────────────────────────────────────────────────────────
            System.out.println("\nSTEP 1 — Ensuring companies table...");
            if (!tableExists(conn, "companies")) {
                exec(conn, """
                        CREATE TABLE companies (
                            id                  SERIAL PRIMARY KEY,
                            name                VARCHAR(255) NOT NULL,
                            domain              VARCHAR(255) UNIQUE,
                            subscription_plan   VARCHAR(50) NOT NULL DEFAULT 'FREE',
                            subscription_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                            max_users           INT NOT NULL DEFAULT 10,
                            created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )""");
                System.out.println("  Created companies table");
            } else {
                System.out.println("  companies table already exists");
            }
            exec(conn, """
                    INSERT INTO companies (id, name, domain, subscription_plan, subscription_status, max_users)
                    VALUES (1, 'Pravarthana Technologies', 'pravarthana.com', 'PRO', 'ACTIVE', -1)
                    ON CONFLICT (id) DO NOTHING""");
            System.out.printf("  Default company (id=1) present: %d row(s)%n",
                    cnt(conn, "SELECT COUNT(*) FROM companies WHERE id=1"));

            // ──────────────────────────────────────────────────────────────
            // STEP 2 — Add company_id to departments
            // ──────────────────────────────────────────────────────────────
            System.out.println("\nSTEP 2 — company_id on departments...");
            if (!colExists(conn, "departments", "company_id")) {
                exec(conn, "ALTER TABLE departments ADD COLUMN company_id INT REFERENCES companies(id)");
                System.out.println("  Added company_id column to departments");
            }
            int deptUpdated = upd(conn, "UPDATE departments SET company_id = 1 WHERE company_id IS NULL");
            System.out.printf("  Set company_id=1 on %d department row(s)%n", deptUpdated);
            // Set NOT NULL + default (only after filling)
            try {
                exec(conn, "ALTER TABLE departments ALTER COLUMN company_id SET NOT NULL");
            } catch (Exception e) {
                /* may already be NOT NULL */ }
            exec(conn, "ALTER TABLE departments ALTER COLUMN company_id SET DEFAULT 1");

            // ──────────────────────────────────────────────────────────────
            // STEP 3 — Create teams table
            // ──────────────────────────────────────────────────────────────
            System.out.println("\nSTEP 3 — Creating teams table...");
            if (!tableExists(conn, "teams")) {
                exec(conn, """
                        CREATE TABLE teams (
                            id            SERIAL PRIMARY KEY,
                            company_id    INT NOT NULL REFERENCES companies(id) ON DELETE CASCADE DEFAULT 1,
                            department_id INT NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
                            name          VARCHAR(100) NOT NULL,
                            description   TEXT,
                            is_active     BOOLEAN DEFAULT true,
                            created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE(company_id, department_id, name)
                        )""");
                exec(conn, "CREATE INDEX idx_teams_company    ON teams(company_id)");
                exec(conn, "CREATE INDEX idx_teams_department ON teams(department_id)");
                System.out.println("  teams table created");
            } else {
                System.out.println("  teams table already exists — skipped");
            }

            // ──────────────────────────────────────────────────────────────
            // STEP 4 — Add team_id to employees
            // ──────────────────────────────────────────────────────────────
            System.out.println("\nSTEP 4 — Adding team_id to employees...");
            if (!colExists(conn, "employees", "team_id")) {
                exec(conn, "ALTER TABLE employees ADD COLUMN team_id INT REFERENCES teams(id) ON DELETE SET NULL");
                System.out.println("  team_id column added to employees");
            } else {
                System.out.println("  team_id already exists — skipped");
            }
            // Ensure indexes
            try {
                exec(conn, "CREATE INDEX IF NOT EXISTS idx_employees_team ON employees(team_id)");
            } catch (Exception ignored) {
            }
            try {
                exec(conn, "CREATE INDEX IF NOT EXISTS idx_employees_department ON employees(department_id)");
            } catch (Exception ignored) {
            }

            // ──────────────────────────────────────────────────────────────
            // STEP 5 — Backfill department_id for employees (by string name)
            // ──────────────────────────────────────────────────────────────
            System.out.println("\nSTEP 5 — Backfilling department_id...");
            int nullBefore = cnt(conn, "SELECT COUNT(*) FROM employees WHERE department_id IS NULL");
            System.out.printf("  Employees with NULL department_id before backfill: %d%n", nullBefore);
            if (nullBefore > 0 && colExists(conn, "employees", "department")) {
                int backfilled = upd(conn, """
                        UPDATE employees e
                        SET department_id = d.id
                        FROM departments d
                        WHERE (e.company_id = d.company_id OR d.company_id = 1)
                          AND e.department_id IS NULL
                          AND d.name ILIKE TRIM(e.department)""");
                System.out.printf("  Backfilled %d rows from string 'department' column%n", backfilled);
            } else if (nullBefore > 0) {
                System.out.println("  No string 'department' column — cannot auto-backfill");
            } else {
                System.out.println("  All employees already have department_id ✅");
            }

            // ──────────────────────────────────────────────────────────────
            // STEP 6 — company_id on employees (ensure set)
            // ──────────────────────────────────────────────────────────────
            if (colExists(conn, "employees", "company_id")) {
                upd(conn, "UPDATE employees SET company_id = 1 WHERE company_id IS NULL");
            }

            // ──────────────────────────────────────────────────────────────
            // STEP 7 — UNIQUE constraint on departments.name
            // ──────────────────────────────────────────────────────────────
            System.out.println("\nSTEP 7 — UNIQUE constraint on departments(company_id,name)...");
            if (!constraintExists(conn, "uq_departments_company_name")) {
                try {
                    exec(conn,
                            "ALTER TABLE departments ADD CONSTRAINT uq_departments_company_name UNIQUE (company_id, name)");
                    System.out.println("  Added UNIQUE constraint");
                } catch (SQLException e) {
                    System.out.println("  Could not add UNIQUE (possible dups): " + e.getMessage());
                }
            } else {
                System.out.println("  Already exists — skipped");
            }

            // ──────────────────────────────────────────────────────────────
            // STEP 8 — Seed demo teams
            // ──────────────────────────────────────────────────────────────
            System.out.println("\nSTEP 8 — Seeding demo teams...");
            String[][] seeds = {
                    { "Engineering", "Backend Team", "Server-side development and APIs" },
                    { "Engineering", "Frontend Team", "UI/UX and client-side development" },
                    { "Human Resources", "Recruitment Team", "Talent acquisition and onboarding" },
                    { "Sales", "Inside Sales", "Inside sales and lead conversion" },
            };
            for (String[] s : seeds) {
                try {
                    int n = upd(conn,
                            "INSERT INTO teams(company_id,department_id,name,description) " +
                                    "SELECT 1, d.id, '" + s[1] + "', '" + s[2] + "' " +
                                    "FROM departments d WHERE d.name='" + s[0] + "' AND d.company_id=1 " +
                                    "ON CONFLICT(company_id,department_id,name) DO NOTHING");
                    System.out.printf("  %-30s → inserted %d%n", s[1], n);
                } catch (SQLException e) {
                    System.out.printf("  %-30s → skipped (%s)%n", s[1], e.getMessage());
                }
            }

            // ──────────────────────────────────────────────────────────────
            // STEP 9 — v_org_structure view
            // ──────────────────────────────────────────────────────────────
            System.out.println("\nSTEP 9 — Creating v_org_structure view...");
            exec(conn, "DROP VIEW IF EXISTS v_org_structure");
            exec(conn, """
                    CREATE VIEW v_org_structure AS
                    SELECT
                        d.company_id,
                        d.id                                                AS department_id,
                        d.name                                              AS department_name,
                        t.id                                                AS team_id,
                        t.name                                              AS team_name,
                        e.id                                                AS employee_id,
                        e.employee_code,
                        e.first_name,
                        e.last_name,
                        (e.first_name || ' ' || COALESCE(e.last_name,''))   AS full_name,
                        e.designation,
                        e.reporting_manager_id                              AS manager_id,
                        e.status
                    FROM departments d
                    LEFT JOIN teams t     ON t.department_id = d.id AND t.company_id = d.company_id
                    LEFT JOIN employees e ON e.department_id = d.id AND e.company_id = d.company_id
                                         AND (e.team_id = t.id OR (e.team_id IS NULL AND t.id IS NULL))""");
            System.out.println("  v_org_structure created");

            conn.commit();
            System.out.println("\n✅ Migration committed!\n");

            // ──────────────────────────────────────────────────────────────
            // VERIFICATION
            // ──────────────────────────────────────────────────────────────
            System.out.println("===========================================");
            System.out.println("  VERIFICATION RESULTS");
            System.out.println("===========================================");

            int teamCount = cnt(conn, "SELECT COUNT(*) FROM teams");
            int nullDept = cnt(conn, "SELECT COUNT(*) FROM employees WHERE department_id IS NULL");
            int orgRows = cnt(conn, "SELECT COUNT(*) FROM v_org_structure WHERE company_id = 1");
            int deptCount = cnt(conn, "SELECT COUNT(*) FROM departments WHERE company_id = 1");

            System.out.printf("  departments with company_id=1  : %d%n", deptCount);
            System.out.printf("  teams                          : %d%n", teamCount);
            System.out.printf("  null department_id employees   : %d (target: 0)%n", nullDept);
            System.out.printf("  v_org_structure rows (co=1)    : %d%n", orgRows);

            System.out.println("\n--- v_org_structure sample ---");
            System.out.printf("%-22s %-18s %-10s %-28s %-22s%n",
                    "DEPARTMENT", "TEAM", "EMP_CODE", "FULL_NAME", "DESIGNATION");
            System.out.println("-".repeat(102));

            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery(
                            "SELECT department_name, team_name, employee_code, full_name, designation " +
                                    "FROM v_org_structure WHERE company_id = 1 " +
                                    "ORDER BY department_name, team_name NULLS LAST, full_name LIMIT 50")) {
                int rowNum = 0;
                while (rs.next()) {
                    rowNum++;
                    System.out.printf("%-22s %-18s %-10s %-28s %-22s%n",
                            nv(rs.getString("department_name")),
                            nv(rs.getString("team_name")),
                            nv(rs.getString("employee_code")),
                            nv(rs.getString("full_name")),
                            nv(rs.getString("designation")));
                }
                if (rowNum == 0)
                    System.out.println("  (no rows — departments may have no employees yet)");
            }

            System.out.println("\n--- Employees with NULL department_id ---");
            if (nullDept > 0) {
                try (Statement st = conn.createStatement();
                        ResultSet rs = st.executeQuery(
                                "SELECT id, employee_code, first_name, last_name FROM employees WHERE department_id IS NULL LIMIT 20")) {
                    while (rs.next()) {
                        System.out.printf("  id=%-4d code=%-10s name=%s %s%n",
                                rs.getInt("id"), nv(rs.getString("employee_code")),
                                nv(rs.getString("first_name")), nv(rs.getString("last_name")));
                    }
                }
                System.out.println("\n  ⚠️  These employees need manual department assignment!");
            } else {
                System.out.println("  ✅ No employees with NULL department_id");
            }

            System.out.println("\n===========================================");
            System.out.println(nullDept == 0
                    ? "  ✅ MIGRATION PASSED"
                    : "  ⚠️  MIGRATION COMPLETE but " + nullDept + " employees need dept assignment");
            System.out.println("===========================================");

        } catch (SQLException e) {
            System.err.println("\n❌ MIGRATION FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    static String nv(String s) {
        return s != null ? s : "(null)";
    }
}
