import java.sql.*;
public class SeedDepts {
    static final String URL="jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0";
    static final String U="postgres.ubbnkuykcztnnqthtsuy",P="Pravarthana@2026!";
    static final long COMPANY_ID = 1L;
    static final String[] DEPTS = {
        "Engineering","Quality Assurance","DevOps","Product","Design",
        "Human Resources","Finance","Sales","Marketing","Customer Support","Operations"
    };
    static final String[] DESCS = {
        "Software development and engineering","QA testing and quality assurance",
        "Infrastructure and DevOps","Product management and roadmap",
        "UI/UX design and research","People operations and recruitment",
        "Finance and accounting","Sales and revenue","Marketing and growth",
        "Customer support and success","Business operations"
    };
    public static void main(String[] a) throws Exception {
        try(Connection c=DriverManager.getConnection(URL,U,P)){
            c.setAutoCommit(true);
            System.out.println("=== SEEDING DEPARTMENTS ===");
            String upsert = "INSERT INTO departments(company_id, name, description, is_active) " +
                "VALUES(?,?,?,true) ON CONFLICT (company_id, name) DO NOTHING";
            try(PreparedStatement ps=c.prepareStatement(upsert)){
                for(int i=0;i<DEPTS.length;i++){
                    ps.setLong(1,COMPANY_ID);
                    ps.setString(2,DEPTS[i]);
                    ps.setString(3,DESCS[i]);
                    int rows=ps.executeUpdate();
                    System.out.println((rows>0?"INSERTED":"EXISTS  ")+": "+DEPTS[i]);
                }
            }
            System.out.println("\n=== FINAL DEPARTMENTS ===");
            try(ResultSet r=c.createStatement().executeQuery(
                "SELECT id, name, is_active FROM departments WHERE company_id=1 ORDER BY id")){
                while(r.next()) System.out.printf("id=%-3d %-25s active=%s%n",
                    r.getInt(1),r.getString(2),r.getString(3));
            }
        }
    }
}