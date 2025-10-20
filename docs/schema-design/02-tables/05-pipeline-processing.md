# 🔄 파이프라인 처리 테이블 명세

## 1. pipeline_definitions (파이프라인 정의)

### 테이블 설명
파일 처리 파이프라인을 정의하는 테이블입니다. 파일 타입별로 다양한 처리 워크플로우를 설정할 수 있습니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 파이프라인 ID |
| pipeline_code | VARCHAR(50) | UK, NOT NULL | - | 파이프라인 코드 |
| pipeline_name | VARCHAR(100) | NOT NULL | - | 파이프라인명 |
| description | TEXT | NULL | NULL | 파이프라인 설명 |
| file_type | ENUM('IMAGE', 'HTML', 'PDF', 'EXCEL', 'ANY') | NOT NULL | - | 대상 파일 타입 |
| pipeline_type | ENUM('SYNC', 'ASYNC', 'BATCH', 'SCHEDULED') | NOT NULL | 'ASYNC' | 실행 타입 |
| trigger_conditions | JSON | NULL | '{}' | 트리거 조건 |
| configuration | JSON | NOT NULL | '{}' | 파이프라인 설정 |
| default_params | JSON | NULL | '{}' | 기본 파라미터 |
| max_file_size | BIGINT | NULL | NULL | 최대 처리 파일 크기 |
| timeout_seconds | INT | NOT NULL | 300 | 타임아웃 (초) |
| max_retries | INT | NOT NULL | 3 | 최대 재시도 횟수 |
| retry_delay_seconds | INT | NOT NULL | 60 | 재시도 지연 (초) |
| priority | INT | NOT NULL | 100 | 우선순위 (낮을수록 높음) |
| parallelism | INT | NOT NULL | 1 | 병렬 처리 수 |
| resource_requirements | JSON | NULL | '{}' | 리소스 요구사항 |
| dependencies | JSON | NULL | '[]' | 의존 파이프라인 |
| success_actions | JSON | NULL | '[]' | 성공 시 액션 |
| failure_actions | JSON | NULL | '[]' | 실패 시 액션 |
| notification_config | JSON | NULL | '{}' | 알림 설정 |
| is_active | BOOLEAN | NOT NULL | TRUE | 활성화 상태 |
| version | INT | NOT NULL | 1 | 버전 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |
| created_by | VARCHAR(100) | NULL | NULL | 생성자 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_pipeline_code (pipeline_code)
INDEX idx_file_type (file_type, is_active)
INDEX idx_pipeline_type (pipeline_type)
INDEX idx_priority (priority, is_active)
INDEX idx_is_active (is_active)
```

### 샘플 데이터
```sql
-- 이미지 최적화 파이프라인
INSERT INTO pipeline_definitions (pipeline_code, pipeline_name, file_type, configuration) VALUES
('IMAGE_OPTIMIZATION_V2', '이미지 최적화 v2', 'IMAGE', 
 '{"enable_webp": true, "enable_avif": true, "quality_levels": [85, 70], "max_dimension": 4096}'),

-- HTML OCR 처리 파이프라인
('HTML_OCR_EXTRACTION', 'HTML OCR 텍스트 추출', 'HTML',
 '{"ocr_engine": "tesseract", "languages": ["ko", "en"], "confidence_threshold": 0.8}'),

-- Excel AI 매핑 파이프라인
('EXCEL_AI_MAPPING', 'Excel AI 데이터 매핑', 'EXCEL',
 '{"ai_model": "gpt-4", "mapping_strategy": "semantic", "validation_rules": true}'),

-- PDF 처리 파이프라인
('PDF_PROCESSING', 'PDF 문서 처리', 'PDF',
 '{"extract_text": true, "generate_thumbnail": true, "split_pages": false}');
```

---

## 2. pipeline_stages (파이프라인 단계)

### 테이블 설명
파이프라인의 개별 처리 단계를 정의합니다. 각 파이프라인은 여러 단계로 구성됩니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 단계 ID |
| pipeline_id | BIGINT | FK, NOT NULL | - | 파이프라인 ID |
| stage_code | VARCHAR(50) | NOT NULL | - | 단계 코드 |
| stage_name | VARCHAR(100) | NOT NULL | - | 단계명 |
| description | TEXT | NULL | NULL | 단계 설명 |
| sequence_order | INT | NOT NULL | - | 실행 순서 |
| processor_type | VARCHAR(100) | NOT NULL | - | 처리기 타입 |
| processor_config | JSON | NOT NULL | '{}' | 처리기 설정 |
| input_validation | JSON | NULL | NULL | 입력 검증 규칙 |
| output_validation | JSON | NULL | NULL | 출력 검증 규칙 |
| is_optional | BOOLEAN | NOT NULL | FALSE | 선택적 실행 여부 |
| is_parallel | BOOLEAN | NOT NULL | FALSE | 병렬 실행 가능 여부 |
| condition | JSON | NULL | NULL | 실행 조건 |
| timeout_seconds | INT | NOT NULL | 60 | 타임아웃 (초) |
| max_retries | INT | NOT NULL | 3 | 최대 재시도 횟수 |
| retry_strategy | ENUM('IMMEDIATE', 'LINEAR', 'EXPONENTIAL') | NOT NULL | 'EXPONENTIAL' | 재시도 전략 |
| on_failure | ENUM('FAIL', 'SKIP', 'CONTINUE', 'RETRY') | NOT NULL | 'FAIL' | 실패 시 동작 |
| resource_allocation | JSON | NULL | NULL | 리소스 할당 |
| metrics_config | JSON | NULL | NULL | 메트릭 설정 |
| is_active | BOOLEAN | NOT NULL | TRUE | 활성화 상태 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_pipeline_stage (pipeline_id, stage_code)
INDEX idx_pipeline_id (pipeline_id, sequence_order)
INDEX idx_processor_type (processor_type)
INDEX idx_is_active (is_active)
-- 외래키 제거: FK constraint 미사용
-- CASCADE 삭제는 애플리케이션 레벨에서 처리
```

### 샘플 데이터
```sql
-- 이미지 최적화 파이프라인 단계들
INSERT INTO pipeline_stages (pipeline_id, stage_code, stage_name, sequence_order, processor_type, processor_config) VALUES
(1, 'VALIDATE', '이미지 검증', 1, 'ImageValidator', '{"check_corruption": true, "check_format": true}'),
(1, 'RESIZE', '리사이징', 2, 'ImageResizer', '{"sizes": [200, 400, 800, 1200, 1920]}'),
(1, 'OPTIMIZE', '최적화', 3, 'ImageOptimizer', '{"strip_metadata": true, "progressive": true}'),
(1, 'CONVERT', '포맷 변환', 4, 'FormatConverter', '{"formats": ["webp", "avif"]}'),
(1, 'THUMBNAIL', '썸네일 생성', 5, 'ThumbnailGenerator', '{"sizes": [[200,200], [400,400]]}'),

-- HTML OCR 파이프라인 단계들
(2, 'PARSE', 'HTML 파싱', 1, 'HtmlParser', '{"clean_tags": true, "extract_images": true}'),
(2, 'IMAGE_EXTRACT', '이미지 추출', 2, 'ImageExtractor', '{"formats": ["jpg", "png"]}'),
(2, 'OCR', 'OCR 처리', 3, 'OcrProcessor', '{"engine": "tesseract", "lang": "kor+eng"}'),
(2, 'TEXT_MERGE', '텍스트 병합', 4, 'TextMerger', '{"dedup": true, "format": "plain"}'),

-- Excel AI 매핑 단계들
(3, 'PARSE_EXCEL', 'Excel 파싱', 1, 'ExcelParser', '{"read_formulas": true}'),
(3, 'ANALYZE_STRUCTURE', '구조 분석', 2, 'StructureAnalyzer', '{"detect_headers": true}'),
(3, 'AI_MAPPING', 'AI 매핑', 3, 'AiMapper', '{"model": "gpt-4", "confidence": 0.8}'),
(3, 'VALIDATE_MAPPING', '매핑 검증', 4, 'MappingValidator', '{"strict": false}'),
(3, 'TRANSFORM', '데이터 변환', 5, 'DataTransformer', '{"target_format": "canonical"}');
```

---

## 3. pipeline_executions (파이프라인 실행)

### 테이블 설명
파이프라인 실행 인스턴스를 추적하는 테이블입니다. 각 파일 처리에 대한 실행 기록을 관리합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 실행 ID |
| execution_id | VARCHAR(36) | UK, NOT NULL | UUID() | 실행 고유 식별자 |
| pipeline_id | BIGINT | FK, NOT NULL | - | 파이프라인 ID |
| file_id | BIGINT | FK, NOT NULL | - | 파일 ID |
| parent_execution_id | BIGINT | FK, NULL | NULL | 부모 실행 ID (중첩 실행) |
| trigger_type | ENUM('UPLOAD', 'MANUAL', 'SCHEDULED', 'DEPENDENCY', 'RETRY') | NOT NULL | - | 트리거 타입 |
| status | ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED', 'TIMEOUT') | NOT NULL | 'PENDING' | 실행 상태 |
| current_stage_id | BIGINT | FK, NULL | NULL | 현재 실행 중인 단계 |
| total_stages | INT | NOT NULL | 0 | 전체 단계 수 |
| completed_stages | INT | NOT NULL | 0 | 완료된 단계 수 |
| input_params | JSON | NULL | '{}' | 입력 파라미터 |
| output_results | JSON | NULL | NULL | 출력 결과 |
| execution_context | JSON | NULL | '{}' | 실행 컨텍스트 |
| error_message | TEXT | NULL | NULL | 에러 메시지 |
| error_code | VARCHAR(50) | NULL | NULL | 에러 코드 |
| error_stage_id | BIGINT | FK, NULL | NULL | 에러 발생 단계 |
| retry_count | INT | NOT NULL | 0 | 재시도 횟수 |
| worker_id | VARCHAR(100) | NULL | NULL | 워커 ID |
| resource_usage | JSON | NULL | NULL | 리소스 사용량 |
| performance_metrics | JSON | NULL | NULL | 성능 메트릭 |
| started_at | DATETIME | NULL | NULL | 시작 시각 |
| completed_at | DATETIME | NULL | NULL | 완료 시각 |
| duration_ms | INT | NULL | NULL | 실행 시간 (밀리초) |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_execution_id (execution_id)
INDEX idx_pipeline_id (pipeline_id, status, created_at DESC)
INDEX idx_file_id (file_id, created_at DESC)
INDEX idx_status (status, created_at DESC)
INDEX idx_trigger_type (trigger_type, status)
INDEX idx_parent_execution (parent_execution_id)
INDEX idx_worker_id (worker_id, status)
INDEX idx_created_at (created_at DESC)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

---

## 4. pipeline_stage_logs (파이프라인 단계 로그)

### 테이블 설명
각 파이프라인 단계의 실행 로그를 상세히 기록합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 로그 ID |
| execution_id | BIGINT | FK, NOT NULL | - | 실행 ID |
| stage_id | BIGINT | FK, NOT NULL | - | 단계 ID |
| stage_execution_id | VARCHAR(36) | UK, NOT NULL | UUID() | 단계 실행 ID |
| status | ENUM('STARTED', 'RUNNING', 'COMPLETED', 'FAILED', 'SKIPPED', 'TIMEOUT') | NOT NULL | 'STARTED' | 단계 상태 |
| input_data | JSON | NULL | NULL | 입력 데이터 |
| output_data | JSON | NULL | NULL | 출력 데이터 |
| stage_context | JSON | NULL | NULL | 단계 컨텍스트 |
| log_entries | JSON | NULL | '[]' | 로그 항목들 |
| error_message | TEXT | NULL | NULL | 에러 메시지 |
| error_stack_trace | TEXT | NULL | NULL | 에러 스택 트레이스 |
| retry_count | INT | NOT NULL | 0 | 재시도 횟수 |
| resource_usage | JSON | NULL | NULL | 리소스 사용량 |
| performance_metrics | JSON | NULL | NULL | 성능 메트릭 |
| started_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 시작 시각 |
| completed_at | DATETIME | NULL | NULL | 완료 시각 |
| duration_ms | INT | NULL | NULL | 실행 시간 (밀리초) |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_stage_execution_id (stage_execution_id)
INDEX idx_execution_id (execution_id, started_at)
INDEX idx_stage_id (stage_id, status)
INDEX idx_status (status, started_at DESC)
-- 외래키 제거: FK constraint 미사용
-- CASCADE 삭제는 애플리케이션 레벨에서 처리
```

---

## 5. pipeline_templates (파이프라인 템플릿) - 선택적

### 테이블 설명
재사용 가능한 파이프라인 템플릿을 관리합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 템플릿 ID |
| template_code | VARCHAR(50) | UK, NOT NULL | - | 템플릿 코드 |
| template_name | VARCHAR(100) | NOT NULL | - | 템플릿명 |
| description | TEXT | NULL | NULL | 템플릿 설명 |
| category | VARCHAR(50) | NOT NULL | - | 카테고리 |
| template_definition | JSON | NOT NULL | - | 템플릿 정의 |
| parameters_schema | JSON | NULL | NULL | 파라미터 스키마 |
| tags | JSON | NULL | '[]' | 태그 |
| is_public | BOOLEAN | NOT NULL | FALSE | 공개 여부 |
| version | VARCHAR(20) | NOT NULL | '1.0.0' | 버전 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |
| created_by | VARCHAR(100) | NULL | NULL | 생성자 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_template_code_version (template_code, version)
INDEX idx_category (category)
INDEX idx_is_public (is_public)
```

---

## 6. pipeline_schedules (파이프라인 스케줄)

### 테이블 설명
정기적으로 실행되는 파이프라인 스케줄을 관리합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 스케줄 ID |
| schedule_name | VARCHAR(100) | NOT NULL | - | 스케줄명 |
| pipeline_id | BIGINT | FK, NOT NULL | - | 파이프라인 ID |
| schedule_type | ENUM('CRON', 'INTERVAL', 'ONCE') | NOT NULL | - | 스케줄 타입 |
| cron_expression | VARCHAR(100) | NULL | NULL | Cron 표현식 |
| interval_seconds | INT | NULL | NULL | 인터벌 (초) |
| target_filter | JSON | NULL | NULL | 대상 파일 필터 |
| execution_params | JSON | NULL | '{}' | 실행 파라미터 |
| timezone | VARCHAR(50) | NOT NULL | 'UTC' | 타임존 |
| is_active | BOOLEAN | NOT NULL | TRUE | 활성화 상태 |
| last_execution_at | DATETIME | NULL | NULL | 마지막 실행 시각 |
| next_execution_at | DATETIME | NULL | NULL | 다음 실행 시각 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
INDEX idx_pipeline_id (pipeline_id)
INDEX idx_is_active (is_active, next_execution_at)
INDEX idx_next_execution (next_execution_at)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

---

## 7. 관계 다이어그램

```
pipeline_definitions ──< pipeline_stages
         │                      │
         │                      │
         └──< pipeline_executions ──< pipeline_stage_logs
         │
         └──< pipeline_schedules
         
pipeline_templates (독립적)
```

## 8. 파이프라인 처리 플로우

### 8.1 이미지 처리 파이프라인
```
1. 이미지 검증 (포맷, 크기, 무결성)
2. EXIF 메타데이터 추출
3. 리사이징 (다중 해상도)
4. 포맷 변환 (WebP, AVIF)
5. 최적화 (압축, 품질 조정)
6. 썸네일 생성
7. CDN 배포
8. 메타데이터 저장
```

### 8.2 HTML OCR 파이프라인
```
1. HTML 파싱 및 정제
2. 이미지 요소 추출
3. 각 이미지에 OCR 실행
4. 텍스트 결과 병합
5. 텍스트 정제 및 정규화
6. 데이터베이스 저장
7. 검색 인덱싱
```

### 8.3 Excel AI 매핑 파이프라인
```
1. Excel 파일 파싱
2. 시트/테이블 구조 분석
3. 헤더 및 데이터 타입 감지
4. AI 모델로 필드 매핑
5. 매핑 신뢰도 평가
6. 캐노니컬 포맷 변환
7. 검증 규칙 적용
8. 결과 저장
```

## 9. 성능 최적화 전략

### 9.1 실행 최적화
- 파이프라인 단계 병렬 처리
- 리소스 풀링 및 재사용
- 결과 캐싱 (중간 결과 포함)
- 배치 처리 그룹화

### 9.2 모니터링
- 실시간 실행 상태 추적
- 성능 메트릭 수집
- 병목 구간 자동 감지
- 알림 임계값 설정

### 9.3 스케일링
- 워커 노드 자동 스케일링
- 큐 기반 작업 분산
- 우선순위 기반 스케줄링
- 리소스 기반 제한

## 10. 에러 처리 및 복구

### 10.1 재시도 전략
- Exponential Backoff
- Circuit Breaker 패턴
- Dead Letter Queue
- 부분 재시도 (실패 단계부터)

### 10.2 에러 분류
- Transient Errors: 자동 재시도
- Permanent Errors: 즉시 실패
- Resource Errors: 대기 후 재시도
- Validation Errors: 사용자 알림
