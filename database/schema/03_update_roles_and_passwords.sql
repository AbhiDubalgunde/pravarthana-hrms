-- =====================================================================
-- Pravarthana HRMS — Supabase DB Update Script
-- Run this in Supabase SQL Editor to apply role rename + new passwords
-- Generated: 2026-02-19
-- =====================================================================

-- ─────────────────────────────────────────────────────────────────────
-- STEP 1: Rename MANAGER role to TEAM_LEAD
-- ─────────────────────────────────────────────────────────────────────
UPDATE roles
SET name        = 'TEAM_LEAD',
    description = 'Team lead access - manage their team'
WHERE name = 'MANAGER';

-- Verify:
-- SELECT id, name, description FROM roles ORDER BY id;

-- ─────────────────────────────────────────────────────────────────────
-- STEP 2: Update password hashes for all demo users
-- New passwords per user (BCrypt cost=10, unique hashes)
-- ─────────────────────────────────────────────────────────────────────

-- admin@pravarthana.com  → admin@123
UPDATE users
SET password_hash = '$2a$10$epOjs7w0vAqBzK6zXM7ac.vPsi9wGVN2RdKwouC4KK0Y6USnCxRAO'
WHERE email = 'admin@pravarthana.com';

-- hr@pravarthana.com  → headhr@123
UPDATE users
SET password_hash = '$2a$10$0zNw3EpVjscAIJF1LmW2Juk/1MnslcasY0twOrTGkON1tdB3A0xdi'
WHERE email = 'hr@pravarthana.com';

-- manager@pravarthana.com  → teamlead@123
UPDATE users
SET password_hash = '$2a$10$gpNv7HC6WyC37XUfgLguR.rQ/kRWKmUX6g.A7NdoLdJ/d0VtiF7Ru'
WHERE email = 'manager@pravarthana.com';

-- employee@pravarthana.com  → employee@123
UPDATE users
SET password_hash = '$2a$10$eVRCfw/iC/aHBCXDsIHnreGeTQHeGG7QRACyk4Zb8XW2Y9SwQSpiO'
WHERE email = 'employee@pravarthana.com';

-- john.doe@pravarthana.com  → employee@123
UPDATE users
SET password_hash = '$2a$10$tqYV9AkSyjnKSXkVKA/9f.tqpGg7D8DN2F5EXChIkdM.m2S/TgtE6'
WHERE email = 'john.doe@pravarthana.com';

-- ─────────────────────────────────────────────────────────────────────
-- STEP 3: Verify everything looks correct
-- ─────────────────────────────────────────────────────────────────────
SELECT u.email, r.name AS role, u.is_active
FROM users u
JOIN roles r ON u.role_id = r.id
ORDER BY u.id;

-- =====================================================================
-- DONE — restart the Spring Boot backend after running this script
-- =====================================================================
