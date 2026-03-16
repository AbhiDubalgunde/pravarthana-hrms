import java.sql.*;
public class FixChatCols {
    static final String URL="jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0";
    static final String U="postgres.ubbnkuykcztnnqthtsuy",P="Pravarthana@2026!";
    public static void main(String[] a) throws Exception {
        try(Connection c=DriverManager.getConnection(URL,U,P)){
            c.setAutoCommit(true);
            String[][] cmds = {
                {"is_active on chat_rooms","ALTER TABLE chat_rooms ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE"},
                {"message_type on chat_messages","ALTER TABLE chat_messages ADD COLUMN IF NOT EXISTS message_type VARCHAR(20) DEFAULT 'TEXT'"},
                {"role on chat_room_members","ALTER TABLE chat_room_members ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'MEMBER'"},
                {"chat_message_reads","CREATE TABLE IF NOT EXISTS chat_message_reads (id BIGSERIAL PRIMARY KEY, message_id BIGINT NOT NULL REFERENCES chat_messages(id) ON DELETE CASCADE, user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, read_at TIMESTAMP DEFAULT NOW(), UNIQUE(message_id,user_id))"},
                {"backfill ADMIN","INSERT INTO chat_room_members(room_id,user_id,role,joined_at) SELECT id,created_by,'ADMIN',created_at FROM chat_rooms WHERE created_by IS NOT NULL ON CONFLICT(room_id,user_id) DO UPDATE SET role='ADMIN'"}
            };
            for(String[] cmd:cmds){
                try(Statement s=c.createStatement()){
                    s.execute(cmd[1]);
                    System.out.println("OK: "+cmd[0]);
                }catch(Exception e){System.out.println("SKIP "+cmd[0]+": "+e.getMessage());}
            }
            // verify
            try(ResultSet r=c.createStatement().executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name='chat_rooms' ORDER BY ordinal_position")){
                System.out.print("chat_rooms: ");while(r.next())System.out.print(r.getString(1)+" ");System.out.println();
            }
            try(ResultSet r=c.createStatement().executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name='chat_room_members' ORDER BY ordinal_position")){
                System.out.print("chat_room_members: ");while(r.next())System.out.print(r.getString(1)+" ");System.out.println();
            }
        }
    }
}