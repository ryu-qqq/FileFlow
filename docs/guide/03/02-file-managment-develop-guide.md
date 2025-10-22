# FileFlow – File Management Phase 1 최소 기능명세서 (Greenfield)

> 범위: 파일 도메인 **핵심 12개 기능**을 최소한으로 정의. 업로드는 별도 에픽(Upload Management)에서 처리하며, 본 문서는 **파일 자산/조회/다운로드/목록/삭제/변종/관계/메타데이터/정리/로그/관찰성**만을 다룸.
> 전제: v2 하드닝 스키마(`file_assets`, `file_variants`, `file_relationships`, `file_metadata`, `file_access_logs`)를 기준. FK 없음, Soft Delete, 서명 URL은 런타임 생성.

---

## 0. 공통 전제

* **컨텍스트**: `X-User-Id`, `X-Tenant-Id`, `X-Org-Id`, `X-Auth-Signature` 서명 헤더.
* **권한 규칙**: IAM `evaluate(permission)` → Role→Permission 매칭 → Scope(SELF/ORG/TENANT/GLOBAL) → ABAC(필요 시).
* **가시성 규칙**: `visibility = PRIVATE/INTERNAL/PUBLIC` (PUBLIC도 기본은 서명 URL). 모든 접근은 **접근 로그** 기록.
* **리소스 스코프**: `tenant_id`, `organization_id`, `uploader_user_context_id`를 리소스 컨텍스트로 사용.
* **오류 포맷**: RFC7807 `{type,title,status,detail,code,traceId}`.

---

## 1) 파일 확정/수신 (이벤트 소비자)

**목적**: Upload Management가 발행한 `upload.completed`를 받아 파일 자산을 **생성/갱신**.

**요구사항**

* 이벤트 페이로드로 `file_assets` 레코드 생성: `status=PROCESSING`, `processing_status=PENDING`, 체크섬/메타 저장.
* 아이덴포턴시: `sessionId` 기준 **중복 방지**.
* (옵션) 필수 변종(Job) 트리거.

**API/이벤트**

* **Consumer**: `upload.completed`

    * 입력: `{ sessionId, tenantId, organizationId, uploaderUserContextId, storage:{bucket,key}, content:{mime,size,checksumSha256}, visibility, occurredAt }`
    * 처리: upsert 후 `200/ACK`

**권한/오류**

* 내부 시스템 권한. 409(중복 처리 충돌), 422(필드 누락), 500(I/O).

**감사/메트릭**

* 이벤트 처리 성공율/지연, 중복 드롭 수.

**테스트/DoD**

* 중복 이벤트 수신 시 1회만 생성, 필수 필드 누락/이상치 방어.

---

## 2) 파일 메타 조회

**목적**

* 파일 상세 메타/상태/가시성 제공.

**요구사항**

* 기본 필터: `deleted_at IS NULL`.
* PUBLIC이라도 접근 로그 기록.

**API**

* **GET /files/{fileId}** → `200 { fileId, tenantId, orgId, mime, size, visibility, status, createdAt, uploaderId, ... }`

**권한/오류**

* `file.read` (SELF/ORG/TENANT) 또는 PUBLIC 허용.
* 403(스코프/가시성 위반), 404(없음/삭제됨).

**감사/메트릭**

* 조회 ALLOW/DENY, P95 응답시간.

**테스트/DoD**

* PRIVATE/INTERNAL/PUBLIC 케이스, 삭제/가시성 차단 확인.

---

## 3) 다운로드 – 서명 URL 발급

**목적**

* 안전한 다운로드 URL 생성(경로만 저장, URL은 서명/TTL).

**요구사항**

* PUBLIC도 기본 **서명 URL**. TTL/Content-Disposition 옵션.

**API**

* **GET /files/{fileId}/download** → `200 { signedUrl, expiresInSec }` 또는 `302` 리다이렉트.

**권한/오류**

* `file.read` 또는 PUBLIC 허용. 403/404.

**감사/메트릭**

* 발급 성공/거부, TTL 분포.

**테스트/DoD**

* ORG/TENANT 경계, 만료/삭제 파일 차단.

---

## 4) 파일 목록/검색(경량)

**목적**

* 테넌트/조직 범위의 기본 조회와 페이징.

**요구사항**

* 필터: `orgId, status, uploader, created_from/to, q(파일명 LIKE)`.

**API**

* **GET /files?orgId=&status=&uploader=&from=&to=&q=** → `200 { items[], page }`

**권한/오류**

* `file.read` 필요. 400(잘못된 필터), 403(스코프 위반).

**감사/메트릭**

* 쿼리 P95, 상위 필터 조합.

**테스트/DoD**

* 기간/상태/업로더 필터 조합, 기본 정렬/페이징.

---

## 5) 파일 삭제 (Soft Delete)

**목적**

* 가역 삭제 후 보류기간 경과 시 물리 삭제.

**요구사항**

* `deleted_at` 설정, `status=DELETED` 전환.

**API**

* **DELETE /files/{fileId}** → `204`

**권한/오류**

* `file.delete`(TENANT 또는 ORG 운영자). 404/403.

**감사/메트릭**

* 삭제 이력/사유 코드.

**테스트/DoD**

* 삭제 후 조회/다운로드 차단, 정리 워커 연동.

---

## 6) 변종 생성 트리거/조회

**목적**

* 썸네일/포맷변환 등 파생 파일 관리.

**요구사항**

* 원본과 동일 테넌트/조직만 허용(크로스-테넌트 금지).

**API**

* **POST /files/{fileId}/variants** `{ variantType }` → `202 { variantId }`
* **GET  /files/{fileId}/variants** → `200 { items[] }`

**권한/오류**

* 생성: 운영자/백오피스 롤. 조회: `file.read`.
* 409(중복), 403/404.

**감사/메트릭**

* 변종 생성 성공율, 처리 SLA.

**테스트/DoD**

* 테넌트 경계 검사, 상태 전이(PENDING→READY/FAILED).

---

## 7) 파일 관계 Link/Unlink

**목적**

* 버전/첨부/파생 등 파일 간 관계 표현.

**요구사항**

* `src/dst` 테넌트 불일치 거부, `rel_type` 사전 정의.

**API**

* **POST /files/{srcFileId}/links** `{ dstFileId, relType }` → `201 { id }`
* **DELETE /files/links/{id}** → `204`

**권한/오류**

* `file.read`(양쪽 스코프 충족). 400/403/404.

**감사/메트릭**

* 링크 생성/해제 로그.

**테스트/DoD**

* 자기참조/중복 방지, 스코프 검사.

---

## 8) 메타데이터 EAV Upsert/조회

**목적**

* 파일별 커스텀 속성 저장. 비밀 키는 마스킹 출력.

**요구사항**

* `(file_id, key_name)` 유니크, `is_secret=1`은 조회 시 마스킹.

**API**

* **PUT /files/{fileId}/metadata** `[{ keyName, valueRaw, isSecret? }]` → `204`
* **GET /files/{fileId}/metadata** → `200 { items[] }`

**권한/오류**

* 조회: `file.read`. 수정: 운영자.
* 422(스키마 불일치).

**감사/메트릭**

* 변경 이력, 비밀 키 접근 시도 카운트.

**테스트/DoD**

* 마스킹/업서트/유니크 위반 처리.

---

## 9) 접근 로그 파이프라인(비동기)

**목적**

* ALLOW/DENY를 유실 없이 적재·분석.

**요구사항**

* 앱→큐→MySQL(7일)→S3/Athena(장기). IP 마스킹 저장.

**API**

* 내부 Producer 인터페이스 + 배치 인서터.

**권한/오류**

* 시스템 권한. 배치 실패 재시도/보류 큐.

**감사/메트릭**

* 적재율/지연/실패 건수.

**테스트/DoD**

* 고QPS에서도 유실 없음.

---

## 10) 만료/정리 워커

**목적**

* `effective_expires_at` 기반 자동 정리.

**요구사항**

* 만료 대상 소프트 삭제 → 보류기간(예: 30일) 후 물리 삭제.

**API**

* 워커 잡(스케줄러), 설정: 보류기간 일수.

**권한/오류**

* 시스템 권한. 실패 시 재시도/알람.

**감사/메트릭**

* 일별 정리 건수, 실패 누적.

**테스트/DoD**

* 경계시각·타임존·보류기간 검증.

---

## 11) 관찰성(대시보드)

**목적**

* 품질/장애 징후 조기 인지.

**요구사항**

* 카드: 처리 실패율, 403 비율, 만료 정리 건수, 중복 TOP-K, 다운로드 TTL 분포.

**테스트/DoD**

* 메트릭 노출/알람 임계치 설정.

---

## 12) 표준 에러 코드(초안)

* `FILE-401-001` 서명 검증 실패
* `FILE-403-001` 권한 없음(스코프 미일치)
* `FILE-403-002` 가시성 제한
* `FILE-403-003` ABAC 조건 불충족
* `FILE-404-001` 파일 없음/삭제됨
* `FILE-409-001` 관계/변종 중복
* `FILE-422-001` 입력 스키마 불일치
* `FILE-500-001` 스토리지/네트워크 오류

---

## 13) DoD 체크리스트 (Phase 1)

* [ ] `upload.completed` 이벤트 수신 → 파일 생성/갱신(아이덴포턴시 보장)
* [ ] 파일 조회/다운로드(서명 URL)에서 가시성/RBAC 준수
* [ ] 목록/검색(경량) 정상 동작
* [ ] 소프트 삭제 → 워커 물리 삭제 흐름
* [ ] 변종 생성/조회 + 테넌트 경계 검사
* [ ] 링크/언링크 + 스코프 검사
* [ ] 메타데이터 업서트/마스킹
* [ ] 접근 로그 비동기 적재(유실 없음)
* [ ] 관찰성 메트릭/알람 구성
