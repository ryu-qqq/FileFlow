# Flyway Database Migration Scripts

이 디렉토리는 FileFlow 프로젝트의 데이터베이스 스키마 버전 관리를 위한 Flyway 마이그레이션 스크립트를 포함합니다.

## 📋 Migration Overview

| Version | Script | Description | Tables |
|---------|--------|-------------|--------|
| V1 | `V1__create_tenant_table.sql` | 테넌트 테이블 생성 | `tenant` |
| V2 | `V2__create_upload_policy_table.sql` | 업로드 정책 테이블 생성 | `upload_policy` |
| V3 | `V3__create_processing_policy_table.sql` | 처리 정책 테이블 생성 | `processing_policy` |
| V4 | `V4__create_policy_change_log_table.sql` | 정책 변경 로그 테이블 생성 | `policy_change_log` |
| V5 | `V5__insert_initial_data.sql` | 초기 데이터 삽입 | - |

## 🎯 Schema Design

### 1. Tenant Table (V1)
**목적**: 멀티 테넌시 지원을 위한 테넌트 관리

```sql
tenant_id (PK, VARCHAR(50))
├─ name (VARCHAR(100))
├─ created_at (DATETIME)
└─ updated_at (DATETIME)
```

**특징**:
- 비즈니스 키(`tenant_id`)를 Primary Key로 사용
- 타임스탬프 자동 관리 (`DEFAULT CURRENT_TIMESTAMP`, `ON UPDATE CURRENT_TIMESTAMP`)

### 2. Upload Policy Table (V2)
**목적**: 파일 업로드 정책 관리 (테넌트별, 사용자 타입별, 서비스별)

```sql
policy_key (PK, VARCHAR(200))  # Format: {tenantId}:{userType}:{serviceType}
├─ file_type_policies (JSON)   # 파일 타입별 정책 (maxSize, maxCount, allowedExtensions)
├─ rate_limiting (JSON)         # Rate limiting 정책 (requestsPerHour, uploadsPerDay)
├─ effective_from (DATETIME)
├─ effective_until (DATETIME)
├─ version (INT)
└─ is_active (TINYINT)
```

**인덱스**:
- `idx_upload_policy_is_active`: 활성 정책 빠른 조회
- `idx_upload_policy_effective_period`: 유효 기간 범위 검색

**특징**:
- MySQL 8.0 JSON 타입 활용 (검증 및 쿼리 최적화)
- 복합 키 구조로 계층적 정책 관리
- JPA 낙관적 락을 위한 version 컬럼

### 3. Processing Policy Table (V3)
**목적**: 파일 처리 정책 관리 (향후 확장용)

```sql
policy_key (PK, VARCHAR(200))
├─ processing_config (JSON)
├─ created_at (DATETIME)
└─ updated_at (DATETIME)
```

**특징**:
- 확장 가능한 구조 (현재는 기본 스키마만)
- Upload Policy와 1:1 관계

### 4. Policy Change Log Table (V4)
**목적**: 정책 변경 이력 추적 (감사 로그)

```sql
id (PK, BIGINT AUTO_INCREMENT)
├─ policy_key (VARCHAR(200))
├─ change_type (VARCHAR(50))    # CREATE, UPDATE, DELETE, ACTIVATE, DEACTIVATE
├─ old_version (INT)
├─ new_version (INT)
├─ old_value (JSON)
├─ new_value (JSON)
├─ changed_by (VARCHAR(100))
└─ changed_at (DATETIME)
```

**인덱스**:
- `idx_policy_change_log_policy_key`: 특정 정책의 변경 이력 조회
- `idx_policy_change_log_changed_at`: 시간 범위 기반 감사

**특징**:
- 정책 변경 전후 스냅샷 저장
- 규정 준수 및 감사 요구사항 충족

### 5. Initial Data (V5)
**목적**: 시스템 기본 데이터 삽입

**Tenants**:
- `b2b`: B2B Platform
- `b2c`: B2C Platform

**Upload Policies**:
1. `b2c:CONSUMER:REVIEW`: 소비자 리뷰 이미지 업로드
   - IMAGE: 10MB, 최대 5개
   - Rate: 100 req/hour, 50 uploads/day

2. `b2c:SELLER:PRODUCT`: 판매자 상품 등록
   - IMAGE: 20MB, 최대 10개
   - PDF: 50MB, 최대 3개
   - Rate: 200 req/hour, 100 uploads/day

3. `b2c:CRAWLER:PRODUCT`: 크롤러 상품 수집
   - IMAGE: 100MB, 최대 50개
   - HTML: 10MB, 최대 10개
   - Rate: 1000 req/hour, 10000 uploads/day

4. `b2b:BUYER:ORDER_SHEET`: 바이어 발주서 업로드
   - EXCEL: 50MB, 최대 5개
   - PDF: 20MB, 최대 3개
   - Rate: 100 req/hour, 200 uploads/day

## 🔧 Naming Convention

### File Naming
```
V{version}__{description}.sql
```
- `V`: Version prefix (필수)
- `{version}`: 순차적 버전 번호 (1, 2, 3...)
- `__`: 구분자 (더블 언더스코어)
- `{description}`: snake_case 설명

### Policy Key Format
```
{tenantId}:{userType}:{serviceType}
```
- `tenantId`: b2c, b2b
- `userType`: CONSUMER, SELLER, CRAWLER, BUYER
- `serviceType`: REVIEW, PRODUCT, ORDER_SHEET

## ✅ Validation

### 테스트 실행
```bash
./gradlew :adapter-out-persistence-jpa:test --tests FlywayMigrationTest
```

### 검증 항목
- ✅ 테이블 스키마 (컬럼 타입, NULL 여부, 기본값)
- ✅ 인덱스 존재 및 구조
- ✅ 초기 데이터 정합성
- ✅ JSON 데이터 형식 및 필드
- ✅ Flyway 마이그레이션 히스토리
- ✅ 문자셋 (UTF8MB4)

### 커버리지
- 11개 통합 테스트 (모두 통과)
- Testcontainers MySQL 8.0 기반

## 🚨 Best Practices

### DO
- ✅ 스크립트는 멱등성(idempotent) 보장
- ✅ 타임스탬프는 DB 레벨에서 관리
- ✅ JSON 타입으로 구조화된 데이터 저장
- ✅ 인덱스는 쿼리 패턴 기반 설계
- ✅ 프로덕션 적용 전 테스트 환경 검증

### DON'T
- ❌ 이미 적용된 마이그레이션 수정 금지
- ❌ 프로덕션에서 `flyway.clean` 사용 금지
- ❌ 순서 변경 또는 버전 건너뛰기 금지
- ❌ 트랜잭션 없이 데이터 변경 금지

## 🔄 Rollback Strategy

Flyway는 기본적으로 rollback을 지원하지 않습니다. 다음 전략을 사용하세요:

1. **새 마이그레이션 생성**: 되돌리기 위한 새 버전 스크립트 작성
   ```sql
   -- V6__rollback_something.sql
   DROP TABLE IF EXISTS new_table;
   ```

2. **버전 관리**: Git을 통한 코드 리뷰 및 승인 프로세스
3. **백업**: 마이그레이션 전 데이터베이스 백업
4. **테스트**: 프로덕션 적용 전 스테이징 환경 검증

## 📚 References

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [MySQL 8.0 JSON Type](https://dev.mysql.com/doc/refman/8.0/en/json.html)
- [Spring Boot Flyway Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
