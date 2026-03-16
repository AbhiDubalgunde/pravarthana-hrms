import java.sql.*;

/**
 * Adds missing 'email' column to employees table so JPA entity mapping works
 */
public class FixEmailColumn {
    static final String URL = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
    static final String USR = "postgres.ubbnkuykcztnnqthtsuy";
    static final String PWD = "Pravarthana@2026!";

    public static void main(String[] a) throws Exception {
        try (Connection c = DriverManager.getConnection(URL, USR, PWD)) {
            System.out.println("✅ Connected\n");
            c.setAutoCommit(false);

            // 1. Check if email column exists
            boolean hasEmail = cnt(c,
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_name='employees' AND column_name='email'") > 0;
            System.out.println("email column exists: " + hasEmail);
            if (!hasEmail) {
                exec(c, "ALTER TABLE employees ADD COLUMN email VARCHAR(255) UNIQUE");
                System.out.println("✅ Added email column to employees");
            } else {
                System.out.println("email column already present — no change needed");
            }

            // 2. Verify employee_code column (added by our migration)
            boolean hasEmployeeCode = cnt(c,
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_name='employees' AND column_name='employee_code'") > 0;
            boolean hasEmployeeId = cnt(c,
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_name='employees' AND column_name='employee_id'") > 0;
            System.out.printf("employee_code column: %s%n", hasEmployeeCode ? "EXISTS ✅" : "MISSING ❌");
            System.out.printf("employee_id column:   %s%n", hasEmployeeId ? "EXISTS ✅" : "MISSING ❌");

            // 3. Backfill employee_code from employee_id if code col is empty
            if (hasEmployeeCode && hasEmployeeId) {
                int backfilled = upd(c,
                        "UPDATE employees SET employee_code = employee_id WHERE employee_code IS NULL AND employee_id IS NOT NULL");
                System.out.printf("Backfilled employee_code from employee_id: %d rows%n", backfilled);
            }

            // 4. Show employees table sample
            System.out.println("\nEmployee sample after fix:");
            try (Statement s = c.createStatement();
                    ResultSet r = s.executeQuery(
                            "SELECT id, first_name, last_name, email, employee_code, employee_id, department_id, company_id FROM employees LIMIT 10")) {
                while (r.next())
                    System.out.printf("  id=%-3d name=%-20s email=%-30s code=%-10s deptId=%s%n",
                            r.getInt("id"), r.getString("first_name") + " " + r.getString("last_name"),
                            nv(r.getString("email")), nv(r.getString("employee_code")),
                            nv(r.getString("department_id")));
            }

            c.commit();
            System.out.println("\n✅ Done — restart the backend now");
        }
    }

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

    static String nv(String s) {
        return s == null ? "(null)" : s;
    }
}
