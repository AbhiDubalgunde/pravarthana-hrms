import java.sql.*;

/**
 * DataPatch v2 — correct schema: users.role_id (FK to roles) not users.role
 * string
 * Applies:
 * 1. Join date updates for all 4 employees
 * 2. Leave_types seed (Casual)
 * 3. Leave inserts (Abhishek + Vikas)
 * 4. Attendance ABSENT inserts
 * 5. HR_ADMIN for abhidubalgunde@gmail.com
 * 6. SUPER_ADMIN for abhishek.prvt2529@gmail.com
 * 7. General chat room seed
 */
public class DataPatch2 {
    static final String URL = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
    static final String USR = "postgres.ubbnkuykcztnnqthtsuy";
    static final String PWD = "Pravarthana@2026!";

    public static void main(String[] a) throws Exception {
        try (Connection c = DriverManager.getConnection(URL, USR, PWD)) {
            System.out.println("✅ Connected\n");

            // ─── 0. Current state ────────────────────────────────────────────
            h("0. CURRENT USERS (with role names)");
            q(c, "SELECT u.id, u.email, r.name AS role FROM users u LEFT JOIN roles r ON r.id = u.role_id ORDER BY u.id");

            // ─── 0b. Schema inspect ──────────────────────────────────────────
            h("0b. TABLE EXISTS CHECK");
            boolean hasLeaves = tbl(c, "leaves");
            boolean hasLeaveTypes = tbl(c, "leave_types");
            boolean hasAttendance = tbl(c, "attendance");
            boolean hasChatRooms = tbl(c, "chat_rooms");
            System.out.printf("  leaves:%s  leave_types:%s  attendance:%s  chat_rooms:%s%n",
                    hasLeaves, hasLeaveTypes, hasAttendance, hasChatRooms);

            // ─── 1. Employee IDs ─────────────────────────────────────────────
            h("1. EMPLOYEE IDs");
            long abhishekId = eid(c, "Abhishek", "Dubalgunde");
            long vikasId = eid(c, "Vikas", null);
            long gajananId = eid(c, "Gajanan", null);
            long maheshId = eid(c, "Mahesh", null);
            System.out.printf("  Abhishek=%d Vikas=%d Gajanan=%d Mahesh=%d%n",
                    abhishekId, vikasId, gajananId, maheshId);
            if (abhishekId == 0 || vikasId == 0)
                throw new RuntimeException("Could not find required employees!");

            // ─── 2. Join dates ───────────────────────────────────────────────
            h("2. JOIN DATE UPDATES");
            exe(c, "UPDATE employees SET date_of_joining='2025-11-17' WHERE id=" + abhishekId);
            say("Abhishek → 2025-11-17");
            exe(c, "UPDATE employees SET date_of_joining='2026-01-12' WHERE id=" + vikasId);
            say("Vikas → 2026-01-12");
            if (gajananId > 0) {
                exe(c, "UPDATE employees SET date_of_joining='2025-10-01' WHERE id=" + gajananId);
                say("Gajanan → 2025-10-01");
            }
            if (maheshId > 0) {
                exe(c, "UPDATE employees SET date_of_joining='2025-10-01' WHERE id=" + maheshId);
                say("Mahesh → 2025-10-01");
            }
            q(c, "SELECT id, first_name, date_of_joining FROM employees ORDER BY id");

            // ─── 3. Leave types ──────────────────────────────────────────────
            if (hasLeaveTypes) {
                h("3. LEAVE TYPES");
                if (cnt(c, "SELECT COUNT(*) FROM leave_types WHERE name ILIKE 'casual'") == 0)
                    exe(c, "INSERT INTO leave_types(name) VALUES('Casual')");
                long ltId = scl(c, "SELECT id FROM leave_types WHERE name ILIKE 'casual' LIMIT 1");
                say("Casual leave_type id=" + ltId);

                // ─── 4. Leave inserts ────────────────────────────────────────
                if (hasLeaves) {
                    h("4. LEAVE INSERTS");
                    insLeave(c, abhishekId, "2025-12-30", ltId);
                    insLeave(c, abhishekId, "2026-01-30", ltId);
                    insLeave(c, vikasId, "2026-01-22", ltId);
                    insLeave(c, vikasId, "2026-01-23", ltId);
                    q(c, "SELECT id, employee_id, start_date, status FROM leaves ORDER BY id");
                } else
                    say("⚠️ No 'leaves' table");
            } else {
                say("⚠️ No 'leave_types' table — looking for alternate names");
                if (tbl(c, "leave_requests"))
                    q(c, "SELECT * FROM leave_requests LIMIT 3");
            }

            // ─── 5. Attendance ABSENT ────────────────────────────────────────
            if (hasAttendance) {
                h("5. ATTENDANCE ABSENT");
                // Show columns to adapt the INSERT
                q(c, "SELECT column_name, data_type FROM information_schema.columns WHERE table_name='attendance' ORDER BY ordinal_position");
                // Try standard schema: (employee_id, date, status)
                insAbsent(c, abhishekId, "2025-12-30");
                insAbsent(c, abhishekId, "2026-01-30");
                insAbsent(c, vikasId, "2026-01-22");
                insAbsent(c, vikasId, "2026-01-23");
            } else
                say("⚠️ No 'attendance' table");

            // ─── 6. User role updates ─────────────────────────────────────────
            h("6. USER ROLE UPDATES (via role_id FK)");
            long hrRoleId = scl(c, "SELECT id FROM roles WHERE name='HR_ADMIN' LIMIT 1");
            long adminRoleId = scl(c, "SELECT id FROM roles WHERE name='SUPER_ADMIN' LIMIT 1");
            System.out.printf("  HR_ADMIN role_id=%d  SUPER_ADMIN role_id=%d%n", hrRoleId, adminRoleId);

            if (hrRoleId > 0) {
                // Check if user exists
                long uid = scl(c, "SELECT id FROM users WHERE email='abhidubalgunde@gmail.com' LIMIT 1");
                if (uid > 0) {
                    exe(c, "UPDATE users SET role_id=" + hrRoleId + " WHERE email='abhidubalgunde@gmail.com'");
                    say("✅ abhidubalgunde@gmail.com → HR_ADMIN");
                } else {
                    say("⚠️ abhidubalgunde@gmail.com NOT FOUND in users table");
                    say("   → Please register this user via /api/auth/register first, then run this patch again");
                }
            }
            if (adminRoleId > 0) {
                long uid = scl(c, "SELECT id FROM users WHERE email='abhishek.prvt2529@gmail.com' LIMIT 1");
                if (uid > 0) {
                    exe(c, "UPDATE users SET role_id=" + adminRoleId + " WHERE email='abhishek.prvt2529@gmail.com'");
                    say("✅ abhishek.prvt2529@gmail.com → SUPER_ADMIN");
                } else {
                    say("⚠️ abhishek.prvt2529@gmail.com NOT FOUND in users table");
                    say("   → Please register this user via /api/auth/register first, then run this patch again");
                    say("   Current SUPER_ADMIN accounts:");
                    q(c, "SELECT u.id, u.email, r.name AS role FROM users u JOIN roles r ON r.id=u.role_id WHERE r.name='SUPER_ADMIN'");
                }
            }
            q(c, "SELECT u.id, u.email, r.name AS role FROM users u LEFT JOIN roles r ON r.id=u.role_id ORDER BY u.id");

            // ─── 7. Chat room seed ───────────────────────────────────────────
            if (hasChatRooms) {
                h("7. CHAT ROOM SEED");
                if (cnt(c, "SELECT COUNT(*) FROM chat_rooms WHERE company_id=1 AND name='General'") == 0) {
                    exe(c, "INSERT INTO chat_rooms(company_id, name, room_type) VALUES(1,'General','GROUP')");
                    say("✅ 'General' chat room seeded");
                } else
                    say("'General' room already exists");
                q(c, "SELECT id, name, room_type, company_id FROM chat_rooms LIMIT 10");
            } else
                say("⚠️ No 'chat_rooms' table");

            h("✅ DataPatch2 complete");
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

    static boolean tbl(Connection c, String nm) throws SQLException {
        return cnt(c, "SELECT COUNT(*) FROM information_schema.tables WHERE table_name='" + nm + "'") > 0;
    }

    static long eid(Connection c, String f, String l) throws SQLException {
        String sql = l == null
                ? "SELECT id FROM employees WHERE first_name ILIKE '" + f + "' LIMIT 1"
                : "SELECT id FROM employees WHERE first_name ILIKE '" + f + "' AND last_name ILIKE '" + l + "' LIMIT 1";
        return scl(c, sql);
    }

    static void insLeave(Connection c, long emp, String date, long lt) {
        try {
            exe(c, String.format(
                    "INSERT INTO leaves(employee_id,start_date,end_date,leave_type_id,status,created_at) " +
                            "VALUES(%d,'%s','%s',%d,'approved',NOW()) ON CONFLICT DO NOTHING",
                    emp, date, date, lt));
            say("Leave " + date + " emp=" + emp + " → OK");
        } catch (Exception e) {
            say("⚠️ leave " + date + " emp=" + emp + ": " + e.getMessage());
        }
    }

    static void insAbsent(Connection c, long emp, String date) {
        try {
            exe(c, String.format(
                    "INSERT INTO attendance(employee_id,date,status) VALUES(%d,'%s','ABSENT') " +
                            "ON CONFLICT(employee_id,date) DO UPDATE SET status='ABSENT'",
                    emp, date));
            say("Attendance ABSENT " + date + " emp=" + emp + " → OK");
        } catch (Exception e) {
            say("⚠️ attendance " + date + " emp=" + emp + ": " + e.getMessage());
        }
    }

    static void q(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            ResultSetMetaData m = rs.getMetaData();
            int cols = m.getColumnCount();
            StringBuilder h = new StringBuilder("  ");
            for (int i = 1; i <= cols; i++)
                h.append(String.format("%-26s", m.getColumnName(i)));
            System.out.println(h);
            System.out.println("  " + "-".repeat(cols * 26));
            int rows = 0;
            while (rs.next() && rows++ < 20) {
                StringBuilder row = new StringBuilder("  ");
                for (int i = 1; i <= cols; i++)
                    row.append(String.format("%-26s", nv(rs.getString(i))));
                System.out.println(row);
            }
            if (rows == 0)
                System.out.println("  (no rows)");
        }
    }

    static String nv(String s) {
        return s == null ? "(null)" : s.length() > 23 ? s.substring(0, 22) + "…" : s;
    }
}
