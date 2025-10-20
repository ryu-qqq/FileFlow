# 🏢 테넌트 & 조직 테이블 명세

## 1. tenants (테넌트)

### 테이블 설명
멀티테넌시의 핵심 테이블로, B2B와 B2C 비즈니스 모델을 구분하고 각 테넌트별 독립적인 운영 환경을 제공합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| tenant_id | VARCHAR(50) | PK, NOT NULL | - | 테넌트 고유 식별자 (예: 'b2c_kr', 'b2b_global') |
| tenant_type | ENUM('B2C', 'B2B') | NOT NULL | - | 테넌트 유형 |
| name | VARCHAR(100) | NOT NULL | - | 테넌트 명칭 |
| description | TEXT | NULL | NULL | 테넌트 설명 |
| status | ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') | NOT NULL | 'ACTIVE' | 테넌트 상태 |
| settings | JSON | NULL | '{}' | 테넌트 전역 설정 (JSON) |
| api_quota_limit | INT | NULL | 10000 | API 일일 호출 제한 |
| storage_quota_gb | BIGINT | NULL | 1000 | 스토리지 할당량 (GB) |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |
| created_by | VARCHAR(100) | NULL | NULL | 생성자 |

### 인덱스
```sql
PRIMARY KEY (tenant_id)
INDEX idx_tenant_type_status (tenant_type, status)
INDEX idx_created_at (created_at)
```

### 샘플 데이터
```sql
INSERT INTO tenants (tenant_id, tenant_type, name, description, settings) VALUES
('b2c_kr', 'B2C', 'B2C Korea', '한국 B2C 마켓플레이스', '{"default_language": "ko", "currency": "KRW"}'),
('b2b_global', 'B2B', 'B2B Global', '글로벌 B2B 플랫폼', '{"default_language": "en", "currency": "USD"}');
```

---

## 2. organizations (조직)

### 테이블 설명
판매자, 입점회사, 내부 관리 조직 등을 관리하는 테이블입니다. 테넌트 하위에 속하며, 각 조직은 독립적인 파일 관리 정책을 가질 수 있습니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 조직 ID |
| tenant_id | VARCHAR(50) | FK, NOT NULL | - | 소속 테넌트 ID |
| org_code | VARCHAR(100) | UK, NOT NULL | - | 조직 코드 (고유값) |
| name | VARCHAR(200) | NOT NULL | - | 조직명 |
| org_type | ENUM('SELLER', 'COMPANY', 'INTERNAL', 'CUSTOMER') | NOT NULL | - | 조직 유형 |
| business_number | VARCHAR(50) | NULL | NULL | 사업자등록번호 |
| contract_start_date | DATE | NULL | NULL | 계약 시작일 |
| contract_end_date | DATE | NULL | NULL | 계약 종료일 |
| status | ENUM('PENDING', 'ACTIVE', 'INACTIVE', 'SUSPENDED', 'TERMINATED') | NOT NULL | 'PENDING' | 조직 상태 |
| tier | ENUM('BASIC', 'STANDARD', 'PREMIUM', 'ENTERPRISE') | NULL | 'BASIC' | 서비스 등급 |
| metadata | JSON | NULL | '{}' | 추가 메타데이터 |
| api_key | VARCHAR(255) | NULL | NULL | API 인증키 (암호화) |
| webhook_url | VARCHAR(500) | NULL | NULL | 웹훅 URL |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |
| created_by | VARCHAR(100) | NULL | NULL | 생성자 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_org_code (org_code)
UNIQUE KEY uk_tenant_org_code (tenant_id, org_code)
INDEX idx_tenant_id (tenant_id)
INDEX idx_org_type_status (org_type, status)
INDEX idx_contract_dates (contract_start_date, contract_end_date)
-- 외래키 제거: 운영 편의성 및 확장성을 위해 FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

### 샘플 데이터
```sql
INSERT INTO organizations (tenant_id, org_code, name, org_type, business_number, status, tier) VALUES
('b2c_kr', 'SELLER_001', '패션플러스', 'SELLER', '123-45-67890', 'ACTIVE', 'PREMIUM'),
('b2c_kr', 'INTERNAL_ADMIN', '내부 관리팀', 'INTERNAL', NULL, 'ACTIVE', 'ENTERPRISE'),
('b2b_global', 'COMPANY_001', 'Global Trade Co.', 'COMPANY', '987-65-43210', 'ACTIVE', 'ENTERPRISE');
```

---

## 3. tenant_settings (테넌트 설정)

### 테이블 설명
테넌트별 세부 설정을 키-값 형태로 저장하는 테이블입니다. 유연한 설정 관리를 위해 EAV(Entity-Attribute-Value) 패턴을 사용합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 설정 ID |
| tenant_id | VARCHAR(50) | FK, NOT NULL | - | 테넌트 ID |
| setting_key | VARCHAR(100) | NOT NULL | - | 설정 키 |
| setting_value | TEXT | NULL | NULL | 설정 값 |
| value_type | ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'DATE') | NOT NULL | 'STRING' | 값 타입 |
| description | TEXT | NULL | NULL | 설정 설명 |
| is_encrypted | BOOLEAN | NOT NULL | FALSE | 암호화 여부 |
| is_system | BOOLEAN | NOT NULL | FALSE | 시스템 설정 여부 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |
| updated_by | VARCHAR(100) | NULL | NULL | 수정자 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_tenant_setting_key (tenant_id, setting_key)
INDEX idx_setting_key (setting_key)
INDEX idx_value_type (value_type)
-- 외래키 제거: 참조 무결성은 애플리케이션 레벨에서 검증
```

### 샘플 데이터
```sql
INSERT INTO tenant_settings (tenant_id, setting_key, setting_value, value_type, description) VALUES
('b2c_kr', 'max_file_size_mb', '100', 'NUMBER', '최대 파일 크기 (MB)'),
('b2c_kr', 'allowed_file_types', '["jpg","jpeg","png","webp","html","pdf"]', 'JSON', '허용 파일 타입'),
('b2c_kr', 'enable_ocr', 'true', 'BOOLEAN', 'OCR 기능 활성화'),
('b2c_kr', 'default_pipeline', 'image_optimization_v2', 'STRING', '기본 파이프라인'),
('b2b_global', 'excel_processing_enabled', 'true', 'BOOLEAN', 'Excel 처리 활성화'),
('b2b_global', 'ai_mapping_confidence_threshold', '0.85', 'NUMBER', 'AI 매핑 신뢰도 임계값');
```

---

## 4. organization_settings (조직 설정)

### 테이블 설명
조직별 세부 설정을 저장합니다. 테넌트 설정을 상속받되, 조직별로 오버라이드할 수 있습니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 설정 ID |
| organization_id | BIGINT | FK, NOT NULL | - | 조직 ID |
| setting_key | VARCHAR(100) | NOT NULL | - | 설정 키 |
| setting_value | TEXT | NULL | NULL | 설정 값 |
| value_type | ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'DATE') | NOT NULL | 'STRING' | 값 타입 |
| is_inherited | BOOLEAN | NOT NULL | TRUE | 테넌트 설정 상속 여부 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_org_setting_key (organization_id, setting_key)
INDEX idx_org_setting_key (setting_key)
-- 외래키 제거: 참조 무결성은 애플리케이션 레벨에서 검증
```

### 샘플 데이터
```sql
INSERT INTO organization_settings (organization_id, setting_key, setting_value, value_type, is_inherited) VALUES
(1, 'max_file_size_mb', '200', 'NUMBER', FALSE),  -- 판매자별 커스텀 설정
(1, 'watermark_enabled', 'true', 'BOOLEAN', FALSE),
(1, 'custom_cdn_domain', 'cdn.fashionplus.co.kr', 'STRING', FALSE),
(2, 'bulk_upload_limit', '1000', 'NUMBER', FALSE);
```

---

## 5. 관계 다이어그램

```
tenants (1) ──────< (N) organizations
   │                         │
   │                         │
   └──< tenant_settings     └──< organization_settings
```

## 6. 비즈니스 로직

### 6.1 테넌트 생성 프로세스
1. 테넌트 기본 정보 생성
2. 기본 설정값 초기화 (tenant_settings)
3. 내부 관리 조직 자동 생성
4. 기본 정책 및 파이프라인 설정

### 6.2 조직 가입 프로세스
1. 조직 정보 등록
2. 계약 정보 설정
3. API 키 발급
4. 기본 업로드 정책 할당
5. 웹훅 설정 (선택사항)

### 6.3 설정 우선순위
1. Organization Settings (최우선)
2. Tenant Settings
3. System Defaults (최하위)

## 7. 보안 고려사항

- API 키는 반드시 암호화하여 저장
- 민감한 설정값은 `is_encrypted` 플래그 사용
- 테넌트 간 데이터 격리 철저히 보장
- 조직 상태 변경 시 감사 로그 기록

## 8. Setting Schema 검증 (추가 권장)

### 8.1 설정 스키마 정의 테이블

```sql
CREATE TABLE setting_schemas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    value_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'DATE') NOT NULL,
    json_schema JSON NULL COMMENT 'JSON Schema for validation',
    default_value TEXT NULL,
    is_required BOOLEAN DEFAULT FALSE,
    allowed_values JSON NULL COMMENT 'Enum values (e.g., ["BASIC", "PREMIUM"])',
    validation_regex VARCHAR(500) NULL,
    min_value DECIMAL(20,4) NULL COMMENT 'For NUMBER type',
    max_value DECIMAL(20,4) NULL COMMENT 'For NUMBER type',
    description TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_setting_key (setting_key),
    INDEX idx_value_type (value_type)
) COMMENT = '설정 키 스키마 정의 (타입 및 검증 규칙)';
```

### 8.2 스키마 샘플 데이터

```sql
INSERT INTO setting_schemas (setting_key, value_type, json_schema, default_value, min_value, max_value, description) VALUES
-- 숫자 타입 검증
('max_file_size_mb', 'NUMBER', '{"type": "integer", "minimum": 1, "maximum": 5000}', '100', 1, 5000, '최대 파일 크기 (MB)'),
('ai_mapping_confidence_threshold', 'NUMBER', '{"type": "number", "minimum": 0.0, "maximum": 1.0}', '0.85', 0.0, 1.0, 'AI 매핑 신뢰도 임계값'),

-- JSON 배열 검증
('allowed_file_types', 'JSON', '{"type": "array", "items": {"type": "string", "enum": ["jpg","jpeg","png","webp","html","pdf","xlsx"]}}', '["jpg","png"]', NULL, NULL, '허용 파일 타입'),

-- Boolean 검증
('enable_ocr', 'BOOLEAN', '{"type": "boolean"}', 'false', NULL, NULL, 'OCR 기능 활성화 여부'),

-- String with regex 검증
('default_pipeline', 'STRING', '{"type": "string", "pattern": "^[a-z_]+_v[0-9]+$"}', 'image_optimization_v2', NULL, NULL, '기본 파이프라인 (예: image_optimization_v2)'),

-- Enum 검증
('storage_provider', 'STRING', '{"type": "string", "enum": ["S3", "GCS", "AZURE"]}', 'S3', NULL, NULL, '스토리지 제공자');
```

### 8.3 애플리케이션 레벨 검증 전략

#### 검증 시점
1. **Admin UI에서 설정 변경 시**: 실시간 검증
2. **API를 통한 설정 변경 시**: Request 검증
3. **설정 로드 시**: 애플리케이션 시작 시 검증
4. **배포 전**: CI/CD 파이프라인 검증

#### 검증 로직 (Java 예시)
```java
@Service
public class SettingValidationService {

    // JSON Schema 검증 (everit-org/json-schema 라이브러리)
    public void validateSetting(String key, String value, String valueType) {
        SettingSchema schema = schemaRepository.findBySettingKey(key)
            .orElseThrow(() -> new InvalidSettingKeyException(key));

        // 1. 타입 검증
        validateType(value, valueType, schema.getValueType());

        // 2. JSON Schema 검증
        if (schema.getJsonSchema() != null) {
            JSONObject jsonSchema = new JSONObject(schema.getJsonSchema());
            Schema validator = SchemaLoader.load(jsonSchema);
            validator.validate(new JSONObject(value)); // ValidationException 발생
        }

        // 3. 범위 검증 (숫자)
        if ("NUMBER".equals(valueType)) {
            validateNumberRange(value, schema.getMinValue(), schema.getMaxValue());
        }

        // 4. Regex 검증
        if (schema.getValidationRegex() != null) {
            if (!value.matches(schema.getValidationRegex())) {
                throw new SettingValidationException(key, "Regex validation failed");
            }
        }
    }
}
```

#### Admin UI 통합
```javascript
// Frontend: 설정 변경 시 실시간 검증
async function validateSetting(key, value, valueType) {
    const response = await fetch('/api/settings/validate', {
        method: 'POST',
        body: JSON.stringify({ key, value, valueType })
    });

    if (!response.ok) {
        const error = await response.json();
        showValidationError(error.message); // "max_file_size_mb must be between 1 and 5000"
        return false;
    }
    return true;
}
```

### 8.4 설정 변경 이력 추적 (옵션)

```sql
CREATE TABLE setting_change_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(50) NULL,
    organization_id BIGINT NULL,
    setting_key VARCHAR(100) NOT NULL,
    old_value TEXT NULL,
    new_value TEXT NOT NULL,
    changed_by BIGINT NOT NULL COMMENT 'user_id',
    change_reason TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_tenant_setting (tenant_id, setting_key, created_at DESC),
    INDEX idx_org_setting (organization_id, setting_key, created_at DESC)
) COMMENT = '설정 변경 이력';
```

## 9. 참조 무결성 검증

### 9.1 애플리케이션 레벨 검증

**외래키를 제거했으므로, 참조 무결성은 애플리케이션에서 보장합니다.**

#### 조직 생성 시 검증
```java
@Service
public class OrganizationService {

    @Transactional
    public Organization createOrganization(CreateOrganizationCommand command) {
        // 1. 테넌트 존재 여부 검증
        if (!tenantRepository.existsById(command.getTenantId())) {
            throw new TenantNotFoundException(command.getTenantId());
        }

        // 2. 조직 생성
        Organization org = Organization.create(command);
        return organizationRepository.save(org);
    }
}
```

#### 조직 삭제 시 연관 데이터 정리
```java
@Service
public class OrganizationDeletionService {

    @Transactional
    public void deleteOrganization(Long orgId) {
        // 1. 연관된 파일 정리 (soft delete)
        fileAssetRepository.updateDeletedAtByOrganization(orgId, LocalDateTime.now());

        // 2. 연관된 사용자 비활성화
        userContextRepository.deactivateByOrganization(orgId);

        // 3. 조직 설정 삭제
        organizationSettingRepository.deleteByOrganizationId(orgId);

        // 4. 조직 삭제 (soft delete)
        organizationRepository.softDelete(orgId);

        // 5. 감사 로그 기록
        auditLogService.logOrganizationDeletion(orgId);
    }
}
```

### 9.2 주기적 데이터 정합성 검증 (배치)

```sql
-- 고아 레코드(Orphan Records) 검증 쿼리
SELECT 'organizations_orphan' as issue_type, COUNT(*) as count
FROM organizations o
LEFT JOIN tenants t ON o.tenant_id = t.tenant_id
WHERE t.tenant_id IS NULL

UNION ALL

SELECT 'tenant_settings_orphan', COUNT(*)
FROM tenant_settings ts
LEFT JOIN tenants t ON ts.tenant_id = t.tenant_id
WHERE t.tenant_id IS NULL

UNION ALL

SELECT 'org_settings_orphan', COUNT(*)
FROM organization_settings os
LEFT JOIN organizations o ON os.organization_id = o.id
WHERE o.id IS NULL;
```

**배치 스케줄:**
- 실행 주기: 매일 새벽 3시
- 발견 시 조치: Slack 알람 + 관리자 대시보드 표시
- 자동 정리: 고아 레코드 30일 이상 → 자동 삭제

## 10. 성능 최적화

- 자주 조회되는 설정은 Redis 캐싱
- 조직 조회 시 테넌트 정보 JOIN 최소화
- 설정값은 애플리케이션 시작 시 로드 및 캐싱
- 대량 조직 조회 시 페이징 처리 필수
