import java.sql.*;
public class ChatDBCheck {
    static final String URL="jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
    static final String U="postgres.ubbnkuykcztnnqthtsuy",P="Pravarthana@2026!";
    public static void main(String[] a) throws Exception {
        try(Connection c=DriverManager.getConnection(URL,U,P)){
            for(String t:new String[]{"chat_rooms","chat_messages","chat_room_members","chat_message_reads","companies"}){
                try(Statement s=c.createStatement();ResultSet r=s.executeQuery(
                    "SELECT column_name,data_type,is_nullable FROM information_schema.columns WHERE table_name='"+t+"' ORDER BY ordinal_position")){
                    System.out.println("=== "+t+" ===");
                    boolean any=false;
                    while(r.next()){any=true; System.out.printf("  %-25s %-20s nullable=%s%n",r.getString(1),r.getString(2),r.getString(3));}
                    if(!any) System.out.println("  (table does not exist)");
                }
            }
            System.out.println("=== employees companyId check ===");
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery(
                "SELECT column_name FROM information_schema.columns WHERE table_name='employees' ORDER BY ordinal_position")){
                while(r.next()) System.out.print(r.getString(1)+" ");
                System.out.println();
            }
            System.out.println("=== attendance column check ===");
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery(
                "SELECT column_name FROM information_schema.columns WHERE table_name='attendance' ORDER BY ordinal_position")){
                while(r.next()) System.out.print(r.getString(1)+" ");
                System.out.println();
            }
            System.out.println("=== leaves column check ===");
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery(
                "SELECT column_name FROM information_schema.columns WHERE table_name='leaves' ORDER BY ordinal_position")){
                while(r.next()) System.out.print(r.getString(1)+" ");
                System.out.println();
            }
        }
    }
}