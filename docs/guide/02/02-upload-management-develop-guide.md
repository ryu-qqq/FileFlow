# FileFlow – Upload Management Phase 1 최소 기능명세서

> 범위: **업로드 행위**에 필요한 최소 기능(세션/멀티파트/외부 다운로드/정책/이벤트/관찰성)을 정의.
> 전제: Greenfield 스키마(에픽 문서의 `upload_*` 테이블), FK 미사용, Soft Delete 우선. Upload는 DB `file_assets`를 **직접 쓰지 않음**(완료 이벤트로 File Management에 위임).

---

## 0. 공통 전제

* **컨텍스트**: 게이트웨이 서명 헤더 `X-User-Id / X-Tenant-Id / X-Org-Id / X-Auth-Signature`.
* **인가 규칙**: `IAM.evaluate("file.upload")` → Scope(SELF/ORG/TENANT) → ABAC(허용 MIME/최대 크기/시간대 등).
* **세션 TTL**: 기본 15분(환경 변수로 조절), 만료 시 업로드 불가.
* **오류 표준**: RFC7807 Problem(JSON) `{type,title,status,detail,code,traceId}`.
* **아이덴포턴시**: 세션 생성/완료 호출에 `Idempotency-Key` 헤더 지원.

---

## 1) 업로드 세션 생성

**목적**: 업로드 시작 전 권한과 정책을 확정하고 저장 경로/서명 파라미터를 결정.

**요구사항**

* `IAM.evaluate("file.upload")` 결과를 기반으로 정책 확정(허용 MIME/최대 크기/시간대 등) → `policy_snapshot_json` 저장.
* 저장 위치(`bucket/key`), 공급자(S3 등), 세션 TTL 계산.
* 싱글/멀티파트 모두 이 엔드포인트에서 시작.

**API**

* `POST /uploads/sessions`

    * 요청 `{ visibility, mime, size_mb, filename }`
    * 응답 `201 { sessionId, bucket, key, provider, presigned: { type, url|form }, expiresAt }`

**권한/오류**

* 권한: `file.upload`(ORG 스코프+ABAC) 필요.
* 오류: 403(권한/ABAC 불충족), 422(입력 스키마/폴리시 불일치).

**감사/메트릭**

* 세션 생성 ALLOW/DENY 카운트, 정책 유형 통계.

**테스트/DoD**

* MIME/사이즈 경계(=허용치/초과), 시간대 규칙, 다른 테넌트/조직 거부.

---

## 2) 세션 조회/취소/만료

**목적**: 클라이언트가 세션 상태를 확인하거나 명시적으로 중단.

**요구사항**

* 만료(`expires_at < now`) 또는 ABORT 시 업로드 금지.

**API**

* `GET /uploads/sessions/{sessionId}` → `200 { status, expiresAt, policySnapshot }`
* `DELETE /uploads/sessions/{sessionId}` → `204` (ABORT)

**권한/오류**

* 권한: 세션 소유자(SELF) 또는 운영자.
* 오류: 404(없음/만료 후 정리), 409(이미 완료된 세션 ABORT 시도).

**감사/메트릭**

* ABORT율, 만료율.

**테스트/DoD**

* 만료 후 사용 불가, ABORT 이후 파트 발급 차단.

---

## 3) 멀티파트 초기화/파트 발급

**목적**: 대용량 업로드를 위해 파트 단위 서명 URL 발급.

**요구사항**

* 초기화 시 `provider_upload_id` 저장.
* 각 `part_no`에 대해 presigned URL(또는 form)을 발급, `upload_parts`에 상태 저장.

**API**

* `POST /uploads/sessions/{sessionId}:multipart-init` → `200 { providerUploadId }`
* `POST /uploads/sessions/{sessionId}:part` `{ partNo }` → `200 { url|form, expiresAt }`

**권한/오류**

* 권한: 세션 소유자(SELF).
* 오류: 409(상태 충돌, 예: 완료/만료 후 발급), 422(잘못된 partNo).

**감사/메트릭**

* 파트 발급 건수/만료율.

**테스트/DoD**

* 중복 파트 발급, 만료 세션 차단, 최대 파트 수 한도.

---

## 4) 파트 업로드 마크/검증

**목적**: 클라이언트가 업로드한 파트를 서버가 검증/기록.

**요구사항**

* 업로드 후 `etag/size_bytes` 수신, 상태 `UPLOADED`로 갱신.
* 필요 시 ETag/크기 검증 실패 처리.

**API**

* `PUT /uploads/sessions/{sessionId}:part` `{ partNo, etag, size }` → `204`

**권한/오류**

* 권한: 세션 소유자(SELF).
* 오류: 404(파트 미발급), 409(상태 충돌), 422(ETag/크기 불일치).

**감사/메트릭**

* 파트 실패율, 재시도율.

**테스트/DoD**

* 누락/중복/순서 불일치 시나리오.

---

## 5) 멀티파트 조립(Complete)

**목적**: 모든 파트가 업로드된 뒤 스토리지에서 병합.

**요구사항**

* 모든 파트 상태 확인 후 조립 API 호출, 성공 시 세션 `COMPLETED` 전환.
* 체크섬(SHA-256) 계산 작업 트리거(비동기 가능).
* **이벤트 발행**: `upload.completed` (아래 §9 참조).

**API**

* `POST /uploads/sessions/{sessionId}:complete` `{ parts[] }` → `200 { completed: true }`

**권한/오류**

* 권한: 세션 소유자(SELF).
* 오류: 409(파트 누락/상태), 500(스토리지 조립 실패).

**감사/메트릭**

* 조립 성공율/P95.

**테스트/DoD**

* 파트 일부 누락/ETag 불일치/조립 실패 롤백.

---

## 6) 싱글파트 업로드(경량 경로)

**목적**: 소형 파일은 멀티파트 없이 단일 presign으로 처리.

**요구사항**

* 세션 생성 시 `presigned.url` 즉시 반환.
* 업로드 완료 웹훅/헤드체크 후 `upload.completed` 발행.

**API**

* (세션 생성과 공유) + `POST /uploads/sessions/{sessionId}:single-complete`

**권한/오류**

* 권한: 세션 소유자(SELF). 오류: 409/422.

**테스트/DoD**

* 분기 로직(임계 사이즈) 및 완료확인 경로.

---

## 7) 외부 다운로드(서버 인게스트)

**목적**: 외부 URL에서 서버가 파일을 가져와 업로드 완료까지 대행.

**요구사항**

* 세션 생성 후 워커가 `source_url`을 다운로드, 진행상태/오류 코드 기록.
* 완료 시 `upload.completed` 발행.

**API**

* `POST /uploads/external` `{ url, visibility, mimeHint? }` → `202 { sessionId }`
* `GET  /uploads/external/{sessionId}` → `200 { status, transferred, error? }`

**권한/오류**

* 권한: `file.upload`.
* 오류: 400(지원 스킴 아님), 4xx/5xx 전파.

**테스트/DoD**

* 리다이렉트/콘텐츠길이 불일치/타임아웃/재시도 백오프.

---

## 8) 정책 평가/스냅샷(Policy Resolver)

**목적**: 업로드 정책을 ABAC+설정(EAV)에서 계산해 세션과 함께 고정.

**요구사항**

* 허용 MIME/최대 크기/시간대/속도/쿼터 계산.
* 결과를 `policy_snapshot_json`에 저장(감사/디버그).

**API**

* 내부 서비스. 세션 생성 시 자동 호출.

**테스트/DoD**

* 템플릿/오버라이드 병합, 시간대/멤버십 타입 분기.

---

## 9) 완료/실패 이벤트 발행 (필수)

**목적**: File Management가 파일을 확정하도록 표준 이벤트를 전달.

**요구사항**

* 최소 1회 보장(At-least-once). `sessionId`를 **아이덴포턴시 키**로 포함.
* `upload.completed` 페이로드(예):

```json
{
  "type": "upload.completed",
  "sessionId": "usn_abc...",
  "tenantId": "tnt_demo",
  "organizationId": 123,
  "uploaderUserContextId": 9001,
  "storage": {"bucket":"bkt","key":"path/to/object"},
  "content": {"mime":"image/png","size":123456,"checksumSha256":"..."},
  "visibility": "PRIVATE",
  "occurredAt": "2025-10-22T10:20:30Z"
}
```

**오류/재시도**

* 전송 실패 시 지수 백오프/데드레터 큐.

**테스트/DoD**

* 중복 수신 시 수용자(idempotent) 안전.

---

## 10) 관찰성/보안(최소)

**요구사항**

* 메트릭: 세션 성공율, ABAC 거부율, 멀티파트 재시도율, 외부 다운로드 속도/성공율, presign 지연.
* 로그: 세션 생성/ABORT/COMPLETE, 파트 상태, 외부 다운로드 오류. PII 최소/마스킹.
* 보안: PUBLIC도 기본 서명 URL 사용, Referrer/CORS/Content-Type 검증, 세션 재사용 방지.

**테스트/DoD**

* 대시보드 카드 5종, 알람 임계치 설정.

---

## 11) 표준 에러 코드(초안)

* `UP-401-001` 서명/인증 실패
* `UP-403-ABAC` 정책 조건 불충족
* `UP-403-SCOPE` 스코프 불일치
* `UP-404-SESN` 세션 없음/만료
* `UP-409-MPSTATE` 멀티파트 상태 충돌
* `UP-409-DUPSHA` 체크섬 중복(정책상 차단 시)
* `UP-422-VALID` 입력 스키마 불일치
* `UP-500-IO` 스토리지/네트워크 오류

---

## 12) DoD 체크리스트 (Phase 1)

* [ ] 세션 생성 시 IAM 평가 + 정책 스냅샷 저장
* [ ] 세션 조회/만료/ABORT 동작
* [ ] 멀티파트 초기화/파트 발급/업로드 마크/조립 완료
* [ ] 싱글파트 경량 경로 동작
* [ ] 외부 다운로드 워커 동작(기본 시나리오)
* [ ] `upload.completed/failed/aborted/expired` 이벤트 발행
* [ ] 관찰성(메트릭/로그) & 보안 가드 동작
* [ ] 아이덴포턴시 및 재시도/백오프 검증
