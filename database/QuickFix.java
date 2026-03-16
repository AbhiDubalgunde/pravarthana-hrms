import java.sql.*;

public class QuickFix {
    static final String URL = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
    static final String U = "postgres.ubbnkuykcztnnqthtsuy", P = "Pravarthana@2026!";

    public static void main(String[] a) throws Exception {
        try (Connection c = DriverManager.getConnection(URL, U, P)) {
            System.out.println("Connected");
            long lt = 1; // Casual Leave id=1
            ins(c, 7, "2025-12-30", lt);
            ins(c, 7, "2026-01-30", lt);
            ins(c, 8, "2026-01-22", lt);
            ins(c, 8, "2026-01-23", lt);
            // Show leaves
            try (Statement s = c.createStatement();
                    ResultSet r = s.executeQuery("SELECT id,employee_id,start_date,status FROM leaves ORDER BY id")) {
                System.out.println("LEAVES:");
                while (r.next())
                    System.out.printf("  id=%d emp=%d date=%s status=%s%n", r.getInt(1), r.getInt(2), r.getString(3),
                            r.getString(4));
            }
            // Chat room - columns: id,name,type,created_by,created_at (no company_id)
            try (Statement s = c.createStatement();
                    ResultSet r = s.executeQuery("SELECT COUNT(*) FROM chat_rooms WHERE name='General'")) {
                r.next();
                if (r.getInt(1) == 0) {
                    try (Statement s2 = c.createStatement()) {
                        s2.execute("INSERT INTO chat_rooms(name,type) VALUES('General','group')");
                        System.out.println("General chat room created");
                    }
                } else
                    System.out.println("General room already exists");
            }
            try (Statement s = c.createStatement();
                    ResultSet r = s.executeQuery("SELECT id,name,type FROM chat_rooms")) {
                System.out.println("CHAT_ROOMS:");
                while (r.next())
                    System.out.printf("  id=%d name=%s type=%s%n", r.getInt(1), r.getString(2), r.getString(3));
            }
            System.out.println("Done");
        }
    }

    static void ins(Connection c, int emp, String d, long lt) throws Exception {
        try (Statement s = c.createStatement()) {
            s.execute(String.format(
                    "INSERT INTO leaves(employee_id,leave_type_id,start_date,end_date,total_days,status,created_at) " +
                            "VALUES(%d,%d,'%s','%s',1,'approved',NOW()) ON CONFLICT DO NOTHING",
                    emp, lt, d, d));
            System.out.printf("Leave %s emp=%d OK%n", d, emp);
        } catch (Exception e) {
            System.out.printf("Leave %s emp=%d failed: %s%n", d, emp, e.getMessage());
        }
    }
}
