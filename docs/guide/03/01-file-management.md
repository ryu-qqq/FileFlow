# FileFlow – File Management (hardened)

> 목표: 파일 저장·처리·전달 전 과정을 **프로덕션 운영 기준**으로 보강. Greenfield(처음 생성) 전제이므로 **모든 테이블은 CREATE TABLE**로 제시합니다. A~I 개선안을 반영해 식별자/만료/가시성/테넌트 경계/중복/상태/검색/로그를 견고화합니다.

---

## 0) 용어 정리

* **File Asset**: 원본 파일(실체).
* **Variant**: 썸네일/리사이즈/포맷변환 등 파생물.
* **Relationship**: 파일 간 관계(버전, 참조, 그룹 등).
* **Visibility**: 접근 공개 수준 (PRIVATE/INTERNAL/PUBLIC).
* **Checksum**: 콘텐츠 동일성 지문(SHA-256).
* **Effective Expires At**: 정책에 따른 실제 만료시각(`expires_at` 또는 `created_at + retention_days`).

---

## 1) 데이터 모델(핵심)

> 원칙
>
> 1. **ID 일원화**: 내부 조인은 BIGINT PK, 외부 노출은 UUID(`file_id`).
> 2. **경계 명시**: 모든 테이블에 `tenant_id`(필수), 가능하면 `organization_id` 포함.
> 3. **Soft Delete**: `deleted_at` 기본, 조회 기본 스코프에서 제외.
> 4. **URL 비저장**: 경로만 저장, URL은 런타임 서명 생성.
> 5. **상태/단계 분리**: 파일 상태(`status`) vs 처리단계(`processing_status`).

### 1.1 file_assets (원본)

```sql
CREATE TABLE file_assets (
  id                          BIGINT PRIMARY KEY AUTO_INCREMENT,
  file_id                     CHAR(36) NOT NULL,               -- 외부 노출 UUID
  tenant_id                   VARCHAR(50) NOT NULL,
  organization_id             BIGINT NULL,
  uploader_user_context_id    BIGINT NOT NULL,                 -- SELF 판정

  storage_bucket              VARCHAR(200) NOT NULL,
  storage_key                 VARCHAR(512) NOT NULL,           -- CDN URL 저장 금지
  mime_type                   VARCHAR(150) NOT NULL,
  original_name               VARCHAR(255) NULL,
  file_size                   BIGINT NOT NULL,
  checksum_sha256             CHAR(64) NULL,

  visibility                  ENUM('PRIVATE','INTERNAL','PUBLIC') NOT NULL DEFAULT 'PRIVATE',
  status                      ENUM('UPLOADING','PROCESSING','AVAILABLE','ARCHIVED','DELETED','ERROR') NOT NULL DEFAULT 'UPLOADING',
  processing_status           ENUM('PENDING','IN_PROGRESS','COMPLETED','FAILED') NULL,

  retention_days              INT NULL,
  expires_at                  DATETIME NULL,
  effective_expires_at        DATETIME AS (
                                COALESCE(expires_at, DATE_ADD(created_at, INTERVAL IFNULL(retention_days, 365) DAY))
                              ) STORED,

  created_at                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at                  DATETIME NULL,

  UNIQUE KEY uk_file_uuid (file_id),
  -- 중복 방지 정책: 엄격 차단 시 UNIQUE, 탐지만 원하면 INDEX로 변경
  UNIQUE KEY uk_tenant_sha256 (tenant_id, checksum_sha256),

  INDEX idx_scope_created (tenant_id, organization_id, created_at),
  INDEX idx_owner (uploader_user_context_id),
  INDEX idx_effective_expires (effective_expires_at)
);
```

### 1.2 file_variants (파생물)

```sql
CREATE TABLE file_variants (
  id                BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_file_id    BIGINT NOT NULL,                -- 원본 PK (FK는 미사용)
  tenant_id         VARCHAR(50) NOT NULL,
  organization_id   BIGINT NULL,

  variant_type      VARCHAR(100) NOT NULL,          -- ex) thumb_200, webp_80, pdf_preview
  storage_bucket    VARCHAR(200) NOT NULL,
  storage_key       VARCHAR(512) NOT NULL,
  mime_type         VARCHAR(150) NOT NULL,
  file_size         BIGINT NOT NULL,
  checksum_sha256   CHAR(64) NULL,
  status            ENUM('PENDING','READY','FAILED') NOT NULL DEFAULT 'PENDING',

  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at        DATETIME NULL,

  INDEX idx_parent (parent_file_id),
  INDEX idx_variant_scope (tenant_id, organization_id)
);
```

### 1.3 file_relationships (파일 간 관계)

```sql
CREATE TABLE file_relationships (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  src_file_id      BIGINT NOT NULL,
  dst_file_id      BIGINT NOT NULL,
  tenant_id        VARCHAR(50) NOT NULL,
  organization_id  BIGINT NULL,
  rel_type         VARCHAR(50) NOT NULL,            -- version_of, derived_from, attached_to 등

  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at       DATETIME NULL,

  INDEX idx_rel_src (src_file_id),
  INDEX idx_rel_dst (dst_file_id),
  INDEX idx_rel_scope (tenant_id, organization_id)
);
```

### 1.4 file_metadata (EAV)

```sql
CREATE TABLE file_metadata (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  file_id          BIGINT NOT NULL,
  tenant_id        VARCHAR(50) NOT NULL,
  organization_id  BIGINT NULL,
  key_name         VARCHAR(150) NOT NULL,
  value_raw        TEXT NULL,
  is_secret        TINYINT(1) NOT NULL DEFAULT 0,
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  UNIQUE KEY uk_file_key (file_id, key_name),
  INDEX idx_meta_scope (tenant_id, organization_id)
);
```

### 1.5 file_access_logs (접근 기록)

```sql
CREATE TABLE file_access_logs (
  id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id                VARCHAR(50) NOT NULL,
  organization_id          BIGINT NULL,
  file_id                  BIGINT NOT NULL,
  action                   ENUM('READ','WRITE','DELETE') NOT NULL,
  actor_user_context_id    BIGINT NULL,
  result                   ENUM('ALLOW','DENY') NOT NULL,
  reason_code              VARCHAR(50) NULL,
  ip_address               VARCHAR(45) NULL,     -- 마스킹 저장 권장(IPv4 /24, IPv6 /64)
  user_agent               VARCHAR(255) NULL,
  created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_log_scope_time (tenant_id, organization_id, created_at),
  INDEX idx_log_file (file_id, created_at)
);
```

---

## 2) 가시성(Visibility) 모델

* **PRIVATE**: 서명 URL **필수** + RBAC/ABAC 통과 필요. 접근 로그 의무.
* **INTERNAL**: 동일 테넌트 내에서만 접근(조직 경계는 RBAC로 제한). 서명 URL 권장.
* **PUBLIC**: 익명 읽기 허용 **가능**하나, 기본은 **짧은 TTL 서명 URL** 사용. 완전 공개 URL은 별도 플래그/버킷 정책으로.

---

## 3) 상태 체계 & 전이

* **status (파일 상태)**: `UPLOADING → PROCESSING → AVAILABLE → (ARCHIVED | DELETED | ERROR)`
* **processing_status (파이프라인)**: `PENDING → IN_PROGRESS → COMPLETED | FAILED`
* **전이 규칙**

  * `UPLOADING → PROCESSING`: 바디 수신 완료
  * `PROCESSING → AVAILABLE`: 필수 변종 생성/검사 완료
  * `AVAILABLE → ARCHIVED`: 장기보관 이동(읽기 전용)
  * `* → ERROR`: 실패 발생
  * `ERROR → PROCESSING`: 재시도 시(단계 `PENDING` 재설정)

---

## 4) 수명주기/정리

* **만료 기준**: `effective_expires_at` 기준으로 정리 배치 실행
* **정리 단계**: 소프트 삭제(`deleted_at`) → 보류기간(예: 30일) 후 물리 삭제
* **보존 정책**: 테넌트/조직 기본값(`retention_days`) 설정 가능(EAV 활용)

---

## 5) 중복/무결성

* 업로드 완료 시 **SHA-256 필수 계산**
* 정책 옵션: (a) 완전 중복 차단(UNIQUE), (b) 중복 허용하되 탐지/표시(UNIQUE→INDEX로 변경)

---

## 6) 검색/목록

* DB 인덱스 기반 필터: 테넌트/조직/상태/업로더/기간
* 파일명 검색은 최소 LIKE 제공. **정확 검색 품질은 OpenSearch/ES**(한국어 형태소/엔그램)로 확장

---

## 7) IAM 연계 (필수 규칙)

* **SELF**: `uploader_user_context_id == current.user_context_id`
* **ORGANIZATION**: `resource.organization_id == current.organization_id` & 동일 테넌트
* **TENANT**: `resource.tenant_id == current.tenant_id`
* **ABAC**: 업로드 전 `mime_type`, `file_size`, `visibility` 조건 평가(예: 이미지/PDF & ≤20MB)

권한 예시

* `file.upload`(ORG 스코프, 조건: 이미지/PDF & ≤20MB)
* `file.read`(ORG/TENANT 스코프)
* `file.delete`(TENANT 스코프 운영자)

---

## 8) API 스케치 (요약)

* **POST /files**: 업로드 세션 생성(내부 훅: `evaluate(file.upload)`)
* **GET /files/{fileId}**: 메타 조회 (가시성/스코프 검사)
* **GET /files/{fileId}/download**: 서명 URL 생성 후 302/200 반환
* **DELETE /files/{fileId}**: 소프트 삭제(권한 필요)
* **GET /files**: 목록(테넌트/조직/상태/기간/업로더 필터)
* **POST /files/{fileId}/variants**: 파생 생성 트리거(테넌트 경계 검사)

---

## 9) 운영/관찰성

* **메트릭**: 업로드 성공율, 처리 실패율, 403/404 비율, 만료 정리 건수, 중복 탐지 TOP-K
* **로그**: 접근(ALLOW/DENY, 사유코드), 상태 전이, 변종 처리 결과
* **알람**: 처리 실패율/403 급증/정리 실패 누적

---

## 10) 체크리스트

* [ ] 내부조인 BIGINT / 외부노출 UUID 규약 준수
* [ ] 모든 테이블에 `tenant_id`(및 가능시 `organization_id`) 보존
* [ ] `effective_expires_at` 생성 칼럼 포함
* [ ] 변종/관계의 테넌트 경계 검사 가드
* [ ] CDN URL 미저장, 런타임 서명 URL 생성
* [ ] SHA-256 필수, (tenant, checksum) 제약 정책 결정
* [ ] 상태/단계 전이 규칙 구현 및 대시보드 연동
* [ ] 접근 로그 비동기 파이프 + PII 마스킹
* [ ] ES/오픈서치 연동 계획(한글 검색)
