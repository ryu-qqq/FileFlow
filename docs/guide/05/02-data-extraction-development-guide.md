# FileFlow – Data Extraction (v2, Greenfield · Hardened)

> 목표: 파일에서 추출된 데이터의 **저장·검증·표준화(매핑)·승인·학습데이터화** 전 과정을 프로덕션 기준으로 설계한다.
> Greenfield 전제: **모든 테이블은 CREATE TABLE**, FK 미사용(앱 레벨 정합성), Soft Delete 우선.
> 핵심 보강: 멀티테넌트 경계, **업무 유니크 키**, **트레이싱(trace_id)**, **콜드/핫 분리**, 승인 게이팅, PII/보존 정책.

---

## 0) 용어 정리

* **Extracted Data**: 파일에서 추출된 1차 결과(텍스트·구조화·품질 지표 포함).
* **Canonical Format**: 테넌트별/도메인별 표준 스키마(목적지에 적합한 공통 포맷).
* **Data Mapping**: 추출 결과 → 표준 포맷으로의 변환(룰/모델/수동승인 포함).
* **Mapping Rule**: 필드 단위 매핑 규칙(정규식/룩업/스코어 가중치 등).
* **Extracted Entity**: 엔티티 인식 결과(개체명/유형/좌표/신뢰도/PII 태그).
* **OCR Region**: OCR/레이아웃 블록 단위 좌표/텍스트/신뢰도.
* **Approved Gate**: 승인된 매핑만 **다운스트림 전달** 허용하는 정책.
* **Cold/Hot Split**: 대용량 텍스트/JSON은 S3 등 외부 스토리지에 보관하고 DB에는 **참조키만** 저장.

---

## 1) 데이터 모델(핵심)

> 원칙
>
> 1. **스코프 보존**: 모든 상위 테이블에 `tenant_id`, `organization_id` 포함.
> 2. **업무 유니크**: 중복 추출 방지 `(file_id, extraction_type, extraction_method, version)` 고정.
> 3. **Trace 전파**: 파이프라인 `trace_id`를 전 테이블에 보존.
> 4. **콜드/핫 분리**: 텍스트/대형 JSON은 `*_ref`로만 참조.

### 1.1 extracted_data (핵심 추출 결과)

```sql
CREATE TABLE extracted_data (
  id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
  extracted_uuid        CHAR(36) NOT NULL,                 -- 외부/로그용
  file_id               BIGINT NOT NULL,                    -- file_assets.id
  tenant_id             VARCHAR(50) NOT NULL,
  organization_id       BIGINT NULL,
  extraction_type       VARCHAR(50) NOT NULL,               -- ex) OCR, FORM, HTML, TABLE
  extraction_method     VARCHAR(50) NOT NULL,               -- ex) tesseract, textract, custom
  version               INT NOT NULL DEFAULT 1,
  trace_id              VARCHAR(64) NULL,

  text_ref              VARCHAR(512) NULL,                  -- 대용량 텍스트는 참조만
  structured_ref        VARCHAR(512) NULL,                  -- 대용량 JSON 참조(S3 등)
  preview_ref           VARCHAR(512) NULL,                  -- (선택) 미리보기 파일 참조

  confidence_score      DECIMAL(5,4) NULL,
  quality_score         DECIMAL(5,4) NULL,
  validation_status     ENUM('PENDING','PASSED','FAILED') NOT NULL DEFAULT 'PENDING',
  notes                 TEXT NULL,

  extracted_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at            DATETIME NULL,

  UNIQUE KEY uk_business (file_id, extraction_type, extraction_method, version),
  UNIQUE KEY uk_uuid (extracted_uuid),
  INDEX idx_scope_time (tenant_id, organization_id, extracted_at),
  INDEX idx_file_type_time (file_id, extraction_type, extracted_at DESC),
  INDEX idx_trace (trace_id)
);
```

### 1.2 canonical_formats (표준 포맷)

```sql
CREATE TABLE canonical_formats (
  id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id             VARCHAR(50) NOT NULL,
  organization_id       BIGINT NULL,
  format_code           VARCHAR(50) NOT NULL,
  format_name           VARCHAR(100) NOT NULL,
  description           TEXT NULL,
  schema_json           JSON NOT NULL,                      -- 표준 스키마
  parent_format_id      BIGINT NULL,                        -- 마이그레이션 계보
  version               INT NOT NULL DEFAULT 1,
  status                ENUM('ACTIVE','DEPRECATED') NOT NULL DEFAULT 'ACTIVE',
  created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_scope_code_ver (tenant_id, organization_id, format_code, version),
  INDEX idx_scope (tenant_id, organization_id)
);
```

### 1.3 data_mappings (표준화 결과)

```sql
CREATE TABLE data_mappings (
  id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
  mapping_uuid          CHAR(36) NOT NULL,
  tenant_id             VARCHAR(50) NOT NULL,
  organization_id       BIGINT NULL,
  extracted_data_id     BIGINT NOT NULL,
  format_id             BIGINT NOT NULL,                    -- canonical_formats.id
  mapped_ref            VARCHAR(512) NULL,                  -- 변환 결과(콜드 스토리지) 참조
  mapping_score         DECIMAL(5,4) NULL,
  status                ENUM('DRAFT','REVIEW','APPROVED','REJECTED') NOT NULL DEFAULT 'DRAFT',
  approver_user_id      BIGINT NULL,
  approved_at           DATETIME NULL,
  trace_id              VARCHAR(64) NULL,
  created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_map_uuid (mapping_uuid),
  INDEX idx_scope_time (tenant_id, organization_id, created_at),
  INDEX idx_review_queue (status, created_at),
  INDEX idx_trace (trace_id)
);
```

### 1.4 mapping_rules (룰 저장소)

```sql
CREATE TABLE mapping_rules (
  id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id             VARCHAR(50) NOT NULL,
  organization_id       BIGINT NULL,
  rule_code             VARCHAR(50) NOT NULL,
  rule_name             VARCHAR(100) NOT NULL,
  description           TEXT NULL,
  rule_type             ENUM('REGEX','LOOKUP','MODEL','SCRIPT','EXPR') NOT NULL,
  rule_config           JSON NOT NULL,                       -- 정규식, 스크립트, 모델파라미터 등
  version               INT NOT NULL DEFAULT 1,
  status                ENUM('ACTIVE','DEPRECATED') NOT NULL DEFAULT 'ACTIVE',
  created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_scope_rule_ver (tenant_id, organization_id, rule_code, version),
  INDEX idx_scope (tenant_id, organization_id)
);
```

### 1.5 extracted_entities (엔티티)

```sql
CREATE TABLE extracted_entities (
  id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
  extracted_data_id     BIGINT NOT NULL,
  tenant_id             VARCHAR(50) NOT NULL,
  organization_id       BIGINT NULL,
  entity_type           VARCHAR(50) NOT NULL,                -- ex) PERSON, DATE, ADDRESS
  entity_value          TEXT NULL,
  bbox                  JSON NULL,                           -- {x,y,w,h} (이미지 문서)
  page_no               INT NULL,
  confidence            DECIMAL(5,4) NULL,
  attributes            JSON NULL,                           -- {contains_pii:true, source:'ocr|nlp'} 등
  trace_id              VARCHAR(64) NULL,
  created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_scope_time (tenant_id, organization_id, created_at),
  INDEX idx_extracted (extracted_data_id),
  INDEX idx_type_conf (entity_type, confidence),
  INDEX idx_trace (trace_id)
);
```

### 1.6 ocr_regions (OCR/레이아웃 블록)

```sql
CREATE TABLE ocr_regions (
  id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
  extracted_data_id     BIGINT NOT NULL,
  tenant_id             VARCHAR(50) NOT NULL,
  organization_id       BIGINT NULL,
  region_index          INT NOT NULL,
  page_no               INT NULL,
  bbox                  JSON NULL,
  text_ref              VARCHAR(512) NULL,                   -- 영역 텍스트(콜드 참조)
  confidence            DECIMAL(5,4) NULL,
  attributes            JSON NULL,                           -- {lang:'ko', lineHeight:...}
  trace_id              VARCHAR(64) NULL,
  created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_region (extracted_data_id, region_index),
  INDEX idx_scope (tenant_id, organization_id),
  INDEX idx_extracted (extracted_data_id),
  INDEX idx_trace (trace_id)
);
```

### 1.7 ai_training_data (학습 데이터 저장소)

```sql
CREATE TABLE ai_training_data (
  id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id             VARCHAR(50) NOT NULL,
  organization_id       BIGINT NULL,
  source_mapping_id     BIGINT NOT NULL,
  sample_ref            VARCHAR(512) NOT NULL,               -- 텍스트/JSON/이미지 등 참조
  label_ref             VARCHAR(512) NULL,                   -- 라벨/어노테이션 참조
  quality_score         DECIMAL(5,4) NULL,
  is_validated          TINYINT(1) NOT NULL DEFAULT 0,
  created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_scope_quality (tenant_id, organization_id, is_validated, quality_score)
);
```

---

## 2) 흐름 / 계약(Contract)

* **Upstream (Pipeline → Extraction)**: 파이프라인 스테이지가 추출을 완료하면 이벤트 발행.

    * 페이로드: `{ executionId, fileId, extractionType, method, confidence, traceId, tenantId, organizationId, version? }`
    * **Idempotent Upsert**: 수신자는 `extracted_data`를 `(file_id, extraction_type, extraction_method, version)` 기준으로 upsert한다.
* **Extraction → Mapping**: `extracted_data`가 생성/갱신되면 자동으로 `data_mappings` 후보를 만들거나 업데이트(룰/모델 적용).
* **Approved Gate**: `data_mappings.status='APPROVED'` 만 **다운스트림**(Export/Sync/Analytics)으로 전달.

---

## 3) 보안 / 컴플라이언스 / 보존

* **테넌트 경계**: 모든 조회는 `tenant_id`(+`organization_id`) 필수 필터. 교차 테넌트 조인 금지.
* **PII**: `extracted_entities.attributes.contains_pii=true` 태깅. 다운스트림 전달 시 마스킹/필터링.
* **보존**: 테넌트 정책 기반 보존일을 준수. 원본 파일 삭제 시 연쇄 처리:

    * 기본: `extracted_data` 소프트 삭제 → 보류 기간 후 물리 삭제
    * 옵션: 익명화(PII 제거) 후 통계/학습에 한해 보관
* **감사**: 매핑 승인/거부/수정은 별도 감사 이벤트로 기록.

---

## 4) 관찰성 / 성능

* **Trace/Span**: `trace_id`로 파일→실행→추출→매핑→엔티티/OCR을 상관 지을 수 있어야 한다.
* **메트릭**: 추출 성공율, 평균/분포 `confidence_score`, 매핑 승인 대기량, PII 검출 건수, 인덱싱(ES) 지연.
* **인덱스**: 실사용 쿼리에 맞춘 보강 인덱스 권장

```sql
CREATE INDEX idx_extracted_file_type ON extracted_data (file_id, extraction_type, extracted_at DESC);
CREATE INDEX idx_mapping_review ON data_mappings (status, created_at);
CREATE INDEX idx_ocr_extracted_order ON ocr_regions (extracted_data_id, region_index);
```

* **검색(한글)**: MySQL FULLTEXT는 보조. 본선은 OpenSearch/ES(형태소/엔그램). 비동기 인덱싱 파이프라인 구성.
* **콜드/핫 규정**: 텍스트·JSON 본문은 DB에 저장하지 않고 `*_ref`에만 참조.

---

## 5) 운영 API(요약, 선택)

* **GET /extractions?tenantId=&orgId=&fileId=&type=&from=&to=** → 추출 목록
* **GET /extractions/{extractedUuid}** → 추출 상세(참조 링크 포함)
* **GET /mappings?status=REVIEW** → 승인 대기 큐
* **POST /mappings/{mappingUuid}:approve** / `:reject` → 승인/거부 (감사 로그)

> 권한: `extraction.read`, `mapping.review`, `mapping.approve` (테넌트/조직 스코프)

---

## 6) 테스트 / DoD

* [ ] 파이프라인 이벤트 수신 시 **업무 유니크** 키 기준 Idempotent Upsert
* [ ] `Approved Gate`: `APPROVED`만 다운스트림 전달 보장
* [ ] 멀티테넌트 필터 강제(越권 방지)
* [ ] PII 태깅/마스킹 경로 검증
* [ ] 콜드/핫 분리 준수(대용량은 참조만)
* [ ] Trace 상관관계로 전 구간 추적 가능
* [ ] 한글 검색은 ES 통해 정상 동작(동기화 지연 알람)

---

## 7) 표준 에러 코드(초안)

* `DE-400-SCHEMA` 이벤트/입력 스키마 오류
* `DE-403-SCOPE` 테넌트/조직 스코프 위반
* `DE-404-NOTFOUND` 대상 없음
* `DE-409-UNIQUE` 업무 유니크 충돌
* `DE-422-RULE` 매핑 룰 검증 실패
* `DE-500-IO` 스토리지/네트워크 오류
