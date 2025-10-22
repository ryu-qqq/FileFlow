# FileFlow – Pipeline Processing Phase 1 최소 기능명세서 (S3→SQS 워커)

> 범위: 업로드 완료 이후 **S3 이벤트 → SQS → 워커**로 동작하는 파일 처리 파이프라인의 **최소 기능**을 명세한다.
> API는 **상태 조회/취소/재시도 트리거** 등 운영용으로 한정하며, 실행 생성은 기본적으로 **이벤트 기반**으로 이뤄진다.
> 스키마는 "Pipeline Processing (v1, Greenfield·Hardened)" 문서의 테이블을 사용한다.

---

## 0. 공통 전제

* **이벤트 흐름**: S3(ObjectCreated|CompleteMultipartUpload) → S3 EventBridge → SQS(ingest queue) → Ingest Consumer → `pipeline_executions` Upsert(아이덴포턴시) → Stage Dispatcher/Worker.
* **권한/스코프**: 모든 처리에는 `tenant_id`, `organization_id`, `file_id`가 포함되어야 하고, 운영 API는 IAM(`pipeline.read`, `pipeline.cancel`, `pipeline.retry`)로 보호.
* **아이덴포턴시 키**: `idempotency_key = <s3_event.eventName>#<bucket>#<key>#<ingest_pipeline_code>`
* **트레이싱**: `trace_id`를 S3 이벤트에서 생성/전파(없으면 Consumer가 생성). Stage/Extraction에도 전파.
* **오류 포맷**: 내부 로그/RFC7807 Problem(JSON)

---

## 1) SQS Ingest Consumer – 실행 생성

**목적**: S3 이벤트를 받아 **파이프라인 실행을 자동 생성**하고 중복을 방지.

**요구사항**

* 메시지 스키마 검증(bucket/key/size/mime, tenant/org 컨텍스트).
* `pipeline_executions` Upsert: `status=PENDING`, `trigger_type=UPLOAD`, `idempotency_key` 고정.
* 실패 시 재시도(최대 N회) 후 DLQ로 이동.

**메시지 스키마(예)**

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

**권한/오류**

* 내부 시스템 권한. 400(스키마 불일치), 409(아이덴포턴시 충돌), 5xx(스토리지/DB).

**감사/메트릭**

* 소비 성공/실패 카운트, DLQ 레이트, 실행 생성 지연.

**테스트/DoD**

* 동일 S3 이벤트 반복 시 실행 1건만 생성. 잘못된 메시지는 DLQ로 격리.

---

## 2) Stage Dispatcher – 실행 픽업/할당

**목적**: PENDING 실행을 우선순위/생성시각 기준으로 **워커에 분배**.

**요구사항**

* 인덱스(`status, priority, created_at`) 조건으로 배치 조회, 락/가시성 타임아웃 고려.
* 워커 과부하 시 백프레셔(동시 처리 제한, 지수 백오프).

**권한/오류**

* 내부 시스템 권한. 409(상태 경합), 5xx.

**감사/메트릭**

* 큐 길이, 픽업/할당 속도, 드롭/충돌 수.

**테스트/DoD**

* N개 동시 Dispatcher에도 중복 할당 없음.

---

## 3) Stage Worker – 상태머신 실행

**목적**: 파이프라인 단계들을 **상태머신**으로 실행하고 재시도/실패를 관리.

**요구사항**

* 실행 상태: `PENDING→RUNNING→{COMPLETED|FAILED|CANCELLED|TIMEOUT}`
* 단계 상태: `STARTED→RUNNING→{COMPLETED|FAILED|SKIPPED|TIMEOUT}`
* 재시도: 단계별 `retry_strategy(MAX)`
* 입력/출력 대용량은 `*_ref`(S3 키 등)로 저장, 행 비대화 방지.

**권한/오류**

* 내부 시스템 권한. 5xx는 재시도, 비가역 실패는 `FAILED` 확정.

**감사/메트릭**

* 단계별 성공율, 실패 TOP-K(에러코드), 평균 `duration_ms`.

**테스트/DoD**

* 실패→재시도→임계초과→FAILED 플로우 커버.

---

## 4) Extraction Event Publisher – 다운스트림 통지

**목적**: OCR/파싱 등 **추출 완료**를 비동기로 알리고 저장자(Extraction 서비스)가 Upsert 하게 함.

**요구사항**

* 페이로드: `{ executionId, fileId, extractionType, method, confidence, traceId }`
* At-least-once. 중복 수신 대비 idempotency 키 포함.

**감사/메트릭**

* 발행 성공율/지연, 중복율.

**테스트/DoD**

* 중복 발행에도 다운스트림 Upsert 1회만.

---

## 5) API – 실행 상태 조회

**목적**: 운영/백오피스에서 실행의 진행상태/결과를 조회.

**요구사항**

* 기본 필터: `tenant_id`, `organization_id`, `status`, 기간.
* 상세 조회는 Stage 로그 요약과 trace 링크 제공.

**API**

* `GET /pipelines/executions?status=&from=&to=&tenantId=&orgId=` → 페이지
* `GET /pipelines/executions/{executionId}` → `200 { status, stages[], startedAt, completedAt, durationMs, traceId }`
* `GET /pipelines/executions/{executionId}/logs` → 단계 로그 요약(콜드 데이터 참조)

**권한/오류**

* `pipeline.read`. 403(越권), 404(없음).

**감사/메트릭**

* 조회 ALLOW/DENY, 응답 P95.

**테스트/DoD**

* 테넌트 경계 필터 강제, 삭제/취소 케이스 표시.

---

## 6) API – 실행 취소(Cancel)

**목적**: 장시간 실행/오작동을 **취소**.

**요구사항**

* `RUNNING`만 취소 가능 → 워커는 스테이지 경계에서 중단/롤백.
* 결과는 `CANCELLED`로 확정, 재시도 정책에서 제외.

**API**

* `POST /pipelines/executions/{executionId}:cancel` → `202 { accepted: true }`

**권한/오류**

* `pipeline.cancel`. 409(이미 완료/실패), 404.

**감사/메트릭**

* 취소 승인/실패 카운트, 평균 취소 소요.

**테스트/DoD**

* 취소 레이스 조건(동시 완료/취소) 안전.

---

## 7) API – 재시도(Manual Retry)

**목적**: 운영자가 실패 실행을 **수동 재시도**.

**요구사항**

* `FAILED|TIMEOUT`만 허용. 새 실행을 만들거나 동일 실행 재개 정책 중 택1(설정화).

**API**

* `POST /pipelines/executions/{executionId}:retry` → `202 { accepted: true, newExecutionId? }`

**권한/오류**

* `pipeline.retry`. 409(상태 부적합), 403.

**감사/메트릭**

* 수동 재시도 성공율/재실패율.

**테스트/DoD**

* 대량 재시도 시 백프레셔 적용.

---

## 8) 스케줄 실행 트리거 (옵션)

**목적**: 배치/정기 처리 파이프라인 실행 생성.

**요구사항**

* `pipeline_schedules.is_active=1 AND next_execution_at<=NOW()` → 실행 생성(아이덴포턴시 포함).

**권한/오류**

* 시스템 권한. 409(중복 생성), 5xx.

**테스트/DoD**

* 타임존/서머타임 경계 커버.

---

## 9) DLQ 처리 & 재처리 툴링

**목적**: DLQ로 빠진 메시지의 **안전한 재처리**.

**요구사항**

* 샘플링/건별 재처리/벌크 재처리 지원, 재처리 시 idempotency 유지.

**API/도구**

* 운영 CLI/어드민 UI(간단).

**감사/메트릭**

* 재처리 성공율, 재DLQ율.

**테스트/DoD**

* Poison 메시지 격리, 무한 루프 방지.

---

## 10) 관찰성/보안(최소)

**요구사항**

* **메트릭**: PENDING 큐 길이, 실행 성공율, 실패 TOP-K, 재시도율, 워커 처리량, DLQ 레이트.
* **로그**: 단계 에러코드/메시지 표준화, trace 연결. PII 최소화.
* **보안**: API는 IAM 보호. 내부 큐/토픽은 네트워크/키 정책으로 제한.

**테스트/DoD**

* 대시보드 카드와 알람 임계치 세팅.

---

## 11) 표준 에러 코드(초안)

* `PP-400-JSON` 메시지 스키마 오류
* `PP-403-SCOPE` 테넌트/조직 스코프 위반(운영 API)
* `PP-404-EXEC` 실행 없음
* `PP-409-IDEMP` 아이덴포턴시 충돌
* `PP-409-STATE` 상태 충돌(취소/재시도 부적합)
* `PP-500-IO` 스토리지/네트워크 오류

---

## 12) DoD 체크리스트 (Phase 1)

* [ ] S3→SQS 인입 경로에서 실행 아이덴포턴시 보장
* [ ] Dispatcher/Worker 상태머신 및 재시도 전략 동작
* [ ] Extraction 이벤트 발행(중복 안전)
* [ ] 운영 API(조회/취소/재시도) IAM 보호 + 스코프 필터
* [ ] DLQ 재처리 툴 동작(무한 루프 방지)
* [ ] 관찰성 대시보드/알람 구성(PENDING 길이/실패 TOP-K/DLQ)
* [ ] 멀티테넌트 스코프 필드 전파(tenant/org)
