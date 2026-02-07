# CommonCodeType E2E 통합 테스트 시나리오

공통 코드 타입(CommonCodeType) 도메인의 E2E 통합 테스트 시나리오 설계 문서입니다.

## 목차

1. [테스트 환경 구성](#1-테스트-환경-구성)
2. [Query 엔드포인트 시나리오](#2-query-엔드포인트-시나리오)
3. [Command 엔드포인트 시나리오](#3-command-엔드포인트-시나리오)
4. [통합 플로우 시나리오](#4-통합-플로우-시나리오)
5. [Fixture 설계](#5-fixture-설계)

---

## 1. 테스트 환경 구성

### 1.1. 테스트 클래스 구조

```
integration-test/
└── src/test/java/com/ryuqq/marketplace/integration/
    └── commoncodetype/
        ├── CommonCodeTypeQueryE2ETest.java       # Query 엔드포인트 테스트
        ├── CommonCodeTypeCommandE2ETest.java     # Command 엔드포인트 테스트
        ├── CommonCodeTypeFlowE2ETest.java        # 전체 플로우 테스트
        └── fixture/
            └── CommonCodeTypeE2EFixture.java     # 테스트 픽스처
```

### 1.2. 필요한 Repository

- `CommonCodeTypeJpaRepository` - 공통 코드 타입 저장소
- `CommonCodeJpaRepository` - 공통 코드 저장소 (비활성화 검증용)

### 1.3. TestFixtures

- `CommonCodeTypeJpaEntityFixtures` - JPA Entity 픽스처
- `CommonCodeJpaEntityFixtures` - 공통 코드 Entity 픽스처

### 1.4. 테스트 데이터 정리

각 테스트 메서드 전후에 데이터 정리 필요:

```java
@BeforeEach
void setUp() {
    commonCodeTypeJpaRepository.deleteAll();
    commonCodeJpaRepository.deleteAll();
}

@AfterEach
void tearDown() {
    commonCodeTypeJpaRepository.deleteAll();
    commonCodeJpaRepository.deleteAll();
}
```

### 1.5. 인증/인가 컨텍스트

#### 슈퍼어드민 토큰 설정
```java
// SUPER_ADMIN 권한 토큰 설정
mockAuthToken(AuthRole.SUPER_ADMIN);
```

#### 일반 사용자 토큰 설정
```java
// 일반 사용자 토큰 설정
mockAuthToken(AuthRole.USER);
```

#### 비인증 상태
```java
// 토큰 없이 요청
// Authorization 헤더 미설정
```

---

## 2. Query 엔드포인트 시나리오

### 인증/인가 정책

Query 엔드포인트는 **@PreAuthorize 없음** - 비인증 사용자도 접근 가능 (공개 API)

---

### 2.1. GET /api/v1/market/admin/common-code-types - 목록 조회

#### 시나리오 2.1.0: 인증 없이 접근 가능 (공개 API)

**Priority**: P0

**사전 데이터**:
- 공통 코드 타입 3건 저장

**요청**:
```http
GET /api/v1/market/admin/common-code-types
# Authorization 헤더 없음
```

**기대 결과**:
- Status: 200 OK
- `success`: true
- `data.content.size()`: 3
- 비인증 사용자도 정상 조회 가능

---

#### 시나리오 2.1.1: 기본 조회 - 데이터 존재 시 정상 조회

**Priority**: P0

**사전 데이터**:
- 공통 코드 타입 5건 저장 (활성화 상태)
  - PAYMENT_METHOD (표시 순서: 1)
  - DELIVERY_COMPANY (표시 순서: 2)
  - BANK (표시 순서: 3)
  - CARD_COMPANY (표시 순서: 4)
  - REFUND_REASON (표시 순서: 5)

**요청**:
```http
GET /api/v1/market/admin/common-code-types?page=0&size=10
```

**기대 결과**:
- Status: 200 OK
- `success`: true
- `data.content.size()`: 5
- `data.totalElements`: 5
- `data.totalPages`: 1
- `data.currentPage`: 0
- `data.hasNext`: false
- `data.hasPrevious`: false
- 각 항목에 `id`, `code`, `name`, `description`, `displayOrder`, `active`, `createdAt`, `updatedAt` 포함

**검증**:
- 응답 필드 완전성
- 정렬 순서 확인 (displayOrder 기본 정렬)

---

#### 시나리오 2.1.2: 빈 결과 - 데이터 없을 때 빈 목록 반환

**Priority**: P0

**사전 데이터**: 없음

**요청**:
```http
GET /api/v1/market/admin/common-code-types?page=0&size=10
```

**기대 결과**:
- Status: 200 OK
- `success`: true
- `data.content.size()`: 0
- `data.totalElements`: 0
- `data.totalPages`: 0
- `data.hasNext`: false
- `data.hasPrevious`: false

---

#### 시나리오 2.1.3: 페이징 - page, size 파라미터 동작

**Priority**: P1

**사전 데이터**:
- 공통 코드 타입 10건 저장

**요청**:
```http
GET /api/v1/market/admin/common-code-types?page=0&size=3
```

**기대 결과**:
- Status: 200 OK
- `data.content.size()`: 3
- `data.totalElements`: 10
- `data.totalPages`: 4
- `data.currentPage`: 0
- `data.hasNext`: true
- `data.hasPrevious`: false

**추가 검증**:
- 두 번째 페이지 조회 (`page=1&size=3`) 시 다른 데이터 반환
- 마지막 페이지 조회 시 `hasNext`: false

---

#### 시나리오 2.1.4: 활성화 필터 - active=true로 필터링

**Priority**: P1

**사전 데이터**:
- 활성화된 공통 코드 타입 3건
- 비활성화된 공통 코드 타입 2건

**요청**:
```http
GET /api/v1/market/admin/common-code-types?active=true
```

**기대 결과**:
- Status: 200 OK
- `data.content.size()`: 3
- `data.totalElements`: 3
- 모든 항목의 `active`: true

---

#### 시나리오 2.1.5: 활성화 필터 - active=false로 필터링

**Priority**: P1

**사전 데이터**:
- 활성화된 공통 코드 타입 3건
- 비활성화된 공통 코드 타입 2건

**요청**:
```http
GET /api/v1/market/admin/common-code-types?active=false
```

**기대 결과**:
- Status: 200 OK
- `data.content.size()`: 2
- `data.totalElements`: 2
- 모든 항목의 `active`: false

---

#### 시나리오 2.1.6: 검색 - searchField=code & searchWord

**Priority**: P1

**사전 데이터**:
- PAYMENT_METHOD
- DELIVERY_COMPANY
- BANK

**요청**:
```http
GET /api/v1/market/admin/common-code-types?searchField=CODE&searchWord=PAYMENT
```

**기대 결과**:
- Status: 200 OK
- `data.content.size()`: 1
- `data.content[0].code`: "PAYMENT_METHOD"

---

#### 시나리오 2.1.7: 검색 - searchField=name & searchWord

**Priority**: P1

**사전 데이터**:
- PAYMENT_METHOD (이름: "결제수단")
- DELIVERY_COMPANY (이름: "배송사")

**요청**:
```http
GET /api/v1/market/admin/common-code-types?searchField=NAME&searchWord=결제
```

**기대 결과**:
- Status: 200 OK
- `data.content.size()`: 1
- `data.content[0].name`: "결제수단"

---

#### 시나리오 2.1.8: 검색 - searchField 없이 searchWord만 (전체 필드 검색)

**Priority**: P1

**사전 데이터**:
- PAYMENT_METHOD (이름: "결제수단")
- DELIVERY_COMPANY (이름: "배송사")

**요청**:
```http
GET /api/v1/market/admin/common-code-types?searchWord=결제
```

**기대 결과**:
- Status: 200 OK
- `data.content.size()`: 1
- code 또는 name에 "결제"가 포함된 항목 반환

---

#### 시나리오 2.1.9: 정렬 - sortKey=CREATED_AT, sortDirection=DESC

**Priority**: P1

**사전 데이터**:
- 공통 코드 타입 3건 (순차적으로 생성, 0.1초 간격)

**요청**:
```http
GET /api/v1/market/admin/common-code-types?sortKey=CREATED_AT&sortDirection=DESC
```

**기대 결과**:
- Status: 200 OK
- `data.content.size()`: 3
- 최신 생성 항목이 첫 번째에 위치
- `data.content[0].createdAt` > `data.content[1].createdAt` > `data.content[2].createdAt`

---

#### 시나리오 2.1.10: 정렬 - sortKey=DISPLAY_ORDER, sortDirection=ASC (기본값)

**Priority**: P1

**사전 데이터**:
- 공통 코드 타입 3건 (displayOrder: 5, 1, 3)

**요청**:
```http
GET /api/v1/market/admin/common-code-types?sortKey=DISPLAY_ORDER&sortDirection=ASC
```

**기대 결과**:
- Status: 200 OK
- `data.content.size()`: 3
- `data.content[0].displayOrder`: 1
- `data.content[1].displayOrder`: 3
- `data.content[2].displayOrder`: 5

---

#### 시나리오 2.1.11: 정렬 - sortKey=CODE, sortDirection=ASC

**Priority**: P1

**사전 데이터**:
- CARD_COMPANY
- BANK
- DELIVERY_COMPANY

**요청**:
```http
GET /api/v1/market/admin/common-code-types?sortKey=CODE&sortDirection=ASC
```

**기대 결과**:
- Status: 200 OK
- `data.content.size()`: 3
- 알파벳 순서로 정렬: BANK → CARD_COMPANY → DELIVERY_COMPANY

---

#### 시나리오 2.1.12: 복합 필터 - active + searchWord + 정렬

**Priority**: P1

**사전 데이터**:
- PAYMENT_METHOD (활성화, 표시 순서: 1)
- PAYMENT_GATEWAY (활성화, 표시 순서: 2)
- PAYMENT_BANK (비활성화, 표시 순서: 3)

**요청**:
```http
GET /api/v1/market/admin/common-code-types?active=true&searchWord=PAYMENT&sortKey=DISPLAY_ORDER&sortDirection=ASC
```

**기대 결과**:
- Status: 200 OK
- `data.content.size()`: 2
- 활성화 + "PAYMENT" 포함 + displayOrder 순서
- `data.content[0].code`: "PAYMENT_METHOD"
- `data.content[1].code`: "PAYMENT_GATEWAY"

---

#### 시나리오 2.1.13: 페이지 범위 초과 - 존재하지 않는 페이지

**Priority**: P2

**사전 데이터**:
- 공통 코드 타입 5건

**요청**:
```http
GET /api/v1/market/admin/common-code-types?page=10&size=10
```

**기대 결과**:
- Status: 200 OK
- `data.content.size()`: 0
- `data.totalElements`: 5
- `data.currentPage`: 10
- `data.hasNext`: false
- `data.hasPrevious`: true

---

#### 시나리오 2.1.14: 잘못된 정렬 키 - 400 에러 (Optional)

**Priority**: P2

**요청**:
```http
GET /api/v1/market/admin/common-code-types?sortKey=INVALID_KEY
```

**기대 결과**:
- Status: 400 Bad Request
- 유효하지 않은 sortKey에 대한 에러 메시지

---

## 3. Command 엔드포인트 시나리오

### 인증/인가 정책

Command 엔드포인트는 **@PreAuthorize("@access.superAdmin()")** - 슈퍼어드민 권한 필요

---

### 3.1. POST /api/v1/market/admin/common-code-types - 등록

#### 시나리오 3.1.0.1: 인증 없이 접근 시 401 Unauthorized

**Priority**: P0

**사전 데이터**: 없음

**요청**:
```http
POST /api/v1/market/admin/common-code-types
Content-Type: application/json
# Authorization 헤더 없음

{
  "code": "PAYMENT_METHOD",
  "name": "결제수단",
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 401 Unauthorized
- 인증 토큰 없음 에러 메시지

---

#### 시나리오 3.1.0.2: 일반 사용자 접근 시 403 Forbidden

**Priority**: P0

**사전 데이터**: 없음

**인증 컨텍스트**: 일반 사용자 (USER 권한)

**요청**:
```http
POST /api/v1/market/admin/common-code-types
Content-Type: application/json
Authorization: Bearer {USER_TOKEN}

{
  "code": "PAYMENT_METHOD",
  "name": "결제수단",
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 403 Forbidden
- 권한 부족 에러 메시지 ("SUPER_ADMIN 권한 필요")

---

#### 시나리오 3.1.1: 생성 성공 - 유효한 요청으로 생성 (SUPER_ADMIN)

**Priority**: P0

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
POST /api/v1/market/admin/common-code-types
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "code": "PAYMENT_METHOD",
  "name": "결제수단",
  "description": "결제 시 사용 가능한 결제수단 목록",
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 201 Created
- `success`: true
- `data`: Long 타입 ID (> 0)

**DB 검증**:
```java
CommonCodeTypeJpaEntity saved = commonCodeTypeJpaRepository.findById(createdId).orElseThrow();
assertThat(saved.getCode()).isEqualTo("PAYMENT_METHOD");
assertThat(saved.getName()).isEqualTo("결제수단");
assertThat(saved.getDescription()).isEqualTo("결제 시 사용 가능한 결제수단 목록");
assertThat(saved.getDisplayOrder()).isEqualTo(1);
assertThat(saved.isActive()).isTrue();
assertThat(saved.getCreatedAt()).isNotNull();
assertThat(saved.getUpdatedAt()).isNotNull();
```

---

#### 시나리오 3.1.2: 생성 성공 - description 없이 생성

**Priority**: P0

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
POST /api/v1/market/admin/common-code-types
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "code": "DELIVERY_COMPANY",
  "name": "배송사",
  "displayOrder": 2
}
```

**기대 결과**:
- Status: 201 Created
- `success`: true
- `data`: Long 타입 ID

**DB 검증**:
```java
CommonCodeTypeJpaEntity saved = commonCodeTypeJpaRepository.findById(createdId).orElseThrow();
assertThat(saved.getDescription()).isNullOrEmpty();
```

---

#### 시나리오 3.1.3: 필수 필드 누락 - code 누락 시 400

**Priority**: P0

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
POST /api/v1/market/admin/common-code-types
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "name": "결제수단",
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "code" 필드 관련 내용 포함

---

#### 시나리오 3.1.4: 필수 필드 누락 - name 누락 시 400

**Priority**: P0

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
POST /api/v1/market/admin/common-code-types
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "code": "PAYMENT_METHOD",
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "name" 필드 관련 내용 포함

---

#### 시나리오 3.1.5: 필수 필드 누락 - displayOrder 누락 시 400

**Priority**: P0

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
POST /api/v1/market/admin/common-code-types
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "code": "PAYMENT_METHOD",
  "name": "결제수단"
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "displayOrder" 필드 관련 내용 포함

---

#### 시나리오 3.1.6: 중복 코드 - 동일 code 등록 시 409

**Priority**: P0

**사전 데이터**:
- 공통 코드 타입 1건 (code: "PAYMENT_METHOD")

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
POST /api/v1/market/admin/common-code-types
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "code": "PAYMENT_METHOD",
  "name": "결제수단2",
  "displayOrder": 2
}
```

**기대 결과**:
- Status: 409 Conflict
- `success`: false
- 에러 코드: "CCT-002"
- 에러 메시지: "동일한 코드가 이미 존재합니다"

**DB 검증**:
```java
long count = commonCodeTypeJpaRepository.count();
assertThat(count).isEqualTo(1); // 기존 1건만 존재
```

---

#### 시나리오 3.1.7: 잘못된 displayOrder - 음수 값 시 400

**Priority**: P1

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
POST /api/v1/market/admin/common-code-types
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "code": "PAYMENT_METHOD",
  "name": "결제수단",
  "displayOrder": -1
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "displayOrder" 최소값 관련 내용 포함

---

#### 시나리오 3.1.8: 빈 문자열 - code가 빈 문자열일 때 400

**Priority**: P1

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
POST /api/v1/market/admin/common-code-types
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "code": "",
  "name": "결제수단",
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "code" 필드 관련 내용 포함

---

#### 시나리오 3.1.9: 빈 문자열 - name이 빈 문자열일 때 400

**Priority**: P1

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
POST /api/v1/market/admin/common-code-types
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "code": "PAYMENT_METHOD",
  "name": "",
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "name" 필드 관련 내용 포함

---

#### 시나리오 3.1.10: 길이 초과 - code가 50자 초과 시 400

**Priority**: P2

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
POST /api/v1/market/admin/common-code-types
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "code": "A".repeat(51),
  "name": "결제수단",
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "code" 길이 제한 관련 내용 포함

---

#### 시나리오 3.1.11: 길이 초과 - name이 100자 초과 시 400

**Priority**: P2

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
POST /api/v1/market/admin/common-code-types
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "code": "PAYMENT_METHOD",
  "name": "가".repeat(101),
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "name" 길이 제한 관련 내용 포함

---

#### 시나리오 3.1.12: 길이 초과 - description이 500자 초과 시 400

**Priority**: P2

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
POST /api/v1/market/admin/common-code-types
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "code": "PAYMENT_METHOD",
  "name": "결제수단",
  "description": "가".repeat(501),
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "description" 길이 제한 관련 내용 포함

---

### 3.2. PUT /api/v1/market/admin/common-code-types/{id} - 수정

#### 시나리오 3.2.0.1: 인증 없이 접근 시 401 Unauthorized

**Priority**: P0

**사전 데이터**:
- 공통 코드 타입 1건

**요청**:
```http
PUT /api/v1/market/admin/common-code-types/{id}
Content-Type: application/json
# Authorization 헤더 없음

{
  "name": "결제수단_수정",
  "displayOrder": 2
}
```

**기대 결과**:
- Status: 401 Unauthorized
- 인증 토큰 없음 에러 메시지

---

#### 시나리오 3.2.0.2: 일반 사용자 접근 시 403 Forbidden

**Priority**: P0

**사전 데이터**:
- 공통 코드 타입 1건

**인증 컨텍스트**: 일반 사용자 (USER 권한)

**요청**:
```http
PUT /api/v1/market/admin/common-code-types/{id}
Content-Type: application/json
Authorization: Bearer {USER_TOKEN}

{
  "name": "결제수단_수정",
  "displayOrder": 2
}
```

**기대 결과**:
- Status: 403 Forbidden
- 권한 부족 에러 메시지 ("SUPER_ADMIN 권한 필요")

---

#### 시나리오 3.2.1: 수정 성공 - 존재하는 리소스 수정 (SUPER_ADMIN)

**Priority**: P0

**사전 데이터**:
- 공통 코드 타입 1건 (PAYMENT_METHOD, 표시 순서: 1)

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PUT /api/v1/market/admin/common-code-types/{id}
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "name": "결제수단_수정",
  "description": "수정된 설명",
  "displayOrder": 2
}
```

**기대 결과**:
- Status: 200 OK
- `success`: true

**DB 검증**:
```java
CommonCodeTypeJpaEntity updated = commonCodeTypeJpaRepository.findById(id).orElseThrow();
assertThat(updated.getName()).isEqualTo("결제수단_수정");
assertThat(updated.getDescription()).isEqualTo("수정된 설명");
assertThat(updated.getDisplayOrder()).isEqualTo(2);
assertThat(updated.getCode()).isEqualTo("PAYMENT_METHOD"); // 코드는 변경 불가
assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
```

---

#### 시나리오 3.2.2: 수정 성공 - description을 null로 변경

**Priority**: P0

**사전 데이터**:
- 공통 코드 타입 1건 (description: "기존 설명")

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PUT /api/v1/market/admin/common-code-types/{id}
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "name": "결제수단",
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 200 OK
- `success`: true

**DB 검증**:
```java
CommonCodeTypeJpaEntity updated = commonCodeTypeJpaRepository.findById(id).orElseThrow();
assertThat(updated.getDescription()).isNullOrEmpty();
```

---

#### 시나리오 3.2.3: 존재하지 않는 리소스 - 없는 ID 수정 시 404

**Priority**: P0

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PUT /api/v1/market/admin/common-code-types/99999
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "name": "결제수단",
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 404 Not Found
- `success`: false
- 에러 코드: "CCT-001"
- 에러 메시지: "공통 코드 타입을 찾을 수 없습니다"

---

#### 시나리오 3.2.4: 필수 필드 누락 - name 누락 시 400

**Priority**: P0

**사전 데이터**:
- 공통 코드 타입 1건

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PUT /api/v1/market/admin/common-code-types/{id}
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "name" 필드 관련 내용 포함

---

#### 시나리오 3.2.5: 필수 필드 누락 - displayOrder 누락 시 400

**Priority**: P0

**사전 데이터**:
- 공통 코드 타입 1건

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PUT /api/v1/market/admin/common-code-types/{id}
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "name": "결제수단"
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "displayOrder" 필드 관련 내용 포함

---

#### 시나리오 3.2.6: displayOrder 중복 - 다른 항목과 중복 시 400

**Priority**: P1

**사전 데이터**:
- 공통 코드 타입 2건
  - ID 1: PAYMENT_METHOD (표시 순서: 1)
  - ID 2: DELIVERY_COMPANY (표시 순서: 2)

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PUT /api/v1/market/admin/common-code-types/1
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "name": "결제수단",
  "displayOrder": 2
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 코드: "CCT-003"
- 에러 메시지: "동일한 표시 순서가 이미 존재합니다"

**DB 검증**:
```java
CommonCodeTypeJpaEntity entity = commonCodeTypeJpaRepository.findById(1L).orElseThrow();
assertThat(entity.getDisplayOrder()).isEqualTo(1); // 변경되지 않음
```

---

#### 시나리오 3.2.7: displayOrder 중복 - 자기 자신의 displayOrder는 허용

**Priority**: P1

**사전 데이터**:
- 공통 코드 타입 1건 (ID 1, 표시 순서: 1)

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PUT /api/v1/market/admin/common-code-types/1
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "name": "결제수단_수정",
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 200 OK
- `success`: true

**DB 검증**:
```java
CommonCodeTypeJpaEntity updated = commonCodeTypeJpaRepository.findById(1L).orElseThrow();
assertThat(updated.getName()).isEqualTo("결제수단_수정");
assertThat(updated.getDisplayOrder()).isEqualTo(1); // 동일한 값으로 변경 가능
```

---

#### 시나리오 3.2.8: 잘못된 displayOrder - 음수 값 시 400

**Priority**: P1

**사전 데이터**:
- 공통 코드 타입 1건

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PUT /api/v1/market/admin/common-code-types/{id}
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "name": "결제수단",
  "displayOrder": -1
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "displayOrder" 최소값 관련 내용 포함

---

#### 시나리오 3.2.9: 빈 문자열 - name이 빈 문자열일 때 400

**Priority**: P1

**사전 데이터**:
- 공통 코드 타입 1건

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PUT /api/v1/market/admin/common-code-types/{id}
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "name": "",
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "name" 필드 관련 내용 포함

---

#### 시나리오 3.2.10: 길이 초과 - name이 100자 초과 시 400

**Priority**: P2

**사전 데이터**:
- 공통 코드 타입 1건

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PUT /api/v1/market/admin/common-code-types/{id}
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "name": "가".repeat(101),
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "name" 길이 제한 관련 내용 포함

---

#### 시나리오 3.2.11: 길이 초과 - description이 500자 초과 시 400

**Priority**: P2

**사전 데이터**:
- 공통 코드 타입 1건

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PUT /api/v1/market/admin/common-code-types/{id}
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "name": "결제수단",
  "description": "가".repeat(501),
  "displayOrder": 1
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "description" 길이 제한 관련 내용 포함

---

### 3.3. PATCH /api/v1/market/admin/common-code-types/active-status - 상태 변경

#### 시나리오 3.3.0.1: 인증 없이 접근 시 401 Unauthorized

**Priority**: P0

**사전 데이터**: 없음

**요청**:
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Content-Type: application/json
# Authorization 헤더 없음

{
  "ids": [1],
  "active": true
}
```

**기대 결과**:
- Status: 401 Unauthorized
- 인증 토큰 없음 에러 메시지

---

#### 시나리오 3.3.0.2: 일반 사용자 접근 시 403 Forbidden

**Priority**: P0

**사전 데이터**: 없음

**인증 컨텍스트**: 일반 사용자 (USER 권한)

**요청**:
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Content-Type: application/json
Authorization: Bearer {USER_TOKEN}

{
  "ids": [1],
  "active": true
}
```

**기대 결과**:
- Status: 403 Forbidden
- 권한 부족 에러 메시지 ("SUPER_ADMIN 권한 필요")

---

#### 시나리오 3.3.1: 활성화 성공 - 단일 항목 활성화 (SUPER_ADMIN)

**Priority**: P0

**사전 데이터**:
- 비활성화된 공통 코드 타입 1건 (ID: 1)

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "ids": [1],
  "active": true
}
```

**기대 결과**:
- Status: 200 OK
- `success`: true

**DB 검증**:
```java
CommonCodeTypeJpaEntity updated = commonCodeTypeJpaRepository.findById(1L).orElseThrow();
assertThat(updated.isActive()).isTrue();
assertThat(updated.getUpdatedAt()).isAfter(savedUpdatedAt);
```

---

#### 시나리오 3.3.2: 비활성화 성공 - 단일 항목 비활성화 (하위 공통 코드 없음)

**Priority**: P0

**사전 데이터**:
- 활성화된 공통 코드 타입 1건 (ID: 1)
- 하위 공통 코드 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "ids": [1],
  "active": false
}
```

**기대 결과**:
- Status: 200 OK
- `success`: true

**DB 검증**:
```java
CommonCodeTypeJpaEntity updated = commonCodeTypeJpaRepository.findById(1L).orElseThrow();
assertThat(updated.isActive()).isFalse();
```

---

#### 시나리오 3.3.3: 일괄 활성화 - 여러 항목 동시 활성화

**Priority**: P0

**사전 데이터**:
- 비활성화된 공통 코드 타입 3건 (ID: 1, 2, 3)

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "ids": [1, 2, 3],
  "active": true
}
```

**기대 결과**:
- Status: 200 OK
- `success`: true

**DB 검증**:
```java
List<CommonCodeTypeJpaEntity> updated = commonCodeTypeJpaRepository.findAllById(List.of(1L, 2L, 3L));
assertThat(updated).allMatch(CommonCodeTypeJpaEntity::isActive);
```

---

#### 시나리오 3.3.4: 일괄 비활성화 - 여러 항목 동시 비활성화 (하위 공통 코드 없음)

**Priority**: P0

**사전 데이터**:
- 활성화된 공통 코드 타입 3건 (ID: 1, 2, 3)
- 하위 공통 코드 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "ids": [1, 2, 3],
  "active": false
}
```

**기대 결과**:
- Status: 200 OK
- `success`: true

**DB 검증**:
```java
List<CommonCodeTypeJpaEntity> updated = commonCodeTypeJpaRepository.findAllById(List.of(1L, 2L, 3L));
assertThat(updated).allMatch(entity -> !entity.isActive());
```

---

#### 시나리오 3.3.5: 비활성화 실패 - 활성화된 하위 공통 코드 존재 시 400

**Priority**: P0

**사전 데이터**:
- 활성화된 공통 코드 타입 1건 (ID: 1, PAYMENT_METHOD)
- 해당 타입의 활성화된 공통 코드 1건 (CREDIT_CARD)

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "ids": [1],
  "active": false
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 코드: "CCT-004"
- 에러 메시지: "활성화된 공통 코드가 존재하여 비활성화할 수 없습니다"

**DB 검증**:
```java
CommonCodeTypeJpaEntity entity = commonCodeTypeJpaRepository.findById(1L).orElseThrow();
assertThat(entity.isActive()).isTrue(); // 변경되지 않음
```

---

#### 시나리오 3.3.6: 비활성화 성공 - 비활성화된 하위 공통 코드만 존재

**Priority**: P0

**사전 데이터**:
- 활성화된 공통 코드 타입 1건 (ID: 1)
- 해당 타입의 비활성화된 공통 코드 2건

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "ids": [1],
  "active": false
}
```

**기대 결과**:
- Status: 200 OK
- `success`: true

**DB 검증**:
```java
CommonCodeTypeJpaEntity updated = commonCodeTypeJpaRepository.findById(1L).orElseThrow();
assertThat(updated.isActive()).isFalse();
```

---

#### 시나리오 3.3.7: 일괄 비활성화 실패 - 일부 항목에 활성 공통 코드 존재

**Priority**: P0

**사전 데이터**:
- 활성화된 공통 코드 타입 3건 (ID: 1, 2, 3)
- ID 1: 하위 활성 공통 코드 없음
- ID 2: 하위 활성 공통 코드 1건 존재
- ID 3: 하위 활성 공통 코드 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "ids": [1, 2, 3],
  "active": false
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 코드: "CCT-004"
- 에러 메시지에 ID 2 관련 내용 포함

**DB 검증**:
```java
List<CommonCodeTypeJpaEntity> entities = commonCodeTypeJpaRepository.findAllById(List.of(1L, 2L, 3L));
assertThat(entities).allMatch(CommonCodeTypeJpaEntity::isActive); // 모두 변경되지 않음 (트랜잭션 롤백)
```

---

#### 시나리오 3.3.8: 필수 필드 누락 - ids 누락 시 400

**Priority**: P0

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "active": true
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "ids" 필드 관련 내용 포함

---

#### 시나리오 3.3.9: 필수 필드 누락 - active 누락 시 400

**Priority**: P0

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "ids": [1]
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "active" 필드 관련 내용 포함

---

#### 시나리오 3.3.10: 빈 배열 - ids가 빈 배열일 때 400

**Priority**: P1

**사전 데이터**: 없음

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "ids": [],
  "active": true
}
```

**기대 결과**:
- Status: 400 Bad Request
- `success`: false
- 에러 메시지에 "ids" 필드 관련 내용 포함 (NotEmpty 검증)

---

#### 시나리오 3.3.11: 존재하지 않는 ID - 일부 ID가 존재하지 않을 때 404

**Priority**: P0

**사전 데이터**:
- 공통 코드 타입 1건 (ID: 1)

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "ids": [1, 99999],
  "active": true
}
```

**기대 결과**:
- Status: 404 Not Found
- `success`: false
- 에러 코드: "CCT-001"
- 에러 메시지: "공통 코드 타입을 찾을 수 없습니다"

**DB 검증**:
```java
CommonCodeTypeJpaEntity entity = commonCodeTypeJpaRepository.findById(1L).orElseThrow();
// 트랜잭션 롤백으로 변경되지 않음
```

---

#### 시나리오 3.3.12: 동일 시간 적용 - 일괄 변경 시 updatedAt 동일

**Priority**: P2

**사전 데이터**:
- 활성화된 공통 코드 타입 3건 (ID: 1, 2, 3)

**인증 컨텍스트**: SUPER_ADMIN

**요청**:
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Content-Type: application/json
Authorization: Bearer {SUPER_ADMIN_TOKEN}

{
  "ids": [1, 2, 3],
  "active": false
}
```

**기대 결과**:
- Status: 200 OK

**DB 검증**:
```java
List<CommonCodeTypeJpaEntity> updated = commonCodeTypeJpaRepository.findAllById(List.of(1L, 2L, 3L));
Instant firstUpdatedAt = updated.get(0).getUpdatedAt();
assertThat(updated).allMatch(entity -> entity.getUpdatedAt().equals(firstUpdatedAt));
// 모든 항목의 updatedAt이 동일 (Factory에서 동일한 시간 생성)
```

---

## 4. 통합 플로우 시나리오

### 4.1. CRUD 전체 플로우

#### 시나리오 4.1.1: 전체 CRUD 플로우 - 생성 → 조회 → 수정 → 비활성화 → 삭제

**Priority**: P0

**인증 컨텍스트**: SUPER_ADMIN (생성/수정/상태 변경 시), 비인증 (조회 시)

**단계**:

**Step 1: 생성 (SUPER_ADMIN 토큰 필요)**
```http
POST /api/v1/market/admin/common-code-types
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "code": "TEST_TYPE",
  "name": "테스트타입",
  "description": "테스트용",
  "displayOrder": 1
}
```
- Status: 201 Created
- ID 저장 (예: createdId = 1)

**Step 2: 목록 조회로 생성 확인 (토큰 불필요)**
```http
GET /api/v1/market/admin/common-code-types?searchWord=TEST_TYPE
```
- Status: 200 OK
- `data.content.size()`: 1
- `data.content[0].code`: "TEST_TYPE"
- `data.content[0].active`: true

**Step 3: 수정 (SUPER_ADMIN 토큰 필요)**
```http
PUT /api/v1/market/admin/common-code-types/{createdId}
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "name": "테스트타입_수정",
  "description": "수정된 설명",
  "displayOrder": 2
}
```
- Status: 200 OK

**Step 4: 목록 조회로 수정 확인 (토큰 불필요)**
```http
GET /api/v1/market/admin/common-code-types?searchWord=TEST_TYPE
```
- Status: 200 OK
- `data.content[0].name`: "테스트타입_수정"
- `data.content[0].description`: "수정된 설명"
- `data.content[0].displayOrder`: 2

**Step 5: 비활성화 (SUPER_ADMIN 토큰 필요)**
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "ids": [{createdId}],
  "active": false
}
```
- Status: 200 OK

**Step 6: 비활성화 확인 (토큰 불필요)**
```http
GET /api/v1/market/admin/common-code-types?active=false&searchWord=TEST_TYPE
```
- Status: 200 OK
- `data.content.size()`: 1
- `data.content[0].active`: false

---

### 4.2. 활성 공통 코드가 있는 경우 비활성화 제약 플로우

#### 시나리오 4.2.1: 공통 코드 존재 시 타입 비활성화 제약

**Priority**: P0

**인증 컨텍스트**: SUPER_ADMIN

**단계**:

**Step 1: 공통 코드 타입 생성**
```http
POST /api/v1/market/admin/common-code-types
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "code": "PAYMENT_METHOD",
  "name": "결제수단",
  "displayOrder": 1
}
```
- Status: 201 Created
- ID 저장 (typeId = 1)

**Step 2: 공통 코드 생성 (활성화)**
```java
// 공통 코드 생성 (별도 API 또는 직접 DB 삽입)
CommonCodeJpaEntity commonCode = CommonCodeJpaEntityFixtures.activeEntity(typeId);
commonCodeJpaRepository.save(commonCode);
```

**Step 3: 타입 비활성화 시도 (실패 예상)**
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "ids": [{typeId}],
  "active": false
}
```
- Status: 400 Bad Request
- 에러 코드: "CCT-004"
- 에러 메시지: "활성화된 공통 코드가 존재하여 비활성화할 수 없습니다"

**Step 4: 공통 코드 비활성화**
```java
// 공통 코드 비활성화 (별도 API 또는 직접 업데이트)
commonCode.changeActiveStatus(false, Instant.now());
commonCodeJpaRepository.save(commonCode);
```

**Step 5: 타입 비활성화 재시도 (성공 예상)**
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "ids": [{typeId}],
  "active": false
}
```
- Status: 200 OK

**Step 6: 비활성화 확인**
```http
GET /api/v1/market/admin/common-code-types?active=false
```
- Status: 200 OK
- `data.content[0].id`: {typeId}
- `data.content[0].active`: false

---

### 4.3. 다중 타입 생성 + 일괄 활성화/비활성화 플로우

#### 시나리오 4.3.1: 다중 타입 생성 후 일괄 상태 변경

**Priority**: P1

**인증 컨텍스트**: SUPER_ADMIN

**단계**:

**Step 1: 타입 3건 생성**
```http
POST /api/v1/market/admin/common-code-types
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "code": "TYPE_A",
  "name": "타입A",
  "displayOrder": 1
}

POST /api/v1/market/admin/common-code-types
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "code": "TYPE_B",
  "name": "타입B",
  "displayOrder": 2
}

POST /api/v1/market/admin/common-code-types
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "code": "TYPE_C",
  "name": "타입C",
  "displayOrder": 3
}
```
- 각각 201 Created
- ID 수집: [id1, id2, id3]

**Step 2: 목록 조회로 생성 확인 (토큰 불필요)**
```http
GET /api/v1/market/admin/common-code-types?active=true
```
- Status: 200 OK
- `data.content.size()`: 3
- 모든 항목 `active`: true

**Step 3: 일괄 비활성화**
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "ids": [id1, id2, id3],
  "active": false
}
```
- Status: 200 OK

**Step 4: 비활성화 확인**
```http
GET /api/v1/market/admin/common-code-types?active=false
```
- Status: 200 OK
- `data.content.size()`: 3
- 모든 항목 `active`: false

**Step 5: 일괄 재활성화**
```http
PATCH /api/v1/market/admin/common-code-types/active-status
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "ids": [id1, id2, id3],
  "active": true
}
```
- Status: 200 OK

**Step 6: 재활성화 확인**
```http
GET /api/v1/market/admin/common-code-types?active=true
```
- Status: 200 OK
- `data.content.size()`: 3
- 모든 항목 `active`: true

---

### 4.4. displayOrder 중복 검증 플로우

#### 시나리오 4.4.1: displayOrder 변경 시 중복 검증

**Priority**: P1

**인증 컨텍스트**: SUPER_ADMIN

**단계**:

**Step 1: 타입 2건 생성**
```http
POST /api/v1/market/admin/common-code-types
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "code": "TYPE_A",
  "name": "타입A",
  "displayOrder": 1
}

POST /api/v1/market/admin/common-code-types
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "code": "TYPE_B",
  "name": "타입B",
  "displayOrder": 2
}
```
- 각각 201 Created
- ID 저장: idA, idB

**Step 2: TYPE_A의 displayOrder를 2로 변경 시도 (실패 예상)**
```http
PUT /api/v1/market/admin/common-code-types/{idA}
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "name": "타입A",
  "displayOrder": 2
}
```
- Status: 400 Bad Request
- 에러 코드: "CCT-003"
- 에러 메시지: "동일한 표시 순서가 이미 존재합니다"

**Step 3: TYPE_B의 displayOrder를 3으로 변경**
```http
PUT /api/v1/market/admin/common-code-types/{idB}
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "name": "타입B",
  "displayOrder": 3
}
```
- Status: 200 OK

**Step 4: 이제 TYPE_A의 displayOrder를 2로 변경 시도 (성공 예상)**
```http
PUT /api/v1/market/admin/common-code-types/{idA}
Authorization: Bearer {SUPER_ADMIN_TOKEN}
{
  "name": "타입A",
  "displayOrder": 2
}
```
- Status: 200 OK

**Step 5: 정렬 순서 확인**
```http
GET /api/v1/market/admin/common-code-types?sortKey=DISPLAY_ORDER&sortDirection=ASC
```
- Status: 200 OK
- `data.content[0].code`: "TYPE_A", displayOrder: 2
- `data.content[1].code`: "TYPE_B", displayOrder: 3

---

### 4.5. 검색 + 페이징 + 정렬 복합 플로우

#### 시나리오 4.5.1: 복합 필터링 및 페이징

**Priority**: P1

**인증 컨텍스트**: 조회는 비인증, 생성은 SUPER_ADMIN

**단계**:

**Step 1: 타입 10건 생성 (다양한 조건)**
```java
// 픽스처로 10건 생성 (SUPER_ADMIN 토큰 사용)
// - 활성화: 7건 (이름에 "결제" 포함: 3건, "배송" 포함: 2건, 기타: 2건)
// - 비활성화: 3건 (이름에 "결제" 포함: 1건, 기타: 2건)
```

**Step 2: 활성화 + "결제" 검색 + 페이징 1페이지**
```http
GET /api/v1/market/admin/common-code-types?active=true&searchWord=결제&page=0&size=2&sortKey=DISPLAY_ORDER&sortDirection=ASC
```
- Status: 200 OK
- `data.content.size()`: 2
- `data.totalElements`: 3
- `data.currentPage`: 0
- `data.hasNext`: true

**Step 3: 활성화 + "결제" 검색 + 페이징 2페이지**
```http
GET /api/v1/market/admin/common-code-types?active=true&searchWord=결제&page=1&size=2&sortKey=DISPLAY_ORDER&sortDirection=ASC
```
- Status: 200 OK
- `data.content.size()`: 1
- `data.totalElements`: 3
- `data.currentPage`: 1
- `data.hasNext`: false
- `data.hasPrevious`: true

**Step 4: 비활성화 + "결제" 검색**
```http
GET /api/v1/market/admin/common-code-types?active=false&searchWord=결제
```
- Status: 200 OK
- `data.content.size()`: 1

---

## 5. Fixture 설계

### 5.1. 필요 Repository

```java
@Autowired
private CommonCodeTypeJpaRepository commonCodeTypeJpaRepository;

@Autowired
private CommonCodeJpaRepository commonCodeJpaRepository;
```

### 5.2. testFixtures 존재 확인

```
adapter-out/persistence-mysql/src/testFixtures/
└── java/com/ryuqq/marketplace/adapter/out/persistence/
    ├── commoncodetype/
    │   └── CommonCodeTypeJpaEntityFixtures.java  ✅
    └── commoncode/
        └── CommonCodeJpaEntityFixtures.java      ✅
```

### 5.3. Fixture 사용 예시

#### 공통 코드 타입 생성

```java
@BeforeEach
void setUp() {
    commonCodeTypeJpaRepository.deleteAll();

    // 기본 타입 3건 생성
    CommonCodeTypeJpaEntity type1 = CommonCodeTypeJpaEntityFixtures.activeEntity("PAYMENT_METHOD", "결제수단", 1);
    CommonCodeTypeJpaEntity type2 = CommonCodeTypeJpaEntityFixtures.activeEntity("DELIVERY_COMPANY", "배송사", 2);
    CommonCodeTypeJpaEntity type3 = CommonCodeTypeJpaEntityFixtures.inactiveEntity("BANK", "은행", 3);

    commonCodeTypeJpaRepository.saveAll(List.of(type1, type2, type3));
}
```

#### 공통 코드 추가 (비활성화 검증용)

```java
// 공통 코드 타입 생성
CommonCodeTypeJpaEntity type = CommonCodeTypeJpaEntityFixtures.activeEntity("PAYMENT_METHOD", "결제수단", 1);
type = commonCodeTypeJpaRepository.save(type);

// 해당 타입의 활성 공통 코드 생성
CommonCodeJpaEntity code = CommonCodeJpaEntityFixtures.activeEntity(type.getId(), "CREDIT_CARD", "신용카드", 1);
commonCodeJpaRepository.save(code);
```

### 5.4. 데이터 정리 패턴

```java
@BeforeEach
void setUp() {
    // 순서 중요: FK 제약 때문에 commonCode 먼저 삭제
    commonCodeJpaRepository.deleteAll();
    commonCodeTypeJpaRepository.deleteAll();
}

@AfterEach
void tearDown() {
    commonCodeJpaRepository.deleteAll();
    commonCodeTypeJpaRepository.deleteAll();
}
```

### 5.5. 픽스처 메서드 목록 (예상)

```java
public class CommonCodeTypeJpaEntityFixtures {

    // 기본 활성화 엔티티
    public static CommonCodeTypeJpaEntity activeEntity(String code, String name, int displayOrder);

    // 비활성화 엔티티
    public static CommonCodeTypeJpaEntity inactiveEntity(String code, String name, int displayOrder);

    // description 포함
    public static CommonCodeTypeJpaEntity entityWithDescription(String code, String name, String description, int displayOrder);

    // 다양한 displayOrder 엔티티 목록
    public static List<CommonCodeTypeJpaEntity> multipleEntities(int count);
}

public class CommonCodeJpaEntityFixtures {

    // 활성 공통 코드
    public static CommonCodeJpaEntity activeEntity(Long typeId, String code, String name, int displayOrder);

    // 비활성 공통 코드
    public static CommonCodeJpaEntity inactiveEntity(Long typeId, String code, String name, int displayOrder);
}
```

---

## 6. 시나리오 요약

### 6.1. Query 엔드포인트 시나리오 통계

| 분류 | P0 | P1 | P2 | 합계 |
|------|----|----|----|----|
| 인증/인가 | 1 | 0 | 0 | 1 |
| 기본 조회 | 2 | 0 | 0 | 2 |
| 페이징 | 0 | 1 | 1 | 2 |
| 활성화 필터 | 0 | 2 | 0 | 2 |
| 검색 | 0 | 3 | 0 | 3 |
| 정렬 | 0 | 3 | 0 | 3 |
| 복합 필터 | 0 | 1 | 0 | 1 |
| 엣지 케이스 | 0 | 0 | 1 | 1 |
| **합계** | **3** | **10** | **2** | **15** |

### 6.2. Command 엔드포인트 시나리오 통계

#### POST (등록)

| 분류 | P0 | P1 | P2 | 합계 |
|------|----|----|----|----|
| 인증/인가 | 2 | 0 | 0 | 2 |
| 생성 성공 | 2 | 0 | 0 | 2 |
| 필수 필드 누락 | 3 | 0 | 0 | 3 |
| 중복 검증 | 1 | 0 | 0 | 1 |
| 잘못된 값 | 0 | 3 | 0 | 3 |
| 길이 제한 | 0 | 0 | 3 | 3 |
| **소계** | **8** | **3** | **3** | **14** |

#### PUT (수정)

| 분류 | P0 | P1 | P2 | 합계 |
|------|----|----|----|----|
| 인증/인가 | 2 | 0 | 0 | 2 |
| 수정 성공 | 2 | 0 | 0 | 2 |
| 존재하지 않는 리소스 | 1 | 0 | 0 | 1 |
| 필수 필드 누락 | 2 | 0 | 0 | 2 |
| displayOrder 중복 | 0 | 2 | 0 | 2 |
| 잘못된 값 | 0 | 2 | 0 | 2 |
| 길이 제한 | 0 | 0 | 2 | 2 |
| **소계** | **7** | **4** | **2** | **13** |

#### PATCH (상태 변경)

| 분류 | P0 | P1 | P2 | 합계 |
|------|----|----|----|----|
| 인증/인가 | 2 | 0 | 0 | 2 |
| 활성화 성공 | 2 | 0 | 0 | 2 |
| 비활성화 성공 | 4 | 0 | 0 | 4 |
| 비활성화 실패 (하위 코드 존재) | 2 | 0 | 0 | 2 |
| 필수 필드 누락 | 2 | 0 | 0 | 2 |
| 빈 배열 | 0 | 1 | 0 | 1 |
| 존재하지 않는 ID | 1 | 0 | 0 | 1 |
| 동일 시간 검증 | 0 | 0 | 1 | 1 |
| **소계** | **13** | **1** | **1** | **15** |

**Command 합계**: P0: 28, P1: 8, P2: 6, 총 42개

### 6.3. 통합 플로우 시나리오 통계

| 분류 | P0 | P1 | 합계 |
|------|----|----|------|
| CRUD 전체 플로우 | 1 | 0 | 1 |
| 비활성화 제약 플로우 | 1 | 0 | 1 |
| 일괄 상태 변경 플로우 | 0 | 1 | 1 |
| displayOrder 중복 검증 플로우 | 0 | 1 | 1 |
| 복합 검색 플로우 | 0 | 1 | 1 |
| **합계** | **2** | **3** | **5** |

### 6.4. 전체 시나리오 통계

| 엔드포인트 분류 | P0 | P1 | P2 | 합계 |
|----------------|----|----|----|----|
| Query | 3 | 10 | 2 | 15 |
| Command - POST | 8 | 3 | 3 | 14 |
| Command - PUT | 7 | 4 | 2 | 13 |
| Command - PATCH | 13 | 1 | 1 | 15 |
| 통합 플로우 | 2 | 3 | 0 | 5 |
| **전체 합계** | **33** | **21** | **8** | **62** |

**인증/인가 시나리오 추가**: 7개 (P0)
- Query: 1개 (비인증 접근 가능 확인)
- Command POST: 2개 (401, 403)
- Command PUT: 2개 (401, 403)
- Command PATCH: 2개 (401, 403)

---

## 7. 우선순위별 실행 계획

### Phase 1: P0 시나리오 (필수, 33개)

핵심 기능의 정상 동작과 주요 에러 케이스 + 인증/인가 검증

**실행 순서**:
1. Query 인증/인가 + 기본 조회 (3개) - 8분
2. Command POST 인증/인가 + 생성 + 검증 (8개) - 20분
3. Command PUT 인증/인가 + 수정 + 검증 (7개) - 18분
4. Command PATCH 인증/인가 + 상태 변경 + 검증 (13개) - 25분
5. 통합 플로우 (2개) - 10분

**예상 소요 시간**: 약 81분

### Phase 2: P1 시나리오 (중요, 21개)

검색, 필터링, 정렬, 엣지 케이스 검증

**실행 순서**:
1. Query 페이징/필터/검색/정렬 (10개) - 25분
2. Command 중복 검증 및 엣지 케이스 (8개) - 20분
3. 통합 플로우 복합 시나리오 (3개) - 15분

**예상 소요 시간**: 약 60분

### Phase 3: P2 시나리오 (선택, 8개)

추가 검증 및 최적화 확인

**실행 순서**:
1. Query 엣지 케이스 (2개) - 5분
2. Command 길이 제한 검증 (6개) - 15분

**예상 소요 시간**: 약 20분

---

## 8. 다음 단계

### 8.1. E2E 테스트 구현 명령어

```bash
# Phase 1: P0 시나리오 구현
/test-e2e admin:commoncodetype --priority P0

# Phase 2: P1 시나리오 구현
/test-e2e admin:commoncodetype --priority P1

# Phase 3: P2 시나리오 구현
/test-e2e admin:commoncodetype --priority P2

# 전체 시나리오 구현
/test-e2e admin:commoncodetype --all
```

### 8.2. 테스트 실행

```bash
# 전체 E2E 테스트 실행
./gradlew :integration-test:test --tests "*CommonCodeType*E2ETest"

# Query 테스트만 실행
./gradlew :integration-test:test --tests "*CommonCodeTypeQueryE2ETest"

# Command 테스트만 실행
./gradlew :integration-test:test --tests "*CommonCodeTypeCommandE2ETest"

# 통합 플로우 테스트만 실행
./gradlew :integration-test:test --tests "*CommonCodeTypeFlowE2ETest"
```

---

## 9. 참고 사항

### 9.1. 비즈니스 규칙 요약

1. **코드 불변성**: `code`는 생성 후 수정 불가
2. **displayOrder 유일성**: 각 공통 코드 타입의 `displayOrder`는 유일해야 함 (수정 시 자기 자신 제외)
3. **활성 공통 코드 제약**: 활성화된 하위 공통 코드가 있으면 타입 비활성화 불가
4. **일괄 처리 원자성**: 일괄 상태 변경 시 트랜잭션 내에서 모두 성공 or 모두 실패
5. **동일 시간 적용**: 일괄 상태 변경 시 모든 항목에 동일한 `updatedAt` 적용

### 9.2. 에러 코드 매핑

| 에러 코드 | HTTP Status | 메시지 | 발생 시점 |
|----------|-------------|--------|----------|
| CCT-001 | 404 | 공통 코드 타입을 찾을 수 없습니다 | 존재하지 않는 ID 조회/수정/삭제 |
| CCT-002 | 409 | 동일한 코드가 이미 존재합니다 | 중복 코드 등록 |
| CCT-003 | 400 | 동일한 표시 순서가 이미 존재합니다 | displayOrder 중복 (수정 시) |
| CCT-004 | 400 | 활성화된 공통 코드가 존재하여 비활성화할 수 없습니다 | 하위 활성 공통 코드 존재 시 비활성화 시도 |

### 9.3. 인증/인가 규칙 요약

| 엔드포인트 | @PreAuthorize | 필요 권한 | 비인증 접근 |
|-----------|---------------|----------|------------|
| GET (검색) | 없음 | 불필요 | 가능 |
| GET (상세) | 없음 | 불필요 | 가능 |
| POST | `@access.superAdmin()` | SUPER_ADMIN | 불가 (401) |
| PUT | `@access.superAdmin()` | SUPER_ADMIN | 불가 (401) |
| PATCH | `@access.superAdmin()` | SUPER_ADMIN | 불가 (401) |

### 9.4. 테스트 태그 활용 (선택)

```java
@Tag("e2e")
@Tag("commoncodetype")
@Tag("query")  // or "command", "flow"
@Tag("auth")   // 인증/인가 테스트
```

---

## 문서 정보

- **작성일**: 2026-02-06
- **최종 수정일**: 2026-02-06 (인증/인가 시나리오 추가)
- **버전**: 2.0
- **분석 대상**: CommonCodeType 도메인 E2E 통합 테스트
- **총 시나리오 수**: 62개 (P0: 33, P1: 21, P2: 8)
- **예상 구현 시간**: P0 81분, P1 60분, P2 20분, 총 161분 (약 2.7시간)
- **신규 추가**: 인증/인가 시나리오 7개 (모두 P0)
