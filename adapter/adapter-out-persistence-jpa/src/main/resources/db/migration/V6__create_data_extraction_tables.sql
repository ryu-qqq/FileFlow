-- =====================================================
-- FileFlow Database Schema Migration
-- Version: V6 - Data Extraction Tables
-- Description: OCR, AI 분석, 데이터 매핑 관련 테이블 생성
-- Author: FileFlow Team
-- Date: 2025-01-20
-- =====================================================

-- =====================================================
-- Table: extracted_data (추출 데이터)
-- Description: 파일로부터 추출된 모든 데이터 저장
-- =====================================================
CREATE TABLE IF NOT EXISTS extracted_data (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '추출 데이터 ID',
    extraction_id           VARCHAR(36)     NOT NULL COMMENT '추출 고유 식별자',
    file_id                 BIGINT          NOT NULL COMMENT '원본 파일 ID',
    execution_id            BIGINT          NULL COMMENT '파이프라인 실행 ID',
    extraction_type         ENUM('OCR', 'AI_ANALYSIS', 'METADATA', 'STRUCTURED_DATA', 'NLP', 'CUSTOM') NOT NULL COMMENT '추출 타입',
    extraction_method       VARCHAR(100)    NOT NULL COMMENT '추출 방법/엔진',
    source_type             ENUM('IMAGE', 'HTML', 'PDF', 'EXCEL', 'TEXT') NOT NULL COMMENT '소스 타입',
    extracted_content       JSON            NULL COMMENT '추출된 콘텐츠 (JSON)',
    extracted_text          LONGTEXT        NULL COMMENT '추출된 텍스트',
    structured_data         JSON            NULL COMMENT '구조화된 데이터',
    confidence_score        DECIMAL(5,4)    NULL COMMENT '신뢰도 점수 (0.0000-1.0000)',
    quality_score           DECIMAL(5,4)    NULL COMMENT '품질 점수',
    language                VARCHAR(10)     NULL COMMENT '감지된 언어',
    encoding                VARCHAR(50)     NULL DEFAULT 'UTF-8' COMMENT '인코딩',
    word_count              INT             NULL COMMENT '단어 수',
    character_count         INT             NULL COMMENT '문자 수',
    processing_time_ms      INT             NULL COMMENT '처리 시간 (밀리초)',
    extraction_config       JSON            NULL DEFAULT ('{}') COMMENT '추출 설정',
    validation_status       ENUM('PENDING', 'VALIDATED', 'INVALID', 'MANUAL_REVIEW') NULL DEFAULT 'PENDING' COMMENT '검증 상태',
    validation_errors       JSON            NULL COMMENT '검증 오류',
    metadata                JSON            NULL DEFAULT ('{}') COMMENT '추가 메타데이터',
    version                 INT             NOT NULL DEFAULT 1 COMMENT '버전',
    extracted_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '추출 시각',
    validated_at            DATETIME        NULL COMMENT '검증 시각',
    validated_by            BIGINT          NULL COMMENT '검증자 ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_extraction_id (extraction_id),
    INDEX idx_file_id (file_id, extraction_type),
    INDEX idx_execution_id (execution_id),
    INDEX idx_extraction_type (extraction_type, extracted_at DESC),
    INDEX idx_confidence_score (confidence_score DESC),
    INDEX idx_validation_status (validation_status),
    INDEX idx_extracted_at (extracted_at DESC),
    FULLTEXT idx_extracted_text (extracted_text)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='추출 데이터';

-- =====================================================
-- Table: canonical_formats (표준 포맷)
-- Description: 데이터 매핑의 대상이 되는 표준 포맷 정의
-- =====================================================
CREATE TABLE IF NOT EXISTS canonical_formats (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '포맷 ID',
    format_code             VARCHAR(50)     NOT NULL COMMENT '포맷 코드',
    format_name             VARCHAR(100)    NOT NULL COMMENT '포맷명',
    domain_type             ENUM('PRODUCT', 'ORDER', 'INVENTORY', 'CUSTOMER', 'INVOICE', 'CUSTOM') NOT NULL COMMENT '도메인 타입',
    description             TEXT            NULL COMMENT '포맷 설명',
    schema_definition       JSON            NOT NULL COMMENT '스키마 정의',
    field_definitions       JSON            NOT NULL COMMENT '필드 정의',
    validation_rules        JSON            NULL DEFAULT ('{}') COMMENT '검증 규칙',
    transformation_rules    JSON            NULL DEFAULT ('{}') COMMENT '변환 규칙',
    sample_data             JSON            NULL COMMENT '샘플 데이터',
    version                 INT             NOT NULL DEFAULT 1 COMMENT '버전',
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '활성화 상태',
    parent_format_id        BIGINT          NULL COMMENT '부모 포맷 ID (상속)',
    tags                    JSON            NULL DEFAULT ('[]') COMMENT '태그',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    created_by              VARCHAR(100)    NULL COMMENT '생성자',
    PRIMARY KEY (id),
    UNIQUE KEY uk_format_code_version (format_code, version),
    INDEX idx_domain_type (domain_type, is_active),
    INDEX idx_is_active (is_active),
    INDEX idx_parent_format (parent_format_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='표준 포맷';

-- =====================================================
-- Table: data_mappings (데이터 매핑)
-- Description: 추출된 데이터를 표준 포맷으로 매핑
-- =====================================================
CREATE TABLE IF NOT EXISTS data_mappings (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '매핑 ID',
    mapping_id              VARCHAR(36)     NOT NULL COMMENT '매핑 고유 식별자',
    extracted_data_id       BIGINT          NOT NULL COMMENT '추출 데이터 ID',
    canonical_format_id     BIGINT          NOT NULL COMMENT '표준 포맷 ID',
    mapping_type            ENUM('AUTO', 'AI', 'RULE_BASED', 'MANUAL', 'HYBRID') NOT NULL COMMENT '매핑 타입',
    mapping_method          VARCHAR(100)    NULL COMMENT '매핑 방법',
    source_fields           JSON            NOT NULL COMMENT '소스 필드 목록',
    mapped_fields           JSON            NOT NULL COMMENT '매핑된 필드',
    field_mappings          JSON            NOT NULL COMMENT '필드별 상세 매핑',
    transformation_log      JSON            NULL COMMENT '변환 로그',
    mapping_score           DECIMAL(5,4)    NULL COMMENT '매핑 점수 (0.0000-1.0000)',
    confidence_scores       JSON            NULL COMMENT '필드별 신뢰도 점수',
    validation_results      JSON            NULL COMMENT '검증 결과',
    status                  ENUM('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'REJECTED', 'ACTIVE', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT' COMMENT '매핑 상태',
    error_fields            JSON            NULL COMMENT '오류 필드 목록',
    warning_fields          JSON            NULL COMMENT '경고 필드 목록',
    review_notes            TEXT            NULL COMMENT '검토 노트',
    approved_by             BIGINT          NULL COMMENT '승인자 ID',
    approved_at             DATETIME        NULL COMMENT '승인 시각',
    metadata                JSON            NULL DEFAULT ('{}') COMMENT '추가 메타데이터',
    version                 INT             NOT NULL DEFAULT 1 COMMENT '버전',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_mapping_id (mapping_id),
    INDEX idx_extracted_data (extracted_data_id),
    INDEX idx_canonical_format (canonical_format_id),
    INDEX idx_mapping_type (mapping_type, status),
    INDEX idx_status (status, created_at DESC),
    INDEX idx_mapping_score (mapping_score DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='데이터 매핑';

-- =====================================================
-- Table: mapping_rules (매핑 규칙)
-- Description: 데이터 매핑을 위한 사전 정의된 규칙
-- =====================================================
CREATE TABLE IF NOT EXISTS mapping_rules (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '규칙 ID',
    rule_code               VARCHAR(50)     NOT NULL COMMENT '규칙 코드',
    rule_name               VARCHAR(100)    NOT NULL COMMENT '규칙명',
    canonical_format_id     BIGINT          NOT NULL COMMENT '대상 표준 포맷 ID',
    source_pattern          JSON            NOT NULL COMMENT '소스 패턴 정의',
    rule_type               ENUM('EXACT', 'PATTERN', 'SEMANTIC', 'CONDITIONAL', 'CUSTOM') NOT NULL COMMENT '규칙 타입',
    mapping_definition      JSON            NOT NULL COMMENT '매핑 정의',
    transformation_script   TEXT            NULL COMMENT '변환 스크립트',
    validation_script       TEXT            NULL COMMENT '검증 스크립트',
    priority                INT             NOT NULL DEFAULT 100 COMMENT '우선순위',
    conditions              JSON            NULL COMMENT '적용 조건',
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '활성화 상태',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_rule_code (rule_code),
    INDEX idx_canonical_format (canonical_format_id, is_active),
    INDEX idx_rule_type (rule_type),
    INDEX idx_priority (priority, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='매핑 규칙';

-- =====================================================
-- Table: ai_training_data (AI 학습 데이터)
-- Description: AI 매핑 모델 학습을 위한 데이터
-- =====================================================
CREATE TABLE IF NOT EXISTS ai_training_data (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '학습 데이터 ID',
    mapping_id              BIGINT          NOT NULL COMMENT '원본 매핑 ID',
    source_schema           JSON            NOT NULL COMMENT '소스 스키마',
    target_schema           JSON            NOT NULL COMMENT '타겟 스키마',
    mapping_result          JSON            NOT NULL COMMENT '매핑 결과',
    quality_score           DECIMAL(5,4)    NOT NULL COMMENT '품질 점수',
    is_validated            BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '검증 완료 여부',
    is_used_for_training    BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '학습 사용 여부',
    model_version           VARCHAR(20)     NULL COMMENT '모델 버전',
    feedback_score          INT             NULL COMMENT '피드백 점수 (1-5)',
    feedback_notes          TEXT            NULL COMMENT '피드백 노트',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    INDEX idx_mapping_id (mapping_id),
    INDEX idx_quality_score (quality_score DESC),
    INDEX idx_is_validated (is_validated, is_used_for_training),
    INDEX idx_model_version (model_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 학습 데이터';

-- =====================================================
-- Table: extracted_entities (추출된 엔티티)
-- Description: 텍스트에서 추출된 명명된 엔티티
-- =====================================================
CREATE TABLE IF NOT EXISTS extracted_entities (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '엔티티 ID',
    extracted_data_id       BIGINT          NOT NULL COMMENT '추출 데이터 ID',
    entity_type             VARCHAR(50)     NOT NULL COMMENT '엔티티 타입 (PRODUCT, BRAND, PRICE 등)',
    entity_value            TEXT            NOT NULL COMMENT '엔티티 값',
    normalized_value        TEXT            NULL COMMENT '정규화된 값',
    position_start          INT             NULL COMMENT '시작 위치',
    position_end            INT             NULL COMMENT '종료 위치',
    confidence_score        DECIMAL(5,4)    NULL COMMENT '신뢰도 점수',
    context                 TEXT            NULL COMMENT '주변 컨텍스트',
    attributes              JSON            NULL DEFAULT ('{}') COMMENT '엔티티 속성',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    INDEX idx_extracted_data (extracted_data_id, entity_type),
    INDEX idx_entity_type (entity_type),
    INDEX idx_entity_value (entity_value(100)),
    INDEX idx_confidence_score (confidence_score DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='추출된 엔티티';

-- =====================================================
-- Table: ocr_regions (OCR 영역)
-- Description: 이미지에서 OCR이 수행된 영역 정보
-- =====================================================
CREATE TABLE IF NOT EXISTS ocr_regions (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '영역 ID',
    extracted_data_id       BIGINT          NOT NULL COMMENT '추출 데이터 ID',
    region_index            INT             NOT NULL COMMENT '영역 인덱스',
    bounding_box            JSON            NOT NULL COMMENT '바운딩 박스 좌표',
    text_content            TEXT            NOT NULL COMMENT '추출된 텍스트',
    confidence_score        DECIMAL(5,4)    NULL COMMENT '신뢰도 점수',
    language                VARCHAR(10)     NULL COMMENT '감지된 언어',
    text_direction          ENUM('LTR', 'RTL', 'TTB', 'BTT') NULL DEFAULT 'LTR' COMMENT '텍스트 방향',
    font_info               JSON            NULL COMMENT '폰트 정보',
    is_handwritten          BOOLEAN         NULL DEFAULT FALSE COMMENT '수기 여부',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    INDEX idx_extracted_data (extracted_data_id, region_index),
    INDEX idx_confidence_score (confidence_score DESC),
    FULLTEXT idx_text_content (text_content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OCR 영역';

-- =====================================================
-- Sample Data
-- =====================================================

-- Insert sample canonical formats
INSERT INTO canonical_formats (format_code, format_name, domain_type, schema_definition, field_definitions) VALUES
('PRODUCT_V2', '상품 정보 v2', 'PRODUCT', 
 '{"type": "object", "required": ["sku", "name", "price"]}',
 '{"sku": {"type": "string", "maxLength": 50}, "name": {"type": "string", "maxLength": 200}, "price": {"type": "number", "minimum": 0}}'),
('ORDER_V1', '주문 정보 v1', 'ORDER',
 '{"type": "object", "required": ["order_no", "customer_id", "items"]}',
 '{"order_no": {"type": "string"}, "customer_id": {"type": "string"}, "items": {"type": "array"}}'),
('INVENTORY_V1', '재고 정보 v1', 'INVENTORY',
 '{"type": "object", "required": ["sku", "quantity", "warehouse"]}',
 '{"sku": {"type": "string"}, "quantity": {"type": "integer"}, "warehouse": {"type": "string"}}');

-- Insert sample extracted data
INSERT INTO extracted_data (extraction_id, file_id, extraction_type, extraction_method, source_type, extracted_text, confidence_score) VALUES
(UUID(), 1, 'OCR', 'tesseract_v4', 'HTML', '상품명: 프리미엄 가죽 자켓\n소재: 천연 소가죽 100%\n사이즈: S, M, L, XL', 0.9234),
(UUID(), 2, 'AI_ANALYSIS', 'gpt-4-vision', 'EXCEL', NULL, 0.8756),
(UUID(), 3, 'METADATA', 'pdfbox', 'PDF', NULL, 1.0000);
