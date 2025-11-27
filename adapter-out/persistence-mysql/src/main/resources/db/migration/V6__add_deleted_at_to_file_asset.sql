-- ========================================
-- V6: Add deleted_at column to file_asset table
-- ========================================
-- Soft delete 지원을 위한 deleted_at 컬럼 추가
-- FileAssetJpaEntity의 @Column(name = "deleted_at") 매핑에 대응

ALTER TABLE file_asset
    ADD COLUMN deleted_at TIMESTAMP NULL AFTER updated_at;

-- 삭제되지 않은(활성) 레코드 조회 최적화를 위한 인덱스
CREATE INDEX idx_file_asset_deleted_at ON file_asset (deleted_at);
