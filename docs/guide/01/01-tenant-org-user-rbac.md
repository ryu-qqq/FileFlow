# FileFlow – Phase 1 최소 기능명세서 (Tenant·Org·User·RBAC)

> 범위: 테넌트·조직·사용자·권한 카탈로그·역할 할당·권한 평가·설정(EAV)·관찰성·초기 시드.
> 목표: 프로덕션 즉시 가동 가능한 **최소 10개 기능**을 명세(요구사항, API 계약, 에러/감사, 테스트 기준 포함).

---

## 0. 공통 전제

* **인증 주체**: 외부 IDP에서 발급한 사용자. 앱은 `user_contexts`로 내부 식별.
* **컨텍스트 전파**: 게이트웨이가 서명된 헤더 전달: `X-User-Id`, `X-Tenant-Id`, `X-Org-Id`, `X-Auth-Signature`.
* **권한 평가 규칙**: Role → Permission 매칭 → Scope(SELF/ORG/TENANT/GLOBAL) → ABAC(CEL) 순서.
* **FK 없음**: 정합성은 애플리케이션 레벨 검증 + Soft Delete(`deleted_at`).
* **오류 표준**: RFC 7807 Problem Details(JSON): `type`, `title`, `status`, `detail`, `code`(내부), `traceId`.

---

## 1. 인증 컨텍스트 주입

### 목적

* 요청 단위로 안전한 사용자/테넌트/조직 컨텍스트를 구성하여 이후 인가/감사에 사용.

### 기능 요구사항

* 서명 검증 실패 시 401/403 반환.
* 성공 시 RequestContext(실제 구현체 자유)에 `userContextId, tenantId, organizationId, roles?` 저장.

### API 계약

* **Interceptor/Middleware** (내부): 모든 보호 리소스에 적용.

### 에러

* 401 Unauthorized: 토큰/서명 검증 실패.
* 403 Forbidden: 허용되지 않은 테넌트/조직 조합.

### 감사/메트릭

* header 검증 실패 카운트, 서명 만료/위조 유형.

### 테스트

* 정상/서명 위조/만료/헤더 누락/테넌트-조직 불일치.

---

## 2. 테넌트/조직 관리 (CRUD + Soft Delete)

### 목적

* 테넌트/조직의 라이프사이클 관리 및 격리 경계 확립.

### 기능 요구사항

* `(tenant_id, org_code)` 유니크 보장.
* 삭제는 Soft Delete(`deleted_at`) 처리.

### API 계약

* **POST /tenants** `{name, status}` → `201 {id}`
* **PATCH /tenants/{id}** `{name?, status?}` → `204`
* **POST /organizations** `{tenantId, orgCode, name, status}` → `201 {id}`
* **PATCH /organizations/{id}** `{name?, status?}` → `204`
* **DELETE /organizations/{id}** → `204` (soft delete)

### 권한

* `org.manage`(TENANT 이상) 필요.

### 에러

* 409 Conflict: `uk_tenant_org_code` 충돌.
* 404 Not Found: 대상 없음/삭제됨.

### 감사/메트릭

* 생성/수정/삭제 감사 로그.

### 테스트

* 유니크 위반/상태 전환/삭제 후 조회 제외.

---

## 3. 설정(EAV) 관리 (우선순위 병합)

### 목적

* 테넌트/조직별 업로드 정책 등 가변 설정을 안전하게 저장/조회.

### 기능 요구사항

* 우선순위: **Org > Tenant > Default**.
* 비밀 키는 저장만(복호 출력 금지, 마스킹 처리).

### API 계약

* **GET /tenants/{id}/settings** → `200 {items[]}`
* **PUT /tenants/{id}/settings** `[{keyName, valueRaw}]` → `204`
* **GET /organizations/{id}/settings** → `200 {items[]}`
* **PUT /organizations/{id}/settings** `[{keyName, valueRaw}]` → `204`

### 권한

* 테넌트 설정: `org.manage`(TENANT)
* 조직 설정: `org.manage`(ORGANIZATION)

### 에러

* 422 Unprocessable: 스키마 불일치.

### 감사/메트릭

* 설정 변경 이력(구/신 값, 변경자).

### 테스트

* 병합 우선순위/스키마 검증/비밀 키 마스킹.

---

## 4. 사용자/멤버십

### 목적

* 외부 사용자와 시스템의 관계(테넌트/조직 소속)를 표현.

### 기능 요구사항

* 한 사용자는 여러 조직에 소속 가능.
* `(user, tenant, org)` 유니크.

### API 계약

* **POST /users** `{externalUserId, email?, displayName?}` → `201 {id}`
* **POST /users/{id}/memberships** `{tenantId, organizationId?, membershipType}` → `201 {membershipId}`
* **DELETE /users/{id}/memberships/{membershipId}** → `204`

### 권한

* 자신의 멤버십 조회는 SELF 허용, 생성/삭제는 운영 롤.

### 에러

* 409 Conflict: 중복 멤버십.

### 감사/메트릭

* 멤버십 추가/삭제 이력.

### 테스트

* 다중 소속/잘못된 tenant-org 조합/삭제 후 인가 범위 축소.

---

## 5. 권한 카탈로그(정의)

### 목적

* 원자 Permission과 Role을 중앙에서 정의/조회.

### 기능 요구사항

* Role ↔ Permission 매핑에 Scope/Condition(CEL) 부여 가능.

### API 계약

* **POST /permissions** `{code, description?}` → `201 {id}`
* **POST /roles** `{code, description?}` → `201 {id}`
* **POST /roles/{roleId}/permissions** `{permissionCode, scope, conditionName?, conditionExpr?}` → `201 {id}`
* **GET /roles/{roleId}/permissions** → `200 {items[]}`

### 권한

* 카탈로그 수정: `org.manage` (TENANT 이상)

### 에러

* 409 Conflict: 중복(role, permission, scope).

### 감사/메트릭

* 롤/퍼미션 변경 로그.

### 테스트

* 중복 방지/스코프/조건 저장 및 조회.

---

## 6. 역할 할당(스코프드)

### 목적

* 사용자에게 역할을 테넌트/조직 스코프 단위로 할당.

### 기능 요구사항

* `tenantId` 또는 `organizationId` 지정 가능(둘 다 NULL이면 전역은 금지).
* 성공 시 권한 캐시 무효화 이벤트 발행.

### API 계약

* **POST /users/{id}/roles** `{roleCode, tenantId?, organizationId?, resourceFilter?}` → `201 {mappingId}`
* **DELETE /users/{id}/roles/{mappingId}** → `204`

### 권한

* 운영 롤 필요(예: `tenant.admin`).

### 에러

* 400 Bad Request: 스코프/컨텍스트 불일치.

### 감사/메트릭

* 할당/회수 로그, 캐시 무효화 카운터.

### 테스트

* ORG/TENANT 스코프별 할당, 중복/불일치 거부.

---

## 7. 권한 평가(코어 엔진)

### 목적

* 주어진 Permission에 대해 허용/거부 판단.

### 기능 요구사항

* Role → Permission 매칭 → Scope 필터 → ABAC(CEL) 평가.
* P95(캐시 적중 시) 50ms 이하.

### API 계약

* **POST /iam/evaluate**

```json
{
  "permission": "file.upload",
  "context": {"tenantId":"tnt_abc","organizationId":123,"userContextId":9001},
  "resource": {"mime":"image/jpeg","size_mb":7}
}
```

* **200 응답**

```json
{
  "allowed": true,
  "matchedRole": "org.uploader",
  "scope": "ORGANIZATION"
}
```

### 에러

* 403 Forbidden: 권한 없음(매칭 실패/조건 불충족).

### 감사/메트릭

* 거부 사유(단계별: role/perm/scope/abac), 평가 지연, 캐시 적중률.

### 테스트

* SELF/ORG/TENANT/GLOBAL 경계/조건식 경계(용량, MIME, 시간).

---

## 8. 업로드 연동 훅(최소)

### 목적

* 업로드 세션 생성 전 권한·정책 검증.

### 기능 요구사항

* 허용 시 ABAC에서 최대 크기/허용 MIME를 추출해 Presigned 생성 파라미터에 전달.

### API 계약

* (내부) 업로드 세션 생성 use case가 `evaluate(file.upload)`를 선 호출.

### 에러/감사/테스트

* 평가 실패 시 세션 생성 중단/사유 로깅. 정상/거부 케이스 유닛테스트.

---

## 9. 관찰성(최소)

### 목적

* 인가 문제를 빠르게 파악하고 UX 개선 피드백 제공.

### 기능 요구사항

* 403 로그: 거부 단계·조건 키 요약.
* 메트릭: 허용/거부 비율, 평가 지연 P50/P95, 캐시 적중률.

### API/수집

* 내장 메트릭 엔드포인트(`/metrics`) 혹은 로그 기반.

### 테스트

* 메트릭 노출/대시보드 카드 생성.

---

## 10. 초기 시드

### 목적

* 기본 동작 검증을 위한 최소 권한/역할/조건 제공.

### 시드 내용(예)

* permissions: `file.upload`, `file.read`, `file.delete`, `org.manage`
* roles: `org.uploader`, `org.manager`, `tenant.admin`
* role_permissions:

    * `org.uploader` + `file.upload` (scope=ORGANIZATION, condition: 이미지/PDF & size≤20MB)
    * `org.uploader` + `file.read`   (scope=ORGANIZATION)
    * `tenant.admin` + `org.manage`  (scope=TENANT)

### DoD

* 시드 후 샘플 사용자에 대한 `file.upload` 허용/거부 시나리오 통과.

---

## 부록 A. 표준 에러 코드(초안)

* `IAM-401-001`: 서명 검증 실패
* `IAM-403-001`: 권한 없음(매칭 실패)
* `IAM-403-002`: 스코프 불일치
* `IAM-403-003`: 조건(ABAC) 불충족
* `IAM-409-001`: 유니크 충돌
* `IAM-422-001`: 설정 스키마 불일치

## 부록 B. 감사 필드 표준

* `actorUserContextId`, `tenantId`, `organizationId`, `action`, `oldValue`, `newValue`, `reason`, `traceId`, `at`
