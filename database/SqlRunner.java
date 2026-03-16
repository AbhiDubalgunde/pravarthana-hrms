import java.sql.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SqlRunner {
    static final String URL = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
    static final String USER = "postgres.ubbnkuykcztnnqthtsuy";
    static final String PASS = "Pravarthana@2026!";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java SqlRunner <script.sql>");
            System.exit(1);
        }

        String sqlContent = Files.readString(Paths.get(args[0]));
        // Simple regex to split by ';' outside of DO $$ blocks
        // For simplicity since we have DO $$ ... END $$ blocks, it's safer to execute
        // as one giant string if the driver allows it.
        // PostgreSQL JDBC driver allows executing multiple statements separated by ';'
        // in one step.

        System.out.println("Executing " + args[0] + " ...");
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                Statement st = conn.createStatement()) {

            // Execute the entire script
            st.execute(sqlContent);
            System.out.println("✅ Script executed successfully.");
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
