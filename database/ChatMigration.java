import java.sql.*;
public class ChatMigration {
    static final String URL="jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require";
    static final String U="postgres.ubbnkuykcztnnqthtsuy",P="Pravarthana@2026!";
    public static void main(String[] a) throws Exception {
        try(Connection c=DriverManager.getConnection(URL,U,P)){
            c.setAutoCommit(false);
            try(Statement s=c.createStatement()){
                // Step 1: Add role to chat_room_members
                try{s.execute("ALTER TABLE chat_room_members ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'MEMBER'");System.out.println("1. role column: OK");}catch(Exception e){System.out.println("1. role: "+e.getMessage());}
                // Step 2: Add is_active to chat_rooms
                try{s.execute("ALTER TABLE chat_rooms ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE");System.out.println("2. is_active: OK");}catch(Exception e){System.out.println("2. is_active: "+e.getMessage());}
                // Step 3: Add message_type to chat_messages
                try{s.execute("ALTER TABLE chat_messages ADD COLUMN IF NOT EXISTS message_type VARCHAR(20) DEFAULT 'TEXT'");System.out.println("3. message_type: OK");}catch(Exception e){System.out.println("3. message_type: "+e.getMessage());}
                // Step 4: Create chat_message_reads
                try{s.execute("CREATE TABLE IF NOT EXISTS chat_message_reads (id BIGSERIAL PRIMARY KEY, message_id BIGINT NOT NULL REFERENCES chat_messages(id) ON DELETE CASCADE, user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, read_at TIMESTAMP DEFAULT NOW(), UNIQUE(message_id, user_id))");System.out.println("4. chat_message_reads: OK");}catch(Exception e){System.out.println("4. reads table: "+e.getMessage());}
                // Step 5: Backfill creator as ADMIN
                try{s.execute("INSERT INTO chat_room_members(room_id,user_id,role,joined_at) SELECT id,created_by,'ADMIN',created_at FROM chat_rooms WHERE created_by IS NOT NULL ON CONFLICT(room_id,user_id) DO UPDATE SET role='ADMIN'");System.out.println("5. backfill ADMIN: OK");}catch(Exception e){System.out.println("5. backfill: "+e.getMessage());}
                c.commit();
            }
            // Verify
            System.out.println("=== VERIFY ===");
            try(ResultSet r=c.createStatement().executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name='chat_room_members' ORDER BY ordinal_position")){
                System.out.print("chat_room_members cols: ");while(r.next())System.out.print(r.getString(1)+" ");System.out.println();
            }
            try(ResultSet r=c.createStatement().executeQuery("SELECT COUNT(*) FROM chat_room_members")){r.next();System.out.println("members: "+r.getInt(1));}
            try(ResultSet r=c.createStatement().executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_name='chat_message_reads'")){r.next();System.out.println("chat_message_reads exists: "+r.getInt(1));}
        }
    }
}