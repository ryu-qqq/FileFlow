# FileFlow – Tenant · Organization · User Permission

> 목표: 다중 조직 소속, 스코프드 롤, 조건부 권한(ABAC), 소프트 삭제, 캐시/인덱스/운영 표준까지 포함한 **실서비스용 권한 체계** 완성.

---

## 0) 용어 정리

* **Tenant**: 서비스 구독 단위(회사/셀러/파트너 등).
* **Organization(Org)**: 테넌트 내부 조직/팀/브랜드/프로젝트 등 하위 스코프.
* **UserContext**: 외부 IDP(SSO)로 인증된 사용자의 FileFlow 내부 식별자/컨텍스트.
* **Role / Permission**: 역할(권한 묶음) / 원자적 권한 코드.
* **Scope**: 권한의 유효 범위: `SELF | ORGANIZATION | TENANT | GLOBAL`.
* **ABAC**: 속성 기반 접근제어(조건식 평가, CEL 기반).

---

## 1) 데이터 모델(핵심)

### 1.1 Tenants / Organizations (+ Soft Delete)

* **원칙**: FK는 운영 편의상 미사용(=느슨한 결합). 정합성은 애플리케이션 가드 + 배치 검증.
* **Soft Delete**: 모든 중요 엔티티에 `deleted_at` 추가, 기본 조회는 `WHERE deleted_at IS NULL`.

```sql
CREATE TABLE tenants (
  id            VARCHAR(50) PRIMARY KEY,
  name          VARCHAR(200) NOT NULL,
  status        ENUM('ACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at    DATETIME NULL,
  UNIQUE KEY uk_tenant_name (name),
  INDEX idx_tenant_status (status),
  INDEX idx_deleted_at (deleted_at)
);

CREATE TABLE organizations (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     VARCHAR(50) NOT NULL,
  org_code      VARCHAR(100) NOT NULL,
  name          VARCHAR(200) NOT NULL,
  status        ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at    DATETIME NULL,
  -- 전역 유니크 제거, 테넌트 스코프 유니크만 유지
  UNIQUE KEY uk_tenant_org_code (tenant_id, org_code),
  INDEX idx_tenant_status (tenant_id, status),
  INDEX idx_deleted_at (deleted_at)
);
```

### 1.2 Settings (EAV) + Schema Registry

* **우선순위**: `Org > Tenant > Default` (가장 구체적인 설정이 승리).
* **스키마**: 키별 타입/제약 정의(`setting_schemas`) + 암호화 여부.

```sql
CREATE TABLE setting_schemas (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  key_name     VARCHAR(150) UNIQUE NOT NULL,
  value_type   ENUM('STRING','INT','BOOL','JSON') NOT NULL,
  json_schema  JSON NULL,      -- value_type = JSON 일 때 유효성
  is_secret    TINYINT(1) NOT NULL DEFAULT 0,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tenant_settings (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id    VARCHAR(50) NOT NULL,
  key_name     VARCHAR(150) NOT NULL,
  value_raw    TEXT NULL,      -- 암호값은 KMS Envelope로 앱단 암복호화
  updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tenant_key (tenant_id, key_name),
  INDEX idx_tenant_key (tenant_id, key_name)
);

CREATE TABLE organization_settings (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id    VARCHAR(50) NOT NULL,
  organization_id BIGINT NOT NULL,
  key_name     VARCHAR(150) NOT NULL,
  value_raw    TEXT NULL,
  updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_org_key (tenant_id, organization_id, key_name),
  INDEX idx_org_key (tenant_id, organization_id, key_name)
);
```

### 1.3 User Context & Memberships (다중 소속)

* 한 사용자가 **여러 조직에 소속**될 수 있음.
* 멤버십 유형별(EMPLOYEE/SELLER_MEMBER/GUEST/SYSTEM) 구분.

```sql
CREATE TABLE user_contexts (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  external_user_id   VARCHAR(200) NOT NULL,  -- IDP의 sub 등
  email              VARCHAR(200) NULL,
  display_name       VARCHAR(200) NULL,
  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_external (external_user_id)
);

CREATE TABLE user_org_memberships (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_context_id  BIGINT NOT NULL,
  tenant_id        VARCHAR(50) NOT NULL,
  organization_id  BIGINT NULL,  -- NULL이면 테넌트 레벨 멤버십
  membership_type  ENUM('EMPLOYEE','SELLER_MEMBER','GUEST','SYSTEM') NOT NULL,
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_tenant_org (user_context_id, tenant_id, organization_id),
  INDEX idx_user (user_context_id),
  INDEX idx_tenant_org (tenant_id, organization_id)
);
```

### 1.4 RBAC(+조건) 모델

* **Permissions**: 원자 권한 코드(예: `file.upload`, `file.read`, `org.manage`).
* **Roles**: Permission 집합.
* **Role → Permission** 매핑에 **조건(ABAC)** 허용.
* **User → Role** 매핑은 **스코프(tenant/org)** 단위로 부여 가능.

```sql
CREATE TABLE permissions (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  code         VARCHAR(150) UNIQUE NOT NULL,  -- ex) file.upload
  description  VARCHAR(300) NULL,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  code         VARCHAR(150) UNIQUE NOT NULL,  -- ex) tenant.admin
  description  VARCHAR(300) NULL,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at   DATETIME NULL,
  INDEX idx_role_deleted (deleted_at)
);

CREATE TABLE role_permissions (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id      BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  scope        ENUM('SELF','ORGANIZATION','TENANT','GLOBAL') NOT NULL,
  condition_name  VARCHAR(100) NULL,  -- condition_schemas.name 참조
  condition_expr  TEXT NULL,          -- CEL 식
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_role_perm_scope (role_id, permission_id, scope)
);

-- ABAC 조건 스키마 레지스트리(선택적)
CREATE TABLE condition_schemas (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  name         VARCHAR(100) UNIQUE NOT NULL,      -- ex) file.upload.limit.v1
  cel_example  TEXT NULL,
  json_schema  JSON NULL,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 사용자에게 역할을 스코프 단위로 할당
CREATE TABLE user_role_mappings (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_context_id  BIGINT NOT NULL,
  role_id          BIGINT NOT NULL,
  tenant_id        VARCHAR(50) NULL,
  organization_id  BIGINT NULL,
  resource_filter  JSON NULL,  -- 리소스 제한(선택)
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_scope (user_context_id, tenant_id, organization_id)
);
```

> **스코프 의미**
>
> * `GLOBAL`: 시스템 전역(보통 SYSTEM 역할 전용)
> * `TENANT`: 같은 `tenant_id` 내에서 유효
> * `ORGANIZATION`: 같은 `tenant_id` & `organization_id`에서만 유효
> * `SELF`: 자원 owner 기준으로 본인에게만 유효

---

## 2) 권한 평가 규칙

### 2.1 Self/Tenant/Org 스코프 명문화

* **SELF**: `resource.owner_user_context_id == current.user_context_id`
* **ORGANIZATION**: `resource.tenant_id == current.tenant_id && resource.organization_id == current.organization_id`
* **TENANT**: `resource.tenant_id == current.tenant_id`
* **GLOBAL**: 항상 참(단, SYSTEM 권한만 부여)

### 2.2 ABAC 조건(CEL) – 컨벤션

* **평가 언어**: Google **CEL** (간결/고성능, JVM 구현체 존재)
* **컨텍스트 표준 키**

  * `ctx.tenant_id`, `ctx.organization_id`, `ctx.user_context_id`, `ctx.membership_type`
  * `ctx.request_ip`, `ctx.user_agent`, `ctx.now_epoch_sec`
* **리소스 표준 키**

  * 파일: `res.owner_user_context_id`, `res.mime`, `res.size_mb`, `res.org_id`, `res.tenant_id`
  * 세션: `res.session_type`, `res.expires_at`, `res.created_by`
* **CEL 예시**

  * 파일 업로드 확장자 제한: `in(res.mime, ["image/jpeg","image/png","application/pdf"])`
  * 사이즈 제한(<= 50MB): `res.size_mb <= 50`
  * 업무시간 제한(09~20시): `9 <= getHour(ctx.now_epoch_sec, "Asia/Seoul") && getHour(ctx.now_epoch_sec, "Asia/Seoul") < 20`
  * 특정 멤버십만 허용: `ctx.membership_type in ["EMPLOYEE","SYSTEM"]`

> **참고**: `getHour()` 같은 헬퍼는 서버에 확장 함수로 제공.

### 2.3 평가 알고리즘(의사코드)

```java
// 1) 결과 캐시 조회 (user, tenant, org)
Set<Grant> grants = cache.get(key);
if (grants == null) {
  grants = grantBuilder.build(userContextId, tenantId, orgId); // roles + role_permissions 조인
  cache.put(key, grants, TTL_5MIN);
}

// 2) 스코프 1차 필터
Stream<Grant> scoped = grants.stream().filter(g -> matchesScope(g.scope(), resource, ctx));

// 3) ABAC 평가(CEL)
boolean allowed = scoped.anyMatch(g -> cel.eval(g.conditionExpr(), ctx, resource));
if (!allowed) throw new PermissionDenied(...);
```

---

## 3) 게이트웨이/마이크로서비스 전달

* 내부 단일 앱: request attribute로 컨텍스트 주입 가능.
* 마이크로서비스간: **서명된 헤더**로 전달 + 각 서비스에서 서명 검증.

  * `X-User-Id`, `X-Tenant-Id`, `X-Org-Id`, `X-Roles`(선택), `X-Auth-Signature`
  * 서명 Payload: 위 헤더 + 만료 시각 + nonce → HMAC(KID)

---

## 4) 캐시 전략

1. **Effective Grants Cache**

* 키: `user_context_id:tenant_id:organization_id`
* 값: 펼친 Permission+Scope+Condition 집합
* TTL: 5~10분, 변경 이벤트(역할/권한/맵핑/조건) 시 Pub/Sub로 무효화

2. **Settings Cache**

* (tenant, org, key) 레벨 캐시(짧은 TTL)
* 비밀 설정은 평문 캐시 금지, KMS DEK로 복호화 후 단기 메모리 캐시만 허용

---

## 5) 운영 규약

* **정합성 가드**: 유스케이스 레벨에서 존재/상태 체크(tenant/org ACTIVE 등).
* **정합성 배치**: 매일 03:00, 소프트 삭제 누락/고아 데이터 점검 및 자동 청소.
* **감사 로깅**: 역할 부여/회수, 설정 변경(누가/언제/무엇을) 이력 테이블 작성 권장.
* **인덱스 표준**: `tenant_id, organization_id, created_at` 복합 인덱스 선호.

---

## 7) API 계약(샘플)

### 7.1 역할 부여

```
POST /api/iam/users/{userContextId}/roles
{
  "roleCode": "org.manager",
  "tenantId": "tnt_abc",
  "organizationId": 123,
  "resourceFilter": {"brandIds": [11, 12]}
}
```

* 응답: 204 No Content

### 7.2 권한 평가(디버그)

```
POST /api/iam/evaluate
{
  "permission": "file.upload",
  "context": {"tenantId": "tnt_abc", "organizationId": 123, "userContextId": 9001},
  "resource": {"tenantId": "tnt_abc", "organizationId": 123, "mime": "image/jpeg", "size_mb": 7}
}
```

* 응답: `{ "allowed": true, "matchedRole": "org.uploader", "scope": "ORGANIZATION" }`

---

## 8) 테스트 가이드(요약)

* **스냅샷 테스트**: 역할/권한/조건 변경 시 기대권한 세트 회귀검증.
* **경계 테스트**: SELF 판정, org 경계, tenant 경계, GLOBAL 오염 여부.
* **조건 평가 벡터**: 허용/거부 케이스 표준 세트(JSON)로 CEL 평가기 단위 테스트.
* **부하 테스트**: 캐시 적중률, 캐시 무효화 빈도, P95 권한 평가 지연.

---

## 10) 체크리스트

* [ ] 조직 코드 유니크 범위: **(tenant_id, org_code)**만 유지
* [ ] 모든 주요 엔티티 `deleted_at` 추가 및 기본 스코프 적용
* [ ] 멤버십/역할 스코프 확장 적용(DDL/백필)
* [ ] CEL 평가기/헬퍼 함수 구현 및 테스트 벡터 작성
* [ ] Effective Grants 캐시 적용 + 변경 이벤트 무효화
* [ ] 서명 헤더 전파 및 검증 로직 배치
* [ ] 설정 암호화(KMS Envelope) 및 캐시 정책 분리
* [ ] 부하/회귀 테스트 통과

---

### 부록 A) CEL 스니펫 모음

* 이미지/PDF만 업로드: `in(res.mime, ["image/jpeg","image/png","application/pdf"])`
* 09~20시만 허용: `9 <= getHour(ctx.now_epoch_sec, "Asia/Seoul") && getHour(ctx.now_epoch_sec, "Asia/Seoul") < 20`
* 파일 크기 제한: `res.size_mb <= 20`
* 셀러 멤버만 허용: `ctx.membership_type == "SELLER_MEMBER"`
