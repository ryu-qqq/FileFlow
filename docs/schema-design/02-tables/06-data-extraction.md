# 🔍 데이터 추출 테이블 명세

## 1. extracted_data (추출 데이터)

### 테이블 설명
파일로부터 추출된 모든 데이터를 저장하는 핵심 테이블입니다. OCR, AI 분석, 메타데이터 추출 등의 결과를 관리합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 추출 데이터 ID |
| extraction_id | VARCHAR(36) | UK, NOT NULL | UUID() | 추출 고유 식별자 |
| file_id | BIGINT | FK, NOT NULL | - | 원본 파일 ID |
| execution_id | BIGINT | FK, NULL | NULL | 파이프라인 실행 ID |
| extraction_type | ENUM('OCR', 'AI_ANALYSIS', 'METADATA', 'STRUCTURED_DATA', 'NLP', 'CUSTOM') | NOT NULL | - | 추출 타입 |
| extraction_method | VARCHAR(100) | NOT NULL | - | 추출 방법/엔진 |
| source_type | ENUM('IMAGE', 'HTML', 'PDF', 'EXCEL', 'TEXT') | NOT NULL | - | 소스 타입 |
| extracted_content | JSON | NULL | NULL | 추출된 콘텐츠 (JSON) |
| extracted_text | LONGTEXT | NULL | NULL | 추출된 텍스트 |
| structured_data | JSON | NULL | NULL | 구조화된 데이터 |
| confidence_score | DECIMAL(5,4) | NULL | NULL | 신뢰도 점수 (0.0000-1.0000) |
| quality_score | DECIMAL(5,4) | NULL | NULL | 품질 점수 |
| language | VARCHAR(10) | NULL | NULL | 감지된 언어 |
| encoding | VARCHAR(50) | NULL | 'UTF-8' | 인코딩 |
| word_count | INT | NULL | NULL | 단어 수 |
| character_count | INT | NULL | NULL | 문자 수 |
| processing_time_ms | INT | NULL | NULL | 처리 시간 (밀리초) |
| extraction_config | JSON | NULL | '{}' | 추출 설정 |
| validation_status | ENUM('PENDING', 'VALIDATED', 'INVALID', 'MANUAL_REVIEW') | NULL | 'PENDING' | 검증 상태 |
| validation_errors | JSON | NULL | NULL | 검증 오류 |
| metadata | JSON | NULL | '{}' | 추가 메타데이터 |
| version | INT | NOT NULL | 1 | 버전 |
| extracted_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 추출 시각 |
| validated_at | DATETIME | NULL | NULL | 검증 시각 |
| validated_by | BIGINT | FK, NULL | NULL | 검증자 ID |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_extraction_id (extraction_id)
INDEX idx_file_id (file_id, extraction_type)
INDEX idx_execution_id (execution_id)
INDEX idx_extraction_type (extraction_type, extracted_at DESC)
INDEX idx_confidence_score (confidence_score DESC)
INDEX idx_validation_status (validation_status)
INDEX idx_extracted_at (extracted_at DESC)
FULLTEXT idx_extracted_text (extracted_text)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

### 샘플 데이터
```sql
-- HTML OCR 추출 데이터
INSERT INTO extracted_data (file_id, extraction_type, extraction_method, source_type, extracted_text, confidence_score) VALUES
(1, 'OCR', 'tesseract_v4', 'HTML', '상품명: 프리미엄 가죽 자켓\n소재: 천연 소가죽 100%\n사이즈: S, M, L, XL', 0.9234),

-- Excel AI 분석 데이터
(2, 'AI_ANALYSIS', 'gpt-4-vision', 'EXCEL', NULL, 0.8756),

-- PDF 메타데이터 추출
(3, 'METADATA', 'pdfbox', 'PDF', NULL, 1.0000);
```

---

## 2. canonical_formats (표준 포맷)

### 테이블 설명
데이터 매핑의 대상이 되는 표준 포맷을 정의합니다. 외부 데이터를 내부 표준 구조로 변환하기 위한 스키마입니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 포맷 ID |
| format_code | VARCHAR(50) | UK, NOT NULL | - | 포맷 코드 |
| format_name | VARCHAR(100) | NOT NULL | - | 포맷명 |
| domain_type | ENUM('PRODUCT', 'ORDER', 'INVENTORY', 'CUSTOMER', 'INVOICE', 'CUSTOM') | NOT NULL | - | 도메인 타입 |
| description | TEXT | NULL | NULL | 포맷 설명 |
| schema_definition | JSON | NOT NULL | - | 스키마 정의 |
| field_definitions | JSON | NOT NULL | - | 필드 정의 |
| validation_rules | JSON | NULL | '{}' | 검증 규칙 |
| transformation_rules | JSON | NULL | '{}' | 변환 규칙 |
| sample_data | JSON | NULL | NULL | 샘플 데이터 |
| version | INT | NOT NULL | 1 | 버전 |
| is_active | BOOLEAN | NOT NULL | TRUE | 활성화 상태 |
| parent_format_id | BIGINT | FK, NULL | NULL | 부모 포맷 ID (상속) |
| tags | JSON | NULL | '[]' | 태그 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |
| created_by | VARCHAR(100) | NULL | NULL | 생성자 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_format_code_version (format_code, version)
INDEX idx_domain_type (domain_type, is_active)
INDEX idx_is_active (is_active)
INDEX idx_parent_format (parent_format_id)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

### 샘플 데이터
```sql
INSERT INTO canonical_formats (format_code, format_name, domain_type, schema_definition, field_definitions) VALUES
-- 상품 표준 포맷
('PRODUCT_V2', '상품 정보 v2', 'PRODUCT', 
 '{"type": "object", "required": ["sku", "name", "price"]}',
 '{"sku": {"type": "string", "maxLength": 50}, "name": {"type": "string", "maxLength": 200}, "price": {"type": "number", "minimum": 0}}'),

-- 주문 표준 포맷
('ORDER_V1', '주문 정보 v1', 'ORDER',
 '{"type": "object", "required": ["order_no", "customer_id", "items"]}',
 '{"order_no": {"type": "string"}, "customer_id": {"type": "string"}, "items": {"type": "array"}}'),

-- 재고 표준 포맷
('INVENTORY_V1', '재고 정보 v1', 'INVENTORY',
 '{"type": "object", "required": ["sku", "quantity", "warehouse"]}',
 '{"sku": {"type": "string"}, "quantity": {"type": "integer"}, "warehouse": {"type": "string"}}');
```

---

## 3. data_mappings (데이터 매핑)

### 테이블 설명
추출된 데이터를 표준 포맷으로 매핑하는 규칙과 결과를 저장합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 매핑 ID |
| mapping_id | VARCHAR(36) | UK, NOT NULL | UUID() | 매핑 고유 식별자 |
| extracted_data_id | BIGINT | FK, NOT NULL | - | 추출 데이터 ID |
| canonical_format_id | BIGINT | FK, NOT NULL | - | 표준 포맷 ID |
| mapping_type | ENUM('AUTO', 'AI', 'RULE_BASED', 'MANUAL', 'HYBRID') | NOT NULL | - | 매핑 타입 |
| mapping_method | VARCHAR(100) | NULL | NULL | 매핑 방법 |
| source_fields | JSON | NOT NULL | - | 소스 필드 목록 |
| mapped_fields | JSON | NOT NULL | - | 매핑된 필드 |
| field_mappings | JSON | NOT NULL | - | 필드별 상세 매핑 |
| transformation_log | JSON | NULL | NULL | 변환 로그 |
| mapping_score | DECIMAL(5,4) | NULL | NULL | 매핑 점수 (0.0000-1.0000) |
| confidence_scores | JSON | NULL | NULL | 필드별 신뢰도 점수 |
| validation_results | JSON | NULL | NULL | 검증 결과 |
| status | ENUM('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'REJECTED', 'ACTIVE', 'ARCHIVED') | NOT NULL | 'DRAFT' | 매핑 상태 |
| error_fields | JSON | NULL | NULL | 오류 필드 목록 |
| warning_fields | JSON | NULL | NULL | 경고 필드 목록 |
| review_notes | TEXT | NULL | NULL | 검토 노트 |
| approved_by | BIGINT | FK, NULL | NULL | 승인자 ID |
| approved_at | DATETIME | NULL | NULL | 승인 시각 |
| metadata | JSON | NULL | '{}' | 추가 메타데이터 |
| version | INT | NOT NULL | 1 | 버전 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_mapping_id (mapping_id)
INDEX idx_extracted_data (extracted_data_id)
INDEX idx_canonical_format (canonical_format_id)
INDEX idx_mapping_type (mapping_type, status)
INDEX idx_status (status, created_at DESC)
INDEX idx_mapping_score (mapping_score DESC)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

---

## 4. mapping_rules (매핑 규칙)

### 테이블 설명
데이터 매핑을 위한 사전 정의된 규칙을 관리합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 규칙 ID |
| rule_code | VARCHAR(50) | UK, NOT NULL | - | 규칙 코드 |
| rule_name | VARCHAR(100) | NOT NULL | - | 규칙명 |
| canonical_format_id | BIGINT | FK, NOT NULL | - | 대상 표준 포맷 ID |
| source_pattern | JSON | NOT NULL | - | 소스 패턴 정의 |
| rule_type | ENUM('EXACT', 'PATTERN', 'SEMANTIC', 'CONDITIONAL', 'CUSTOM') | NOT NULL | - | 규칙 타입 |
| mapping_definition | JSON | NOT NULL | - | 매핑 정의 |
| transformation_script | TEXT | NULL | NULL | 변환 스크립트 |
| validation_script | TEXT | NULL | NULL | 검증 스크립트 |
| priority | INT | NOT NULL | 100 | 우선순위 |
| conditions | JSON | NULL | NULL | 적용 조건 |
| is_active | BOOLEAN | NOT NULL | TRUE | 활성화 상태 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_rule_code (rule_code)
INDEX idx_canonical_format (canonical_format_id, is_active)
INDEX idx_rule_type (rule_type)
INDEX idx_priority (priority, is_active)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

---

## 5. ai_training_data (AI 학습 데이터)

### 테이블 설명
AI 매핑 모델 학습을 위한 데이터를 저장합니다. 성공적인 매핑 사례를 학습 데이터로 활용합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 학습 데이터 ID |
| mapping_id | BIGINT | FK, NOT NULL | - | 원본 매핑 ID |
| source_schema | JSON | NOT NULL | - | 소스 스키마 |
| target_schema | JSON | NOT NULL | - | 타겟 스키마 |
| mapping_result | JSON | NOT NULL | - | 매핑 결과 |
| quality_score | DECIMAL(5,4) | NOT NULL | - | 품질 점수 |
| is_validated | BOOLEAN | NOT NULL | FALSE | 검증 완료 여부 |
| is_used_for_training | BOOLEAN | NOT NULL | FALSE | 학습 사용 여부 |
| model_version | VARCHAR(20) | NULL | NULL | 모델 버전 |
| feedback_score | INT | NULL | NULL | 피드백 점수 (1-5) |
| feedback_notes | TEXT | NULL | NULL | 피드백 노트 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
INDEX idx_mapping_id (mapping_id)
INDEX idx_quality_score (quality_score DESC)
INDEX idx_is_validated (is_validated, is_used_for_training)
INDEX idx_model_version (model_version)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

---

## 6. extracted_entities (추출된 엔티티)

### 테이블 설명
텍스트에서 추출된 명명된 엔티티(Named Entity)를 저장합니다. 상품명, 브랜드, 가격 등의 주요 정보를 구조화합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 엔티티 ID |
| extracted_data_id | BIGINT | FK, NOT NULL | - | 추출 데이터 ID |
| entity_type | VARCHAR(50) | NOT NULL | - | 엔티티 타입 (PRODUCT, BRAND, PRICE 등) |
| entity_value | TEXT | NOT NULL | - | 엔티티 값 |
| normalized_value | TEXT | NULL | NULL | 정규화된 값 |
| position_start | INT | NULL | NULL | 시작 위치 |
| position_end | INT | NULL | NULL | 종료 위치 |
| confidence_score | DECIMAL(5,4) | NULL | NULL | 신뢰도 점수 |
| context | TEXT | NULL | NULL | 주변 컨텍스트 |
| attributes | JSON | NULL | '{}' | 엔티티 속성 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
INDEX idx_extracted_data (extracted_data_id, entity_type)
INDEX idx_entity_type (entity_type)
INDEX idx_entity_value (entity_value(100))
INDEX idx_confidence_score (confidence_score DESC)
-- 외래키 제거: FK constraint 미사용
-- CASCADE 삭제는 애플리케이션 레벨에서 처리
```

---

## 7. ocr_regions (OCR 영역)

### 테이블 설명
이미지에서 OCR이 수행된 영역 정보를 저장합니다. 텍스트 위치와 바운딩 박스 정보를 관리합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 영역 ID |
| extracted_data_id | BIGINT | FK, NOT NULL | - | 추출 데이터 ID |
| region_index | INT | NOT NULL | - | 영역 인덱스 |
| bounding_box | JSON | NOT NULL | - | 바운딩 박스 좌표 |
| text_content | TEXT | NOT NULL | - | 추출된 텍스트 |
| confidence_score | DECIMAL(5,4) | NULL | NULL | 신뢰도 점수 |
| language | VARCHAR(10) | NULL | NULL | 감지된 언어 |
| text_direction | ENUM('LTR', 'RTL', 'TTB', 'BTT') | NULL | 'LTR' | 텍스트 방향 |
| font_info | JSON | NULL | NULL | 폰트 정보 |
| is_handwritten | BOOLEAN | NULL | FALSE | 수기 여부 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
INDEX idx_extracted_data (extracted_data_id, region_index)
INDEX idx_confidence_score (confidence_score DESC)
FULLTEXT idx_text_content (text_content)
-- 외래키 제거: FK constraint 미사용
-- CASCADE 삭제는 애플리케이션 레벨에서 처리
```

---

## 8. 관계 다이어그램

```
file_assets ──< extracted_data ──< extracted_entities
                     │         └──< ocr_regions
                     │
                     └──< data_mappings >──── canonical_formats
                              │                      │
                              │                      │
                              └──< ai_training_data  └──< mapping_rules
```

## 9. 데이터 추출 프로세스

### 9.1 OCR 추출 플로우
```
1. 이미지/HTML에서 이미지 영역 식별
2. 전처리 (노이즈 제거, 향상)
3. OCR 엔진 실행 (Tesseract/Google Vision)
4. 텍스트 및 좌표 추출
5. 후처리 (철자 교정, 정규화)
6. 엔티티 인식 (NER)
7. 신뢰도 평가
8. 결과 저장
```

### 9.2 Excel AI 매핑 플로우
```
1. Excel 구조 분석
2. 헤더 및 데이터 타입 감지
3. 패턴 매칭 시도
4. AI 모델 추론 (GPT-4/Custom)
5. 후보 매핑 생성
6. 신뢰도 점수 계산
7. 수동 검토 (필요시)
8. 최종 매핑 확정
9. 캐노니컬 포맷 변환
```

### 9.3 메타데이터 추출 플로우
```
1. 파일 타입별 파서 선택
2. 기본 메타데이터 추출
3. EXIF/XMP 데이터 파싱
4. 커스텀 메타데이터 추출
5. 데이터 정규화
6. 저장 및 인덱싱
```

## 10. AI 모델 관리

### 10.1 모델 버전 관리
- 모델별 버전 추적
- A/B 테스팅 지원
- 롤백 메커니즘
- 성능 메트릭 추적

### 10.2 학습 데이터 관리
- 고품질 매핑 자동 수집
- 수동 레이블링 인터페이스
- 데이터 품질 검증
- 정기적 재학습 스케줄

### 10.3 추론 최적화
- 모델 캐싱
- 배치 추론
- GPU 가속
- 엣지 배포 옵션

## 11. 성능 및 품질 지표

### 11.1 OCR 품질 지표
- 문자 정확도 (CER)
- 단어 정확도 (WER)
- 신뢰도 분포
- 처리 시간

### 11.2 매핑 품질 지표
- 매핑 정확도
- 필드 커버리지
- 변환 오류율
- 수동 개입률
