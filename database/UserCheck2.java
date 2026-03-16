import java.sql.*;
public class UserCheck2 {
    static final String URL="jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0";
    static final String U="postgres.ubbnkuykcztnnqthtsuy",P="Pravarthana@2026!";
    public static void main(String[] a) throws Exception {
        try(Connection c=DriverManager.getConnection(URL,U,P)){
            System.out.println("=== USERS cols ===");
            try(ResultSet r=c.createStatement().executeQuery(
                "SELECT column_name,data_type FROM information_schema.columns WHERE table_name='users' ORDER BY ordinal_position")){
                while(r.next()) System.out.printf("  %-25s %s%n",r.getString(1),r.getString(2));
            }
            System.out.println("=== USERS rows ===");
            try(ResultSet r=c.createStatement().executeQuery("SELECT * FROM users LIMIT 5")){
                ResultSetMetaData m=r.getMetaData();
                while(r.next()){
                    for(int i=1;i<=m.getColumnCount();i++) System.out.print(m.getColumnName(i)+"="+r.getString(i)+" | ");
                    System.out.println();
                }
            }
        }
    }
}