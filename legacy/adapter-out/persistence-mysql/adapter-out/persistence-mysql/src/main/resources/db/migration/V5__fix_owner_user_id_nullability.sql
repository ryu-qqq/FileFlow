-- =====================================================
-- Migration: V5__fix_owner_user_id_nullability.sql
-- Description: Allow NULL for owner_user_id to support anonymous uploads and external downloads
-- =====================================================
-- Change owner_user_id from NOT NULL to NULL to support:
-- 1. Anonymous uploads (no authenticated user)
-- 2. External downloads (system-initiated downloads)
-- 3. Batch processing scenarios

ALTER TABLE file_assets MODIFY owner_user_id BIGINT NULL COMMENT 'Owner User ID (nullable for anonymous uploads and external downloads)';
