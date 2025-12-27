# Integration Test Fixture 패턴

> **목적**: 통합 테스트용 API Request/Response Fixture 작성 가이드

---

## 1️⃣ Fixture 계층 구조

### 단위 테스트 Fixture vs 통합 테스트 Fixture

```
┌─────────────────────────────────────────────────────────────┐
│                    Fixture 계층 구조                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  domain/src/testFixtures/                                   │
│  └── OrderFixture.java          ← Domain 객체 (Order)       │
│                                                             │
│  application/src/testFixtures/                              │
│  └── PlaceOrderCommandFixture   ← Application DTO           │
│                                                             │
│  integration-test/src/test/.../fixture/                     │
│  └── OrderIntegrationTestFixture ← API Request/Response    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 역할 분리

| Fixture 위치 | 역할 | 예시 |
|-------------|------|------|
| **domain testFixtures** | Domain 객체 생성 | `Order`, `Product`, `Category` |
| **application testFixtures** | Command/Query DTO | `PlaceOrderCommand` |
| **integration-test fixture** | API Request/Response | `CreateOrderApiRequest` |

---

## 2️⃣ Integration Test Fixture 구조

### 디렉토리 레이아웃

```
integration-test/src/test/java/
└── com/ryuqq/setof/integration/
    ├── product/
    │   ├── ProductCrudIntegrationTest.java
    │   └── fixture/
    │       └── ProductIntegrationTestFixture.java
    ├── order/
    │   ├── OrderFlowIntegrationTest.java
    │   └── fixture/
    │       └── OrderIntegrationTestFixture.java
    └── category/
        ├── CategoryCrudIntegrationTest.java
        ├── CategoryTreeIntegrationTest.java
        └── fixture/
            └── CategoryIntegrationTestFixture.java
```

---

## 3️⃣ Fixture 클래스 작성 규칙

### 기본 템플릿

```java
package com.ryuqq.setof.integration.category.fixture;

import com.ryuqq.setof.adapter.in.rest.v1.category.dto.*;

/**
 * 카테고리 통합 테스트 Fixture
 *
 * <p>API Request/Response 객체 생성 유틸리티</p>
 *
 * @author Development Team
 * @since 1.0.0
 */
public final class CategoryIntegrationTestFixture {

    // ========================================
    // 인스턴스 생성 방지
    // ========================================
    private CategoryIntegrationTestFixture() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    // ========================================
    // 루트 카테고리 생성 요청
    // ========================================
    public static CreateCategoryApiRequest createRootCategoryRequest() {
        return createRootCategoryRequest("FASHION", "패션", "Fashion");
    }

    public static CreateCategoryApiRequest createRootCategoryRequest(
            String code,
            String nameKo,
            String nameEn) {
        return new CreateCategoryApiRequest(
            null,           // parentId (루트이므로 null)
            code,
            nameKo,
            nameEn,
            0,              // sortOrder
            true,           // isListable
            true,           // isVisible
            "FASHION",      // department
            "ETC",          // categoryGroup
            "UNISEX",       // genderScope
            "ALL",          // ageGroup
            nameKo,         // displayName
            code.toLowerCase(), // seoSlug
            null            // iconUrl
        );
    }

    // ========================================
    // 자식 카테고리 생성 요청
    // ========================================
    public static CreateCategoryApiRequest createChildCategoryRequest(Long parentId) {
        return createChildCategoryRequest(parentId, "APPAREL", "의류", "Apparel");
    }

    public static CreateCategoryApiRequest createChildCategoryRequest(
            Long parentId,
            String code,
            String nameKo,
            String nameEn) {
        return new CreateCategoryApiRequest(
            parentId,
            code,
            nameKo,
            nameEn,
            0,              // sortOrder
            true,           // isListable
            true,           // isVisible
            "FASHION",
            "APPAREL",
            "UNISEX",
            "ALL",
            nameKo,
            code.toLowerCase(),
            null
        );
    }

    // ========================================
    // 수정 요청
    // ========================================
    public static UpdateCategoryApiRequest updateCategoryRequest() {
        return new UpdateCategoryApiRequest(
            "패션 업데이트",       // nameKo
            "Fashion Updated",   // nameEn
            null,                // isListable (변경 안함)
            null,                // isVisible (변경 안함)
            1,                   // sortOrder
            null,                // genderScope
            null,                // ageGroup
            "패션 업데이트",       // displayName
            null,                // seoSlug
            "https://cdn.example.com/icon.png"
        );
    }

    // ========================================
    // 상태 변경 요청
    // ========================================
    public static ChangeCategoryStatusApiRequest deactivateRequest() {
        return new ChangeCategoryStatusApiRequest("INACTIVE", null);
    }

    public static ChangeCategoryStatusApiRequest activateRequest() {
        return new ChangeCategoryStatusApiRequest("ACTIVE", null);
    }

    // ========================================
    // 카테고리 이동 요청
    // ========================================
    public static MoveCategoryApiRequest moveToRootRequest() {
        return new MoveCategoryApiRequest(null, 0);
    }

    public static MoveCategoryApiRequest moveToParentRequest(Long newParentId) {
        return new MoveCategoryApiRequest(newParentId, 0);
    }

    public static MoveCategoryApiRequest moveWithSortOrder(Long newParentId, int sortOrder) {
        return new MoveCategoryApiRequest(newParentId, sortOrder);
    }

    // ========================================
    // 비노출 카테고리 (테스트용)
    // ========================================
    public static CreateCategoryApiRequest createInvisibleCategoryRequest(
            Long parentId,
            String code) {
        return new CreateCategoryApiRequest(
            parentId,
            code,
            code + " 한글",
            code + " English",
            99,             // sortOrder (높은 순서)
            false,          // isListable (상품 등록 불가)
            false,          // isVisible (비노출)
            "FASHION",
            "ETC",
            "UNISEX",
            "ALL",
            code,
            code.toLowerCase(),
            null
        );
    }
}
```

---

## 4️⃣ 다양한 Fixture 예제

### 상품 Fixture

```java
public final class ProductIntegrationTestFixture {

    private ProductIntegrationTestFixture() {
        throw new AssertionError("Utility class");
    }

    // ========================================
    // 기본 상품 생성
    // ========================================
    public static CreateProductApiRequest createProductRequest() {
        return createProductRequest("TEST-PRODUCT-001", "테스트 상품", 50000);
    }

    public static CreateProductApiRequest createProductRequest(
            String code,
            String name,
            int price) {
        return new CreateProductApiRequest(
            code,
            name,
            name,                    // nameEn
            "상품 설명입니다.",         // description
            BigDecimal.valueOf(price),
            BigDecimal.valueOf(price * 1.1), // regularPrice
            1L,                      // brandId
            1L,                      // categoryId
            1L,                      // sellerId
            List.of(                 // options
                createOptionRequest("SIZE", "M"),
                createOptionRequest("SIZE", "L")
            ),
            List.of(                 // images
                createImageRequest("MAIN", "https://cdn.example.com/main.jpg"),
                createImageRequest("SUB", "https://cdn.example.com/sub.jpg")
            )
        );
    }

    // ========================================
    // 옵션 생성
    // ========================================
    public static CreateProductOptionApiRequest createOptionRequest(
            String optionName,
            String optionValue) {
        return new CreateProductOptionApiRequest(
            optionName,
            optionValue,
            100,                     // stock
            BigDecimal.ZERO,         // additionalPrice
            "SKU-" + optionValue
        );
    }

    // ========================================
    // 이미지 생성
    // ========================================
    public static CreateProductImageApiRequest createImageRequest(
            String imageType,
            String imageUrl) {
        return new CreateProductImageApiRequest(
            imageType,
            imageUrl,
            0                        // sortOrder
        );
    }

    // ========================================
    // 품절 상품
    // ========================================
    public static CreateProductApiRequest createOutOfStockProductRequest() {
        var request = createProductRequest();
        // 재고 0인 옵션으로 대체
        return new CreateProductApiRequest(
            request.code(),
            request.name(),
            request.nameEn(),
            request.description(),
            request.price(),
            request.regularPrice(),
            request.brandId(),
            request.categoryId(),
            request.sellerId(),
            List.of(createOptionRequest("SIZE", "M").withStock(0)),
            request.images()
        );
    }

    // ========================================
    // 럭셔리 상품 (고가)
    // ========================================
    public static CreateProductApiRequest createLuxuryProductRequest() {
        return createProductRequest("LUXURY-001", "럭셔리 상품", 5000000);
    }
}
```

### 주문 Fixture

```java
public final class OrderIntegrationTestFixture {

    private OrderIntegrationTestFixture() {
        throw new AssertionError("Utility class");
    }

    // ========================================
    // 기본 주문 생성
    // ========================================
    public static CreateOrderApiRequest createOrderRequest() {
        return createOrderRequest(1L, List.of(
            createOrderItemRequest(1L, 2),
            createOrderItemRequest(2L, 1)
        ));
    }

    public static CreateOrderApiRequest createOrderRequest(
            Long memberId,
            List<CreateOrderItemApiRequest> items) {
        return new CreateOrderApiRequest(
            memberId,
            items,
            createShippingAddressRequest(),
            "CARD",                  // paymentMethod
            null                     // couponId
        );
    }

    // ========================================
    // 주문 아이템
    // ========================================
    public static CreateOrderItemApiRequest createOrderItemRequest(
            Long productId,
            int quantity) {
        return new CreateOrderItemApiRequest(
            productId,
            quantity,
            BigDecimal.valueOf(50000)
        );
    }

    // ========================================
    // 배송 주소
    // ========================================
    public static ShippingAddressApiRequest createShippingAddressRequest() {
        return new ShippingAddressApiRequest(
            "홍길동",
            "010-1234-5678",
            "서울시 강남구 테헤란로 123",
            "456호",
            "06234"
        );
    }

    // ========================================
    // 쿠폰 적용 주문
    // ========================================
    public static CreateOrderApiRequest createOrderWithCouponRequest(Long couponId) {
        return new CreateOrderApiRequest(
            1L,
            List.of(createOrderItemRequest(1L, 1)),
            createShippingAddressRequest(),
            "CARD",
            couponId
        );
    }

    // ========================================
    // 주문 취소 요청
    // ========================================
    public static CancelOrderApiRequest cancelOrderRequest() {
        return cancelOrderRequest("단순 변심");
    }

    public static CancelOrderApiRequest cancelOrderRequest(String reason) {
        return new CancelOrderApiRequest(reason);
    }
}
```

---

## 5️⃣ Fixture 네이밍 컨벤션

### 메서드 네이밍 규칙

| 패턴 | 용도 | 예시 |
|------|------|------|
| `create*Request()` | 생성 API 요청 | `createProductRequest()` |
| `update*Request()` | 수정 API 요청 | `updateCategoryRequest()` |
| `default*()` | 기본값 객체 | `defaultShippingAddress()` |
| `*With*()` | 특정 속성 지정 | `createProductWithPrice(100000)` |
| `invalid*()` | 유효하지 않은 객체 | `invalidOrderRequest()` (검증 테스트용) |
| `empty*()` | 빈 객체 | `emptyOrderItemList()` |

### 클래스 네이밍

```
{Domain}IntegrationTestFixture.java

예시:
- CategoryIntegrationTestFixture.java
- ProductIntegrationTestFixture.java
- OrderIntegrationTestFixture.java
- BrandIntegrationTestFixture.java
```

---

## 6️⃣ Domain Fixture 재사용

### testFixtures 의존성 활용

```java
// integration-test/build.gradle
dependencies {
    // Domain/Application Fixture 재사용
    testImplementation testFixtures(project(':domain'))
    testImplementation testFixtures(project(':application'))
}
```

### 사용 예시

```java
import com.ryuqq.setof.domain.order.OrderFixture;        // Domain Fixture
import com.ryuqq.setof.integration.order.fixture.*;      // Integration Fixture

class OrderFlowIntegrationTest {

    @Test
    void completeOrderFlow() {
        // Domain Fixture로 예상 결과 생성
        Order expectedOrder = OrderFixture.defaultNewOrder();

        // Integration Fixture로 API 요청 생성
        var request = OrderIntegrationTestFixture.createOrderRequest();

        // API 호출
        var response = restTemplate.postForEntity("/api/orders", request, OrderResponse.class);

        // 검증
        assertThat(response.getBody().status()).isEqualTo(expectedOrder.getStatus().name());
    }
}
```

---

## 7️⃣ 검증용 Fixture (Invalid Data)

### 검증 테스트용 잘못된 데이터

```java
public final class ValidationTestFixture {

    // ========================================
    // 필수 필드 누락
    // ========================================
    public static CreateProductApiRequest productWithoutName() {
        return new CreateProductApiRequest(
            "CODE-001",
            null,           // name 누락!
            null,
            "설명",
            BigDecimal.valueOf(10000),
            BigDecimal.valueOf(12000),
            1L, 1L, 1L,
            List.of(),
            List.of()
        );
    }

    // ========================================
    // 잘못된 형식
    // ========================================
    public static CreateProductApiRequest productWithInvalidPrice() {
        return new CreateProductApiRequest(
            "CODE-001",
            "상품명",
            "Product Name",
            "설명",
            BigDecimal.valueOf(-100),  // 음수 가격!
            BigDecimal.valueOf(12000),
            1L, 1L, 1L,
            List.of(),
            List.of()
        );
    }

    // ========================================
    // 중복 데이터 (두 번 호출용)
    // ========================================
    public static CreateBrandApiRequest duplicateBrandCodeRequest() {
        return new CreateBrandApiRequest(
            "DUPLICATE_CODE",    // 동일 코드로 두 번 생성 테스트
            "First Brand",
            "첫 번째 브랜드"
        );
    }
}
```

### 검증 테스트 예시

```java
@Test
@DisplayName("필수 필드 누락 시 400 BAD_REQUEST")
void createProduct_missingName_returns400() {
    // given
    var request = ValidationTestFixture.productWithoutName();

    // when
    ResponseEntity<ProblemDetail> response = restTemplate.exchange(
        baseUrl(),
        HttpMethod.POST,
        new HttpEntity<>(request),
        ProblemDetail.class
    );

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().getDetail()).contains("name");
}

@Test
@DisplayName("중복 코드 시 409 CONFLICT")
void createBrand_duplicateCode_returns409() {
    // given - 첫 번째 생성 (성공)
    var request = ValidationTestFixture.duplicateBrandCodeRequest();
    restTemplate.postForEntity(brandUrl(), request, ApiResponse.class);

    // when - 동일 코드로 두 번째 생성
    ResponseEntity<ProblemDetail> response = restTemplate.exchange(
        brandUrl(),
        HttpMethod.POST,
        new HttpEntity<>(request),
        ProblemDetail.class
    );

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
}
```

---

## 8️⃣ 체크리스트

### Fixture 클래스 작성
- [ ] `final class` 선언
- [ ] `private` 생성자 (인스턴스화 방지)
- [ ] `static` 메서드만 사용
- [ ] 명확한 메서드 네이밍 (`create*Request`, `update*Request`)

### 테스트 커버리지
- [ ] 기본 CRUD 요청 Fixture
- [ ] 수정/상태 변경 요청 Fixture
- [ ] 검증 실패용 Invalid Fixture
- [ ] 중복 체크용 Fixture

### 의존성
- [ ] `testFixtures(project(':domain'))` 활용
- [ ] `testFixtures(project(':application'))` 활용
- [ ] 불필요한 중복 방지

---

## 📖 관련 문서

- **[Test Fixtures Guide](../test-fixtures/01_test-fixtures-guide.md)** - Gradle testFixtures 기본 가이드
- **[Integration Test Module](./02_integration-test-module.md)** - 통합 테스트 모듈 구성
- **[External Service Mock](./03_external-service-mock.md)** - 외부 서비스 모킹

---

**작성자**: Development Team
**최종 수정일**: 2025-12-23
**버전**: 1.0.0
