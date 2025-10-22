# FileFlow – Pipeline Processing (S3→SQS·Worker, Greenfield · Hardened)

> 목표: 업로드 완료 후 **S3 이벤트→SQS→워커** 기반으로 동작하는 후처리 파이프라인을, 프로덕션 운영 기준으로 **처음부터** 설계한다.
> Greenfield 전제: **모든 테이블은 CREATE TABLE**, FK 미사용(앱 레벨 정합성), Soft Delete 우선.
> 핵심 보강: 멀티테넌트 경계, 아이덴포턴시, 트레이싱, 픽업 인덱스, 콜드/핫 분리, 취소/재시도 정책, DLQ 재처리.

---

## 0) 용어 정리

* **Pipeline Definition**: 파이프라인 설계 스펙(목적/타입/리소스/단계 구조).
* **Pipeline Stage**: 개별 처리 스텝(순서/병렬/재시도 규칙 포함).
* **Pipeline Execution**: 특정 파일에 대한 실행 인스턴스(상태/재시도/에러/메트릭).
* **Stage Log**: 스테이지 단위의 입력/출력/로그/메트릭 요약(대용량은 참조키).
* **Schedule**: 크론/인터벌 기반 파이프라인 기동 스케줄.
* **Idempotency Key**: 중복 실행 방지 키(예: `s3Event#bucket#key#pipeline_code`).
* **Trace Id**: 실행 전반을 관통하는 분산 추적 키.

---

## 1) 데이터 모델(핵심)

> 원칙
>
> 1. **ID 일원화**: 내부 조인은 BIGINT PK, 외부/이벤트에는 UUID/키 허용.
> 2. **경계 명시**: 실행/로그/스케줄 등 런타임 데이터는 `tenant_id`, `organization_id` 보존.
> 3. **콜드/핫 분리**: 대용량 JSON/TEXT는 오브젝트 스토리지 키로 참조(`*_ref`).
> 4. **상태/재시도 통일**: 실행/단계 공통 상태 체계.

### 1.1 pipeline_definitions (정의)

```sql
CREATE TABLE pipeline_definitions (
  id                      BIGINT PRIMARY KEY AUTO_INCREMENT,
  pipeline_code           VARCHAR(50) NOT NULL,
  pipeline_name           VARCHAR(100) NOT NULL,
  description             TEXT NULL,
  file_type               ENUM('IMAGE','HTML','PDF','EXCEL','ANY') NOT NULL,
  pipeline_type           ENUM('ASYNC','SYNC','BATCH','SCHEDULED') NOT NULL DEFAULT 'ASYNC',
  trigger_conditions      JSON NULL,
  configuration           JSON NOT NULL DEFAULT (JSON_OBJECT()),
  default_params          JSON NULL,
  max_file_size           BIGINT NULL,
  timeout_seconds         INT NOT NULL DEFAULT 300,
  max_retries             INT NOT NULL DEFAULT 3,
  retry_delay_seconds     INT NOT NULL DEFAULT 60,
  priority                INT NOT NULL DEFAULT 100,
  parallelism             INT NOT NULL DEFAULT 1,
  resource_requirements   JSON NULL,
  dependencies            JSON NULL,
  success_actions         JSON NULL,
  failure_actions         JSON NULL,
  notification_config     JSON NULL,
  is_active               TINYINT(1) NOT NULL DEFAULT 1,
  version                 INT NOT NULL DEFAULT 1,
  created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by              VARCHAR(100) NULL,
  UNIQUE KEY uk_pipeline_code (pipeline_code),
  INDEX idx_file_type (file_type, is_active),
  INDEX idx_pipeline_type (pipeline_type),
  INDEX idx_priority (priority, is_active)
);
```

### 1.2 pipeline_stages (단계)

```sql
CREATE TABLE pipeline_stages (
  id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
  pipeline_id          BIGINT NOT NULL,               -- FK 미사용(앱 레벨 정합성)
  stage_code           VARCHAR(50) NOT NULL,
  stage_name           VARCHAR(100) NOT NULL,
  description          TEXT NULL,
  sequence_order       INT NOT NULL,
  processor_type       VARCHAR(100) NOT NULL,         -- ex) ImageResizer, OcrProcessor
  processor_config     JSON NOT NULL DEFAULT (JSON_OBJECT()),
  input_validation     JSON NULL,
  output_validation    JSON NULL,
  is_optional          TINYINT(1) NOT NULL DEFAULT 0,
  is_parallel          TINYINT(1) NOT NULL DEFAULT 0,
  condition            JSON NULL,
  timeout_seconds      INT NOT NULL DEFAULT 60,
  max_retries          INT NOT NULL DEFAULT 3,
  retry_strategy       ENUM('IMMEDIATE','LINEAR','EXPONENTIAL') NOT NULL DEFAULT 'EXPONENTIAL',
  on_failure           ENUM('FAIL','SKIP','CONTINUE','RETRY') NOT NULL DEFAULT 'FAIL',
  resource_allocation  JSON NULL,
  metrics_config       JSON NULL,
  is_active            TINYINT(1) NOT NULL DEFAULT 1,
  created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_pipeline_stage (pipeline_id, stage_code),
  INDEX idx_pipeline_seq (pipeline_id, sequence_order),
  INDEX idx_processor (processor_type),
  INDEX idx_stage_active (is_active)
);
```

### 1.3 pipeline_executions (실행)

```sql
CREATE TABLE pipeline_executions (
  id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
  execution_id         CHAR(36) NOT NULL,             -- 외부/로그용 UUID
  pipeline_id          BIGINT NOT NULL,
  file_id              BIGINT NOT NULL,               -- file_assets.id
  tenant_id            VARCHAR(50) NOT NULL,
  organization_id      BIGINT NULL,
  parent_execution_id  BIGINT NULL,
  trigger_type         ENUM('UPLOAD','MANUAL','SCHEDULED','DEPENDENCY','RETRY') NOT NULL,
  status               ENUM('PENDING','RUNNING','COMPLETED','FAILED','CANCELLED','TIMEOUT') NOT NULL DEFAULT 'PENDING',
  priority             INT NOT NULL DEFAULT 100,      -- 픽업 정렬용 (정의에서 복사 또는 생성 칼럼)
  current_stage_id     BIGINT NULL,
  total_stages         INT NOT NULL DEFAULT 0,
  completed_stages     INT NOT NULL DEFAULT 0,
  input_params         JSON NULL,
  output_results       JSON NULL,
  execution_context    JSON NULL,
  error_message        TEXT NULL,
  error_code           VARCHAR(50) NULL,
  error_stage_id       BIGINT NULL,
  retry_count          INT NOT NULL DEFAULT 0,
  worker_id            VARCHAR(100) NULL,
  resource_usage       JSON NULL,
  performance_metrics  JSON NULL,
  idempotency_key      VARCHAR(150) NULL,             -- 중복 실행 방지
  trace_id             VARCHAR(64) NULL,              -- 분산 추적
  started_at           DATETIME NULL,
  completed_at         DATETIME NULL,
  duration_ms          INT NULL,
  created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_execution_uuid (execution_id),
  UNIQUE KEY uk_exec_idem (idempotency_key),
  INDEX idx_scope_time (tenant_id, organization_id, created_at),
  INDEX idx_pipeline_pick (status, priority, created_at),
  INDEX idx_file_recent (file_id, created_at),
  INDEX idx_status_time (status, created_at),
  INDEX idx_trace (trace_id)
);
```

### 1.4 pipeline_stage_logs (단계 로그)

```sql
CREATE TABLE pipeline_stage_logs (
  id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
  execution_id         BIGINT NOT NULL,               -- pipeline_executions.id
  stage_id             BIGINT NOT NULL,               -- pipeline_stages.id
  stage_execution_id   CHAR(36) NOT NULL,             -- 외부/로그용 UUID
  tenant_id            VARCHAR(50) NOT NULL,
  organization_id      BIGINT NULL,
  status               ENUM('STARTED','RUNNING','COMPLETED','FAILED','SKIPPED','TIMEOUT') NOT NULL DEFAULT 'STARTED',
  input_ref            VARCHAR(512) NULL,             -- 대용량은 참조키로 (콜드 스토리지)
  output_ref           VARCHAR(512) NULL,
  stage_context        JSON NULL,
  error_message        TEXT NULL,
  error_stack_trace    TEXT NULL,
  retry_count          INT NOT NULL DEFAULT 0,
  resource_usage       JSON NULL,
  performance_metrics  JSON NULL,
  trace_id             VARCHAR(64) NULL,
  started_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at         DATETIME NULL,
  duration_ms          INT NULL,
  UNIQUE KEY uk_stage_exec_uuid (stage_execution_id),
  INDEX idx_exec_time (execution_id, started_at),
  INDEX idx_stage_status (stage_id, status),
  INDEX idx_scope_time (tenant_id, organization_id, started_at),
  INDEX idx_trace (trace_id)
);
```

### 1.5 pipeline_schedules (스케줄)

```sql
CREATE TABLE pipeline_schedules (
  id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
  schedule_name        VARCHAR(100) NOT NULL,
  pipeline_id          BIGINT NOT NULL,
  tenant_id            VARCHAR(50) NOT NULL,
  organization_id      BIGINT NULL,
  schedule_type        ENUM('CRON','INTERVAL','ONCE') NOT NULL,
  cron_expression      VARCHAR(100) NULL,
  interval_seconds     INT NULL,
  target_filter        JSON NULL,                      -- 파일 필터 보조 조건
  execution_params     JSON NULL,
  timezone             VARCHAR(50) NOT NULL DEFAULT 'UTC',
  is_active            TINYINT(1) NOT NULL DEFAULT 1,
  last_execution_at    DATETIME NULL,
  next_execution_at    DATETIME NULL,
  created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_active_next (is_active, next_execution_at),
  INDEX idx_scope (tenant_id, organization_id)
);
```

---

## 2) 상태머신 / 정책

* **실행(Pipeline Execution)**: `PENDING → RUNNING → {COMPLETED | FAILED | CANCELLED | TIMEOUT}`
* **단계(Stage)**: `STARTED → RUNNING → {COMPLETED | FAILED | SKIPPED | TIMEOUT}`
* **재시도**: `FAILED|TIMEOUT` 시 `retry_count++` 후 전략(`IMMEDIATE|LINEAR|EXPONENTIAL`). 임계 초과 시 실행 `FAILED` 확정.
* **취소(Cancel)**: `RUNNING`에서만 허용. 워커는 **스테이지 경계**에서 중단/롤백 후 `CANCELLED` 확정. 다른 상태의 취소는 **409(STATE_CONFLICT)**.
* **수동 재시도(Manual Retry)**: `FAILED|TIMEOUT`에서만 허용.

    * **Resume**(동일 실행 재개) 또는 **Re-run**(새 실행 생성, `parent_execution_id` 연결, `idempotency_key` 갱신) 중 정책 선택.

---

## 3) 이벤트 플로우 (S3→SQS)

* **인입**: S3(ObjectCreated|CompleteMultipartUpload) → EventBridge → **SQS: `ff-pipeline-ingest`**
* **메시지 속성**: `tenantId`, `organizationId`, `traceId`, `bucket`, `key`, `size`, `mime`, `eventName`, `occurredAt`
* **Consumer 동작**: 메시지 검증 → `pipeline_executions` **Upsert**(`status=PENDING`, `trigger_type=UPLOAD`, `idempotency_key`) → Ack
* **Visibility Timeout**: 워커 최대 처리 시간 + 여유(예: 5분)
* **Redrive Policy**: `maxReceiveCount=5` → **DLQ: `ff-pipeline-ingest-dlq`**
* **Backoff**: 지수 백오프(`base=2`, `max=60s`)

메시지 예시

```json
{
  "type": "s3.object.created",
  "bucket": "bkt",
  "key": "path/to/object",
  "size": 123456,
  "mime": "image/png",
  "tenantId": "tnt_demo",
  "organizationId": 123,
  "uploaderUserContextId": 9001,
  "traceId": "...",
  "occurredAt": "2025-10-22T10:20:30Z"
}
```

---

## 4) 실행 픽업 & 워커

* **Dispatcher**: `status='PENDING'`를 `(priority, created_at)` 정렬로 배치 픽업(인덱스 `idx_pipeline_pick`). 동시 Dispatcher에서도 **중복 할당 방지**.
* **Worker**: 스테이지 상태머신 실행. 대용량 입/출력은 오브젝트 스토리지에 저장하고 `*_ref`만 기록.
* **백프레셔**: 워커 과부하 시 동시 처리 제한/지수 백오프.

---

## 5) 다운스트림 이벤트(Extraction)

* 스테이지가 추출을 완료하면 `{ executionId, fileId, extractionType, method, confidence, traceId }` 이벤트 **at-least-once** 발행.
* 다운스트림 소비자는 **idempotent Upsert**를 구현.

---

## 6) 운영 API (조회/취소/재시도)

* **GET /pipelines/executions?status=&from=&to=&tenantId=&orgId=** → 페이지
* **GET /pipelines/executions/{executionId}** → `200 { status, stages[], startedAt, completedAt, durationMs, traceId }`
* **GET /pipelines/executions/{executionId}/logs** → 단계 로그 요약(콜드 데이터 참조)
* **POST /pipelines/executions/{executionId}:cancel** → `202 { accepted: true }` (조건: RUNNING)
* **POST /pipelines/executions/{executionId}:retry** → `202 { accepted: true, newExecutionId? }` (조건: FAILED|TIMEOUT)

권한: `pipeline.read`/`pipeline.cancel`/`pipeline.retry` (테넌트/조직 스코프 필수)

---

## 7) 관찰성 / 보안

* **Trace/Span**: `trace_id`를 실행/단계/추출 전파. 상관관계 대시보드 구축.
* **메트릭**: 실행 성공율, PENDING 체류시간, 단계 실패 TOP-K, 재시도율, 워커 처리량, DLQ 레이트.
* **로그**: `pipeline_stage_logs`는 **핫 테이블에 대용량 저장 금지**. 오브젝트 스토리지에 저장하고 `input_ref`/`output_ref` 참조. PII 최소화.

---

## 8) 운영 정책

* **우선순위 정규화**: `pipeline_definitions.priority` → 실행 시 **복사**(또는 생성 칼럼). 픽업 인덱스 `(status, priority, created_at)` 보장.
* **테넌트 격리**: 필요 시 큐/워커 라벨링으로 테넌트별 워크로드 분리.
* **청구/비용**: `tenant_id` 기준 실행/리소스 사용량 집계.
* **DLQ 재처리**: 샘플링→건별→벌크 재주입까지 툴 제공, 재주입 시 **idempotency** 유지.

---

## 9) 테스트 / DoD

* [ ] S3→SQS 인입에서 실행 Upsert(아이덴포턴시 보장)
* [ ] 픽업 인덱스에서 풀스캔 없음(우선순위/시간 정렬)
* [ ] 상태머신/재시도 경계값 테스트(임계 초과 시 FAILED 확정)
* [ ] 취소 요청 레이스(동시 완료/취소)에도 일관성 유지
* [ ] trace_id 상관관계(파일→실행→단계→추출) 확인
* [ ] 스케줄러 타임존/경계값 검증(옵션)
* [ ] 대용량 데이터는 참조키만 저장(행 비대화 방지)
* [ ] 테넌트/조직 스코프 필터 강제(越권 방지)
* [ ] 대시보드(성공율/PENDING 체류/실패 TOP-K/재시도율/DLQ)와 알람

---

## 10) 표준 에러 코드(초안)

* `PP-400-JSON` 메시지 스키마 오류
* `PP-403-SCOPE` 테넌트/조직 스코프 위반(운영 API)
* `PP-404-EXEC` 실행 없음
* `PP-409-IDEMP` 아이덴포턴시 충돌
* `PP-409-STATE` 상태 충돌(취소/재시도 부적합)
* `PP-500-IO` 스토리지/네트워크 오류
