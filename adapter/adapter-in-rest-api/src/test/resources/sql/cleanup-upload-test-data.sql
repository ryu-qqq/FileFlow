-- ========================================
-- Cleanup test data for upload integration tests
-- ========================================
-- Clean up in reverse order of dependencies
-- ========================================

-- Delete file relationships
DELETE FROM file_relationship WHERE TRUE;

-- Delete file metadata
DELETE FROM file_metadata WHERE TRUE;

-- Delete file assets
DELETE FROM file_asset WHERE TRUE;

-- Delete upload sessions
DELETE FROM upload_session WHERE TRUE;

-- Delete policy change logs
DELETE FROM policy_change_log WHERE TRUE;

-- NOTE: Keep upload_policy, processing_policy, and tenant tables
-- as they are populated by Flyway migrations V5
