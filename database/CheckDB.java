import java.sql.*;

public class CheckDB {
    public static void main(String[] args) throws Exception {
        String URL = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
        String USER = "postgres.ubbnkuykcztnnqthtsuy";
        String PASS = "Pravarthana@2026!";
        try (Connection c = DriverManager.getConnection(URL, USER, PASS);
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT column_name FROM information_schema.columns WHERE table_name = 'departments'")) {
            while (rs.next())
                System.out.println("Dept col: " + rs.getString(1));
        }
        try (Connection c = DriverManager.getConnection(URL, USER, PASS);
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT column_name FROM information_schema.columns WHERE table_name = 'employees'")) {
            while (rs.next())
                System.out.println("Emp col: " + rs.getString(1));
        }
    }
}
