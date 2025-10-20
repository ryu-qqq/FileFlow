# 📁 FileFlow 데이터베이스 스키마 설계

## 📌 개요

FileFlow는 이커머스 환경에서 대규모 파일 업로드 및 처리를 담당하는 엔터프라이즈급 파일 관리 시스템입니다. 
B2B와 B2C 비즈니스 모델을 모두 지원하며, 테넌트 기반의 멀티테넌시 아키텍처를 통해 유연하고 확장 가능한 구조를 제공합니다.

## 🎯 핵심 요구사항

### 1. 비즈니스 모델
- **B2C**: 판매자, 내부 관리자, 고객(리뷰)
- **B2B**: 입점 회사, 내부 관리자

### 2. 지원 파일 타입
- **이미지**: 상품 이미지 (최적화, 썸네일 생성)
- **HTML**: 상품 상세 설명 (OCR 텍스트 추출)
- **PDF**: 문서 관리
- **Excel**: 외부몰 연동, 발주 문서 (AI 기반 데이터 매핑)

### 3. 업로드 방식
- **직접 업로드**: Presigned URL을 통한 클라이언트 직접 업로드
- **간접 업로드**: 외부 URL 제공 → 서버 다운로드 → 업로드

### 4. 파일 처리 파이프라인
- 이미지 최적화 및 리사이징
- HTML OCR 텍스트 추출
- Excel AI 기반 캐노니컬 포맷 변환

## 🏗️ 아키텍처 설계 원칙

### 1. 확장성 (Scalability)
- 수평 확장 가능한 테이블 구조
- 파티셔닝 전략 적용 가능
- 대용량 파일 처리 고려
- **Foreign Key 제거**: DB 레벨 FK 제약조건 미사용으로 샤딩/분산 확장성 확보

### 2. 유연성 (Flexibility)
- 다양한 비즈니스 모델 지원
- 새로운 파일 타입 및 파이프라인 추가 용이
- 정책 기반 설정 관리
- **JSON 스키마 검증**: 동적 설정 값에 대한 런타임 타입 안전성 보장

### 3. 보안성 (Security)
- 테넌트 격리
- 세분화된 접근 권한 관리
- 감사 로그 추적
- **외부 인증 서버 분리**: 인증(Authentication)과 인가(Authorization) 분리 아키텍처

### 4. 성능 (Performance)
- 효율적인 인덱싱 전략
- 캐싱 고려
- 배치 처리 지원
- **하이브리드 로깅**: MySQL(단기) + S3(장기) + CloudWatch(알림) + Prometheus(메트릭)

### 5. 데이터 무결성 (Data Integrity)
- **애플리케이션 레벨 참조 무결성**: 서비스 계층에서 참조 검증 수행
- **트랜잭션 기반 CASCADE**: `@Transactional`을 통한 일관된 삭제/업데이트
- **배치 검증**: 주기적인 orphan record 탐지 및 정리
- **감사 추적**: 모든 데이터 변경 기록 및 추적

## 📊 데이터베이스 스키마 구성

### 핵심 도메인
1. **테넌트 관리**: 멀티테넌시 지원
2. **조직 관리**: 판매자, 입점회사 관리
3. **사용자 관리**: 권한 및 역할 관리
4. **파일 자산**: 파일 메타데이터 및 저장 정보
5. **업로드 세션**: 업로드 프로세스 추적
6. **정책 관리**: 업로드/처리 정책
7. **파이프라인**: 파일 처리 워크플로우
8. **분석 데이터**: OCR, AI 추출 데이터

### 테이블 그룹

#### 1. 테넌트 & 조직
- `tenants`: 테넌트 정보
- `organizations`: 조직(판매자/회사) 정보
- `organization_settings`: 조직별 설정

#### 2. 사용자 & 권한
- `users`: 사용자 정보
- `user_roles`: 사용자 역할
- `permissions`: 권한 정의
- `role_permissions`: 역할-권한 매핑

#### 3. 파일 관리
- `file_assets`: 파일 기본 정보
- `file_variants`: 파일 변종 (썸네일, 최적화 버전)
- `file_metadata`: 파일 메타데이터
- `file_relationships`: 파일 간 관계

#### 4. 업로드 관리
- `upload_sessions`: 업로드 세션
- `upload_policies`: 업로드 정책
- `upload_parts`: 멀티파트 업로드 파트

#### 5. 파이프라인 처리
- `pipeline_definitions`: 파이프라인 정의
- `pipeline_stages`: 파이프라인 단계
- `pipeline_executions`: 파이프라인 실행
- `pipeline_stage_logs`: 단계별 실행 로그

#### 6. 데이터 추출 & 분석
- `extracted_data`: 추출된 데이터
- `data_mappings`: 데이터 매핑 규칙
- `canonical_formats`: 표준 포맷 정의

#### 7. 감사 & 로깅
- `audit_logs`: 감사 로그
- `access_logs`: 파일 접근 로그
- `processing_errors`: 처리 오류 로그

## 📂 문서 구조

```
docs/schema-design/
├── README.md                           # 현재 문서
├── 01-overview/
│   ├── architecture.md                 # 전체 아키텍처 설명
│   ├── er-diagram.md                   # ER 다이어그램
│   └── data-flow.md                    # 데이터 흐름도
├── 02-tables/
│   ├── 01-tenant-organization.md       # 테넌트 & 조직 테이블
│   ├── 02-user-permission.md           # 사용자 & 권한 테이블
│   ├── 03-file-management.md           # 파일 관리 테이블
│   ├── 04-upload-management.md         # 업로드 관리 테이블
│   ├── 05-pipeline-processing.md       # 파이프라인 처리 테이블
│   ├── 06-data-extraction.md           # 데이터 추출 테이블
│   └── 07-audit-logging.md             # 감사 & 로깅 테이블
├── 03-migration/
│   ├── V001_to_V020.sql                # 기본 테이블 생성
│   ├── V021_to_V040.sql                # 파이프라인 테이블
│   ├── V041_to_V060.sql                # 데이터 추출 테이블
│   └── V061_to_V080.sql                # 인덱스 및 최적화
└── 04-examples/
    ├── use-cases.md                    # 사용 사례
    ├── sample-queries.md               # 샘플 쿼리
    └── performance-tips.md             # 성능 최적화 팁
```

## 🚀 다음 단계

1. [아키텍처 개요](./01-overview/architecture.md) 확인
2. [테이블 상세 명세](./02-tables/) 검토
3. [마이그레이션 스크립트](./03-migration/) 실행
4. [사용 예제](./04-examples/) 참고

## 🔄 주요 변경사항 (v1.1.0)

### 데이터베이스 아키텍처 개선

#### 1. Foreign Key 제약조건 제거
**배경**: 분산 환경에서의 확장성 및 성능 최적화를 위해 DB 레벨 FK 제약조건을 제거하고 애플리케이션 레벨에서 참조 무결성을 보장하는 방식으로 전환

**영향받는 테이블**: 전체 테이블 (약 40개 테이블)

**변경 내용**:
```sql
-- Before (FK 제약조건)
FOREIGN KEY fk_file_tenant (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE

-- After (애플리케이션 레벨 검증)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
INDEX idx_tenant_id (tenant_id)
```

**애플리케이션 레벨 검증 전략**:
- 생성 시: 참조 대상 존재 여부 검증
- 삭제 시: `@Transactional`을 통한 CASCADE 처리
- 주기적: Batch Job을 통한 orphan record 탐지 및 정리

**장점**:
- 데이터베이스 샤딩 및 분산 아키텍처 준비
- Lock contention 감소로 성능 향상
- 유연한 스키마 변경 가능
- 크로스 데이터베이스 참조 지원 가능

#### 2. 사용자 테이블 구조 개선 (users → user_contexts)
**배경**: 외부 인증 서버(Auth Server)와의 명확한 역할 분리

**변경사항**:
- `users` 테이블 → `user_contexts` 테이블로 변경
- 인증(Authentication): 외부 Auth Server 담당 (JWT 발급)
- 인가(Authorization): FileFlow 담당 (권한 관리)

**데이터 흐름**:
```
1. 사용자 로그인 → Auth Server (JWT 발급)
2. API 요청 → API Gateway (JWT 검증)
3. FileFlow 요청 → user_contexts 조회 (권한 확인)
4. 비즈니스 로직 실행
```

**영향받는 테이블**:
- `user_contexts`: 사용자 컨텍스트 및 권한 정보
- 모든 참조 테이블: `user_id` → `user_context_id`

#### 3. JSON Schema 검증 추가
**배경**: 테넌트/조직 설정 값의 동적 타입 검증 필요

**적용 테이블**:
- `tenants.settings`: 테넌트 설정 JSON 검증
- `organizations.settings`: 조직 설정 JSON 검증
- `upload_policies.settings`: 정책 설정 JSON 검증

**검증 방식**:
```java
// JSON Schema 정의
{
  "type": "object",
  "properties": {
    "max_file_size": {"type": "integer", "minimum": 1},
    "allowed_extensions": {"type": "array", "items": {"type": "string"}}
  },
  "required": ["max_file_size"]
}

// 애플리케이션 레벨 검증
@Service
public class TenantService {
    public void validateSettings(JsonNode settings) {
        jsonSchemaValidator.validate(TENANT_SETTINGS_SCHEMA, settings);
    }
}
```

**장점**:
- 런타임 타입 안전성 보장
- 잘못된 설정으로 인한 에러 사전 방지
- 명확한 설정 구조 문서화

#### 4. 하이브리드 로그 전략 도입
**배경**: 실시간 조회, 장기 보관, 알림, 메트릭 수집을 각각 최적화된 스토리지로 분산

**아키텍처**:
```
MySQL (단기 저장)
  ├─ audit_logs: 30일
  ├─ access_logs: 7일
  ├─ processing_errors: 30일
  └─ security_events: 90일
         ↓ (자동 아카이빙)
S3 (장기 아카이브)
  ├─ audit_logs: 7년 (규정 준수)
  ├─ access_logs: 90일
  └─ processing_errors: 1년

CloudWatch (실시간 알림)
  ├─ security_events: 실시간 스트리밍
  ├─ processing_errors: ERROR 이상
  └─ audit_logs: HIGH 위험도 이상

Prometheus + Grafana (메트릭)
  ├─ 시스템 메트릭: CPU, Memory, Network
  ├─ 애플리케이션 메트릭: API 응답시간, 에러율
  └─ 비즈니스 메트릭: 업로드 수, 처리 성공률
```

**아카이빙 프로세스**:
1. Spring Batch Job (매일 새벽 2시 실행)
2. MySQL → Parquet 변환 (압축률 ~80%)
3. S3 업로드 (연/월/일 파티셔닝)
4. MySQL 데이터 삭제
5. S3 Lifecycle: Standard → Standard-IA (90일) → Glacier (365일)

**비용 효율**:
- MySQL: ~$10/월 (단기 고성능 저장)
- S3: ~$250/월 (장기 저비용 보관)
- CloudWatch: ~$30/월 (실시간 모니터링)
- Prometheus: 자체 호스팅
- **총 예상 비용**: ~$350-400/월

**조회 전략**:
```java
// 최근 30일: MySQL 직접 조회
if (request.isRecentQuery()) {
    return auditLogRepository.search(request);
}
// 30일 이후: S3 Athena 쿼리
else {
    return athenaQueryService.queryArchive("audit_logs", request);
}
```

#### 5. Prometheus + Grafana 모니터링 추가
**배경**: 실시간 시스템 메트릭 및 비즈니스 메트릭 시각화 필요

**구성 요소**:
- **Prometheus**: 메트릭 수집 및 저장 (7일 보관)
- **Grafana**: 대시보드 시각화
- **Node Exporter**: 시스템 메트릭 수집
- **Micrometer**: 애플리케이션 메트릭 수집

**수집 메트릭**:
```yaml
시스템 메트릭:
  - CPU 사용률, Memory 사용률
  - Disk I/O, Network throughput
  - JVM heap, GC 시간

애플리케이션 메트릭:
  - API 응답시간 (P50, P95, P99)
  - 에러율 (4xx, 5xx)
  - 동시 접속자 수

비즈니스 메트릭:
  - 파일 업로드 수/크기
  - 파이프라인 처리 성공/실패율
  - 테넌트별 리소스 사용량
```

**대시보드**:
- FileFlow Overview: 전체 시스템 현황
- Upload Performance: 업로드 성능 모니터링
- Pipeline Processing: 파이프라인 처리 상태
- Security Dashboard: 보안 이벤트 추적

### 참고 문서

#### 상세 설계 문서
- [03-file-management.md](./02-tables/03-file-management.md): 참조 무결성 검증 전략 (Section 7)
- [04-upload-management.md](./02-tables/04-upload-management.md): 업로드 정책 기반 검증
- [07-audit-logging.md](./02-tables/07-audit-logging.md): 하이브리드 로그 전략 (Section 11)

#### 구현 가이드
- Spring Service Layer: 참조 무결성 검증 로직
- Spring Batch: 로그 아카이빙 Job 구현
- Prometheus: 메트릭 수집 설정
- Grafana: 대시보드 구성

## 📝 버전 히스토리

| 버전 | 날짜 | 작성자 | 변경사항 |
|------|------|--------|----------|
| 1.1.0 | 2025-01-20 | FileFlow Team | FK 제거, user_contexts 전환, 하이브리드 로그 전략, Prometheus 모니터링 추가 |
| 1.0.0 | 2025-01-20 | FileFlow Team | 초기 설계 |

## 💡 참고사항

- 모든 테이블은 InnoDB 스토리지 엔진 사용
- 문자셋: utf8mb4, Collation: utf8mb4_unicode_ci
- 타임존: UTC 기준 (애플리케이션에서 로컬 시간 변환)
- Soft Delete 정책 적용 (deleted_at 컬럼)
