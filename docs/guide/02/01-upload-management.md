# FileFlow – Upload Management (Epic, greenfield)

> 목표: **업로드 행위 전반**(세션/멀티파트/청크/외부 다운로드/정책/완료 이벤트)을 File Management와 분리된 에픽으로 정의.
> Greenfield 전제 – 모든 스키마는 `CREATE TABLE`, FK 미사용(앱 레벨 정합성), Soft Delete 우선.

---

## 0) 용어 정리

* **Upload Session**: 업로드를 시작하기 위해 발급되는 단기 컨텍스트. 저장 경로·정책·서명 파라미터 포함.
* **Multipart Upload**: 대용량 파일을 파트 단위로 병렬 업로드하는 프로토콜(S3 등).
* **Chunked Upload**: 애플리케이션 레벨의 작은 조각 단위 업로드(필요 시).
* **External Download**: 외부 URL에서 서버 측이 받아오는 간접 업로드(ingest).
* **Upload Policy**: ABAC/설정(EAV)로 결정된 허용 MIME/최대 크기/속도/쿼터 등 룰의 실체.
* **Completion Event**: 업로드 완료 시 File Management로 전달하는 표준 이벤트(`upload.completed`).

---

## 1) 데이터 모델(핵심)

> 모든 테이블은 멀티테넌트 경계(`tenant_id`, `organization_id`)를 포함.
> 세션은 짧은 수명(예: 15분), 파트/청크/에러는 보존 기간 제한(예: 7일).

### 1.1 upload_sessions

```sql
CREATE TABLE upload_sessions (
  id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id            CHAR(27) NOT NULL,                    -- usn_xxx 형태(외부노출 키)
  tenant_id             VARCHAR(50) NOT NULL,
  organization_id       BIGINT NULL,
  uploader_user_context_id BIGINT NOT NULL,

  file_id_hint          CHAR(36) NULL,                        -- 클라가 미리 생성한 UUID(선택)
  visibility            ENUM('PRIVATE','INTERNAL','PUBLIC') NOT NULL DEFAULT 'PRIVATE',
  expected_mime         VARCHAR(150) NULL,
  expected_size_mb      INT NULL,

  storage_bucket        VARCHAR(200) NOT NULL,
  storage_key           VARCHAR(512) NOT NULL,
  storage_provider      ENUM('S3','GCS','AZURE','LOCAL') NOT NULL DEFAULT 'S3',

  policy_snapshot_json  JSON NULL,                            -- 허용 MIME/크기 등 평가 결과 스냅샷

  status                ENUM('INIT','IN_PROGRESS','COMPLETED','ABORTED','EXPIRED','FAILED') NOT NULL DEFAULT 'INIT',
  expires_at            DATETIME NOT NULL,                    -- 세션 TTL(예: 15분)

  created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at            DATETIME NULL,

  UNIQUE KEY uk_session_id (session_id),
  INDEX idx_scope_status (tenant_id, organization_id, status),
  INDEX idx_uploader_time (uploader_user_context_id, created_at),
  INDEX idx_expires (expires_at)
);
```

### 1.2 upload_multipart

```sql
CREATE TABLE upload_multipart (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id         CHAR(27) NOT NULL,              -- upload_sessions.session_id(앱 레벨 참조)
  provider_upload_id VARCHAR(200) NOT NULL,          -- S3 UploadId 등
  part_count         INT NULL,
  status             ENUM('INIT','IN_PROGRESS','COMPLETED','ABORTED','FAILED') NOT NULL DEFAULT 'INIT',
  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_session (session_id)
);
```

### 1.3 upload_parts

```sql
CREATE TABLE upload_parts (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id         CHAR(27) NOT NULL,
  part_no            INT NOT NULL,
  etag               VARCHAR(200) NULL,              -- S3 ETag
  size_bytes         BIGINT NULL,
  status             ENUM('PRESIGNED','UPLOADED','COMPLETED','FAILED','EXPIRED') NOT NULL DEFAULT 'PRESIGNED',
  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_session_part (session_id, part_no),
  INDEX idx_session (session_id)
);
```

### 1.4 upload_chunks (옵션)

```sql
CREATE TABLE upload_chunks (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id         CHAR(27) NOT NULL,
  chunk_seq          INT NOT NULL,
  size_bytes         BIGINT NULL,
  checksum_md5       CHAR(32) NULL,
  status             ENUM('RECEIVED','COMMITTED','FAILED','EXPIRED') NOT NULL DEFAULT 'RECEIVED',
  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_session_chunk (session_id, chunk_seq),
  INDEX idx_session (session_id)
);
```

### 1.5 external_downloads (서버 측 인게스트)

```sql
CREATE TABLE external_downloads (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id         CHAR(27) NOT NULL,
  source_url         TEXT NOT NULL,
  byte_transferred   BIGINT NOT NULL DEFAULT 0,
  status             ENUM('INIT','DOWNLOADING','COMPLETED','FAILED','ABORTED') NOT NULL DEFAULT 'INIT',
  error_code         VARCHAR(50) NULL,
  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_session (session_id)
);
```

### 1.6 upload_errors (진단)

```sql
CREATE TABLE upload_errors (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id         CHAR(27) NOT NULL,
  code               VARCHAR(50) NOT NULL,     -- ex) UP-403-ABAC, UP-409-DUPSHA, UP-500-IO
  message            VARCHAR(500) NULL,
  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_session_time (session_id, created_at)
);
```

### 1.7 upload_policies (선택: 템플릿)

```sql
CREATE TABLE upload_policies (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id          VARCHAR(50) NOT NULL,
  organization_id    BIGINT NULL,
  name               VARCHAR(100) NOT NULL,
  allowed_mime_json  JSON NULL,      -- ["image/png","application/pdf"]
  max_size_mb        INT NULL,
  time_window_start  TINYINT NULL,   -- 0~23
  time_window_end    TINYINT NULL,   -- 0~23
  rate_limit_per_min INT NULL,
  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_scope_name (tenant_id, organization_id, name)
);
```

---

## 2) 정책(Policy) & 인가

* 업로드 시작 전 `IAM.evaluate("file.upload")` 호출 → **Scope(SELF/ORG/TENANT)** 매치 후 **ABAC** 조건 확인.
* ABAC 예시

    * MIME: `in(res.mime,["image/jpeg","image/png","application/pdf"])`
    * 사이즈: `res.size_mb <= 20`
    * 시간대: `9 <= getHour(ctx.now, "Asia/Seoul") < 20`
* 평가 결과는 `upload_sessions.policy_snapshot_json`에 **스냅샷 저장**(감사/디버그 용도).

---

## 3) API 계약(초안)

### 3.1 세션

* **POST /uploads/sessions**
  요청: `{ visibility, mime, size_mb, filename }`
  응답: `201 { sessionId, fileIdHint?, bucket, key, provider, presigned: { type, url|form }, expiresAt }`
* **GET /uploads/sessions/{sessionId}** → 상태/만료 조회
* **DELETE /uploads/sessions/{sessionId}** → ABORT(업로드 취소)

### 3.2 멀티파트

* **POST /uploads/sessions/{sessionId}:multipart-init** → `{ providerUploadId }`
* **POST /uploads/sessions/{sessionId}:part** `{ partNo }` → presigned 반환
* **PUT  /uploads/sessions/{sessionId}:part** `{ partNo, etag, size }` → 업로드 완료 마킹
* **POST /uploads/sessions/{sessionId}:complete** `{ parts[] }` → 멀티파트 조립

### 3.3 청크(옵션)

* **POST /uploads/sessions/{sessionId}:chunk** `{ seq, size, md5, data? }` → 스트리밍/버퍼 구현체에 의존
* **POST /uploads/sessions/{sessionId}:commit** → 최종 커밋

### 3.4 외부 다운로드

* **POST /uploads/external** `{ url, visibility, mimeHint? }` → `202 { sessionId }`
* **GET  /uploads/external/{sessionId}** → 진행상태 조회/취소

### 3.5 완료/콜백(동기 API는 아님)

* 업로드 완료 시 **이벤트 발행**으로 File Management에 위임(아래 §4 참조).

**오류 코드(초안)**

* `UP-401-001` 서명/인증 실패, `UP-403-ABAC` 조건 불충족, `UP-409-DUPSHA` 체크섬 중복 정책 위반, `UP-422-VALID` 요청 불일치, `UP-409-MPSTATE` 멀티파트 상태 충돌

---

## 4) 이벤트(필수 계약)

### 4.1 upload.completed

```json
{
  "type": "upload.completed",
  "sessionId": "usn_abc...",
  "tenantId": "tnt_demo",
  "organizationId": 123,
  "uploaderUserContextId": 9001,
  "storage": {"bucket":"bkt","key":"path/to/object"},
  "content": {"mime":"image/png","size": 123456,"checksumSha256":"..."},
  "visibility": "PRIVATE",
  "occurredAt": "2025-10-22T10:20:30Z"
}
```

### 4.2 upload.failed / upload.aborted / upload.expired

```json
{ "type":"upload.failed", "sessionId":"...", "code":"UP-500-IO", "message":"...", "occurredAt":"..." }
```

> **원칙**: Upload는 DB `file_assets`를 직접 쓰지 않는다. File Management가 이벤트를 받아 파일을 **확정/처리**한다(안티코럽션).

---

## 5) 관찰성 & 보안

* **로그**: 세션 생성/ABORT/COMPLETE, 멀티파트 파트 상태, 외부 다운로드 오류.
* **메트릭**: 세션 성공율, ABAC 거부율, 멀티파트 재시도율, 외부 다운로드 속도/성공율, 평균 세션 TTL 소모.
* **보안**: PUBLIC 요청도 기본 서명 URL 발급, 세션 재사용 방지, 리퍼러/콘텐츠-타입 검증(서버사이드).

---

## 6) 테스트/DoD

* **권한/정책**: IAM 평가(허용/거부) 경계 테스트(MIME/사이즈/시간대)
* **세션 수명**: 만료/재시도/중복 호출 방지(idempotency key)
* **멀티파트**: 파트 누락/중복/순서 뒤바뀜/ETag 검증/조립 실패 롤백
* **외부 다운로드**: 3xx/4xx/5xx 처리, 콘텐츠 길이 불일치, 타임아웃/재시도 백오프
* **이벤트**: `upload.completed` 발행 보장(최소 1회), 중복 수신 대비 idempotency 키 포함
* **성능**: 동시 세션 N만 건에서 presign 지연/오류율 임계 이하

---

## 7) 체크리스트

* [ ] 업로드 정책 평가 → policy_snapshot 저장
* [ ] 세션 TTL과 ABORT 처리
* [ ] 멀티파트 상태머신/ETag 검증
* [ ] 외부 다운로드 워커/취소/재시도
* [ ] 이벤트 발행 표준(완료/실패/만료)
* [ ] 관찰성 대시보드(성공율/거부율/재시도율)
* [ ] 보안 가드(서명 URL, CORS/CT 검증)
