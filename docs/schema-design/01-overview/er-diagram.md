# 📊 FileFlow ER 다이어그램

## 1. 전체 ER 다이어그램

```mermaid
erDiagram
    %% 테넌트 & 조직
    TENANTS ||--o{ ORGANIZATIONS : has
    TENANTS ||--o{ TENANT_SETTINGS : has
    ORGANIZATIONS ||--o{ ORGANIZATION_SETTINGS : has
    ORGANIZATIONS ||--o{ USERS : belongs_to
    
    %% 사용자 & 권한
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : assigned_to
    ROLES ||--o{ ROLE_PERMISSIONS : has
    PERMISSIONS ||--o{ ROLE_PERMISSIONS : granted_to
    
    %% 업로드 관리
    TENANTS ||--o{ UPLOAD_POLICIES : defines
    ORGANIZATIONS ||--o{ UPLOAD_POLICIES : customizes
    USERS ||--o{ UPLOAD_SESSIONS : creates
    UPLOAD_SESSIONS ||--o{ UPLOAD_PARTS : contains
    UPLOAD_POLICIES ||--o{ UPLOAD_SESSIONS : governs
    
    %% 파일 관리
    UPLOAD_SESSIONS ||--o{ FILE_ASSETS : produces
    FILE_ASSETS ||--o{ FILE_VARIANTS : has
    FILE_ASSETS ||--o{ FILE_METADATA : has
    FILE_ASSETS ||--o{ FILE_RELATIONSHIPS : has_source
    FILE_ASSETS ||--o{ FILE_RELATIONSHIPS : has_target
    
    %% 파이프라인 처리
    PIPELINE_DEFINITIONS ||--o{ PIPELINE_STAGES : contains
    FILE_ASSETS ||--o{ PIPELINE_EXECUTIONS : triggers
    PIPELINE_DEFINITIONS ||--o{ PIPELINE_EXECUTIONS : uses
    PIPELINE_EXECUTIONS ||--o{ PIPELINE_STAGE_LOGS : generates
    PIPELINE_STAGES ||--o{ PIPELINE_STAGE_LOGS : executed_in
    
    %% 데이터 추출
    FILE_ASSETS ||--o{ EXTRACTED_DATA : produces
    EXTRACTED_DATA ||--o{ DATA_MAPPINGS : uses
    CANONICAL_FORMATS ||--o{ DATA_MAPPINGS : defines
    
    %% 감사 로그
    USERS ||--o{ AUDIT_LOGS : generates
    FILE_ASSETS ||--o{ ACCESS_LOGS : tracked_in
    PIPELINE_EXECUTIONS ||--o{ PROCESSING_ERRORS : may_have
```

## 2. 도메인별 상세 관계도

### 2.1 테넌트 & 조직 관계

```mermaid
erDiagram
    TENANTS {
        varchar(50) tenant_id PK
        enum tenant_type "B2B/B2C"
        varchar(100) name
        enum status
        json settings
        datetime created_at
        datetime updated_at
    }
    
    ORGANIZATIONS {
        bigint id PK
        varchar(50) tenant_id FK
        varchar(100) org_code UK
        varchar(200) name
        enum org_type "SELLER/COMPANY/INTERNAL"
        enum status
        json metadata
        datetime created_at
        datetime updated_at
    }
    
    TENANT_SETTINGS {
        bigint id PK
        varchar(50) tenant_id FK
        varchar(100) setting_key
        text setting_value
        enum value_type
        datetime created_at
        datetime updated_at
    }
    
    ORGANIZATION_SETTINGS {
        bigint id PK
        bigint organization_id FK
        varchar(100) setting_key
        text setting_value
        datetime created_at
        datetime updated_at
    }
    
    TENANTS ||--o{ ORGANIZATIONS : "1:N"
    TENANTS ||--o{ TENANT_SETTINGS : "1:N"
    ORGANIZATIONS ||--o{ ORGANIZATION_SETTINGS : "1:N"
```

### 2.2 사용자 & 권한 관계

```mermaid
erDiagram
    USERS {
        bigint id PK
        varchar(50) tenant_id FK
        bigint organization_id FK
        varchar(100) username UK
        varchar(200) email UK
        varchar(255) password_hash
        enum user_type "SELLER/COMPANY_ADMIN/INTERNAL_ADMIN/CUSTOMER"
        enum status
        json profile
        datetime last_login_at
        datetime created_at
        datetime updated_at
    }
    
    ROLES {
        bigint id PK
        varchar(50) tenant_id FK
        varchar(50) role_code UK
        varchar(100) role_name
        text description
        enum role_type "SYSTEM/CUSTOM"
        datetime created_at
        datetime updated_at
    }
    
    USER_ROLES {
        bigint id PK
        bigint user_id FK
        bigint role_id FK
        datetime assigned_at
        datetime expires_at
    }
    
    PERMISSIONS {
        bigint id PK
        varchar(100) permission_code UK
        varchar(200) permission_name
        varchar(50) resource_type
        varchar(50) action
        text description
    }
    
    ROLE_PERMISSIONS {
        bigint id PK
        bigint role_id FK
        bigint permission_id FK
        json conditions
    }
    
    USERS ||--o{ USER_ROLES : "1:N"
    ROLES ||--o{ USER_ROLES : "1:N"
    ROLES ||--o{ ROLE_PERMISSIONS : "1:N"
    PERMISSIONS ||--o{ ROLE_PERMISSIONS : "1:N"
```

### 2.3 파일 관리 관계

```mermaid
erDiagram
    FILE_ASSETS {
        bigint id PK
        varchar(36) file_id UK
        varchar(36) session_id FK
        varchar(50) tenant_id FK
        bigint organization_id FK
        varchar(255) original_name
        varchar(255) stored_name
        varchar(20) file_type "IMAGE/HTML/PDF/EXCEL"
        bigint file_size
        varchar(100) mime_type
        varchar(2048) storage_path
        varchar(2048) cdn_url
        varchar(64) checksum
        enum status
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }
    
    FILE_VARIANTS {
        bigint id PK
        bigint original_file_id FK
        varchar(36) variant_file_id UK
        varchar(50) variant_type "THUMBNAIL/OPTIMIZED/CONVERTED"
        json variant_config
        varchar(2048) storage_path
        varchar(2048) cdn_url
        bigint file_size
        json dimensions
        datetime created_at
    }
    
    FILE_METADATA {
        bigint id PK
        bigint file_id FK
        varchar(100) metadata_key
        text metadata_value
        enum value_type "STRING/NUMBER/JSON/BINARY"
        datetime created_at
    }
    
    FILE_RELATIONSHIPS {
        bigint id PK
        bigint source_file_id FK
        bigint target_file_id FK
        varchar(50) relationship_type "DERIVED/RELATED/REPLACEMENT"
        json metadata
        datetime created_at
    }
    
    FILE_ASSETS ||--o{ FILE_VARIANTS : "1:N"
    FILE_ASSETS ||--o{ FILE_METADATA : "1:N"
    FILE_ASSETS ||--o{ FILE_RELATIONSHIPS : "1:N source"
    FILE_ASSETS ||--o{ FILE_RELATIONSHIPS : "1:N target"
```

### 2.4 업로드 관리 관계

```mermaid
erDiagram
    UPLOAD_SESSIONS {
        bigint id PK
        varchar(36) session_id UK
        varchar(50) tenant_id FK
        bigint user_id FK
        bigint policy_id FK
        enum upload_type "PRESIGNED/EXTERNAL_URL"
        enum status
        varchar(2048) external_url
        json session_config
        varchar(64) idempotency_key
        integer total_parts
        datetime expires_at
        datetime created_at
        datetime updated_at
    }
    
    UPLOAD_PARTS {
        bigint id PK
        varchar(36) session_id FK
        integer part_number
        varchar(255) etag
        bigint size
        enum status
        datetime uploaded_at
    }
    
    UPLOAD_POLICIES {
        bigint id PK
        varchar(50) tenant_id FK
        bigint organization_id FK
        varchar(50) policy_code UK
        varchar(100) policy_name
        json allowed_types
        bigint max_file_size
        bigint max_total_size
        integer max_files
        json allowed_sources
        json processing_rules
        boolean is_active
        datetime created_at
        datetime updated_at
    }
    
    UPLOAD_SESSIONS ||--o{ UPLOAD_PARTS : "1:N"
    UPLOAD_POLICIES ||--o{ UPLOAD_SESSIONS : "1:N"
```

### 2.5 파이프라인 처리 관계

```mermaid
erDiagram
    PIPELINE_DEFINITIONS {
        bigint id PK
        varchar(50) pipeline_code UK
        varchar(100) pipeline_name
        varchar(20) file_type
        json trigger_conditions
        json configuration
        boolean is_active
        integer priority
        datetime created_at
        datetime updated_at
    }
    
    PIPELINE_STAGES {
        bigint id PK
        bigint pipeline_id FK
        varchar(50) stage_code
        varchar(100) stage_name
        integer sequence_order
        varchar(100) processor_type
        json stage_config
        boolean is_optional
        integer timeout_seconds
    }
    
    PIPELINE_EXECUTIONS {
        bigint id PK
        varchar(36) execution_id UK
        bigint file_id FK
        bigint pipeline_id FK
        enum status
        json input_params
        json output_results
        datetime started_at
        datetime completed_at
        integer duration_ms
    }
    
    PIPELINE_STAGE_LOGS {
        bigint id PK
        bigint execution_id FK
        bigint stage_id FK
        enum status
        json input_data
        json output_data
        text error_message
        datetime started_at
        datetime completed_at
        integer duration_ms
    }
    
    PIPELINE_DEFINITIONS ||--o{ PIPELINE_STAGES : "1:N"
    PIPELINE_DEFINITIONS ||--o{ PIPELINE_EXECUTIONS : "1:N"
    PIPELINE_EXECUTIONS ||--o{ PIPELINE_STAGE_LOGS : "1:N"
    PIPELINE_STAGES ||--o{ PIPELINE_STAGE_LOGS : "1:N"
```

### 2.6 데이터 추출 관계

```mermaid
erDiagram
    EXTRACTED_DATA {
        bigint id PK
        bigint file_id FK
        varchar(50) extraction_type "OCR/AI_MAPPING/METADATA"
        json extracted_content
        float confidence_score
        varchar(50) extraction_method
        datetime extracted_at
    }
    
    DATA_MAPPINGS {
        bigint id PK
        bigint extracted_data_id FK
        bigint canonical_format_id FK
        json source_fields
        json mapped_fields
        float mapping_score
        enum status
        datetime created_at
    }
    
    CANONICAL_FORMATS {
        bigint id PK
        varchar(50) format_code UK
        varchar(100) format_name
        varchar(50) domain_type "PRODUCT/ORDER/INVENTORY"
        json schema_definition
        integer version
        boolean is_active
        datetime created_at
        datetime updated_at
    }
    
    EXTRACTED_DATA ||--o{ DATA_MAPPINGS : "1:N"
    CANONICAL_FORMATS ||--o{ DATA_MAPPINGS : "1:N"
```

## 3. 인덱스 전략

### 3.1 Primary Keys
- 모든 테이블은 `id` (BIGINT AUTO_INCREMENT) 사용
- 비즈니스 키는 별도 Unique Index 설정

### 3.2 Foreign Keys
- 모든 FK에 인덱스 자동 생성
- CASCADE 옵션은 신중히 사용 (대부분 RESTRICT)

### 3.3 조회 성능 인덱스

```sql
-- 자주 사용되는 복합 인덱스
CREATE INDEX idx_files_tenant_org ON file_assets(tenant_id, organization_id, created_at DESC);
CREATE INDEX idx_files_type_status ON file_assets(file_type, status, created_at DESC);
CREATE INDEX idx_sessions_user_status ON upload_sessions(user_id, status, created_at DESC);
CREATE INDEX idx_executions_file_pipeline ON pipeline_executions(file_id, pipeline_id, status);

-- 검색용 인덱스
CREATE FULLTEXT INDEX idx_files_search ON file_assets(original_name);
CREATE INDEX idx_metadata_key_value ON file_metadata(metadata_key, metadata_value(100));
```

### 3.4 파티셔닝 전략

대용량 테이블의 경우 파티셔닝 적용:

```sql
-- 날짜 기반 파티셔닝 (월별)
ALTER TABLE file_assets
PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
    PARTITION p202501 VALUES LESS THAN (202502),
    PARTITION p202502 VALUES LESS THAN (202503),
    ...
);

-- 테넌트 기반 파티셔닝
ALTER TABLE audit_logs
PARTITION BY KEY(tenant_id)
PARTITIONS 10;
```

## 4. 제약조건 & 비즈니스 규칙

### 4.1 데이터 무결성
- 모든 FK는 유효한 참조 보장
- Soft Delete 시 관련 데이터 처리 규칙
- 순환 참조 방지

### 4.2 비즈니스 규칙
- 테넌트 간 데이터 격리 (Row Level Security)
- 파일 크기 제한 체크
- 동시성 제어 (Optimistic Locking)

### 4.3 감사 규칙
- 모든 CUD 작업 로깅
- 민감 데이터 접근 추적
- 변경 이력 관리
