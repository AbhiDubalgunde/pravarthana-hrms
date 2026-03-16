import java.sql.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Full org-structure diagnostic: DB checks + RLS check + API health */
public class DiagnosticRunner {
    static final String DB_URL = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
    static final String DB_USR = "postgres.ubbnkuykcztnnqthtsuy";
    static final String DB_PWD = "Pravarthana@2026!";
    static final String API = "http://localhost:8081";

    static void h(String title) {
        System.out.println("\n══ " + title + " " + "═".repeat(Math.max(0, 60 - title.length())));
    }

    static int cnt(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getInt(1) : 0;
        }
    }

    static void q(Connection c, String label, String sql) throws SQLException {
        System.out.println("\n  [" + label + "]");
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            ResultSetMetaData m = rs.getMetaData();
            int cols = m.getColumnCount();
            StringBuilder header = new StringBuilder("  ");
            for (int i = 1; i <= cols; i++)
                header.append(String.format("%-22s", m.getColumnName(i)));
            System.out.println(header);
            System.out.println("  " + "-".repeat(cols * 22));
            int rows = 0;
            while (rs.next() && rows++ < 30) {
                StringBuilder row = new StringBuilder("  ");
                for (int i = 1; i <= cols; i++)
                    row.append(String.format("%-22s", nv(rs.getString(i))));
                System.out.println(row);
            }
            if (rows == 0)
                System.out.println("  (no rows)");
        }
    }

    public static void main(String[] a) throws Exception {
        System.out.println("╔═════════════════════════════════════════════════════╗");
        System.out.println("║  HRMS Org-Structure — Full Diagnostic Report        ║");
        System.out.println("╚═════════════════════════════════════════════════════╝");

        try (Connection c = DriverManager.getConnection(DB_URL, DB_USR, DB_PWD)) {
            System.out.println("✅ Connected to Supabase\n");

            // ── 1. Employees exist? ─────────────────────────────────────────
            h("1. EMPLOYEE ROWS");
            int empTotal = cnt(c, "SELECT COUNT(*) FROM employees");
            int empCo1 = cnt(c, "SELECT COUNT(*) FROM employees WHERE company_id = 1");
            System.out.printf("  Total employees             : %d%n", empTotal);
            System.out.printf("  Employees with company_id=1 : %d%n", empCo1);
            q(c, "Sample employees",
                    "SELECT id, employee_code, first_name, last_name, department_id, team_id, company_id " +
                            "FROM employees ORDER BY id LIMIT 15");

            // ── 2. Departments exist? ───────────────────────────────────────
            h("2. DEPARTMENT ROWS");
            int deptTotal = cnt(c, "SELECT COUNT(*) FROM departments");
            int deptCo1 = cnt(c, "SELECT COUNT(*) FROM departments WHERE company_id = 1");
            System.out.printf("  Total departments           : %d%n", deptTotal);
            System.out.printf("  Departments with company_id=1: %d%n", deptCo1);
            q(c, "All departments",
                    "SELECT id, name, company_id, is_active, head_employee_id FROM departments ORDER BY id");

            // ── 3. Join check ───────────────────────────────────────────────
            h("3. DEPT ↔ EMP JOIN CHECK");
            q(c, "Depts with their employees",
                    "SELECT d.id AS dept_id, d.name AS dept_name, COUNT(e.id) AS emp_count " +
                            "FROM departments d LEFT JOIN employees e ON e.department_id = d.id AND e.company_id = d.company_id "
                            +
                            "GROUP BY d.id, d.name ORDER BY d.id");

            // ── 4. NULL department_id ───────────────────────────────────────
            h("4. NULL DEPARTMENT_ID CHECK");
            int nullDept = cnt(c, "SELECT COUNT(*) FROM employees WHERE department_id IS NULL");
            System.out.printf("  Employees with NULL department_id: %d (target: 0)%n", nullDept);
            if (nullDept > 0) {
                q(c, "Employees with NULL dept",
                        "SELECT id, employee_code, first_name, last_name, company_id FROM employees WHERE department_id IS NULL LIMIT 20");
            }

            // ── 5. Tenant/company_id mismatch ──────────────────────────────
            h("5. COMPANY_ID DISTRIBUTION");
            q(c, "employees.company_id values",
                    "SELECT company_id, COUNT(*) AS cnt FROM employees GROUP BY company_id");
            q(c, "departments.company_id values",
                    "SELECT company_id, COUNT(*) AS cnt FROM departments GROUP BY company_id");
            int mismatch = cnt(c,
                    "SELECT COUNT(*) FROM employees e WHERE NOT EXISTS (" +
                            "  SELECT 1 FROM departments d WHERE d.id = e.department_id AND d.company_id = e.company_id)");
            System.out.printf("  Employees whose dept.company_id ≠ emp.company_id: %d%n", mismatch);

            // ── 6. Orphan dept references ───────────────────────────────────
            h("6. ORPHAN DEPARTMENT_ID (in emp but missing in dept table)");
            q(c, "department_ids in employees missing from departments",
                    "SELECT DISTINCT e.department_id FROM employees e " +
                            "WHERE e.department_id IS NOT NULL " +
                            "  AND NOT EXISTS (SELECT 1 FROM departments d WHERE d.id = e.department_id)");

            // ── 7. Teams ────────────────────────────────────────────────────
            h("7. TEAMS");
            int teamCount = cnt(c, "SELECT COUNT(*) FROM teams");
            System.out.printf("  Total teams: %d%n", teamCount);
            q(c, "Teams",
                    "SELECT id, name, department_id, company_id, is_active FROM teams ORDER BY department_id, id");

            // ── 8. v_org_structure ──────────────────────────────────────────
            h("8. v_org_structure VIEW");
            boolean viewExists = cnt(c,
                    "SELECT COUNT(*) FROM information_schema.views WHERE table_name='v_org_structure'") > 0;
            System.out.printf("  View exists: %s%n", viewExists ? "YES ✅" : "NO ❌");
            if (viewExists) {
                q(c, "v_org_structure sample",
                        "SELECT department_name, team_name, employee_code, full_name, designation " +
                                "FROM v_org_structure WHERE company_id = 1 " +
                                "ORDER BY department_name, team_name NULLS LAST, full_name LIMIT 30");
            }

            // ── 9. RLS check ────────────────────────────────────────────────
            h("9. ROW-LEVEL SECURITY (RLS) CHECK");
            q(c, "RLS status on key tables",
                    "SELECT relname AS table_name, relrowsecurity AS rls_enabled " +
                            "FROM pg_class WHERE relname IN ('employees','departments','teams','companies') " +
                            "ORDER BY relname");
            q(c, "RLS Policies",
                    "SELECT tablename, policyname, cmd, roles::text, qual " +
                            "FROM pg_policies WHERE tablename IN ('employees','departments','teams') " +
                            "ORDER BY tablename, policyname");

            // ── 10. Legacy 'department' string column ───────────────────────
            h("10. LEGACY VARCHAR 'department' COLUMN ON EMPLOYEES");
            boolean hasDeptCol = cnt(c,
                    "SELECT COUNT(*) FROM information_schema.columns " +
                            "WHERE table_name='employees' AND column_name='department'") > 0;
            System.out.printf("  Legacy 'department' varchar column exists: %s%n",
                    hasDeptCol ? "YES (still present)" : "NO (already removed or was never there)");

            // ── Summary ─────────────────────────────────────────────────────
            h("SUMMARY");
            boolean pass = (nullDept == 0 && empCo1 > 0 && deptCo1 > 0 && teamCount > 0 && viewExists && mismatch == 0);
            System.out.printf("  %-45s %s%n", "Employees in DB (company_id=1):",
                    empCo1 > 0 ? "✅ " + empCo1 : "❌ 0 — no employees!");
            System.out.printf("  %-45s %s%n", "Departments in DB (company_id=1):",
                    deptCo1 > 0 ? "✅ " + deptCo1 : "❌ 0 — no departments!");
            System.out.printf("  %-45s %s%n", "Teams in DB:",
                    teamCount > 0 ? "✅ " + teamCount : "⚠️  0 teams (seed may have had no match)");
            System.out.printf("  %-45s %s%n", "NULL department_id employees:",
                    nullDept == 0 ? "✅ 0" : "❌ " + nullDept + " — need backfill");
            System.out.printf("  %-45s %s%n", "Company_id mismatch (emp↔dept):",
                    mismatch == 0 ? "✅ 0" : "❌ " + mismatch);
            System.out.printf("  %-45s %s%n", "v_org_structure view:", viewExists ? "✅ exists" : "❌ missing");
            System.out.println("\n  Overall: " + (pass ? "✅ PASS — DB looks correct" : "⚠️  ISSUES FOUND — see above"));
        }

        // ── 11. API health checks (requires running backend) ────────────────
        h("11. API HEALTH CHECKS (backend localhost:8081)");
        checkApi("/api/departments", "GET /api/departments");
        checkApi("/api/employees/org-structure", "GET /api/employees/org-structure");
        // Also try with no auth to confirm it's not auth that's broken
        System.out.println("\n  (401/403 responses without token are expected for authenticated endpoints)");

        System.out.println("\n══════════════════════════════════════════════════════════════");
        System.out.println("  Diagnostic complete.");
        System.out.println("══════════════════════════════════════════════════════════════");
    }

    static void checkApi(String path, String label) {
        try {
            URL url = new URL(API + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            System.out.printf("  %-45s → HTTP %d%n", label, code);
            if (code < 500) {
                InputStream is = code < 400 ? conn.getInputStream() : conn.getErrorStream();
                if (is != null) {
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    System.out.printf("    Body (first 300 chars): %s%n",
                            body.length() > 300 ? body.substring(0, 300) + "..." : body);
                }
            }
        } catch (Exception e) {
            System.out.printf("  %-45s → ❌ Connection failed: %s%n", label, e.getMessage());
        }
    }

    static String nv(String s) {
        return s == null ? "(null)" : s.length() > 20 ? s.substring(0, 19) + "…" : s;
    }
}
