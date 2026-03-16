import java.sql.*;
public class OfferLetterAudit {
    static final String URL="jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
    static final String U="postgres.ubbnkuykcztnnqthtsuy",P="Pravarthana@2026!";
    public static void main(String[] a) throws Exception {
        try(Connection c=DriverManager.getConnection(URL,U,P)){
            System.out.println("=== offer_letters TABLE SCHEMA ===");
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery(
                "SELECT column_name, data_type, is_nullable, column_default "+
                "FROM information_schema.columns WHERE table_name='offer_letters' ORDER BY ordinal_position")){
                while(r.next()) System.out.printf("  %-25s %-20s nullable=%-5s default=%s%n",
                    r.getString(1),r.getString(2),r.getString(3),r.getString(4));
            }
            boolean hasTable = false;
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name='offer_letters'")){
                r.next(); hasTable = r.getInt(1)>0;
            }
            System.out.println("  Table exists: "+hasTable);
            if(hasTable){
                System.out.println("=== SAMPLE ROW COUNT ===");
                try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM offer_letters")){
                    r.next(); System.out.println("  Rows: "+r.getInt(1));
                }
            }
        }
    }
}