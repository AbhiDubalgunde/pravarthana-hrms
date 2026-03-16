import java.sql.*;
public class EmpCheck {
    static final String URL="jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0";
    static final String U="postgres.ubbnkuykcztnnqthtsuy",P="Pravarthana@2026!";
    public static void main(String[] a) throws Exception {
        try(Connection c=DriverManager.getConnection(URL,U,P)){
            System.out.println("=== ALL EMPLOYEES ===");
            try(ResultSet r=c.createStatement().executeQuery(
                "SELECT id, first_name, last_name, company_id, department_id, status FROM employees ORDER BY id")){
                while(r.next()) System.out.printf("id=%-3d name=%-20s company_id=%-6s dept_id=%-6s status=%s%n",
                    r.getInt(1), r.getString(2)+" "+r.getString(3), r.getObject(4), r.getObject(5), r.getString(6));
            }
            System.out.println("\n=== DEPARTMENTS ===");
            try(ResultSet r=c.createStatement().executeQuery("SELECT id, name, company_id FROM departments ORDER BY id")){
                while(r.next()) System.out.printf("id=%-3d name=%-25s company_id=%s%n", r.getInt(1), r.getString(2), r.getObject(3));
            }
        }
    }
}