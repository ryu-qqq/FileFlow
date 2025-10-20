# 📝 감사 & 로깅 테이블 명세

## 1. audit_logs (감사 로그)

### 테이블 설명
시스템 전반의 중요한 작업과 변경사항을 추적하는 감사 로그 테이블입니다. 보안 및 규정 준수를 위한 핵심 테이블입니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 로그 ID |
| audit_id | VARCHAR(36) | UK, NOT NULL | UUID() | 감사 고유 식별자 |
| tenant_id | VARCHAR(50) | FK, NULL | NULL | 테넌트 ID |
| organization_id | BIGINT | FK, NULL | NULL | 조직 ID |
| user_id | BIGINT | FK, NULL | NULL | 사용자 ID |
| user_type | VARCHAR(50) | NULL | NULL | 사용자 타입 |
| username | VARCHAR(100) | NOT NULL | - | 사용자명 (변경 방지용) |
| action_type | ENUM('CREATE', 'READ', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT', 'UPLOAD', 'DOWNLOAD', 'APPROVE', 'REJECT', 'SYSTEM') | NOT NULL | - | 작업 타입 |
| resource_type | VARCHAR(50) | NOT NULL | - | 리소스 타입 (file, user, policy 등) |
| resource_id | VARCHAR(100) | NULL | NULL | 리소스 ID |
| resource_name | VARCHAR(255) | NULL | NULL | 리소스 명칭 |
| action_detail | VARCHAR(500) | NOT NULL | - | 작업 상세 설명 |
| old_value | JSON | NULL | NULL | 변경 전 값 |
| new_value | JSON | NULL | NULL | 변경 후 값 |
| change_summary | JSON | NULL | NULL | 변경 요약 |
| request_method | VARCHAR(10) | NULL | NULL | HTTP 메서드 |
| request_uri | VARCHAR(500) | NULL | NULL | 요청 URI |
| request_params | JSON | NULL | NULL | 요청 파라미터 |
| ip_address | VARCHAR(45) | NOT NULL | - | IP 주소 |
| user_agent | TEXT | NULL | NULL | User Agent |
| session_id | VARCHAR(100) | NULL | NULL | 세션 ID |
| correlation_id | VARCHAR(36) | NULL | NULL | 상관관계 ID (분산 추적) |
| response_code | INT | NULL | NULL | 응답 코드 |
| response_time_ms | INT | NULL | NULL | 응답 시간 (밀리초) |
| error_message | TEXT | NULL | NULL | 에러 메시지 |
| risk_level | ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') | NOT NULL | 'LOW' | 위험 수준 |
| compliance_tags | JSON | NULL | '[]' | 규정 준수 태그 |
| metadata | JSON | NULL | '{}' | 추가 메타데이터 |
| is_sensitive | BOOLEAN | NOT NULL | FALSE | 민감 정보 포함 여부 |
| retention_days | INT | NOT NULL | 2555 | 보관 기간 (일, 기본 7년) |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_audit_id (audit_id)
INDEX idx_tenant_org (tenant_id, organization_id, created_at DESC)
INDEX idx_user_id (user_id, created_at DESC)
INDEX idx_action_type (action_type, created_at DESC)
INDEX idx_resource (resource_type, resource_id)
INDEX idx_ip_address (ip_address, created_at DESC)
INDEX idx_risk_level (risk_level, created_at DESC)
INDEX idx_created_at (created_at DESC)
-- 파티셔닝: 월별
PARTITION BY RANGE (TO_DAYS(created_at))
```

### 샘플 데이터
```sql
INSERT INTO audit_logs (tenant_id, user_id, username, action_type, resource_type, resource_id, action_detail, ip_address, risk_level) VALUES
('b2c_kr', 1, 'seller001', 'UPLOAD', 'file', 'f123-456-789', '상품 이미지 업로드', '192.168.1.100', 'LOW'),
('b2c_kr', 2, 'admin001', 'UPDATE', 'policy', 'p987-654-321', '업로드 정책 수정', '192.168.1.101', 'MEDIUM'),
('b2b_global', 3, 'company001', 'DOWNLOAD', 'file', 'f456-789-012', 'Excel 파일 다운로드', '203.0.113.1', 'LOW');
```

---

## 2. access_logs (접근 로그)

### 테이블 설명
파일 및 리소스에 대한 모든 접근 기록을 저장합니다. 성능 분석과 보안 모니터링에 활용됩니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 로그 ID |
| tenant_id | VARCHAR(50) | FK, NULL | NULL | 테넌트 ID |
| file_id | BIGINT | FK, NULL | NULL | 파일 ID |
| user_id | BIGINT | FK, NULL | NULL | 사용자 ID |
| access_type | ENUM('VIEW', 'DOWNLOAD', 'PREVIEW', 'SHARE', 'EMBED') | NOT NULL | - | 접근 타입 |
| access_method | ENUM('WEB', 'API', 'DIRECT', 'CDN') | NOT NULL | - | 접근 방법 |
| request_uri | VARCHAR(2048) | NOT NULL | - | 요청 URI |
| query_params | TEXT | NULL | NULL | 쿼리 파라미터 |
| referer | TEXT | NULL | NULL | Referer |
| ip_address | VARCHAR(45) | NOT NULL | - | IP 주소 |
| country_code | VARCHAR(2) | NULL | NULL | 국가 코드 |
| user_agent | TEXT | NULL | NULL | User Agent |
| device_type | ENUM('DESKTOP', 'MOBILE', 'TABLET', 'BOT', 'UNKNOWN') | NULL | 'UNKNOWN' | 디바이스 타입 |
| browser | VARCHAR(50) | NULL | NULL | 브라우저 |
| os | VARCHAR(50) | NULL | NULL | 운영체제 |
| response_code | INT | NOT NULL | - | HTTP 응답 코드 |
| response_size | BIGINT | NULL | NULL | 응답 크기 (bytes) |
| response_time_ms | INT | NULL | NULL | 응답 시간 (밀리초) |
| cache_status | ENUM('HIT', 'MISS', 'BYPASS', 'EXPIRED') | NULL | NULL | 캐시 상태 |
| cdn_pop | VARCHAR(50) | NULL | NULL | CDN PoP 위치 |
| ssl_protocol | VARCHAR(20) | NULL | NULL | SSL 프로토콜 |
| error_message | TEXT | NULL | NULL | 에러 메시지 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 접근 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
INDEX idx_tenant_id (tenant_id, created_at DESC)
INDEX idx_file_id (file_id, created_at DESC)
INDEX idx_user_id (user_id, created_at DESC)
INDEX idx_access_type (access_type, created_at DESC)
INDEX idx_ip_address (ip_address, created_at DESC)
INDEX idx_response_code (response_code)
INDEX idx_created_at (created_at DESC)
-- 파티셔닝: 일별
PARTITION BY RANGE (TO_DAYS(created_at))
```

---

## 3. processing_errors (처리 오류 로그)

### 테이블 설명
파이프라인 처리 중 발생한 오류를 상세히 기록합니다. 디버깅과 개선을 위한 정보를 제공합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 오류 ID |
| error_id | VARCHAR(36) | UK, NOT NULL | UUID() | 오류 고유 식별자 |
| execution_id | BIGINT | FK, NULL | NULL | 파이프라인 실행 ID |
| file_id | BIGINT | FK, NULL | NULL | 파일 ID |
| stage_id | BIGINT | FK, NULL | NULL | 파이프라인 단계 ID |
| error_type | ENUM('VALIDATION', 'PROCESSING', 'TIMEOUT', 'RESOURCE', 'DEPENDENCY', 'SYSTEM', 'UNKNOWN') | NOT NULL | - | 오류 타입 |
| error_code | VARCHAR(50) | NOT NULL | - | 오류 코드 |
| error_message | TEXT | NOT NULL | - | 오류 메시지 |
| error_details | JSON | NULL | NULL | 상세 오류 정보 |
| stack_trace | TEXT | NULL | NULL | 스택 트레이스 |
| context_data | JSON | NULL | NULL | 컨텍스트 데이터 |
| severity | ENUM('DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL') | NOT NULL | 'ERROR' | 심각도 |
| component | VARCHAR(100) | NULL | NULL | 컴포넌트명 |
| host_name | VARCHAR(255) | NULL | NULL | 호스트명 |
| process_id | VARCHAR(50) | NULL | NULL | 프로세스 ID |
| thread_id | VARCHAR(50) | NULL | NULL | 스레드 ID |
| retry_count | INT | NOT NULL | 0 | 재시도 횟수 |
| is_resolved | BOOLEAN | NOT NULL | FALSE | 해결 여부 |
| resolved_at | DATETIME | NULL | NULL | 해결 시각 |
| resolved_by | BIGINT | FK, NULL | NULL | 해결자 ID |
| resolution_notes | TEXT | NULL | NULL | 해결 노트 |
| tags | JSON | NULL | '[]' | 태그 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 발생 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_error_id (error_id)
INDEX idx_execution_id (execution_id)
INDEX idx_file_id (file_id)
INDEX idx_stage_id (stage_id)
INDEX idx_error_type (error_type, created_at DESC)
INDEX idx_error_code (error_code)
INDEX idx_severity (severity, is_resolved)
INDEX idx_is_resolved (is_resolved, created_at DESC)
INDEX idx_created_at (created_at DESC)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

---

## 4. security_events (보안 이벤트)

### 테이블 설명
보안 관련 이벤트를 전문적으로 추적하는 테이블입니다. 침입 시도, 비정상 접근 등을 기록합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 이벤트 ID |
| event_id | VARCHAR(36) | UK, NOT NULL | UUID() | 이벤트 고유 식별자 |
| event_type | ENUM('LOGIN_FAILED', 'UNAUTHORIZED_ACCESS', 'PERMISSION_DENIED', 'SUSPICIOUS_ACTIVITY', 'DATA_BREACH', 'MALWARE_DETECTED', 'BRUTE_FORCE', 'SQL_INJECTION', 'XSS_ATTEMPT') | NOT NULL | - | 이벤트 타입 |
| severity | ENUM('INFO', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL') | NOT NULL | - | 심각도 |
| user_id | BIGINT | FK, NULL | NULL | 관련 사용자 ID |
| target_resource | VARCHAR(255) | NULL | NULL | 대상 리소스 |
| attack_vector | VARCHAR(100) | NULL | NULL | 공격 벡터 |
| ip_address | VARCHAR(45) | NOT NULL | - | IP 주소 |
| geo_location | JSON | NULL | NULL | 지리적 위치 |
| user_agent | TEXT | NULL | NULL | User Agent |
| request_data | JSON | NULL | NULL | 요청 데이터 |
| threat_indicators | JSON | NULL | NULL | 위협 지표 |
| detection_method | VARCHAR(100) | NULL | NULL | 탐지 방법 |
| response_action | ENUM('BLOCKED', 'ALLOWED', 'MONITORED', 'QUARANTINED') | NULL | NULL | 대응 조치 |
| is_false_positive | BOOLEAN | NULL | NULL | 오탐 여부 |
| investigation_status | ENUM('PENDING', 'INVESTIGATING', 'RESOLVED', 'ESCALATED') | NOT NULL | 'PENDING' | 조사 상태 |
| investigation_notes | TEXT | NULL | NULL | 조사 노트 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 발생 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_event_id (event_id)
INDEX idx_event_type (event_type, created_at DESC)
INDEX idx_severity (severity, investigation_status)
INDEX idx_user_id (user_id)
INDEX idx_ip_address (ip_address, created_at DESC)
INDEX idx_investigation_status (investigation_status)
INDEX idx_created_at (created_at DESC)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

---

## 5. performance_metrics (성능 메트릭)

### 테이블 설명
시스템 및 애플리케이션 성능 메트릭을 수집합니다. 성능 모니터링과 최적화에 활용됩니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 메트릭 ID |
| metric_type | VARCHAR(50) | NOT NULL | - | 메트릭 타입 |
| metric_name | VARCHAR(100) | NOT NULL | - | 메트릭명 |
| metric_value | DECIMAL(20,4) | NOT NULL | - | 메트릭 값 |
| unit | VARCHAR(20) | NULL | NULL | 단위 |
| component | VARCHAR(100) | NOT NULL | - | 컴포넌트 |
| host_name | VARCHAR(255) | NULL | NULL | 호스트명 |
| tags | JSON | NULL | '{}' | 태그 |
| dimensions | JSON | NULL | '{}' | 차원 정보 |
| timestamp | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 타임스탬프 |

### 인덱스
```sql
PRIMARY KEY (id)
INDEX idx_metric_type (metric_type, timestamp DESC)
INDEX idx_metric_name (metric_name, timestamp DESC)
INDEX idx_component (component, timestamp DESC)
INDEX idx_timestamp (timestamp DESC)
-- 파티셔닝: 일별
PARTITION BY RANGE (TO_DAYS(timestamp))
```

---

## 6. api_usage_logs (API 사용 로그)

### 테이블 설명
API 호출 기록을 저장합니다. Rate limiting과 사용량 분석에 활용됩니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 로그 ID |
| tenant_id | VARCHAR(50) | FK, NULL | NULL | 테넌트 ID |
| organization_id | BIGINT | FK, NULL | NULL | 조직 ID |
| api_key | VARCHAR(100) | NULL | NULL | API 키 (해시) |
| endpoint | VARCHAR(255) | NOT NULL | - | API 엔드포인트 |
| method | VARCHAR(10) | NOT NULL | - | HTTP 메서드 |
| request_id | VARCHAR(36) | UK, NOT NULL | UUID() | 요청 ID |
| request_size | BIGINT | NULL | NULL | 요청 크기 (bytes) |
| response_size | BIGINT | NULL | NULL | 응답 크기 (bytes) |
| response_code | INT | NOT NULL | - | 응답 코드 |
| response_time_ms | INT | NOT NULL | - | 응답 시간 (밀리초) |
| rate_limit_remaining | INT | NULL | NULL | 남은 Rate Limit |
| ip_address | VARCHAR(45) | NOT NULL | - | IP 주소 |
| user_agent | TEXT | NULL | NULL | User Agent |
| error_message | TEXT | NULL | NULL | 에러 메시지 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 호출 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_request_id (request_id)
INDEX idx_tenant_org (tenant_id, organization_id, created_at DESC)
INDEX idx_api_key (api_key, created_at DESC)
INDEX idx_endpoint (endpoint, method, created_at DESC)
INDEX idx_response_code (response_code)
INDEX idx_created_at (created_at DESC)
-- 파티셔닝: 시간별
PARTITION BY RANGE (UNIX_TIMESTAMP(created_at))
```

---

## 7. compliance_logs (규정 준수 로그)

### 테이블 설명
법적 규정 준수를 위한 특별 로그를 관리합니다. GDPR, CCPA 등의 요구사항을 충족합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 로그 ID |
| compliance_type | ENUM('GDPR', 'CCPA', 'HIPAA', 'PCI_DSS', 'SOC2', 'ISO27001', 'CUSTOM') | NOT NULL | - | 규정 타입 |
| event_type | VARCHAR(100) | NOT NULL | - | 이벤트 타입 |
| data_subject_id | VARCHAR(100) | NULL | NULL | 데이터 주체 ID |
| data_category | VARCHAR(100) | NULL | NULL | 데이터 카테고리 |
| action | VARCHAR(100) | NOT NULL | - | 수행 작업 |
| lawful_basis | VARCHAR(100) | NULL | NULL | 법적 근거 |
| consent_id | VARCHAR(100) | NULL | NULL | 동의 ID |
| purpose | TEXT | NULL | NULL | 처리 목적 |
| data_controller | VARCHAR(200) | NULL | NULL | 데이터 컨트롤러 |
| data_processor | VARCHAR(200) | NULL | NULL | 데이터 프로세서 |
| retention_period | INT | NULL | NULL | 보관 기간 (일) |
| cross_border_transfer | BOOLEAN | NOT NULL | FALSE | 국경 간 전송 여부 |
| recipient_country | VARCHAR(2) | NULL | NULL | 수신국 코드 |
| safeguards | JSON | NULL | NULL | 보호 조치 |
| metadata | JSON | NULL | '{}' | 추가 메타데이터 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
INDEX idx_compliance_type (compliance_type, created_at DESC)
INDEX idx_data_subject (data_subject_id)
INDEX idx_consent_id (consent_id)
INDEX idx_created_at (created_at DESC)
```

---

## 8. 관계 다이어그램

```
audit_logs ← users
    │
    └─ tenants/organizations

access_logs ← files/users

processing_errors ← pipeline_executions/stages

security_events ← users

api_usage_logs ← tenants/organizations

compliance_logs (독립적)

performance_metrics (독립적)
```

## 9. 로그 관리 정책

### 9.1 보관 정책
- 감사 로그: 7년 (법적 요구사항)
- 접근 로그: 90일 (성능 분석)
- 처리 오류: 1년 (디버깅)
- 보안 이벤트: 3년 (보안 감사)
- API 사용: 30일 (과금/분석)
- 성능 메트릭: 7일 (실시간 모니터링)

### 9.2 아카이빙 전략
- 30일 이상: 압축 저장
- 90일 이상: Cold Storage 이동
- 1년 이상: Glacier 아카이브

### 9.3 익명화 정책
- PII 데이터 자동 마스킹
- IP 주소 부분 익명화
- 사용자 식별 정보 해싱

## 10. 모니터링 및 알림

### 10.1 실시간 모니터링
- 보안 이벤트 즉시 알림
- 임계값 초과 시 경고
- 이상 패턴 자동 감지

### 10.2 정기 보고서
- 일일 감사 요약
- 주간 보안 리포트
- 월간 규정 준수 보고서

### 10.3 대시보드
- 실시간 접근 통계
- 오류 발생 추이
- API 사용량 현황
- 성능 메트릭 시각화

---

## 11. 하이브리드 로그 전략

### 11.1 전략 개요

FileFlow는 **하이브리드 로깅 아키텍처**를 사용하여 실시간 조회, 장기 보관, 알림, 메트릭 수집을 각각 최적화된 스토리지로 분산합니다.

```
┌─────────────────────────────────────────────────────────────┐
│                   Application Layer                          │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌───────────────┐  ┌──────────────────┐ │
│  │   MySQL DB   │  │  CloudWatch   │  │   Prometheus     │ │
│  │ (단기 저장)    │  │ (실시간 알림)  │  │  (메트릭 수집)    │ │
│  └──────┬───────┘  └───────────────┘  └──────────────────┘ │
│         │                                                    │
│         │ 아카이빙 (30일 후)                                  │
│         ▼                                                    │
│  ┌──────────────┐                                           │
│  │   S3 Bucket  │                                           │
│  │ (장기 보관)    │                                           │
│  └──────────────┘                                           │
└─────────────────────────────────────────────────────────────┘
```

### 11.2 스토리지 역할 분담

#### MySQL Database (단기 실시간 저장)
**목적**: 실시간 조회 및 트랜잭션 로그

**대상 데이터**:
- `audit_logs`: 최근 30일
- `access_logs`: 최근 7일
- `processing_errors`: 최근 30일
- `security_events`: 최근 90일
- `api_usage_logs`: 최근 30일

**특징**:
- 빠른 인덱싱 및 조회
- 복잡한 쿼리 지원 (JOIN, GROUP BY 등)
- 트랜잭션 보장
- 파티셔닝을 통한 성능 최적화

**보관 정책**:
```sql
-- 파티션 자동 정리 예시 (audit_logs - 월별 파티션)
ALTER TABLE audit_logs DROP PARTITION p202401;

-- 오래된 로그 아카이빙 후 삭제
DELETE FROM access_logs WHERE created_at < DATE_SUB(NOW(), INTERVAL 7 DAY);
```

#### Amazon S3 (장기 아카이브)
**목적**: 저비용 장기 보관 및 규정 준수

**대상 데이터**:
- `audit_logs`: 30일 이후 → 7년 보관
- `access_logs`: 7일 이후 → 90일 보관
- `processing_errors`: 30일 이후 → 1년 보관
- `security_events`: 90일 이후 → 3년 보관

**저장 형식**:
- Parquet 포맷 (컬럼 기반 압축)
- Gzip 압축
- 연/월/일 파티셔닝

**경로 구조**:
```
s3://fileflow-logs-archive/
  ├── audit_logs/
  │   ├── year=2024/
  │   │   ├── month=01/
  │   │   │   ├── day=01/
  │   │   │   │   └── audit_logs_20240101_part1.parquet.gz
  │   │   │   └── day=02/
  │   │   └── month=02/
  ├── access_logs/
  └── processing_errors/
```

**S3 Lifecycle 정책**:
```json
{
  "Rules": [
    {
      "Id": "ArchiveAuditLogs",
      "Status": "Enabled",
      "Transitions": [
        {
          "Days": 90,
          "StorageClass": "STANDARD_IA"
        },
        {
          "Days": 365,
          "StorageClass": "GLACIER"
        }
      ],
      "Expiration": {
        "Days": 2555
      }
    }
  ]
}
```

#### AWS CloudWatch Logs (실시간 모니터링)
**목적**: 실시간 알림 및 이상 탐지

**대상 데이터**:
- `security_events`: 모든 이벤트 실시간 스트리밍
- `processing_errors`: severity >= ERROR
- `audit_logs`: risk_level >= HIGH

**특징**:
- 실시간 로그 스트리밍
- CloudWatch Alarms 연동
- Lambda 트리거 지원
- 로그 인사이트 쿼리

**알림 규칙 예시**:
```yaml
# CloudWatch Alarm 설정
SecurityEventAlarm:
  Threshold: 5 # 5분 내 5회 이상
  Metric: security_events.CRITICAL
  Action:
    - SNS: security-alerts-topic
    - Lambda: incident-response-handler

ProcessingErrorAlarm:
  Threshold: 10 # 5분 내 10회 이상
  Metric: processing_errors.ERROR
  Action:
    - SNS: engineering-alerts-topic
```

#### Prometheus + Grafana (메트릭 수집)
**목적**: 시스템 성능 메트릭 및 비즈니스 메트릭 시각화

**대상 데이터**:
- `performance_metrics`: 모든 메트릭
- `api_usage_logs`: API 호출 통계
- `access_logs`: 접근 패턴 통계

**메트릭 유형**:

1. **시스템 메트릭** (Prometheus Node Exporter)
   - CPU, Memory, Disk I/O
   - Network throughput
   - JVM metrics (heap, GC)

2. **애플리케이션 메트릭** (Custom Metrics)
   ```java
   // Micrometer를 통한 메트릭 수집
   @Timed(value = "file.upload", description = "File upload time")
   public FileAsset uploadFile(MultipartFile file) {
       Counter.builder("file.upload.total")
           .tag("tenant", tenantId)
           .register(meterRegistry)
           .increment();
       // ...
   }
   ```

3. **비즈니스 메트릭**
   - 파일 업로드 수/크기
   - 파이프라인 처리 성공/실패율
   - API 호출 횟수/응답시간

**Grafana 대시보드**:
- 실시간 업로드 현황
- 파이프라인 처리 상태
- API 성능 모니터링
- 에러율 추적

### 11.3 아카이빙 워크플로우

#### 자동 아카이빙 Job (Spring Batch)
```java
@Configuration
public class LogArchivingJobConfig {

    @Bean
    public Job auditLogArchivingJob() {
        return jobBuilderFactory.get("auditLogArchiving")
            .start(extractOldLogsStep())
            .next(transformToParquetStep())
            .next(uploadToS3Step())
            .next(deleteFromMysqlStep())
            .build();
    }

    @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시
    public void scheduleArchiving() {
        // 30일 이상 지난 audit_logs 아카이빙
        Date cutoffDate = DateUtils.addDays(new Date(), -30);
        jobLauncher.run(auditLogArchivingJob,
            new JobParametersBuilder()
                .addDate("cutoffDate", cutoffDate)
                .toJobParameters());
    }
}
```

#### 아카이빙 프로세스
```
1. MySQL에서 오래된 로그 조회 (배치 단위)
   ↓
2. Parquet 포맷으로 변환 및 압축
   ↓
3. S3에 업로드 (연/월/일 파티션)
   ↓
4. 업로드 성공 확인
   ↓
5. MySQL에서 삭제
   ↓
6. 아카이빙 메타데이터 기록
```

### 11.4 로그 조회 전략

#### 애플리케이션 레벨 라우팅
```java
@Service
public class AuditLogService {

    public List<AuditLog> searchAuditLogs(AuditLogSearchRequest request) {
        // 날짜 범위에 따라 스토리지 선택
        if (request.isRecentQuery()) {
            // 최근 30일: MySQL에서 직접 조회
            return auditLogRepository.search(request);
        } else {
            // 30일 이후: S3 Athena 쿼리
            return athenaQueryService.queryArchive(
                "audit_logs",
                request.toAthenaQuery()
            );
        }
    }
}
```

#### AWS Athena를 통한 S3 조회
```sql
-- Athena 테이블 정의 (Parquet 파일 위에 테이블 생성)
CREATE EXTERNAL TABLE audit_logs_archive (
    audit_id STRING,
    tenant_id STRING,
    user_id BIGINT,
    action_type STRING,
    resource_type STRING,
    created_at TIMESTAMP
)
PARTITIONED BY (year INT, month INT, day INT)
STORED AS PARQUET
LOCATION 's3://fileflow-logs-archive/audit_logs/';

-- 과거 로그 검색
SELECT * FROM audit_logs_archive
WHERE year = 2023
  AND month = 6
  AND tenant_id = 'b2c_kr'
  AND action_type = 'UPDATE'
ORDER BY created_at DESC
LIMIT 100;
```

### 11.5 실시간 알림 워크플로우

#### CloudWatch → SNS → Lambda
```python
# Lambda 함수: 보안 이벤트 자동 대응
def lambda_handler(event, context):
    security_event = parse_cloudwatch_log(event)

    if security_event['severity'] == 'CRITICAL':
        # 1. Slack 알림
        send_slack_alert(security_event)

        # 2. IP 차단 (WAF)
        block_ip_address(security_event['ip_address'])

        # 3. 사용자 계정 임시 잠금
        lock_user_account(security_event['user_id'])

        # 4. Incident 티켓 자동 생성
        create_jira_incident(security_event)

    return {'statusCode': 200}
```

### 11.6 비용 최적화

#### 스토리지 비용 추정 (월별)
```
가정:
- audit_logs: 1,000,000건/월 (각 1KB)
- access_logs: 10,000,000건/월 (각 500B)
- MySQL 보관: 30일
- S3 보관: 7년 (audit_logs), 90일 (access_logs)

MySQL:
- audit_logs (30일): ~30GB → $3-5/월
- access_logs (7일): ~35GB → $3-5/월

S3 Standard-IA:
- audit_logs (7년): ~84TB (압축 후 ~20TB) → $250/월
- access_logs (90일): ~450GB (압축 후 ~100GB) → $1.3/월

S3 Glacier:
- audit_logs (1년 후): ~12TB/년 증가 → $48/월 증가

CloudWatch Logs:
- 실시간 스트리밍 (3일 보관): ~10GB/일 → $30/월

Prometheus:
- 메트릭 데이터 (7일 보관): ~5GB → 자체 호스팅

총 예상 비용: ~$350-400/월
```

### 11.7 모니터링 대시보드 구성

#### Grafana 대시보드 예시
```yaml
FileFlow Logging Overview:
  Panels:
    - MySQL 로그 크기 (by 테이블)
    - S3 아카이브 크기 추이
    - CloudWatch 알림 발생 횟수
    - 아카이빙 Job 성공/실패율
    - 로그 조회 응답 시간 (MySQL vs S3)

FileFlow Security Dashboard:
  Panels:
    - 보안 이벤트 실시간 피드
    - 위협 수준별 분포 (24시간)
    - IP 차단 현황
    - 비정상 로그인 시도 지도

FileFlow Performance Dashboard:
  Panels:
    - API 응답시간 분포
    - 파이프라인 처리 성공률
    - 에러율 추이
    - 리소스 사용률 (CPU/Memory)
```

### 11.8 재해 복구 (DR)

#### S3 Cross-Region Replication
```json
{
  "Rules": [
    {
      "Id": "ReplicateAuditLogs",
      "Status": "Enabled",
      "Priority": 1,
      "Filter": {
        "Prefix": "audit_logs/"
      },
      "Destination": {
        "Bucket": "arn:aws:s3:::fileflow-logs-dr-backup",
        "ReplicationTime": {
          "Status": "Enabled",
          "Time": {
            "Minutes": 15
          }
        }
      }
    }
  ]
}
```

#### MySQL 백업
- RDS 자동 백업: 일일 스냅샷 (7일 보관)
- 수동 스냅샷: 주간 (30일 보관)
- Point-in-Time Recovery 활성화

### 11.9 규정 준수 고려사항

#### GDPR 준수
- **Right to Erasure (삭제권)**: S3에서 특정 사용자 로그 삭제 가능
- **Data Portability (이동권)**: Athena 쿼리로 사용자 데이터 추출
- **Privacy by Design**: PII 자동 마스킹 적용

#### 감사 추적
- 모든 로그 변경 기록 (metadata 테이블)
- 아카이빙/삭제 작업 감사 로그
- 접근 권한 로그 (S3 Access Logging)
