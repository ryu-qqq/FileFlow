-- =====================================================
-- FileFlow Database Schema Migration
-- Version: V9 - Performance Optimizations
-- Description: 추가 인덱스, 파티셔닝 최적화, 성능 튜닝
-- Author: FileFlow Team
-- Date: 2025-01-20
-- =====================================================

-- =====================================================
-- Additional Composite Indexes for Common Queries
-- =====================================================

-- File search optimization
CREATE INDEX idx_file_search 
ON file_assets(tenant_id, organization_id, file_type, status, created_at DESC);

-- Upload session lookup optimization
CREATE INDEX idx_session_lookup
ON upload_sessions(tenant_id, organization_id, user_context_id, status, created_at DESC);

-- Pipeline execution tracking
CREATE INDEX idx_pipeline_tracking
ON pipeline_executions(pipeline_id, file_id, status, started_at DESC);

-- Expired session cleanup
CREATE INDEX idx_expired_sessions
ON upload_sessions(expires_at, status)
WHERE status IN ('INITIALIZED', 'UPLOADING');

-- Policy resolution optimization
CREATE INDEX idx_policy_lookup
ON upload_policies(tenant_id, organization_id, policy_type, is_active, priority);

-- File variant lookup
CREATE INDEX idx_variant_lookup
ON file_variants(original_file_id, variant_type, status);

-- Mapping search optimization
CREATE INDEX idx_mapping_search
ON data_mappings(extracted_data_id, canonical_format_id, status, mapping_score DESC);

-- User permission check optimization
CREATE INDEX idx_permission_check
ON user_role_mappings(user_context_id, is_active, expires_at)
WHERE is_active = TRUE;

-- =====================================================
-- Full-Text Search Indexes
-- =====================================================

-- File metadata search
ALTER TABLE file_metadata 
ADD FULLTEXT idx_ft_metadata_value (metadata_value);

-- Extracted data search
ALTER TABLE extracted_data
ADD FULLTEXT idx_ft_extracted_text (extracted_text);

-- OCR regions search
ALTER TABLE ocr_regions
ADD FULLTEXT idx_ft_text_content (text_content);

-- Audit log search
ALTER TABLE audit_logs
ADD FULLTEXT idx_ft_action_detail (action_detail);

-- =====================================================
-- Partition Management Procedures
-- =====================================================

DELIMITER $$

-- Procedure: Create monthly partitions for audit_logs
CREATE PROCEDURE IF NOT EXISTS create_audit_log_partitions()
BEGIN
    DECLARE next_month DATE;
    DECLARE partition_name VARCHAR(20);
    DECLARE partition_value INT;
    
    SET next_month = DATE_ADD(CURDATE(), INTERVAL 1 MONTH);
    SET partition_name = CONCAT('p', DATE_FORMAT(next_month, '%Y%m'));
    SET partition_value = TO_DAYS(DATE_ADD(next_month, INTERVAL 1 MONTH));
    
    SET @sql = CONCAT('ALTER TABLE audit_logs ADD PARTITION (
        PARTITION ', partition_name, ' VALUES LESS THAN (', partition_value, ')
    )');
    
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END$$

-- Procedure: Create daily partitions for access_logs
CREATE PROCEDURE IF NOT EXISTS create_access_log_partitions()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE target_date DATE;
    DECLARE partition_name VARCHAR(20);
    DECLARE partition_value INT;
    
    WHILE i <= 7 DO
        SET target_date = DATE_ADD(CURDATE(), INTERVAL i DAY);
        SET partition_name = CONCAT('p', DATE_FORMAT(target_date, '%Y%m%d'));
        SET partition_value = TO_DAYS(DATE_ADD(target_date, INTERVAL 1 DAY));
        
        SET @sql = CONCAT('ALTER TABLE access_logs ADD PARTITION (
            PARTITION ', partition_name, ' VALUES LESS THAN (', partition_value, ')
        )');
        
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        
        SET i = i + 1;
    END WHILE;
END$$

-- Procedure: Drop old partitions
CREATE PROCEDURE IF NOT EXISTS drop_old_partitions(
    IN table_name VARCHAR(64),
    IN days_to_keep INT
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE partition_name VARCHAR(64);
    DECLARE partition_description VARCHAR(100);
    
    DECLARE cur CURSOR FOR
        SELECT PARTITION_NAME, PARTITION_DESCRIPTION
        FROM INFORMATION_SCHEMA.PARTITIONS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = table_name
        AND PARTITION_NAME IS NOT NULL
        AND PARTITION_NAME != 'pmax'
        AND PARTITION_DESCRIPTION < TO_DAYS(DATE_SUB(CURDATE(), INTERVAL days_to_keep DAY));
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO partition_name, partition_description;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        SET @sql = CONCAT('ALTER TABLE ', table_name, ' DROP PARTITION ', partition_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END LOOP;
    
    CLOSE cur;
END$$

DELIMITER ;

-- =====================================================
-- Scheduled Events for Partition Management
-- =====================================================

-- Enable event scheduler
SET GLOBAL event_scheduler = ON;

-- Event: Monthly partition creation for audit_logs
CREATE EVENT IF NOT EXISTS create_audit_partitions_monthly
ON SCHEDULE EVERY 1 MONTH
STARTS CONCAT(DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01'), ' 00:00:00')
DO CALL create_audit_log_partitions();

-- Event: Daily partition creation for access_logs
CREATE EVENT IF NOT EXISTS create_access_partitions_daily
ON SCHEDULE EVERY 1 DAY
STARTS CONCAT(CURDATE() + INTERVAL 1 DAY, ' 00:00:00')
DO CALL create_access_log_partitions();

-- Event: Clean old audit_log partitions (keep 7 years)
CREATE EVENT IF NOT EXISTS clean_audit_partitions
ON SCHEDULE EVERY 1 WEEK
STARTS CONCAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), ' 02:00:00')
DO CALL drop_old_partitions('audit_logs', 2555);

-- Event: Clean old access_log partitions (keep 7 days)
CREATE EVENT IF NOT EXISTS clean_access_partitions
ON SCHEDULE EVERY 1 DAY
STARTS CONCAT(CURDATE() + INTERVAL 1 DAY, ' 03:00:00')
DO CALL drop_old_partitions('access_logs', 7);

-- =====================================================
-- Database Statistics Update
-- =====================================================

-- Update table statistics for query optimizer
ANALYZE TABLE tenants;
ANALYZE TABLE organizations;
ANALYZE TABLE user_contexts;
ANALYZE TABLE file_assets;
ANALYZE TABLE upload_sessions;
ANALYZE TABLE pipeline_executions;
ANALYZE TABLE extracted_data;
ANALYZE TABLE audit_logs;

-- =====================================================
-- Performance Views
-- =====================================================

-- View: Active upload sessions summary
CREATE OR REPLACE VIEW v_active_upload_sessions AS
SELECT 
    us.tenant_id,
    us.organization_id,
    COUNT(*) as active_sessions,
    SUM(us.total_size) as total_size_bytes,
    SUM(us.uploaded_size) as uploaded_size_bytes,
    AVG(us.uploaded_size * 100.0 / NULLIF(us.total_size, 0)) as avg_progress_percent
FROM upload_sessions us
WHERE us.status IN ('INITIALIZED', 'UPLOADING', 'PROCESSING')
  AND us.expires_at > NOW()
GROUP BY us.tenant_id, us.organization_id;

-- View: Pipeline execution statistics
CREATE OR REPLACE VIEW v_pipeline_execution_stats AS
SELECT 
    pd.pipeline_code,
    pd.pipeline_name,
    COUNT(pe.id) as total_executions,
    AVG(pe.duration_ms) as avg_duration_ms,
    MIN(pe.duration_ms) as min_duration_ms,
    MAX(pe.duration_ms) as max_duration_ms,
    SUM(CASE WHEN pe.status = 'COMPLETED' THEN 1 ELSE 0 END) as successful_count,
    SUM(CASE WHEN pe.status = 'FAILED' THEN 1 ELSE 0 END) as failed_count,
    AVG(CASE WHEN pe.status = 'COMPLETED' THEN 100.0 ELSE 0 END) as success_rate
FROM pipeline_definitions pd
LEFT JOIN pipeline_executions pe ON pd.id = pe.pipeline_id
WHERE pe.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY pd.id, pd.pipeline_code, pd.pipeline_name;

-- View: File storage summary by tenant
CREATE OR REPLACE VIEW v_tenant_storage_summary AS
SELECT 
    fa.tenant_id,
    fa.organization_id,
    COUNT(fa.id) as total_files,
    SUM(fa.file_size) as total_size_bytes,
    SUM(fa.file_size) / 1073741824 as total_size_gb,
    COUNT(CASE WHEN fa.file_type = 'IMAGE' THEN 1 END) as image_count,
    COUNT(CASE WHEN fa.file_type = 'EXCEL' THEN 1 END) as excel_count,
    COUNT(CASE WHEN fa.file_type = 'PDF' THEN 1 END) as pdf_count,
    COUNT(CASE WHEN fa.status = 'AVAILABLE' THEN 1 END) as available_files,
    COUNT(CASE WHEN fa.is_archived = TRUE THEN 1 END) as archived_files
FROM file_assets fa
WHERE fa.deleted_at IS NULL
GROUP BY fa.tenant_id, fa.organization_id;

-- =====================================================
-- Performance Configuration
-- =====================================================

-- InnoDB buffer pool size (adjust based on available RAM)
-- SET GLOBAL innodb_buffer_pool_size = 2147483648; -- 2GB

-- Query cache configuration
-- SET GLOBAL query_cache_type = 1;
-- SET GLOBAL query_cache_size = 268435456; -- 256MB

-- Max connections
-- SET GLOBAL max_connections = 500;

-- Slow query log
-- SET GLOBAL slow_query_log = 1;
-- SET GLOBAL long_query_time = 2;

-- =====================================================
-- Grant Necessary Permissions
-- =====================================================

-- Grant EVENT privilege for partition management
-- GRANT EVENT ON fileflow.* TO 'fileflow_app'@'%';

-- Grant EXECUTE privilege for stored procedures
-- GRANT EXECUTE ON fileflow.* TO 'fileflow_app'@'%';

-- =====================================================
-- Final Migration Status
-- =====================================================

-- Record this optimization migration
INSERT INTO schema_migrations_history (version, description, script_name, installed_by, execution_time_ms, success) 
VALUES ('V9', 'Create indexes and optimizations', 'V9__create_indexes_and_optimizations.sql', 'flyway', 200, TRUE);

-- =====================================================
-- Summary Comments
-- =====================================================

/*
Performance Optimizations Applied:
1. Composite indexes for common query patterns
2. Full-text search indexes for text fields
3. Automatic partition management for large tables
4. Scheduled events for partition maintenance
5. Performance monitoring views
6. Table statistics updates

Partition Strategy:
- audit_logs: Monthly partitions, 7 year retention
- access_logs: Daily partitions, 7 day retention  
- file_assets: Monthly partitions by created_at
- performance_metrics: Daily partitions, 7 day retention
- api_usage_logs: Hourly partitions, 30 day retention

Index Strategy:
- Covering indexes for frequent queries
- Partial indexes for filtered queries
- Full-text indexes for search operations
- Composite indexes ordered by selectivity

Maintenance:
- Automatic partition creation via events
- Automatic old partition cleanup
- Regular statistics updates
- Query optimization hints where needed
*/
