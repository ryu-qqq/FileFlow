# SellerAdmin E2E 테스트 시나리오 설계 (인증/인가 포함)

## 개요

셀러 관리자(SellerAdmin) 도메인의 E2E 통합 테스트 시나리오입니다. **인증/인가 시나리오 포함**.

**도메인**: 셀러 관리자 가입 신청 관리
**Base Path**: `/api/v1/market/admin/seller-admin-applications`
**엔드포인트 개수**: 9개 (Query 2개, Command 7개)

**분석 기준 문서**:
- `api-endpoints/selleradmin.md`
- `api-flow/selleradmin.md`
- **인증/인가**: `@PreAuthorize` + `MarketAccessChecker`

---

## 인증/인가 매핑

| 엔드포인트 | @PreAuthorize | 설명 |
|-----------|---------------|------|
| GET /seller-admins | `@access.superAdmin()` | 관리자 목록 검색 (슈퍼어드민) |
| GET /seller-admins/{id} | `@access.myselfOr(#sellerAdminId, 'seller-admin:read')` | 상세 조회 (본인 or 권한) |
| POST /seller-admins/apply | `@access.authenticated()` | 가입 신청 (인증만) |
| POST /seller-admins/{id}/approve | `@access.superAdmin()` | 승인 (슈퍼어드민) |
| POST /seller-admins/{id}/reject | `@access.superAdmin()` | 거절 (슈퍼어드민) |
| POST /seller-admins/bulk-approve | `@access.superAdmin()` | 일괄 승인 (슈퍼어드민) |
| POST /seller-admins/bulk-reject | `@access.superAdmin()` | 일괄 거절 (슈퍼어드민) |
| POST /seller-admins/{id}/reset-password | `@access.superAdmin()` | 비밀번호 초기화 (슈퍼어드민) |
| PUT /seller-admins/{id}/password | `@access.myselfOr(#sellerAdminId, 'seller-admin:manage')` | 비밀번호 변경 (본인 or 권한) |

---

## 목차

1. [Query 시나리오](#1-query-시나리오)
   - [목록 조회](#11-get-seller-admins---목록-조회)
   - [상세 조회](#12-get-seller-adminsid---상세-조회)

2. [Command 시나리오](#2-command-시나리오)
   - [가입 신청](#21-post-seller-admins---가입-신청)
   - [승인](#22-post-seller-adminsidapprove---승인)
   - [거절](#23-post-seller-adminsidreject---거절)
   - [일괄 승인](#24-post-seller-adminsbatch-approve---일괄-승인)
   - [일괄 거절](#25-post-seller-adminsbatch-reject---일괄-거절)
   - [비밀번호 초기화](#26-post-seller-adminsidresetpassword---비밀번호-초기화)
   - [비밀번호 변경](#27-patch-seller-adminsidpassword---비밀번호-변경)

3. [통합 플로우 시나리오](#3-통합-플로우-시나리오)
   - [전체 CRUD 플로우](#31-전체-crud-플로우)
   - [상태 전이 플로우](#32-상태-전이-플로우)
   - [비밀번호 관리 플로우](#33-비밀번호-관리-플로우)

4. [Fixture 설계](#4-fixture-설계)

5. [테스트 체크리스트](#5-테스트-체크리스트)

---

## 1. Query 시나리오

### 1.1 GET /seller-admins - 목록 조회

#### P0: 필수 시나리오

##### SC-Q1-01: 데이터 존재 시 정상 조회 (슈퍼어드민)
**우선순위**: P0
**카테고리**: 기본 조회

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**:
- PENDING_APPROVAL 상태 2건
- ACTIVE 상태 2건
- REJECTED 상태 1건

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications?page=0&size=10
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 200
- `content.size = 5`
- `totalElements = 5`
- `totalPages = 1`
- 각 항목에 필수 필드 존재 (sellerAdminId, sellerId, loginId, name, status)

---

##### SC-Q1-02: 토큰 없이 요청 시 401
**우선순위**: P0
**카테고리**: 인증 실패

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications?page=0&size=10
(Authorization 헤더 없음)
```

**검증**:
- 응답 코드: 401
- 에러 메시지: "인증이 필요합니다."

---

##### SC-Q1-03: 슈퍼어드민 아닌 사용자 접근 시 403
**우선순위**: P0
**카테고리**: 인가 실패

**인증 컨텍스트**: `SELLER_ADMIN` 역할 (일반 사용자)

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications?page=0&size=10
Authorization: Bearer {seller_admin_token}
```

**검증**:
- 응답 코드: 403
- 에러 메시지: "접근 권한이 없습니다."

---

##### SC-Q1-04: 데이터 없을 때 빈 목록 반환
**우선순위**: P0
**카테고리**: 빈 결과

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: 없음

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications?page=0&size=10
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 200
- `content.size = 0`
- `totalElements = 0`
- `totalPages = 0`

---

#### P1: 중요 시나리오

##### SC-Q1-05: sellerIds 필터 조회
**우선순위**: P1
**카테고리**: 검색 필터

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**:
- Seller 1번의 관리자 2건
- Seller 2번의 관리자 3건

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications?sellerIds=1&page=0&size=10
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 200
- `content.size = 2`
- 모든 항목의 `sellerId = 1`

---

##### SC-Q1-06: status 필터 조회 (PENDING_APPROVAL)
**우선순위**: P1
**카테고리**: 검색 필터

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**:
- PENDING_APPROVAL 상태 3건
- ACTIVE 상태 2건

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications?status=PENDING_APPROVAL&page=0&size=10
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 200
- `content.size = 3`
- 모든 항목의 `status = PENDING_APPROVAL`

---

##### SC-Q1-07: 페이징 동작 확인
**우선순위**: P1
**카테고리**: 페이징

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: 관리자 5건

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications?page=0&size=2
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 200
- `content.size = 2`
- `totalElements = 5`
- `totalPages = 3`
- `currentPage = 0`

---

##### SC-Q1-08: searchField 조합 검색 (NAME)
**우선순위**: P1
**카테고리**: 검색 필터

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**:
- 이름 "홍길동" 1건
- 이름 "김철수" 1건

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications?searchField=NAME&searchWord=홍길동
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 200
- `content.size = 1`
- `content[0].name = "홍길동"`

---

##### SC-Q1-09: 날짜 범위 필터 (startDate, endDate)
**우선순위**: P1
**카테고리**: 검색 필터

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**:
- 2026-02-01 생성 2건
- 2026-02-05 생성 1건
- 2026-02-10 생성 2건

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications?startDate=2026-02-01&endDate=2026-02-05
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 200
- `content.size = 3`
- 모든 항목의 `createdAt`이 2026-02-01 ~ 2026-02-05 사이

---

##### SC-Q1-10: 정렬 동작 확인 (sortKey, sortDirection)
**우선순위**: P1
**카테고리**: 정렬

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: 관리자 3건 (생성일시 다름)

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications?sortKey=CREATED_AT&sortDirection=ASC
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 200
- `content[0].createdAt < content[1].createdAt < content[2].createdAt`

---

#### P2: 선택 시나리오

##### SC-Q1-11: 복합 필터 조합 (sellerIds + status + searchField)
**우선순위**: P2
**카테고리**: 복합 필터

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**:
- Seller 1번, ACTIVE 상태, "홍길동" 1건
- Seller 1번, PENDING_APPROVAL 상태, "김철수" 1건
- Seller 2번, ACTIVE 상태, "홍길동" 1건

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications?sellerIds=1&status=ACTIVE&searchField=NAME&searchWord=홍길동
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 200
- `content.size = 1`
- `content[0].sellerId = 1`
- `content[0].status = ACTIVE`
- `content[0].name = "홍길동"`

---

### 1.2 GET /seller-admins/{id} - 상세 조회

#### P0: 필수 시나리오

##### SC-Q2-01: 본인 정보 조회 성공
**우선순위**: P0
**카테고리**: 상세 조회 (본인)

**인증 컨텍스트**: `SELLER_ADMIN` 역할, `sellerAdminId = id1`

**사전 데이터**: ACTIVE 상태 관리자 1건 (id1)

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications/id1
Authorization: Bearer {seller_admin_token_id1}
```

**검증**:
- 응답 코드: 200
- `data.sellerAdminId = id1`
- 필수 필드 존재 (sellerId, loginId, name, phoneNumber, status, createdAt, updatedAt)

---

##### SC-Q2-02: 슈퍼어드민은 모든 정보 조회 가능
**우선순위**: P0
**카테고리**: 상세 조회 (슈퍼어드민 바이패스)

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: PENDING_APPROVAL 상태 관리자 1건 (id2)

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications/id2
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 200
- `data.sellerAdminId = id2`
- 필수 필드 존재

---

##### SC-Q2-03: 다른 관리자의 정보 조회 시 403 (권한 없음)
**우선순위**: P0
**카테고리**: 인가 실패

**인증 컨텍스트**: `SELLER_ADMIN` 역할, `sellerAdminId = id1` (권한 없음)

**사전 데이터**: ACTIVE 상태 관리자 1건 (id2)

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications/id2
Authorization: Bearer {seller_admin_token_id1}
```

**검증**:
- 응답 코드: 403
- 에러 메시지: "접근 권한이 없습니다."

---

##### SC-Q2-04: 토큰 없이 요청 시 401
**우선순위**: P0
**카테고리**: 인증 실패

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications/id1
(Authorization 헤더 없음)
```

**검증**:
- 응답 코드: 401
- 에러 메시지: "인증이 필요합니다."

---

##### SC-Q2-05: 없는 ID로 조회 시 404
**우선순위**: P0
**카테고리**: 존재하지 않는 리소스

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: 없음

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications/01999999-9999-9999-9999-999999999999
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 404
- 에러 코드: `SELLER_ADMIN_NOT_FOUND`

---

#### P1: 중요 시나리오

##### SC-Q2-06: 'seller-admin:read' 권한으로 타인 조회 가능
**우선순위**: P1
**카테고리**: 권한 기반 접근

**인증 컨텍스트**: `SELLER_ADMIN` 역할, `sellerAdminId = id1`, `seller-admin:read` 권한 보유

**사전 데이터**: ACTIVE 상태 관리자 1건 (id2)

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications/id2
Authorization: Bearer {seller_admin_token_id1_with_permission}
```

**검증**:
- 응답 코드: 200
- `data.sellerAdminId = id2`

---

##### SC-Q2-07: ACTIVE 상태 조회 가능 확인
**우선순위**: P1
**카테고리**: 상태별 조회

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: ACTIVE 상태 관리자 1건

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications/{sellerAdminId}
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 200
- `data.status = ACTIVE`
- `data.authUserId` 존재

---

##### SC-Q2-08: REJECTED 상태 조회 가능 확인
**우선순위**: P1
**카테고리**: 상태별 조회

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: REJECTED 상태 관리자 1건

**요청**:
```http
GET /api/v1/market/admin/seller-admin-applications/{sellerAdminId}
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 200
- `data.status = REJECTED`
- `data.authUserId` null

---

---

## 2. Command 시나리오

### 2.1 POST /seller-admins - 가입 신청

#### P0: 필수 시나리오

##### SC-C1-01: 인증된 사용자의 유효한 가입 신청 성공
**우선순위**: P0
**카테고리**: 생성 성공

**인증 컨텍스트**: `AUTHENTICATED` (인증된 모든 사용자)

**사전 데이터**: Seller 1번 존재

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "sellerId": 1,
  "loginId": "newadmin@test.com",
  "name": "신규관리자",
  "phoneNumber": "010-1111-2222",
  "password": "Password123!"
}
```

**검증**:
- 응답 코드: 201
- `data.sellerAdminId` 존재 (UUIDv7 형식)
- DB 조회: `status = PENDING_APPROVAL`
- DB 조회: `authUserId = null` (승인 전)

---

##### SC-C1-02: 토큰 없이 가입 신청 시 401
**우선순위**: P0
**카테고리**: 인증 실패

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications
(Authorization 헤더 없음)
{
  "sellerId": 1,
  "loginId": "newadmin@test.com",
  "name": "신규관리자",
  "phoneNumber": "010-1111-2222",
  "password": "Password123!"
}
```

**검증**:
- 응답 코드: 401
- 에러 메시지: "인증이 필요합니다."

---

##### SC-C1-03: sellerId 누락 시 400
**우선순위**: P0
**카테고리**: 필수 필드 누락

**인증 컨텍스트**: `AUTHENTICATED`

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "loginId": "newadmin@test.com",
  "name": "신규관리자",
  "phoneNumber": "010-1111-2222",
  "password": "Password123!"
}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "셀러 ID는 필수입니다."

---

##### SC-C1-04: loginId 누락 시 400
**우선순위**: P0
**카테고리**: 필수 필드 누락

**인증 컨텍스트**: `AUTHENTICATED`

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "sellerId": 1,
  "name": "신규관리자",
  "phoneNumber": "010-1111-2222",
  "password": "Password123!"
}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "로그인 ID는 필수입니다."

---

##### SC-C1-05: name 누락 시 400
**우선순위**: P0
**카테고리**: 필수 필드 누락

**인증 컨텍스트**: `AUTHENTICATED`

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "sellerId": 1,
  "loginId": "newadmin@test.com",
  "phoneNumber": "010-1111-2222",
  "password": "Password123!"
}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "이름은 필수입니다."

---

##### SC-C1-06: phoneNumber 누락 시 400
**우선순위**: P0
**카테고리**: 필수 필드 누락

**인증 컨텍스트**: `AUTHENTICATED`

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "sellerId": 1,
  "loginId": "newadmin@test.com",
  "name": "신규관리자",
  "password": "Password123!"
}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "휴대폰 번호는 필수입니다."

---

##### SC-C1-07: password 누락 시 400
**우선순위**: P0
**카테고리**: 필수 필드 누락

**인증 컨텍스트**: `AUTHENTICATED`

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "sellerId": 1,
  "loginId": "newadmin@test.com",
  "name": "신규관리자",
  "phoneNumber": "010-1111-2222"
}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "비밀번호는 필수입니다."

---

#### P1: 중요 시나리오

##### SC-C1-08: loginId 이메일 형식 아닐 때 400
**우선순위**: P1
**카테고리**: Validation 실패

**인증 컨텍스트**: `AUTHENTICATED`

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "sellerId": 1,
  "loginId": "invalid-email",
  "name": "신규관리자",
  "phoneNumber": "010-1111-2222",
  "password": "Password123!"
}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "올바른 이메일 형식이 아닙니다."

---

##### SC-C1-09: loginId 100자 초과 시 400
**우선순위**: P1
**카테고리**: Validation 실패

**인증 컨텍스트**: `AUTHENTICATED`

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "sellerId": 1,
  "loginId": "a".repeat(90) + "@test.com",
  "name": "신규관리자",
  "phoneNumber": "010-1111-2222",
  "password": "Password123!"
}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "로그인 ID는 100자 이하여야 합니다."

---

##### SC-C1-10: name 50자 초과 시 400
**우선순위**: P1
**카테고리**: Validation 실패

**인증 컨텍스트**: `AUTHENTICATED`

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "sellerId": 1,
  "loginId": "admin@test.com",
  "name": "가".repeat(51),
  "phoneNumber": "010-1111-2222",
  "password": "Password123!"
}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "이름은 50자 이하여야 합니다."

---

##### SC-C1-11: phoneNumber 잘못된 형식 시 400
**우선순위**: P1
**카테고리**: Validation 실패

**인증 컨텍스트**: `AUTHENTICATED`

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "sellerId": 1,
  "loginId": "admin@test.com",
  "name": "신규관리자",
  "phoneNumber": "010-abcd-1234",
  "password": "Password123!"
}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "휴대폰 번호는 숫자와 하이픈만 입력 가능합니다."

---

##### SC-C1-12: password 8자 미만 시 400
**우선순위**: P1
**카테고리**: Validation 실패

**인증 컨텍스트**: `AUTHENTICATED`

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "sellerId": 1,
  "loginId": "admin@test.com",
  "name": "신규관리자",
  "phoneNumber": "010-1111-2222",
  "password": "Pass1!"
}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "비밀번호는 8자 이상 100자 이하여야 합니다."

---

##### SC-C1-13: 존재하지 않는 sellerId로 신청 시 404
**우선순위**: P1
**카테고리**: 외래키 제약

**인증 컨텍스트**: `AUTHENTICATED`

**사전 데이터**: Seller 9999번 존재하지 않음

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "sellerId": 9999,
  "loginId": "admin@test.com",
  "name": "신규관리자",
  "phoneNumber": "010-1111-2222",
  "password": "Password123!"
}
```

**검증**:
- 응답 코드: 404
- 에러 코드: `SELLER_NOT_FOUND`

---

##### SC-C1-14: 중복된 loginId로 신청 시 409
**우선순위**: P1
**카테고리**: 중복

**인증 컨텍스트**: `AUTHENTICATED`

**사전 데이터**: loginId "admin@test.com" 이미 존재

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "sellerId": 1,
  "loginId": "admin@test.com",
  "name": "신규관리자",
  "phoneNumber": "010-1111-2222",
  "password": "Password123!"
}
```

**검증**:
- 응답 코드: 409
- 에러 코드: `LOGIN_ID_DUPLICATE`

---

### 2.2 POST /seller-admins/{id}/approve - 승인

#### P0: 필수 시나리오

##### SC-C2-01: 슈퍼어드민이 PENDING_APPROVAL 상태 승인 성공
**우선순위**: P0
**카테고리**: 승인 성공

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: PENDING_APPROVAL 상태 관리자 1건

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/approve
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 200
- `data.sellerAdminId` 존재
- DB 조회: `status = ACTIVE`
- DB 조회: `authUserId` 존재 (UUIDv7)
- DB 조회: `seller_admin_auth_outbox` 레코드 생성 (PENDING 상태)

---

##### SC-C2-02: 토큰 없이 승인 시 401
**우선순위**: P0
**카테고리**: 인증 실패

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/approve
(Authorization 헤더 없음)
```

**검증**:
- 응답 코드: 401
- 에러 메시지: "인증이 필요합니다."

---

##### SC-C2-03: 일반 사용자가 승인 시 403
**우선순위**: P0
**카테고리**: 인가 실패

**인증 컨텍스트**: `SELLER_ADMIN` 역할 (일반 사용자)

**사전 데이터**: PENDING_APPROVAL 상태 관리자 1건

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/approve
Authorization: Bearer {seller_admin_token}
```

**검증**:
- 응답 코드: 403
- 에러 메시지: "접근 권한이 없습니다."

---

##### SC-C2-04: 존재하지 않는 ID 승인 시 404
**우선순위**: P0
**카테고리**: 존재하지 않는 리소스

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/01999999-9999-9999-9999-999999999999/approve
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 404
- 에러 코드: `SELLER_ADMIN_NOT_FOUND`

---

##### SC-C2-05: ACTIVE 상태 재승인 시 409
**우선순위**: P0
**카테고리**: 중복 처리

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: ACTIVE 상태 관리자 1건

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/approve
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 409
- 에러 코드: `SELLER_ADMIN_INVALID_STATUS`
- 에러 메시지: "승인할 수 없는 상태입니다. 현재 상태: ACTIVE"

---

#### P1: 중요 시나리오

##### SC-C2-06: REJECTED 상태 재승인 가능 확인
**우선순위**: P1
**카테고리**: 상태 전이

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: REJECTED 상태 관리자 1건

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/approve
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 409 (현재 구현에서는 PENDING_APPROVAL만 승인 가능)
- 에러 코드: `SELLER_ADMIN_INVALID_STATUS`

> **참고**: 도메인 로직에서 `canApprove()`는 PENDING_APPROVAL만 허용. 재승인 시나리오가 필요하면 도메인 규칙 변경 필요.

---

##### SC-C2-07: 승인 후 이벤트 발행 확인
**우선순위**: P1
**카테고리**: 이벤트 처리

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: PENDING_APPROVAL 상태 관리자 1건

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/approve
Authorization: Bearer {super_admin_token}
```

**검증**:
- DB 조회: `SellerAdminApprovedEvent` 발행됨 (이벤트 리스너 확인)
- Outbox 레코드: `event_type = SELLER_ADMIN_APPROVED`

---

### 2.3 POST /seller-admins/{id}/reject - 거절

#### P0: 필수 시나리오

##### SC-C3-01: 슈퍼어드민이 PENDING_APPROVAL 상태 거절 성공
**우선순위**: P0
**카테고리**: 거절 성공

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: PENDING_APPROVAL 상태 관리자 1건

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reject
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 204
- DB 조회: `status = REJECTED`
- DB 조회: `authUserId = null`

---

##### SC-C3-02: 토큰 없이 거절 시 401
**우선순위**: P0
**카테고리**: 인증 실패

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reject
(Authorization 헤더 없음)
```

**검증**:
- 응답 코드: 401
- 에러 메시지: "인증이 필요합니다."

---

##### SC-C3-03: 일반 사용자가 거절 시 403
**우선순위**: P0
**카테고리**: 인가 실패

**인증 컨텍스트**: `SELLER_ADMIN` 역할 (일반 사용자)

**사전 데이터**: PENDING_APPROVAL 상태 관리자 1건

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reject
Authorization: Bearer {seller_admin_token}
```

**검증**:
- 응답 코드: 403
- 에러 메시지: "접근 권한이 없습니다."

---

##### SC-C3-04: 존재하지 않는 ID 거절 시 404
**우선순위**: P0
**카테고리**: 존재하지 않는 리소스

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/01999999-9999-9999-9999-999999999999/reject
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 404
- 에러 코드: `SELLER_ADMIN_NOT_FOUND`

---

##### SC-C3-05: ACTIVE 상태 거절 시 409
**우선순위**: P0
**카테고리**: 중복 처리

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: ACTIVE 상태 관리자 1건

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reject
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 409
- 에러 코드: `SELLER_ADMIN_INVALID_STATUS`
- 에러 메시지: "거절할 수 없는 상태입니다. 현재 상태: ACTIVE"

---

#### P1: 중요 시나리오

##### SC-C3-06: REJECTED 상태 재거절 시 409
**우선순위**: P1
**카테고리**: 중복 처리

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: REJECTED 상태 관리자 1건

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reject
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 409
- 에러 코드: `SELLER_ADMIN_INVALID_STATUS`

---

### 2.4 POST /seller-admins/batch-approve - 일괄 승인

#### P0: 필수 시나리오

##### SC-C4-01: 슈퍼어드민이 여러 건 일괄 승인 성공
**우선순위**: P0
**카테고리**: 일괄 승인 성공

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: PENDING_APPROVAL 상태 관리자 3건

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications/bulk-approve
Authorization: Bearer {super_admin_token}
{
  "sellerAdminIds": ["id1", "id2", "id3"]
}
```

**검증**:
- 응답 코드: 200
- `data.totalCount = 3`
- `data.successCount = 3`
- `data.failureCount = 0`
- `data.items.size = 3`
- 모든 `items[].success = true`
- DB 조회: 3건 모두 `status = ACTIVE`

---

##### SC-C4-02: 토큰 없이 일괄 승인 시 401
**우선순위**: P0
**카테고리**: 인증 실패

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications/bulk-approve
(Authorization 헤더 없음)
{
  "sellerAdminIds": ["id1", "id2", "id3"]
}
```

**검증**:
- 응답 코드: 401
- 에러 메시지: "인증이 필요합니다."

---

##### SC-C4-03: 일반 사용자가 일괄 승인 시 403
**우선순위**: P0
**카테고리**: 인가 실패

**인증 컨텍스트**: `SELLER_ADMIN` 역할 (일반 사용자)

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications/bulk-approve
Authorization: Bearer {seller_admin_token}
{
  "sellerAdminIds": ["id1", "id2", "id3"]
}
```

**검증**:
- 응답 코드: 403
- 에러 메시지: "접근 권한이 없습니다."

---

##### SC-C4-04: sellerAdminIds 누락 시 400
**우선순위**: P0
**카테고리**: 필수 필드 누락

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications/bulk-approve
Authorization: Bearer {super_admin_token}
{}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "sellerAdminIds는 필수입니다."

---

#### P1: 중요 시나리오

##### SC-C4-05: 일부 성공, 일부 실패 (혼합)
**우선순위**: P1
**카테고리**: 부분 성공

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**:
- id1: PENDING_APPROVAL
- id2: ACTIVE (이미 승인됨)
- id3: PENDING_APPROVAL

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications/bulk-approve
Authorization: Bearer {super_admin_token}
{
  "sellerAdminIds": ["id1", "id2", "id3"]
}
```

**검증**:
- 응답 코드: 200
- `data.totalCount = 3`
- `data.successCount = 2`
- `data.failureCount = 1`
- `data.items[0].success = true` (id1)
- `data.items[1].success = false` (id2)
- `data.items[1].errorCode = "SELLER_ADMIN_INVALID_STATUS"`
- `data.items[2].success = true` (id3)
- DB 조회: id1, id3는 ACTIVE, id2는 원래 상태 유지

---

##### SC-C4-06: 존재하지 않는 ID 포함 시 일부 실패
**우선순위**: P1
**카테고리**: 부분 성공

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**:
- id1: PENDING_APPROVAL
- id2: 존재하지 않음

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications/bulk-approve
Authorization: Bearer {super_admin_token}
{
  "sellerAdminIds": ["id1", "id2"]
}
```

**검증**:
- 응답 코드: 200
- `data.totalCount = 2`
- `data.successCount = 1`
- `data.failureCount = 1`
- `data.items[0].success = true` (id1)
- `data.items[1].success = false` (id2)
- `data.items[1].errorCode = "SELLER_ADMIN_NOT_FOUND"`

---

##### SC-C4-07: 빈 배열로 요청 시 400
**우선순위**: P1
**카테고리**: Validation 실패

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications/bulk-approve
Authorization: Bearer {super_admin_token}
{
  "sellerAdminIds": []
}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "sellerAdminIds는 비어 있을 수 없습니다."

---

### 2.5 POST /seller-admins/batch-reject - 일괄 거절

#### P0: 필수 시나리오

##### SC-C5-01: 슈퍼어드민이 여러 건 일괄 거절 성공
**우선순위**: P0
**카테고리**: 일괄 거절 성공

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: PENDING_APPROVAL 상태 관리자 3건

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications/bulk-reject
Authorization: Bearer {super_admin_token}
{
  "sellerAdminIds": ["id1", "id2", "id3"]
}
```

**검증**:
- 응답 코드: 204
- DB 조회: 3건 모두 `status = REJECTED`

---

##### SC-C5-02: 토큰 없이 일괄 거절 시 401
**우선순위**: P0
**카테고리**: 인증 실패

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications/bulk-reject
(Authorization 헤더 없음)
{
  "sellerAdminIds": ["id1", "id2", "id3"]
}
```

**검증**:
- 응답 코드: 401
- 에러 메시지: "인증이 필요합니다."

---

##### SC-C5-03: 일반 사용자가 일괄 거절 시 403
**우선순위**: P0
**카테고리**: 인가 실패

**인증 컨텍스트**: `SELLER_ADMIN` 역할 (일반 사용자)

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications/bulk-reject
Authorization: Bearer {seller_admin_token}
{
  "sellerAdminIds": ["id1", "id2", "id3"]
}
```

**검증**:
- 응답 코드: 403
- 에러 메시지: "접근 권한이 없습니다."

---

##### SC-C5-04: sellerAdminIds 누락 시 400
**우선순위**: P0
**카테고리**: 필수 필드 누락

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications/bulk-reject
Authorization: Bearer {super_admin_token}
{}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "sellerAdminIds는 필수입니다."

---

#### P1: 중요 시나리오

##### SC-C5-05: ACTIVE 상태 포함 시 일괄 거절 실패 (전체 롤백)
**우선순위**: P1
**카테고리**: 트랜잭션 롤백

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**:
- id1: PENDING_APPROVAL
- id2: ACTIVE (거절 불가)
- id3: PENDING_APPROVAL

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications/bulk-reject
Authorization: Bearer {super_admin_token}
{
  "sellerAdminIds": ["id1", "id2", "id3"]
}
```

**검증**:
- 응답 코드: 409
- 에러 코드: `SELLER_ADMIN_INVALID_STATUS`
- DB 조회: 3건 모두 원래 상태 유지 (트랜잭션 롤백)

> **참고**: 일괄 거절은 하나의 트랜잭션에서 처리되므로, 실패 시 전체 롤백됨.

---

##### SC-C5-06: 존재하지 않는 ID 포함 시 전체 실패
**우선순위**: P1
**카테고리**: 트랜잭션 롤백

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**:
- id1: PENDING_APPROVAL
- id2: 존재하지 않음

**요청**:
```json
POST /api/v1/market/admin/seller-admin-applications/bulk-reject
Authorization: Bearer {super_admin_token}
{
  "sellerAdminIds": ["id1", "id2"]
}
```

**검증**:
- 응답 코드: 404
- 에러 코드: `SELLER_ADMIN_NOT_FOUND`
- DB 조회: id1은 원래 상태 유지 (롤백)

---

### 2.6 POST /seller-admins/{id}/resetpassword - 비밀번호 초기화

#### P0: 필수 시나리오

##### SC-C6-01: 슈퍼어드민이 ACTIVE 상태 비밀번호 초기화 성공
**우선순위**: P0
**카테고리**: 비밀번호 초기화 성공

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: ACTIVE 상태, authUserId 존재 관리자 1건

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reset-password
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 204
- 외부 인증 서버 호출 확인: `POST /auth/seller-admins/{authUserId}/reset-password`

> **참고**: E2E 테스트에서는 인증 서버를 Mock으로 대체.

---

##### SC-C6-02: 토큰 없이 비밀번호 초기화 시 401
**우선순위**: P0
**카테고리**: 인증 실패

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reset-password
(Authorization 헤더 없음)
```

**검증**:
- 응답 코드: 401
- 에러 메시지: "인증이 필요합니다."

---

##### SC-C6-03: 일반 사용자가 비밀번호 초기화 시 403
**우선순위**: P0
**카테고리**: 인가 실패

**인증 컨텍스트**: `SELLER_ADMIN` 역할 (일반 사용자)

**사전 데이터**: ACTIVE 상태 관리자 1건

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reset-password
Authorization: Bearer {seller_admin_token}
```

**검증**:
- 응답 코드: 403
- 에러 메시지: "접근 권한이 없습니다."

---

##### SC-C6-04: 존재하지 않는 ID 초기화 시 404
**우선순위**: P0
**카테고리**: 존재하지 않는 리소스

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/01999999-9999-9999-9999-999999999999/reset-password
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 404
- 에러 코드: `SELLER_ADMIN_NOT_FOUND`

---

##### SC-C6-05: PENDING_APPROVAL 상태 초기화 시 400
**우선순위**: P0
**카테고리**: 상태 제약

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: PENDING_APPROVAL 상태 관리자 1건

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reset-password
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "비밀번호 초기화는 ACTIVE 상태에서만 가능합니다. 현재 상태: PENDING_APPROVAL"

---

#### P1: 중요 시나리오

##### SC-C6-06: authUserId 없는 관리자 초기화 시 400
**우선순위**: P1
**카테고리**: 상태 제약

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: ACTIVE 상태, authUserId null 관리자 1건

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reset-password
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "인증 서버에 등록되지 않은 관리자입니다."

---

##### SC-C6-07: INACTIVE 상태 초기화 시 400
**우선순위**: P1
**카테고리**: 상태 제약

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: INACTIVE 상태 관리자 1건

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reset-password
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "비밀번호 초기화는 ACTIVE 상태에서만 가능합니다. 현재 상태: INACTIVE"

---

##### SC-C6-08: 인증 서버 연동 실패 시 500
**우선순위**: P1
**카테고리**: 외부 연동 실패

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: ACTIVE 상태 관리자 1건

**Mock 설정**: 인증 서버 API 호출 시 500 에러 반환

**요청**:
```http
POST /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/reset-password
Authorization: Bearer {super_admin_token}
```

**검증**:
- 응답 코드: 500
- 에러 메시지: "인증 서버 연동 실패"

---

### 2.7 PATCH /seller-admins/{id}/password - 비밀번호 변경

#### P0: 필수 시나리오

##### SC-C7-01: 본인이 ACTIVE 상태 비밀번호 변경 성공
**우선순위**: P0
**카테고리**: 비밀번호 변경 성공 (본인)

**인증 컨텍스트**: `SELLER_ADMIN` 역할, `sellerAdminId = id1`

**사전 데이터**: ACTIVE 상태, authUserId 존재 관리자 1건 (id1)

**요청**:
```json
PATCH /api/v1/market/admin/seller-admin-applications/id1/change-password
Authorization: Bearer {seller_admin_token_id1}
{
  "newPassword": "NewPassword123!"
}
```

**검증**:
- 응답 코드: 204
- 외부 인증 서버 호출 확인: `PATCH /auth/seller-admins/{authUserId}/password`

---

##### SC-C7-02: 슈퍼어드민은 모든 관리자 비밀번호 변경 가능
**우선순위**: P0
**카테고리**: 비밀번호 변경 성공 (슈퍼어드민 바이패스)

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: ACTIVE 상태, authUserId 존재 관리자 1건 (id2)

**요청**:
```json
PATCH /api/v1/market/admin/seller-admin-applications/id2/change-password
Authorization: Bearer {super_admin_token}
{
  "newPassword": "NewPassword123!"
}
```

**검증**:
- 응답 코드: 204
- 외부 인증 서버 호출 확인

---

##### SC-C7-03: 다른 관리자의 비밀번호 변경 시 403 (권한 없음)
**우선순위**: P0
**카테고리**: 인가 실패

**인증 컨텍스트**: `SELLER_ADMIN` 역할, `sellerAdminId = id1` (권한 없음)

**사전 데이터**: ACTIVE 상태 관리자 1건 (id2)

**요청**:
```json
PATCH /api/v1/market/admin/seller-admin-applications/id2/change-password
Authorization: Bearer {seller_admin_token_id1}
{
  "newPassword": "NewPassword123!"
}
```

**검증**:
- 응답 코드: 403
- 에러 메시지: "접근 권한이 없습니다."

---

##### SC-C7-04: 토큰 없이 비밀번호 변경 시 401
**우선순위**: P0
**카테고리**: 인증 실패

**요청**:
```json
PATCH /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/change-password
(Authorization 헤더 없음)
{
  "newPassword": "NewPassword123!"
}
```

**검증**:
- 응답 코드: 401
- 에러 메시지: "인증이 필요합니다."

---

##### SC-C7-05: newPassword 누락 시 400
**우선순위**: P0
**카테고리**: 필수 필드 누락

**인증 컨텍스트**: `SELLER_ADMIN` 역할, `sellerAdminId = id1`

**요청**:
```json
PATCH /api/v1/market/admin/seller-admin-applications/id1/change-password
Authorization: Bearer {seller_admin_token_id1}
{}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "새 비밀번호는 필수입니다."

---

##### SC-C7-06: 존재하지 않는 ID 변경 시 404
**우선순위**: P0
**카테고리**: 존재하지 않는 리소스

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**요청**:
```json
PATCH /api/v1/market/admin/seller-admin-applications/01999999-9999-9999-9999-999999999999/change-password
Authorization: Bearer {super_admin_token}
{
  "newPassword": "NewPassword123!"
}
```

**검증**:
- 응답 코드: 404
- 에러 코드: `SELLER_ADMIN_NOT_FOUND`

---

#### P1: 중요 시나리오

##### SC-C7-07: 'seller-admin:manage' 권한으로 타인 비밀번호 변경 가능
**우선순위**: P1
**카테고리**: 권한 기반 접근

**인증 컨텍스트**: `SELLER_ADMIN` 역할, `sellerAdminId = id1`, `seller-admin:manage` 권한 보유

**사전 데이터**: ACTIVE 상태 관리자 1건 (id2)

**요청**:
```json
PATCH /api/v1/market/admin/seller-admin-applications/id2/change-password
Authorization: Bearer {seller_admin_token_id1_with_permission}
{
  "newPassword": "NewPassword123!"
}
```

**검증**:
- 응답 코드: 204

---

##### SC-C7-08: newPassword 8자 미만 시 400
**우선순위**: P1
**카테고리**: Validation 실패

**인증 컨텍스트**: `SELLER_ADMIN` 역할, `sellerAdminId = id1`

**요청**:
```json
PATCH /api/v1/market/admin/seller-admin-applications/id1/change-password
Authorization: Bearer {seller_admin_token_id1}
{
  "newPassword": "Pass1!"
}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "비밀번호는 8자 이상 100자 이하여야 합니다."

---

##### SC-C7-09: PENDING_APPROVAL 상태 변경 시 400
**우선순위**: P1
**카테고리**: 상태 제약

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: PENDING_APPROVAL 상태 관리자 1건

**요청**:
```json
PATCH /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/change-password
Authorization: Bearer {super_admin_token}
{
  "newPassword": "NewPassword123!"
}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "비밀번호 변경은 ACTIVE 상태에서만 가능합니다. 현재 상태: PENDING_APPROVAL"

---

##### SC-C7-10: authUserId 없는 관리자 변경 시 400
**우선순위**: P1
**카테고리**: 상태 제약

**인증 컨텍스트**: `SUPER_ADMIN` 역할

**사전 데이터**: ACTIVE 상태, authUserId null 관리자 1건

**요청**:
```json
PATCH /api/v1/market/admin/seller-admin-applications/{sellerAdminId}/change-password
Authorization: Bearer {super_admin_token}
{
  "newPassword": "NewPassword123!"
}
```

**검증**:
- 응답 코드: 400
- 에러 메시지: "인증 서버에 등록되지 않은 관리자입니다."

---

---

## 3. 통합 플로우 시나리오

### 3.1 전체 CRUD 플로우

##### SC-F1-01: 가입 신청 → 승인 → 조회 → 비밀번호 변경 (인증/인가 포함)
**우선순위**: P0
**카테고리**: 전체 플로우

**Step 1**: 가입 신청 (POST) - 인증된 사용자
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "sellerId": 1,
  "loginId": "flow-test@test.com",
  "name": "플로우테스트",
  "phoneNumber": "010-1234-5678",
  "password": "Password123!"
}
```

**검증 1**:
- 응답 코드: 201
- `data.sellerAdminId` 저장 (변수 `createdId`)

---

**Step 2**: 상세 조회 (GET) - 승인 전 (슈퍼어드민)
```http
GET /api/v1/market/admin/seller-admin-applications/{createdId}
Authorization: Bearer {super_admin_token}
```

**검증 2**:
- 응답 코드: 200
- `data.status = PENDING_APPROVAL`
- `data.authUserId = null`

---

**Step 3**: 승인 (POST) - 슈퍼어드민
```http
POST /api/v1/market/admin/seller-admin-applications/{createdId}/approve
Authorization: Bearer {super_admin_token}
```

**검증 3**:
- 응답 코드: 200
- `data.sellerAdminId = {createdId}`

---

**Step 4**: 상세 조회 (GET) - 승인 후 (본인)
```http
GET /api/v1/market/admin/seller-admin-applications/{createdId}
Authorization: Bearer {seller_admin_token_createdId}
```

**검증 4**:
- 응답 코드: 200
- `data.status = ACTIVE`
- `data.authUserId` 존재

---

**Step 5**: 비밀번호 변경 (PATCH) - 본인
```json
PATCH /api/v1/market/admin/seller-admin-applications/{createdId}/change-password
Authorization: Bearer {seller_admin_token_createdId}
{
  "newPassword": "ChangedPassword123!"
}
```

**검증 5**:
- 응답 코드: 204

---

### 3.2 상태 전이 플로우

##### SC-F2-01: 신청 → 승인 → 활성 → 비활성 (인증/인가 포함)
**우선순위**: P0
**카테고리**: 상태 전이

**Step 1**: 가입 신청 (PENDING_APPROVAL) - 인증된 사용자
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{...}
```

**검증 1**: `status = PENDING_APPROVAL`

---

**Step 2**: 승인 (ACTIVE) - 슈퍼어드민
```http
POST /api/v1/market/admin/seller-admin-applications/{id}/approve
Authorization: Bearer {super_admin_token}
```

**검증 2**: `status = ACTIVE`

---

**Step 3**: 비활성화 (수동 DB 변경 또는 별도 API)
```
DB UPDATE: status = INACTIVE
```

**검증 3**: 비밀번호 초기화 불가능 확인

---

##### SC-F2-02: 신청 → 거절 → 재신청 (인증/인가 포함)
**우선순위**: P1
**카테고리**: 상태 전이

**Step 1**: 가입 신청 (id1) - 인증된 사용자
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "loginId": "reject-test@test.com",
  ...
}
```

**검증 1**: `status = PENDING_APPROVAL`

---

**Step 2**: 거절 (REJECTED) - 슈퍼어드민
```http
POST /api/v1/market/admin/seller-admin-applications/{id1}/reject
Authorization: Bearer {super_admin_token}
```

**검증 2**: `status = REJECTED`

---

**Step 3**: 동일 loginId로 재신청 (id2) - 인증된 사용자
```json
POST /api/v1/market/admin/seller-admin-applications
Authorization: Bearer {authenticated_token}
{
  "loginId": "reject-test@test.com",
  ...
}
```

**검증 3**: 409 Conflict (loginId 중복)

> **참고**: 거절된 관리자의 loginId는 재사용 불가. 실제 시스템에서는 재신청 정책 필요.

---

### 3.3 비밀번호 관리 플로우

##### SC-F3-01: 비밀번호 초기화 → 변경 (인증/인가 포함)
**우선순위**: P0
**카테고리**: 비밀번호 관리

**사전 데이터**: ACTIVE 상태, authUserId 존재 관리자 1건

**Step 1**: 비밀번호 초기화 - 슈퍼어드민
```http
POST /api/v1/market/admin/seller-admin-applications/{id}/reset-password
Authorization: Bearer {super_admin_token}
```

**검증 1**:
- 응답 코드: 204
- 인증 서버: 임시 비밀번호 생성 + 이메일 발송

---

**Step 2**: 외부 본인인증 완료 (Mock)
```
(외부 시스템에서 이메일/문자 인증 완료)
```

---

**Step 3**: 새 비밀번호로 변경 - 본인
```json
PATCH /api/v1/market/admin/seller-admin-applications/{id}/change-password
Authorization: Bearer {seller_admin_token_id}
{
  "newPassword": "MyNewPassword123!"
}
```

**검증 3**:
- 응답 코드: 204

---

---

## 4. Fixture 설계

### 4.1 필요 Repository

| Repository | 용도 |
|-----------|------|
| `SellerAdminJpaRepository` | 셀러 관리자 저장/조회 |
| `SellerAdminAuthOutboxJpaRepository` | Outbox 레코드 조회 (승인 확인) |
| `SellerJpaRepository` | Seller 존재 여부 확인 (외래키 제약) |

---

### 4.2 testFixtures 활용

**위치**: `adapter-out/persistence-mysql/src/testFixtures/java/com/ryuqq/marketplace/adapter/out/persistence/selleradmin/SellerAdminJpaEntityFixtures.java`

**제공 Fixture**:
- `activeEntity()`: ACTIVE 상태, authUserId 존재
- `pendingApprovalEntity()`: PENDING_APPROVAL 상태, authUserId null
- `rejectedEntity()`: REJECTED 상태
- `suspendedEntity()`: SUSPENDED 상태
- `inactiveEntity()`: INACTIVE 상태
- `deletedEntity()`: 삭제된 상태 (deletedAt 존재)
- `customEntity()`: 전체 필드 커스텀

---

### 4.3 setUp 방법

```java
@BeforeEach
void setUp() {
    // 전체 테이블 초기화
    sellerAdminJpaRepository.deleteAll();
    sellerAdminAuthOutboxJpaRepository.deleteAll();
    sellerJpaRepository.deleteAll();

    // 필요한 Seller 사전 데이터
    SellerJpaEntity seller = SellerJpaEntityFixtures.activeEntity(1L);
    sellerJpaRepository.save(seller);
}
```

---

### 4.4 사전 데이터 예시

#### 시나리오: 목록 조회 (다양한 상태)
```java
@Test
void search_WithMultipleStatuses_ShouldReturnAll() {
    // Given: 다양한 상태의 관리자 5건
    sellerAdminJpaRepository.save(SellerAdminJpaEntityFixtures.pendingApprovalEntity());
    sellerAdminJpaRepository.save(SellerAdminJpaEntityFixtures.pendingApprovalEntity("id2", "pending2@test.com"));
    sellerAdminJpaRepository.save(SellerAdminJpaEntityFixtures.activeEntity());
    sellerAdminJpaRepository.save(SellerAdminJpaEntityFixtures.activeEntity("id4"));
    sellerAdminJpaRepository.save(SellerAdminJpaEntityFixtures.rejectedEntity());

    // When: 전체 조회 (슈퍼어드민 토큰)
    // Then: content.size = 5
}
```

#### 시나리오: 승인
```java
@Test
void approve_PendingApproval_ShouldActivate() {
    // Given: PENDING_APPROVAL 상태 관리자 1건
    SellerAdminJpaEntity pending = SellerAdminJpaEntityFixtures.pendingApprovalEntity();
    SellerAdminJpaEntity saved = sellerAdminJpaRepository.save(pending);
    String sellerAdminId = saved.id();

    // When: 승인 (슈퍼어드민 토큰)
    // Then: status = ACTIVE, authUserId 존재, Outbox 레코드 생성
}
```

---

### 4.5 인증 토큰 Mock 설정

```java
@MockBean
private JwtTokenProvider jwtTokenProvider;

@BeforeEach
void setUpAuth() {
    // 슈퍼어드민 토큰
    when(jwtTokenProvider.validateToken("super_admin_token")).thenReturn(true);
    when(jwtTokenProvider.getRoles("super_admin_token")).thenReturn(List.of("SUPER_ADMIN"));

    // 일반 사용자 토큰 (id1)
    when(jwtTokenProvider.validateToken("seller_admin_token_id1")).thenReturn(true);
    when(jwtTokenProvider.getRoles("seller_admin_token_id1")).thenReturn(List.of("SELLER_ADMIN"));
    when(jwtTokenProvider.getSellerAdminId("seller_admin_token_id1")).thenReturn("id1");

    // 권한 있는 사용자 토큰 (id1 + permission)
    when(jwtTokenProvider.validateToken("seller_admin_token_id1_with_permission")).thenReturn(true);
    when(jwtTokenProvider.getRoles("seller_admin_token_id1_with_permission")).thenReturn(List.of("SELLER_ADMIN"));
    when(jwtTokenProvider.getSellerAdminId("seller_admin_token_id1_with_permission")).thenReturn("id1");
    when(jwtTokenProvider.getPermissions("seller_admin_token_id1_with_permission"))
        .thenReturn(List.of("seller-admin:read", "seller-admin:manage"));
}
```

---

---

## 5. 테스트 체크리스트

### 5.1 Query 엔드포인트 (2개)

#### GET /seller-admins (목록 조회)
- [ ] 데이터 존재 시 정상 조회 (슈퍼어드민) (P0)
- [ ] 토큰 없이 요청 시 401 (P0) **[NEW]**
- [ ] 슈퍼어드민 아닌 사용자 접근 시 403 (P0) **[NEW]**
- [ ] 데이터 없을 때 빈 목록 반환 (P0)
- [ ] sellerIds 필터 조회 (P1)
- [ ] status 필터 조회 (P1)
- [ ] 페이징 동작 확인 (P1)
- [ ] searchField 조합 검색 (P1)
- [ ] 날짜 범위 필터 (P1)
- [ ] 정렬 동작 확인 (P1)
- [ ] 복합 필터 조합 (P2)

#### GET /seller-admins/{id} (상세 조회)
- [ ] 본인 정보 조회 성공 (P0) **[NEW]**
- [ ] 슈퍼어드민은 모든 정보 조회 가능 (P0) **[NEW]**
- [ ] 다른 관리자의 정보 조회 시 403 (권한 없음) (P0) **[NEW]**
- [ ] 토큰 없이 요청 시 401 (P0) **[NEW]**
- [ ] 없는 ID로 조회 시 404 (P0)
- [ ] 'seller-admin:read' 권한으로 타인 조회 가능 (P1) **[NEW]**
- [ ] ACTIVE 상태 조회 가능 확인 (P1)
- [ ] REJECTED 상태 조회 가능 확인 (P1)

---

### 5.2 Command 엔드포인트 (7개)

#### POST /seller-admins (가입 신청)
- [ ] 인증된 사용자의 유효한 가입 신청 성공 (P0)
- [ ] 토큰 없이 가입 신청 시 401 (P0) **[NEW]**
- [ ] sellerId 누락 시 400 (P0)
- [ ] loginId 누락 시 400 (P0)
- [ ] name 누락 시 400 (P0)
- [ ] phoneNumber 누락 시 400 (P0)
- [ ] password 누락 시 400 (P0)
- [ ] loginId 이메일 형식 아닐 때 400 (P1)
- [ ] loginId 100자 초과 시 400 (P1)
- [ ] name 50자 초과 시 400 (P1)
- [ ] phoneNumber 잘못된 형식 시 400 (P1)
- [ ] password 8자 미만 시 400 (P1)
- [ ] 존재하지 않는 sellerId로 신청 시 404 (P1)
- [ ] 중복된 loginId로 신청 시 409 (P1)

#### POST /seller-admins/{id}/approve (승인)
- [ ] 슈퍼어드민이 PENDING_APPROVAL 상태 승인 성공 (P0)
- [ ] 토큰 없이 승인 시 401 (P0) **[NEW]**
- [ ] 일반 사용자가 승인 시 403 (P0) **[NEW]**
- [ ] 존재하지 않는 ID 승인 시 404 (P0)
- [ ] ACTIVE 상태 재승인 시 409 (P0)
- [ ] REJECTED 상태 재승인 가능 확인 (P1)
- [ ] 승인 후 이벤트 발행 확인 (P1)

#### POST /seller-admins/{id}/reject (거절)
- [ ] 슈퍼어드민이 PENDING_APPROVAL 상태 거절 성공 (P0)
- [ ] 토큰 없이 거절 시 401 (P0) **[NEW]**
- [ ] 일반 사용자가 거절 시 403 (P0) **[NEW]**
- [ ] 존재하지 않는 ID 거절 시 404 (P0)
- [ ] ACTIVE 상태 거절 시 409 (P0)
- [ ] REJECTED 상태 재거절 시 409 (P1)

#### POST /seller-admins/batch-approve (일괄 승인)
- [ ] 슈퍼어드민이 여러 건 일괄 승인 성공 (P0)
- [ ] 토큰 없이 일괄 승인 시 401 (P0) **[NEW]**
- [ ] 일반 사용자가 일괄 승인 시 403 (P0) **[NEW]**
- [ ] sellerAdminIds 누락 시 400 (P0)
- [ ] 일부 성공, 일부 실패 (혼합) (P1)
- [ ] 존재하지 않는 ID 포함 시 일부 실패 (P1)
- [ ] 빈 배열로 요청 시 400 (P1)

#### POST /seller-admins/batch-reject (일괄 거절)
- [ ] 슈퍼어드민이 여러 건 일괄 거절 성공 (P0)
- [ ] 토큰 없이 일괄 거절 시 401 (P0) **[NEW]**
- [ ] 일반 사용자가 일괄 거절 시 403 (P0) **[NEW]**
- [ ] sellerAdminIds 누락 시 400 (P0)
- [ ] ACTIVE 상태 포함 시 일괄 거절 실패 (전체 롤백) (P1)
- [ ] 존재하지 않는 ID 포함 시 전체 실패 (P1)

#### POST /seller-admins/{id}/resetpassword (비밀번호 초기화)
- [ ] 슈퍼어드민이 ACTIVE 상태 비밀번호 초기화 성공 (P0)
- [ ] 토큰 없이 비밀번호 초기화 시 401 (P0) **[NEW]**
- [ ] 일반 사용자가 비밀번호 초기화 시 403 (P0) **[NEW]**
- [ ] 존재하지 않는 ID 초기화 시 404 (P0)
- [ ] PENDING_APPROVAL 상태 초기화 시 400 (P0)
- [ ] authUserId 없는 관리자 초기화 시 400 (P1)
- [ ] INACTIVE 상태 초기화 시 400 (P1)
- [ ] 인증 서버 연동 실패 시 500 (P1)

#### PATCH /seller-admins/{id}/password (비밀번호 변경)
- [ ] 본인이 ACTIVE 상태 비밀번호 변경 성공 (P0) **[NEW]**
- [ ] 슈퍼어드민은 모든 관리자 비밀번호 변경 가능 (P0) **[NEW]**
- [ ] 다른 관리자의 비밀번호 변경 시 403 (권한 없음) (P0) **[NEW]**
- [ ] 토큰 없이 비밀번호 변경 시 401 (P0) **[NEW]**
- [ ] newPassword 누락 시 400 (P0)
- [ ] 존재하지 않는 ID 변경 시 404 (P0)
- [ ] 'seller-admin:manage' 권한으로 타인 비밀번호 변경 가능 (P1) **[NEW]**
- [ ] newPassword 8자 미만 시 400 (P1)
- [ ] PENDING_APPROVAL 상태 변경 시 400 (P1)
- [ ] authUserId 없는 관리자 변경 시 400 (P1)

---

### 5.3 통합 플로우 시나리오 (3개)

#### 전체 CRUD 플로우
- [ ] 가입 신청 → 승인 → 조회 → 비밀번호 변경 (인증/인가 포함) (P0)

#### 상태 전이 플로우
- [ ] 신청 → 승인 → 활성 → 비활성 (인증/인가 포함) (P0)
- [ ] 신청 → 거절 → 재신청 (인증/인가 포함) (P1)

#### 비밀번호 관리 플로우
- [ ] 비밀번호 초기화 → 변경 (인증/인가 포함) (P0)

---

---

## 6. 시나리오 요약

### 6.1 전체 시나리오 개수

| 카테고리 | P0 | P1 | P2 | 합계 |
|---------|----|----|----|----|
| **Query** | 8 (+4) | 7 (+1) | 1 | **16** (+5) |
| **Command** | 36 (+12) | 21 (+2) | 0 | **57** (+14) |
| **통합 플로우** | 3 | 1 | 0 | **4** |
| **합계** | **47** (+16) | **29** (+3) | **1** | **77** (+19) |

> **[NEW]**: 인증/인가 시나리오 추가로 **19개 시나리오 증가**

---

### 6.2 우선순위별 권장 테스트 범위

- **P0 (47개)**: 필수 테스트, CI/CD에 포함
- **P1 (29개)**: 중요 테스트, 주간 빌드에 포함
- **P2 (1개)**: 선택 테스트, 릴리스 전 수동 실행

---

### 6.3 테스트 태그 권장

```java
@Tag("selleradmin")
@Tag("e2e")
@Tag("auth")  // [NEW]
public class SellerAdminE2ETest {

    @Test
    @Tag("p0")
    @DisplayName("SC-Q1-01: 데이터 존재 시 정상 조회 (슈퍼어드민)")
    void search_WithExistingData_ShouldReturnList() {
        // ...
    }

    @Test
    @Tag("p0")
    @Tag("auth")
    @DisplayName("SC-Q1-02: 토큰 없이 요청 시 401")
    void search_WithoutToken_ShouldReturn401() {
        // ...
    }

    @Test
    @Tag("p0")
    @Tag("auth")
    @DisplayName("SC-Q2-03: 다른 관리자의 정보 조회 시 403 (권한 없음)")
    void get_OtherAdminWithoutPermission_ShouldReturn403() {
        // ...
    }

    @Test
    @Tag("flow")
    @Tag("p0")
    @DisplayName("SC-F1-01: 가입 신청 → 승인 → 조회 → 비밀번호 변경")
    void fullCrudFlow_ShouldWorkCorrectly() {
        // ...
    }
}
```

---

### 6.4 외부 연동 Mock 설계

#### SellerAdminIdentityClient Mock

```java
@MockBean
private SellerAdminIdentityClient sellerAdminIdentityClient;

@BeforeEach
void setUp() {
    // 비밀번호 초기화 Mock
    doNothing().when(sellerAdminIdentityClient)
        .resetSellerAdminPassword(anyString());

    // 비밀번호 변경 Mock
    doNothing().when(sellerAdminIdentityClient)
        .changeSellerAdminPassword(anyString(), anyString());

    // 실패 시나리오 Mock (필요 시)
    doThrow(new ExternalApiException("인증 서버 연동 실패"))
        .when(sellerAdminIdentityClient)
        .resetSellerAdminPassword("fail-user-id");
}
```

---

---

## 7. 참고사항

### 7.1 Outbox 패턴 검증

승인 시나리오에서 Outbox 레코드 생성 확인:

```java
// 승인 후
List<SellerAdminAuthOutboxJpaEntity> outboxes =
    sellerAdminAuthOutboxJpaRepository.findBySellerAdminId(sellerAdminId);

assertThat(outboxes).hasSize(1);
assertThat(outboxes.get(0).status()).isEqualTo(SellerAdminAuthOutboxStatus.PENDING);
assertThat(outboxes.get(0).eventType()).isEqualTo("SELLER_ADMIN_APPROVED");
```

---

### 7.2 트랜잭션 격리

- **일괄 승인**: 각 항목별 개별 트랜잭션 (부분 성공 가능)
- **일괄 거절**: 하나의 트랜잭션 (실패 시 전체 롤백)

---

### 7.3 날짜/시간 처리

- `createdAt`, `updatedAt`은 `Instant` 타입
- 테스트에서는 `Instant.now()` 사용
- 날짜 범위 필터 테스트 시 `LocalDate.parse("yyyy-MM-dd")`로 변환

---

### 7.4 UUIDv7 ID 생성

- ID는 `SellerAdminId.generate()` 또는 외부 Factory에서 생성
- 테스트에서는 Fixtures의 기본 ID 사용

---

### 7.5 인증/인가 구현 방식

- **인증**: Spring Security + JWT (AuthHub SDK)
- **인가**: `@PreAuthorize` + `MarketAccessChecker`
- **superAdmin()**: SUPER_ADMIN 역할 확인
- **authenticated()**: 인증된 사용자면 true
- **myselfOr(long id, String permission)**: 본인 또는 권한 보유 확인

---

---

## 문서 버전

- **작성일**: 2026-02-06
- **수정일**: 2026-02-06 (인증/인가 시나리오 추가)
- **작성자**: Claude Code
- **기준 문서**:
  - `api-endpoints/selleradmin.md`
  - `api-flow/selleradmin.md`
  - **Controller**: `SellerAdminApplicationQueryController`, `SellerAdminApplicationCommandController`
  - **인증/인가**: `MarketAccessChecker` (AuthHub SDK)
- **총 시나리오**: 77개 (P0: 47, P1: 29, P2: 1)
- **테스트 범위**: Query 2개, Command 7개, 통합 플로우 3개 엔드포인트
- **신규 추가**: 인증/인가 시나리오 19개 (+401, +403, 권한 기반 접근)
