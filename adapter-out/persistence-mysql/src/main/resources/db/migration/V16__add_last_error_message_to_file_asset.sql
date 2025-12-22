-- V16: Add last_error_message column to file_asset table
-- Purpose: Store detailed error messages when file processing fails
-- This enables debugging by preserving the actual exception message at the time of failure

ALTER TABLE file_asset
    ADD COLUMN last_error_message VARCHAR(2000) NULL COMMENT 'Last error message when processing failed';

-- Add index for querying failed assets with errors
CREATE INDEX idx_file_asset_status_error ON file_asset (status, last_error_message(100));
