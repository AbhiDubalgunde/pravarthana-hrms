import java.sql.*;

/**
 * Fixes: patch all departments to company_id=1, seed teams with actual dept
 * names
 */
public class FixDepartments {
    static final String URL = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
    static final String USR = "postgres.ubbnkuykcztnnqthtsuy";
    static final String PWD = "Pravarthana@2026!";

    public static void main(String[] a) throws Exception {
        try (Connection c = DriverManager.getConnection(URL, USR, PWD)) {
            c.setAutoCommit(false);
            System.out.println("✅ Connected\n");

            // 1. Show all departments as they are
            System.out.println("All departments (before fix):");
            try (Statement s = c.createStatement();
                    ResultSet r = s
                            .executeQuery("SELECT id, name, company_id, is_active FROM departments ORDER BY id")) {
                while (r.next())
                    System.out.printf("  id=%-3d company_id=%-5s is_active=%-5s name=%s%n",
                            r.getInt("id"), r.getString("company_id"), r.getString("is_active"), r.getString("name"));
            }

            // 2. Patch all departments to company_id=1 where null
            int patched;
            try (Statement s = c.createStatement()) {
                patched = s.executeUpdate(
                        "UPDATE departments SET company_id = 1 WHERE company_id IS NULL OR company_id != 1");
            }
            System.out.printf("%nPatched %d department rows to company_id=1%n", patched);

            // 3. Show all departments now
            System.out.println("\nAll departments (after fix):");
            try (Statement s = c.createStatement();
                    ResultSet r = s
                            .executeQuery("SELECT id, name, company_id, is_active FROM departments ORDER BY id")) {
                while (r.next())
                    System.out.printf("  id=%-3d company_id=%-5s name=%s%n",
                            r.getInt("id"), r.getString("company_id"), r.getString("name"));
            }

            // 4. Seed teams for EVERY active department (using actual names from DB, one
            // default team each)
            System.out.println("\nSeeding default team for each department that has none:");
            try (Statement s = c.createStatement();
                    ResultSet r = s.executeQuery(
                            "SELECT id, name FROM departments WHERE company_id = 1 AND is_active = true ORDER BY id")) {
                while (r.next()) {
                    long deptId = r.getLong("id");
                    String dName = r.getString("name");
                    String tName = dName + " Team";

                    // Check if department already has teams
                    int existing;
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT COUNT(*) FROM teams WHERE department_id = ? AND company_id = 1")) {
                        ps.setLong(1, deptId);
                        ResultSet er = ps.executeQuery();
                        existing = er.next() ? er.getInt(1) : 0;
                    }

                    if (existing == 0) {
                        try (PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO teams(company_id, department_id, name, description) " +
                                        "VALUES(1, ?, ?, ?) ON CONFLICT(company_id, department_id, name) DO NOTHING")) {
                            ps.setLong(1, deptId);
                            ps.setString(2, tName);
                            ps.setString(3, "Default team for " + dName);
                            int n = ps.executeUpdate();
                            System.out.printf("  dept '%s' (id=%d) → inserted '%s': %d row%n", dName, deptId, tName, n);
                        }
                    } else {
                        System.out.printf("  dept '%s' (id=%d) → already has %d team(s)%n", dName, deptId, existing);
                    }
                }
            }

            c.commit();

            // 5. Final verification
            System.out.println("\n── Final Check ───────────────────────────────────────────");
            int depts = 0, teams = 0, emps = 0, orgRows = 0;
            try (Statement s = c.createStatement()) {
                ResultSet r;
                r = s.executeQuery("SELECT COUNT(*) FROM departments WHERE company_id=1");
                r.next();
                depts = r.getInt(1);
                r = s.executeQuery("SELECT COUNT(*) FROM teams WHERE company_id=1");
                r.next();
                teams = r.getInt(1);
                r = s.executeQuery("SELECT COUNT(*) FROM employees WHERE company_id=1");
                r.next();
                emps = r.getInt(1);
                r = s.executeQuery("SELECT COUNT(*) FROM v_org_structure WHERE company_id=1");
                r.next();
                orgRows = r.getInt(1);
            }
            System.out.printf("  departments (co=1): %d%n", depts);
            System.out.printf("  teams (co=1):       %d%n", teams);
            System.out.printf("  employees (co=1):   %d%n", emps);
            System.out.printf("  v_org_structure rows: %d%n", orgRows);

            System.out.println("\nv_org_structure sample:");
            try (Statement s = c.createStatement();
                    ResultSet r = s.executeQuery(
                            "SELECT department_name, team_name, employee_code, full_name, designation " +
                                    "FROM v_org_structure WHERE company_id=1 ORDER BY department_name, team_name NULLS LAST, full_name LIMIT 30")) {
                System.out.printf("  %-22s %-20s %-12s %-28s %-20s%n",
                        "DEPT", "TEAM", "EMP_CODE", "NAME", "DESIGNATION");
                System.out.println("  " + "-".repeat(106));
                while (r.next())
                    System.out.printf("  %-22s %-20s %-12s %-28s %-20s%n",
                            nv(r.getString("department_name")), nv(r.getString("team_name")),
                            nv(r.getString("employee_code")), nv(r.getString("full_name")),
                            nv(r.getString("designation")));
            }
        }
    }

    static String nv(String s) {
        return s == null ? "(null)" : s;
    }
}
