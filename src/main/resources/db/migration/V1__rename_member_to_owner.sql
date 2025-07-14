-- Migration script to rename member_id columns to owner_id across all tables

-- Rename column in category table
ALTER TABLE category RENAME COLUMN member_id TO owner_id;

-- Rename column in todo table
ALTER TABLE todo RENAME COLUMN member_id TO owner_id;

-- Rename column in todo_original table
ALTER TABLE todo_original RENAME COLUMN member_id TO owner_id;

-- Update any indexes that reference member_id
-- Note: The exact index names may vary depending on your database
-- You may need to adjust these based on your actual schema

-- For PostgreSQL:
-- ALTER INDEX idx_category_member_id RENAME TO idx_category_owner_id;
-- ALTER INDEX idx_todo_member_id RENAME TO idx_todo_owner_id;
-- ALTER INDEX idx_todo_original_member_id RENAME TO idx_todo_original_owner_id;

-- For MySQL:
-- ALTER TABLE category DROP INDEX idx_category_member_id, ADD INDEX idx_category_owner_id (owner_id);
-- ALTER TABLE todo DROP INDEX idx_todo_member_id, ADD INDEX idx_todo_owner_id (owner_id);
-- ALTER TABLE todo_original DROP INDEX idx_todo_original_member_id, ADD INDEX idx_todo_original_owner_id (owner_id);