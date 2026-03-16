import java.sql.*;

/**
 * FinalPatch — handles:
 * 1. leave_types insert with correct schema (has 'code' NOT NULL column)
 * 2. leaves inserts for Abhishek + Vikas
 * 3. attendance ABSENT records
 * 4. User roles (role_id FK)
 * 5. Chat room seed
 */
public class FinalPatch {
    static final String URL = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
    static final String USR = "postgres.ubbnkuykcztnnqthtsuy";
    static final String PWD = "Pravarthana@2026!";

    public static void main(String[] a) throws Exception {
        try (Connection c = DriverManager.getConnection(URL, USR, PWD)) {
            System.out.println("✅ Connected\n");

            // ─── 1. Inspect leave_types schema ───────────────────────────────
            h("1. LEAVE_TYPES SCHEMA + EXISTING DATA");
            q(c, "SELECT column_name, is_nullable, column_default FROM information_schema.columns WHERE table_name='leave_types' ORDER BY ordinal_position");
            q(c, "SELECT * FROM leave_types ORDER BY id");

            // ─── 2. Seed Casual leave type (with code) ───────────────────────
            h("2. SEED LEAVE TYPE");
            int casualExists = cnt(c, "SELECT COUNT(*) FROM leave_types WHERE name ILIKE 'casual'");
            if (casualExists == 0) {
                // 'code' is NOT NULL — use 'CL' as the code
                try {
                    exe(c, "INSERT INTO leave_types(name, code) VALUES('Casual','CL')");
                    say("✅ Inserted Casual / CL");
                } catch (Exception e) {
                    // Maybe different required columns — try with more fields
                    say("⚠️ Failed: " + e.getMessage());
                    try {
                        exe(c, "INSERT INTO leave_types(name, code, max_days_per_year, is_paid) VALUES('Casual','CL',12,true)");
                        say("✅ Inserted with extra fields");
                    } catch (Exception e2) {
                        say("⚠️ Also failed: " + e2.getMessage());
                    }
                }
            } else
                say("'Casual' already exists");
            long ltId = scl(c, "SELECT id FROM leave_types WHERE name ILIKE 'casual' OR code='CL' LIMIT 1");
            say("Casual leave_type_id = " + ltId);

            // ─── 3. Employee IDs ─────────────────────────────────────────────
            h("3. EMPLOYEE IDs");
            long abhishekId = eid(c, "Abhishek", "Dubalgunde");
            long vikasId = eid(c, "Vikas", null);
            say("Abhishek=" + abhishekId + "  Vikas=" + vikasId);

            // ─── 4. Leaves ───────────────────────────────────────────────────
            if (ltId > 0 && abhishekId > 0) {
                h("4. LEAVE INSERTS");
                q(c, "SELECT column_name FROM information_schema.columns WHERE table_name='leaves' ORDER BY ordinal_position");
                insLeave(c, abhishekId, "2025-12-30", ltId);
                insLeave(c, abhishekId, "2026-01-30", ltId);
                insLeave(c, vikasId, "2026-01-22", ltId);
                insLeave(c, vikasId, "2026-01-23", ltId);
                q(c, "SELECT id, employee_id, start_date, end_date, status FROM leaves ORDER BY id LIMIT 20");
            }

            // ─── 5. Attendance ────────────────────────────────────────────────
            h("5. ATTENDANCE ABSENT");
            q(c, "SELECT column_name, is_nullable FROM information_schema.columns WHERE table_name='attendance' ORDER BY ordinal_position");
            insAbsent(c, abhishekId, "2025-12-30");
            insAbsent(c, abhishekId, "2026-01-30");
            insAbsent(c, vikasId, "2026-01-22");
            insAbsent(c, vikasId, "2026-01-23");

            // ─── 6. User roles ────────────────────────────────────────────────
            h("6. USER ROLE UPDATES");
            long hrRoleId = scl(c, "SELECT id FROM roles WHERE name='HR_ADMIN' LIMIT 1");
            long adminRoleId = scl(c, "SELECT id FROM roles WHERE name='SUPER_ADMIN' LIMIT 1");
            say("HR_ADMIN id=" + hrRoleId + "  SUPER_ADMIN id=" + adminRoleId);
            // abhidubalgunde@gmail.com → HR_ADMIN
            long u1 = scl(c, "SELECT id FROM users WHERE email='abhidubalgunde@gmail.com' LIMIT 1");
            if (u1 > 0 && hrRoleId > 0) {
                exe(c, "UPDATE users SET role_id=" + hrRoleId + " WHERE id=" + u1);
                say("✅ abhidubalgunde@gmail.com → HR_ADMIN");
            } else
                say("⚠️ abhidubalgunde@gmail.com not found in users (email=" + u1 + ") — register first");
            // abhishek.prvt2529@gmail.com → SUPER_ADMIN
            long u2 = scl(c, "SELECT id FROM users WHERE email='abhishek.prvt2529@gmail.com' LIMIT 1");
            if (u2 > 0 && adminRoleId > 0) {
                exe(c, "UPDATE users SET role_id=" + adminRoleId + " WHERE id=" + u2);
                say("✅ abhishek.prvt2529@gmail.com → SUPER_ADMIN");
            } else
                say("⚠️ abhishek.prvt2529@gmail.com not found in users — register first");
            say("Current users:");
            q(c, "SELECT u.id, u.email, r.name AS role FROM users u LEFT JOIN roles r ON r.id=u.role_id ORDER BY u.id");

            // ─── 7. Chat room ─────────────────────────────────────────────────
            if (tbl(c, "chat_rooms")) {
                h("7. CHAT ROOM SEED");
                q(c, "SELECT column_name FROM information_schema.columns WHERE table_name='chat_rooms' ORDER BY ordinal_position");
                if (cnt(c, "SELECT COUNT(*) FROM chat_rooms WHERE company_id=1 AND name='General'") == 0) {
                    try {
                        exe(c, "INSERT INTO chat_rooms(company_id, name, room_type) VALUES(1,'General','GROUP')");
                        say("✅ General chat room created");
                    } catch (Exception e) {
                        try {
                            exe(c, "INSERT INTO chat_rooms(company_id, name) VALUES(1,'General')");
                            say("✅ General room created (no room_type col)");
                        } catch (Exception e2) {
                            say("⚠️ " + e2.getMessage());
                        }
                    }
                } else
                    say("General room already exists");
                q(c, "SELECT * FROM chat_rooms LIMIT 10");
            }
            h("✅ FinalPatch complete");
        }
    }

    static void h(String t) {
        System.out.println("\n══ " + t + " " + "═".repeat(Math.max(0, 50 - t.length())));
    }

    static void say(String m) {
        System.out.println("  " + m);
    }

    static void exe(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute(sql);
        }
    }

    static int cnt(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getInt(1) : 0;
        }
    }

    static long scl(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getLong(1) : 0;
        }
    }

    static boolean tbl(Connection c, String n) throws SQLException {
        return cnt(c, "SELECT COUNT(*) FROM information_schema.tables WHERE table_name='" + n + "'") > 0;
    }

    static long eid(Connection c, String f, String l) throws SQLException {
        String sql = l == null ? "SELECT id FROM employees WHERE first_name ILIKE '" + f + "' LIMIT 1"
                : "SELECT id FROM employees WHERE first_name ILIKE '" + f + "' AND last_name ILIKE '" + l + "' LIMIT 1";
        return scl(c, sql);
    }

    static void insLeave(Connection c, long emp, String date, long lt) {
        String[] sqls = {
                String.format(
                        "INSERT INTO leaves(employee_id,start_date,end_date,leave_type_id,status,created_at) VALUES(%d,'%s','%s',%d,'approved',NOW()) ON CONFLICT DO NOTHING",
                        emp, date, date, lt),
                String.format(
                        "INSERT INTO leaves(employee_id,start_date,end_date,leave_type_id,status) VALUES(%d,'%s','%s',%d,'approved') ON CONFLICT DO NOTHING",
                        emp, date, date, lt),
        };
        for (String sql : sqls) {
            try {
                exe(c, sql);
                say("Leave " + date + " emp=" + emp + " → OK");
                return;
            } catch (Exception e) {
                say("  attempt failed: " + e.getMessage());
            }
        }
    }

    static void insAbsent(Connection c, long emp, String date) {
        String[] sqls = {
                String.format(
                        "INSERT INTO attendance(employee_id,date,status) VALUES(%d,'%s','ABSENT') ON CONFLICT(employee_id,date) DO UPDATE SET status='ABSENT'",
                        emp, date),
                String.format(
                        "INSERT INTO attendance(employee_id,date,status) VALUES(%d,'%s','ABSENT') ON CONFLICT DO NOTHING",
                        emp, date),
        };
        for (String sql : sqls) {
            try {
                exe(c, sql);
                say("Absent " + date + " emp=" + emp + " → OK");
                return;
            } catch (Exception e) {
                say("  attempt failed: " + e.getMessage());
            }
        }
    }

    static void q(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            ResultSetMetaData m = rs.getMetaData();
            int cols = m.getColumnCount();
            StringBuilder hdr = new StringBuilder("  ");
            for (int i = 1; i <= cols; i++)
                hdr.append(String.format("%-22s", m.getColumnName(i)));
            System.out.println(hdr);
            System.out.println("  " + "-".repeat(cols * 22));
            int n = 0;
            while (rs.next() && n++ < 20) {
                StringBuilder row = new StringBuilder("  ");
                for (int i = 1; i <= cols; i++)
                    row.append(String.format("%-22s", nv(rs.getString(i))));
                System.out.println(row);
            }
            if (n == 0)
                System.out.println("  (no rows)");
        }
    }

    static String nv(String s) {
        return s == null ? "(null)" : s.length() > 20 ? s.substring(0, 19) + "…" : s;
    }
}
