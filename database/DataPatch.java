import java.sql.*;
import java.time.LocalDateTime;

/**
 * DataPatch — applies all business data changes to HRMS DB:
 * 1. Backups for leaves & attendance
 * 2. Update employee join dates
 * 3. Seed leave_types (Casual)
 * 4. Insert leave records (Abhishek + Vikas)
 * 5. Insert attendance ABSENT on leave days
 * 6. Update user roles (HR_ADMIN + SUPER_ADMIN)
 * 7. Seed General chat room
 */
public class DataPatch {
    static final String URL = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
    static final String USR = "postgres.ubbnkuykcztnnqthtsuy";
    static final String PWD = "Pravarthana@2026!";

    public static void main(String[] args) throws Exception {
        try (Connection c = DriverManager.getConnection(URL, USR, PWD)) {
            System.out.println("✅ Connected\n");

            // ─── 0. Inspect current users table ─────────────────────────────
            h("0. CURRENT USERS");
            q(c, "SELECT id, email, role FROM users ORDER BY id");

            // ─── 0b. Inspect leaves & attendance table ───────────────────────
            h("0b. SCHEMA CHECK");
            boolean hasLeaves = tableExists(c, "leaves");
            boolean hasLeaveTypes = tableExists(c, "leave_types");
            boolean hasAttendance = tableExists(c, "attendance");
            boolean hasChatRooms = tableExists(c, "chat_rooms");
            boolean hasChatMsgs = tableExists(c, "chat_messages");
            System.out.printf("  leaves table:       %s%n", hasLeaves ? "EXISTS" : "MISSING");
            System.out.printf("  leave_types table:  %s%n", hasLeaveTypes ? "EXISTS" : "MISSING");
            System.out.printf("  attendance table:   %s%n", hasAttendance ? "EXISTS" : "MISSING");
            System.out.printf("  chat_rooms table:   %s%n", hasChatRooms ? "EXISTS" : "MISSING");
            System.out.printf("  chat_messages:      %s%n", hasChatMsgs ? "EXISTS" : "MISSING");

            // ─── 1. Backups ──────────────────────────────────────────────────
            h("1. BACKUPS");
            if (hasLeaves && !tableExists(c, "leaves_backup")) {
                exec(c, "CREATE TABLE leaves_backup AS TABLE leaves WITH DATA");
                System.out.println("  ✅ leaves_backup created");
            } else
                System.out.println("  leaves_backup: already exists or no leaves table");
            if (hasAttendance && !tableExists(c, "attendance_backup")) {
                exec(c, "CREATE TABLE attendance_backup AS TABLE attendance WITH DATA");
                System.out.println("  ✅ attendance_backup created");
            } else
                System.out.println("  attendance_backup: already exists or no attendance table");

            // ─── 2. Employee IDs ──────────────────────────────────────────────
            h("2. EMPLOYEE IDs");
            long abhishekId = empId(c, "Abhishek", "Dubalgunde");
            long vikasId = empId(c, "Vikas", null);
            long gajananId = empId(c, "Gajanan", null);
            long maheshId = empId(c, "Mahesh", null);
            System.out.printf("  Abhishek id=%d, Vikas id=%d, Gajanan id=%d, Mahesh id=%d%n",
                    abhishekId, vikasId, gajananId, maheshId);

            // ─── 3. Update join dates ─────────────────────────────────────────
            h("3. JOIN DATE UPDATES");
            upd(c, String.format("UPDATE employees SET date_of_joining = '2025-11-17' WHERE id = %d", abhishekId));
            System.out.println("  ✅ Abhishek join date → 2025-11-17");
            upd(c, String.format("UPDATE employees SET date_of_joining = '2026-01-12' WHERE id = %d", vikasId));
            System.out.println("  ✅ Vikas join date → 2026-01-12");
            if (gajananId > 0)
                upd(c, String.format("UPDATE employees SET date_of_joining = '2025-10-01' WHERE id = %d", gajananId));
            if (maheshId > 0)
                upd(c, String.format("UPDATE employees SET date_of_joining = '2025-10-01' WHERE id = %d", maheshId));
            System.out.println("  ✅ Gajanan + Mahesh join date → 2025-10-01");
            q(c, "SELECT id, first_name, date_of_joining FROM employees ORDER BY id");

            // ─── 4. Leave types ───────────────────────────────────────────────
            if (hasLeaveTypes) {
                h("4. LEAVE TYPES");
                int exists = cnt(c, "SELECT COUNT(*) FROM leave_types WHERE name ILIKE 'casual'");
                if (exists == 0) {
                    exec(c, "INSERT INTO leave_types (name) VALUES ('Casual')");
                    System.out.println("  ✅ Inserted 'Casual' leave type");
                } else
                    System.out.println("  'Casual' leave type already exists");
                long ltId = scalar(c, "SELECT id FROM leave_types WHERE name ILIKE 'casual' LIMIT 1");
                System.out.printf("  leave_type_id (Casual) = %d%n", ltId);

                // ─── 5. Leave records ─────────────────────────────────────────
                if (hasLeaves) {
                    h("5. LEAVE INSERTS");
                    // Abhishek: 2025-12-30 and 2026-01-30
                    insertLeave(c, abhishekId, "2025-12-30", ltId);
                    insertLeave(c, abhishekId, "2026-01-30", ltId);
                    // Vikas: 2026-01-22 and 2026-01-23
                    insertLeave(c, vikasId, "2026-01-22", ltId);
                    insertLeave(c, vikasId, "2026-01-23", ltId);
                    System.out.println("  ✅ Leave records inserted");
                    q(c, "SELECT id, employee_id, start_date, end_date, status FROM leaves ORDER BY id");
                } else
                    System.out.println("  ⚠️  No 'leaves' table — skipping leave inserts");
            } else {
                System.out.println("  ⚠️  No 'leave_types' table — check schema name (leave_requests?)");
                // Try alternate table name
                if (tableExists(c, "leave_requests")) {
                    System.out.println("  Found 'leave_requests' table instead");
                    q(c, "SELECT * FROM leave_requests LIMIT 5");
                }
            }

            // ─── 6. Attendance ABSENT records ────────────────────────────────
            if (hasAttendance) {
                h("6. ATTENDANCE ABSENT ON LEAVE DAYS");
                // Check column names
                q(c, "SELECT column_name FROM information_schema.columns WHERE table_name='attendance' ORDER BY ordinal_position");
                // Try insert — adapt to schema
                insertAbsent(c, abhishekId, "2025-12-30");
                insertAbsent(c, abhishekId, "2026-01-30");
                insertAbsent(c, vikasId, "2026-01-22");
                insertAbsent(c, vikasId, "2026-01-23");
                System.out.println("  ✅ Attendance ABSENT rows inserted");
            } else
                System.out.println("  ⚠️  No 'attendance' table — skipping");

            // ─── 7. User role updates ────────────────────────────────────────
            h("7. USER ROLE UPDATES");
            // Show current roles column
            boolean hasRoleCol = colExists(c, "users", "role");
            boolean hasRoleId = colExists(c, "users", "role_id");
            System.out.printf("  users.role col: %s  /  users.role_id FK: %s%n", hasRoleCol, hasRoleId);

            if (hasRoleCol) {
                // Direct role column
                int n1 = upd(c, "UPDATE users SET role = 'HR_ADMIN' WHERE email = 'abhidubalgunde@gmail.com'");
                System.out.printf("  abhidubalgunde@gmail.com → HR_ADMIN: %d row updated%n", n1);
                int n2 = upd(c, "UPDATE users SET role = 'SUPER_ADMIN' WHERE email = 'abhishek.prvt2529@gmail.com'");
                System.out.printf("  abhishek.prvt2529@gmail.com → SUPER_ADMIN: %d row updated%n", n2);
                // If the target email doesn't exist yet, create or update current admin
                if (n2 == 0) {
                    System.out.println(
                            "  ⚠️  abhishek.prvt2529@gmail.com not found — showing current SUPER_ADMIN users:");
                    q(c, "SELECT id, email, role FROM users WHERE role = 'SUPER_ADMIN'");
                    System.out.println(
                            "  → Please manually update: UPDATE users SET email='abhishek.prvt2529@gmail.com' WHERE id=<id>;");
                }
            } else if (hasRoleId) {
                // role_id FK approach — find HR_ADMIN role id
                long hrRoleId = scalar(c, "SELECT id FROM roles WHERE name = 'HR_ADMIN' LIMIT 1");
                long adminRoleId = scalar(c, "SELECT id FROM roles WHERE name = 'SUPER_ADMIN' LIMIT 1");
                System.out.printf("  HR_ADMIN role_id=%d  SUPER_ADMIN role_id=%d%n", hrRoleId, adminRoleId);
                if (hrRoleId > 0) {
                    int n = upd(c, String.format(
                            "UPDATE users SET role_id = %d WHERE email = 'abhidubalgunde@gmail.com'", hrRoleId));
                    System.out.printf("  abhidubalgunde@gmail.com → HR_ADMIN role_id=%d: %d row(s)%n", hrRoleId, n);
                }
                if (adminRoleId > 0) {
                    int n = upd(c, String.format(
                            "UPDATE users SET role_id = %d WHERE email = 'abhishek.prvt2529@gmail.com'", adminRoleId));
                    System.out.printf("  abhishek.prvt2529@gmail.com → SUPER_ADMIN role_id=%d: %d row(s)%n",
                            adminRoleId, n);
                }
            }
            q(c, "SELECT id, email, role FROM users ORDER BY id");

            // ─── 8. Seed General chat room ───────────────────────────────────
            if (hasChatRooms) {
                h("8. CHAT ROOM SEED");
                int existing = cnt(c, "SELECT COUNT(*) FROM chat_rooms WHERE company_id = 1 AND name = 'General'");
                if (existing == 0) {
                    exec(c, "INSERT INTO chat_rooms(company_id, name, room_type) VALUES(1,'General','GROUP')");
                    System.out.println("  ✅ Seeded 'General' chat room");
                } else
                    System.out.println("  'General' chat room already exists");
                q(c, "SELECT id, name, room_type, company_id FROM chat_rooms LIMIT 10");
            } else
                System.out.println("  ⚠️  No 'chat_rooms' table found");

            h("DONE — DataPatch complete");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    static void h(String t) {
        System.out.println("\n══ " + t + " " + "═".repeat(Math.max(0, 50 - t.length())));
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

    static long scalar(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getLong(1) : 0;
        }
    }

    static boolean tableExists(Connection c, String name) throws SQLException {
        return cnt(c, "SELECT COUNT(*) FROM information_schema.tables WHERE table_name='" + name + "'") > 0;
    }

    static boolean colExists(Connection c, String table, String col) throws SQLException {
        return cnt(c, "SELECT COUNT(*) FROM information_schema.columns WHERE table_name='" + table
                + "' AND column_name='" + col + "'") > 0;
    }

    static long empId(Connection c, String first, String last) throws SQLException {
        String sql = last == null
                ? "SELECT id FROM employees WHERE first_name ILIKE '" + first + "' LIMIT 1"
                : "SELECT id FROM employees WHERE first_name ILIKE '" + first + "' AND last_name ILIKE '" + last
                        + "' LIMIT 1";
        return scalar(c, sql);
    }

    static void insertLeave(Connection c, long empId, String date, long ltId) throws SQLException {
        try {
            exec(c, String.format(
                    "INSERT INTO leaves(employee_id, start_date, end_date, leave_type_id, status, created_at) " +
                            "VALUES(%d,'%s','%s',%d,'approved',NOW()) ON CONFLICT DO NOTHING",
                    empId, date, date, ltId));
            System.out.printf("  Leave %s for emp %d inserted%n", date, empId);
        } catch (Exception e) {
            System.out.printf("  ⚠️  Leave insert %s for emp %d failed: %s%n", date, empId, e.getMessage());
        }
    }

    static void insertAbsent(Connection c, long empId, String date) throws SQLException {
        try {
            // Try common schema - adapt if columns differ
            exec(c, String.format(
                    "INSERT INTO attendance(employee_id, date, status) VALUES(%d,'%s','ABSENT') " +
                            "ON CONFLICT(employee_id, date) DO UPDATE SET status='ABSENT'",
                    empId, date));
            System.out.printf("  Attendance ABSENT %s for emp %d OK%n", date, empId);
        } catch (Exception e) {
            System.out.printf("  ⚠️  Attendance insert %s for emp %d failed: %s%n", date, empId, e.getMessage());
        }
    }

    static void q(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            ResultSetMetaData m = rs.getMetaData();
            int cols = m.getColumnCount();
            StringBuilder hdr = new StringBuilder("  ");
            for (int i = 1; i <= cols; i++)
                hdr.append(String.format("%-25s", m.getColumnName(i)));
            System.out.println(hdr);
            System.out.println("  " + "-".repeat(cols * 25));
            int rows = 0;
            while (rs.next() && rows++ < 20) {
                StringBuilder row = new StringBuilder("  ");
                for (int i = 1; i <= cols; i++)
                    row.append(String.format("%-25s", nv(rs.getString(i))));
                System.out.println(row);
            }
            if (rows == 0)
                System.out.println("  (no rows)");
        }
    }

    static String nv(String s) {
        return s == null ? "(null)" : s.length() > 22 ? s.substring(0, 21) + "…" : s;
    }
}
