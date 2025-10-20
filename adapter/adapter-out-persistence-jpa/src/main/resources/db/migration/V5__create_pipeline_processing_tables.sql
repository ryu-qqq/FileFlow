-- =====================================================
-- FileFlow Database Schema Migration
-- Version: V5 - Pipeline Processing Tables
-- Description: 파일 처리 파이프라인 관련 테이블 생성
-- Author: FileFlow Team
-- Date: 2025-01-20
-- =====================================================

-- =====================================================
-- Table: pipeline_definitions (파이프라인 정의)
-- Description: 파일 처리 파이프라인 정의
-- =====================================================
CREATE TABLE IF NOT EXISTS pipeline_definitions (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '파이프라인 ID',
    pipeline_code           VARCHAR(50)     NOT NULL COMMENT '파이프라인 코드',
    pipeline_name           VARCHAR(100)    NOT NULL COMMENT '파이프라인명',
    description             TEXT            NULL COMMENT '파이프라인 설명',
    file_type               ENUM('IMAGE', 'HTML', 'PDF', 'EXCEL', 'ANY') NOT NULL COMMENT '대상 파일 타입',
    pipeline_type           ENUM('SYNC', 'ASYNC', 'BATCH', 'SCHEDULED') NOT NULL DEFAULT 'ASYNC' COMMENT '실행 타입',
    trigger_conditions      JSON            NULL DEFAULT ('{}') COMMENT '트리거 조건',
    configuration           JSON            NOT NULL DEFAULT ('{}') COMMENT '파이프라인 설정',
    default_params          JSON            NULL DEFAULT ('{}') COMMENT '기본 파라미터',
    max_file_size           BIGINT          NULL COMMENT '최대 처리 파일 크기',
    timeout_seconds         INT             NOT NULL DEFAULT 300 COMMENT '타임아웃 (초)',
    max_retries             INT             NOT NULL DEFAULT 3 COMMENT '최대 재시도 횟수',
    retry_delay_seconds     INT             NOT NULL DEFAULT 60 COMMENT '재시도 지연 (초)',
    priority                INT             NOT NULL DEFAULT 100 COMMENT '우선순위 (낮을수록 높음)',
    parallelism             INT             NOT NULL DEFAULT 1 COMMENT '병렬 처리 수',
    resource_requirements   JSON            NULL DEFAULT ('{}') COMMENT '리소스 요구사항',
    dependencies            JSON            NULL DEFAULT ('[]') COMMENT '의존 파이프라인',
    success_actions         JSON            NULL DEFAULT ('[]') COMMENT '성공 시 액션',
    failure_actions         JSON            NULL DEFAULT ('[]') COMMENT '실패 시 액션',
    notification_config     JSON            NULL DEFAULT ('{}') COMMENT '알림 설정',
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '활성화 상태',
    version                 INT             NOT NULL DEFAULT 1 COMMENT '버전',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    created_by              VARCHAR(100)    NULL COMMENT '생성자',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pipeline_code (pipeline_code),
    INDEX idx_file_type (file_type, is_active),
    INDEX idx_pipeline_type (pipeline_type),
    INDEX idx_priority (priority, is_active),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파이프라인 정의';

-- =====================================================
-- Table: pipeline_stages (파이프라인 단계)
-- Description: 파이프라인의 개별 처리 단계
-- =====================================================
CREATE TABLE IF NOT EXISTS pipeline_stages (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '단계 ID',
    pipeline_id             BIGINT          NOT NULL COMMENT '파이프라인 ID',
    stage_code              VARCHAR(50)     NOT NULL COMMENT '단계 코드',
    stage_name              VARCHAR(100)    NOT NULL COMMENT '단계명',
    description             TEXT            NULL COMMENT '단계 설명',
    sequence_order          INT             NOT NULL COMMENT '실행 순서',
    processor_type          VARCHAR(100)    NOT NULL COMMENT '처리기 타입',
    processor_config        JSON            NOT NULL DEFAULT ('{}') COMMENT '처리기 설정',
    input_validation        JSON            NULL COMMENT '입력 검증 규칙',
    output_validation       JSON            NULL COMMENT '출력 검증 규칙',
    is_optional             BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '선택적 실행 여부',
    is_parallel             BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '병렬 실행 가능 여부',
    condition               JSON            NULL COMMENT '실행 조건',
    timeout_seconds         INT             NOT NULL DEFAULT 60 COMMENT '타임아웃 (초)',
    max_retries             INT             NOT NULL DEFAULT 3 COMMENT '최대 재시도 횟수',
    retry_strategy          ENUM('IMMEDIATE', 'LINEAR', 'EXPONENTIAL') NOT NULL DEFAULT 'EXPONENTIAL' COMMENT '재시도 전략',
    on_failure              ENUM('FAIL', 'SKIP', 'CONTINUE', 'RETRY') NOT NULL DEFAULT 'FAIL' COMMENT '실패 시 동작',
    resource_allocation     JSON            NULL COMMENT '리소스 할당',
    metrics_config          JSON            NULL COMMENT '메트릭 설정',
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '활성화 상태',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pipeline_stage (pipeline_id, stage_code),
    INDEX idx_pipeline_id (pipeline_id, sequence_order),
    INDEX idx_processor_type (processor_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파이프라인 단계';

-- =====================================================
-- Table: pipeline_executions (파이프라인 실행)
-- Description: 파이프라인 실행 인스턴스 추적
-- =====================================================
CREATE TABLE IF NOT EXISTS pipeline_executions (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '실행 ID',
    execution_id            VARCHAR(36)     NOT NULL COMMENT '실행 고유 식별자',
    pipeline_id             BIGINT          NOT NULL COMMENT '파이프라인 ID',
    file_id                 BIGINT          NOT NULL COMMENT '파일 ID',
    parent_execution_id     BIGINT          NULL COMMENT '부모 실행 ID (중첩 실행)',
    trigger_type            ENUM('UPLOAD', 'MANUAL', 'SCHEDULED', 'DEPENDENCY', 'RETRY') NOT NULL COMMENT '트리거 타입',
    status                  ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED', 'TIMEOUT') NOT NULL DEFAULT 'PENDING' COMMENT '실행 상태',
    current_stage_id        BIGINT          NULL COMMENT '현재 실행 중인 단계',
    total_stages            INT             NOT NULL DEFAULT 0 COMMENT '전체 단계 수',
    completed_stages        INT             NOT NULL DEFAULT 0 COMMENT '완료된 단계 수',
    input_params            JSON            NULL DEFAULT ('{}') COMMENT '입력 파라미터',
    output_results          JSON            NULL COMMENT '출력 결과',
    execution_context       JSON            NULL DEFAULT ('{}') COMMENT '실행 컨텍스트',
    error_message           TEXT            NULL COMMENT '에러 메시지',
    error_code              VARCHAR(50)     NULL COMMENT '에러 코드',
    error_stage_id          BIGINT          NULL COMMENT '에러 발생 단계',
    retry_count             INT             NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    worker_id               VARCHAR(100)    NULL COMMENT '워커 ID',
    resource_usage          JSON            NULL COMMENT '리소스 사용량',
    performance_metrics     JSON            NULL COMMENT '성능 메트릭',
    started_at              DATETIME        NULL COMMENT '시작 시각',
    completed_at            DATETIME        NULL COMMENT '완료 시각',
    duration_ms             INT             NULL COMMENT '실행 시간 (밀리초)',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_execution_id (execution_id),
    INDEX idx_pipeline_id (pipeline_id, status, created_at DESC),
    INDEX idx_file_id (file_id, created_at DESC),
    INDEX idx_status (status, created_at DESC),
    INDEX idx_trigger_type (trigger_type, status),
    INDEX idx_parent_execution (parent_execution_id),
    INDEX idx_worker_id (worker_id, status),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파이프라인 실행';

-- =====================================================
-- Table: pipeline_stage_logs (파이프라인 단계 로그)
-- Description: 각 파이프라인 단계의 실행 로그
-- =====================================================
CREATE TABLE IF NOT EXISTS pipeline_stage_logs (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '로그 ID',
    execution_id            BIGINT          NOT NULL COMMENT '실행 ID',
    stage_id                BIGINT          NOT NULL COMMENT '단계 ID',
    stage_execution_id      VARCHAR(36)     NOT NULL COMMENT '단계 실행 ID',
    status                  ENUM('STARTED', 'RUNNING', 'COMPLETED', 'FAILED', 'SKIPPED', 'TIMEOUT') NOT NULL DEFAULT 'STARTED' COMMENT '단계 상태',
    input_data              JSON            NULL COMMENT '입력 데이터',
    output_data             JSON            NULL COMMENT '출력 데이터',
    stage_context           JSON            NULL COMMENT '단계 컨텍스트',
    log_entries             JSON            NULL DEFAULT ('[]') COMMENT '로그 항목들',
    error_message           TEXT            NULL COMMENT '에러 메시지',
    error_stack_trace       TEXT            NULL COMMENT '에러 스택 트레이스',
    retry_count             INT             NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    resource_usage          JSON            NULL COMMENT '리소스 사용량',
    performance_metrics     JSON            NULL COMMENT '성능 메트릭',
    started_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '시작 시각',
    completed_at            DATETIME        NULL COMMENT '완료 시각',
    duration_ms             INT             NULL COMMENT '실행 시간 (밀리초)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stage_execution_id (stage_execution_id),
    INDEX idx_execution_id (execution_id, started_at),
    INDEX idx_stage_id (stage_id, status),
    INDEX idx_status (status, started_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파이프라인 단계 로그';

-- =====================================================
-- Table: pipeline_templates (파이프라인 템플릿)
-- Description: 재사용 가능한 파이프라인 템플릿
-- =====================================================
CREATE TABLE IF NOT EXISTS pipeline_templates (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '템플릿 ID',
    template_code           VARCHAR(50)     NOT NULL COMMENT '템플릿 코드',
    template_name           VARCHAR(100)    NOT NULL COMMENT '템플릿명',
    description             TEXT            NULL COMMENT '템플릿 설명',
    category                VARCHAR(50)     NOT NULL COMMENT '카테고리',
    template_definition     JSON            NOT NULL COMMENT '템플릿 정의',
    parameters_schema       JSON            NULL COMMENT '파라미터 스키마',
    tags                    JSON            NULL DEFAULT ('[]') COMMENT '태그',
    is_public               BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '공개 여부',
    version                 VARCHAR(20)     NOT NULL DEFAULT '1.0.0' COMMENT '버전',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    created_by              VARCHAR(100)    NULL COMMENT '생성자',
    PRIMARY KEY (id),
    UNIQUE KEY uk_template_code_version (template_code, version),
    INDEX idx_category (category),
    INDEX idx_is_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파이프라인 템플릿';

-- =====================================================
-- Table: pipeline_schedules (파이프라인 스케줄)
-- Description: 정기적으로 실행되는 파이프라인 스케줄
-- =====================================================
CREATE TABLE IF NOT EXISTS pipeline_schedules (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '스케줄 ID',
    schedule_name           VARCHAR(100)    NOT NULL COMMENT '스케줄명',
    pipeline_id             BIGINT          NOT NULL COMMENT '파이프라인 ID',
    schedule_type           ENUM('CRON', 'INTERVAL', 'ONCE') NOT NULL COMMENT '스케줄 타입',
    cron_expression         VARCHAR(100)    NULL COMMENT 'Cron 표현식',
    interval_seconds        INT             NULL COMMENT '인터벌 (초)',
    target_filter           JSON            NULL COMMENT '대상 파일 필터',
    execution_params        JSON            NULL DEFAULT ('{}') COMMENT '실행 파라미터',
    timezone                VARCHAR(50)     NOT NULL DEFAULT 'UTC' COMMENT '타임존',
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '활성화 상태',
    last_execution_at       DATETIME        NULL COMMENT '마지막 실행 시각',
    next_execution_at       DATETIME        NULL COMMENT '다음 실행 시각',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (id),
    INDEX idx_pipeline_id (pipeline_id),
    INDEX idx_is_active (is_active, next_execution_at),
    INDEX idx_next_execution (next_execution_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파이프라인 스케줄';

-- =====================================================
-- Sample Data
-- =====================================================

-- Insert sample pipeline definitions
INSERT INTO pipeline_definitions (pipeline_code, pipeline_name, file_type, configuration) VALUES
('IMAGE_OPTIMIZATION_V2', '이미지 최적화 v2', 'IMAGE', 
 '{"enable_webp": true, "enable_avif": true, "quality_levels": [85, 70], "max_dimension": 4096}'),
('HTML_OCR_EXTRACTION', 'HTML OCR 텍스트 추출', 'HTML',
 '{"ocr_engine": "tesseract", "languages": ["ko", "en"], "confidence_threshold": 0.8}'),
('EXCEL_AI_MAPPING', 'Excel AI 데이터 매핑', 'EXCEL',
 '{"ai_model": "gpt-4", "mapping_strategy": "semantic", "validation_rules": true}'),
('PDF_PROCESSING', 'PDF 문서 처리', 'PDF',
 '{"extract_text": true, "generate_thumbnail": true, "split_pages": false}');

-- Insert sample pipeline stages for IMAGE_OPTIMIZATION_V2
INSERT INTO pipeline_stages (pipeline_id, stage_code, stage_name, sequence_order, processor_type, processor_config) VALUES
(1, 'VALIDATE', '이미지 검증', 1, 'ImageValidator', '{"check_corruption": true, "check_format": true}'),
(1, 'RESIZE', '리사이징', 2, 'ImageResizer', '{"sizes": [200, 400, 800, 1200, 1920]}'),
(1, 'OPTIMIZE', '최적화', 3, 'ImageOptimizer', '{"strip_metadata": true, "progressive": true}'),
(1, 'CONVERT', '포맷 변환', 4, 'FormatConverter', '{"formats": ["webp", "avif"]}'),
(1, 'THUMBNAIL', '썸네일 생성', 5, 'ThumbnailGenerator', '{"sizes": [[200,200], [400,400]]}');

-- Insert sample pipeline stages for HTML_OCR_EXTRACTION
INSERT INTO pipeline_stages (pipeline_id, stage_code, stage_name, sequence_order, processor_type, processor_config) VALUES
(2, 'PARSE', 'HTML 파싱', 1, 'HtmlParser', '{"clean_tags": true, "extract_images": true}'),
(2, 'IMAGE_EXTRACT', '이미지 추출', 2, 'ImageExtractor', '{"formats": ["jpg", "png"]}'),
(2, 'OCR', 'OCR 처리', 3, 'OcrProcessor', '{"engine": "tesseract", "lang": "kor+eng"}'),
(2, 'TEXT_MERGE', '텍스트 병합', 4, 'TextMerger', '{"dedup": true, "format": "plain"}');

-- Insert sample pipeline stages for EXCEL_AI_MAPPING
INSERT INTO pipeline_stages (pipeline_id, stage_code, stage_name, sequence_order, processor_type, processor_config) VALUES
(3, 'PARSE_EXCEL', 'Excel 파싱', 1, 'ExcelParser', '{"read_formulas": true}'),
(3, 'ANALYZE_STRUCTURE', '구조 분석', 2, 'StructureAnalyzer', '{"detect_headers": true}'),
(3, 'AI_MAPPING', 'AI 매핑', 3, 'AiMapper', '{"model": "gpt-4", "confidence": 0.8}'),
(3, 'VALIDATE_MAPPING', '매핑 검증', 4, 'MappingValidator', '{"strict": false}'),
(3, 'TRANSFORM', '데이터 변환', 5, 'DataTransformer', '{"target_format": "canonical"}');
