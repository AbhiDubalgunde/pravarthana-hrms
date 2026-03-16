import java.sql.*;
public class UserCheck {
    static final String URL="jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0";
    static final String U="postgres.ubbnkuykcztnnqthtsuy",P="Pravarthana@2026!";
    public static void main(String[] a) throws Exception {
        try(Connection c=DriverManager.getConnection(URL,U,P)){
            System.out.println("=== USERS ===");
            try(ResultSet r=c.createStatement().executeQuery(
                "SELECT id, email, role, password FROM users ORDER BY id")){
                while(r.next()) System.out.printf("id=%-3d email=%-35s role=%-15s pwd_len=%d%n",
                    r.getInt(1),r.getString(2),r.getString(3),
                    r.getString(4)!=null?r.getString(4).length():0);
            }
        }
    }
}