-- =====================================================
-- TTODO-API Nickname Refactoring - Phase 3: Performance Optimization
-- =====================================================

BEGIN;

-- Add indexes for optimized queries
CREATE INDEX IF NOT EXISTS idx_profiles_owner_id_nickname 
    ON profiles (owner_id, nickname);

-- Add composite index for user+profile queries
CREATE INDEX IF NOT EXISTS idx_user_profile_join 
    ON profiles (owner_id) 
    INCLUDE (nickname, time_zone, locale);

-- Log index creation
INSERT INTO migration_log (operation, details)
VALUES ('performance_indexes_added', json_build_object(
    'indexes', json_build_array(
        'idx_profiles_owner_id_nickname',
        'idx_user_profile_join'
    )
));

COMMIT;