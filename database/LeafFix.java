import java.sql.*;
public class LeafFix {
    static final String URL="jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
    static final String U="postgres.ubbnkuykcztnnqthtsuy",P="Pravarthana@2026!";
    public static void main(String[] a) throws Exception {
        try(Connection c=DriverManager.getConnection(URL,U,P)){
            long lt=1;
            ins(c,7,"2025-12-30",lt,"Casual leave"); ins(c,7,"2026-01-30",lt,"Casual leave");
            ins(c,8,"2026-01-22",lt,"Casual leave"); ins(c,8,"2026-01-23",lt,"Casual leave");
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,employee_id,start_date,status FROM leaves ORDER BY id")){
                System.out.println("LEAVES:"); while(r.next()) System.out.printf("  id=%d emp=%d date=%s status=%s%n",r.getInt(1),r.getInt(2),r.getString(3),r.getString(4));
            }
        }
    }
    static void ins(Connection c,int emp,String d,long lt,String reason) throws Exception {
        try(Statement s=c.createStatement()){
            s.execute(String.format("INSERT INTO leaves(employee_id,leave_type_id,start_date,end_date,total_days,reason,status,created_at) VALUES(%d,%d,'%s','%s',1,'%s','approved',NOW()) ON CONFLICT DO NOTHING",emp,lt,d,d,reason));
            System.out.printf("Leave %s emp=%d OK%n",d,emp);
        } catch(Exception e){System.out.printf("FAIL %s emp=%d: %s%n",d,emp,e.getMessage());}
    }
}