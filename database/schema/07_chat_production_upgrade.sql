-- 07_chat_production_upgrade.sql
-- Production-grade chat schema upgrade
-- Run on staging first, then production

-- 1. Add role column to chat_room_members (ADMIN or MEMBER)
ALTER TABLE chat_room_members
  ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'MEMBER';

-- 2. Add is_active to chat_rooms
ALTER TABLE chat_rooms
  ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- 3. Add message_type to chat_messages (TEXT, FILE, IMAGE)
ALTER TABLE chat_messages
  ADD COLUMN IF NOT EXISTS message_type VARCHAR(20) DEFAULT 'TEXT';

-- 4. Create chat_message_reads for per-user read receipts
CREATE TABLE IF NOT EXISTS chat_message_reads (
  id          BIGSERIAL PRIMARY KEY,
  message_id  BIGINT NOT NULL REFERENCES chat_messages(id) ON DELETE CASCADE,
  user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  read_at     TIMESTAMP DEFAULT NOW(),
  UNIQUE(message_id, user_id)
);

-- 5. Backfill: make creator of every existing room an ADMIN member
--    (skips rooms where creator is already a member)
INSERT INTO chat_room_members (room_id, user_id, role, joined_at)
SELECT id, created_by, 'ADMIN', created_at
FROM chat_rooms
WHERE created_by IS NOT NULL
ON CONFLICT (room_id, user_id) DO UPDATE SET role = 'ADMIN';

-- 6. Add UNIQUE constraint to chat_room_members if not already present
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'chat_room_members_room_id_user_id_key'
  ) THEN
    ALTER TABLE chat_room_members ADD CONSTRAINT chat_room_members_room_id_user_id_key
      UNIQUE (room_id, user_id);
  END IF;
END $$;

-- Verification
SELECT 'chat_rooms cols' AS check, column_name FROM information_schema.columns WHERE table_name='chat_rooms';
SELECT 'chat_room_members cols' AS check, column_name FROM information_schema.columns WHERE table_name='chat_room_members';
SELECT 'chat_message_reads exists' AS check, COUNT(*) FROM information_schema.tables WHERE table_name='chat_message_reads';
SELECT 'members' AS check, COUNT(*) FROM chat_room_members;
