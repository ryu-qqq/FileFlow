-- =====================================================
-- Migration: V3__add_failed_at_column.sql
-- Description: Add file_id, failed_at, and failure_reason columns to upload_session table
-- =====================================================
-- Add file_id to link session to file
-- Add failed_at timestamp column to track session failure time
-- Add failure_reason text column to store failure details

ALTER TABLE upload_session
ADD COLUMN file_id BIGINT NULL COMMENT 'File ID reference',
ADD COLUMN failed_at DATETIME NULL COMMENT 'Session failure timestamp',
ADD COLUMN failure_reason TEXT NULL COMMENT 'Failure reason and details';
