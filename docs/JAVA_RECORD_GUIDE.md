# Java Record 사용 가이드

## 개요

Java 14에서 도입된 `record`는 불변 데이터 객체를 간결하게 정의할 수 있는 기능입니다.
DDD의 Value Object와 Domain Event처럼 불변성이 중요한 도메인 객체에 적합합니다.

## 문제점: 기존 final class 방식

### 보일러플레이트 과다

```java
public final class PolicyActivatedEvent {
    private final String policyKey;
    private final int version;
    private final String activatedBy;
    private final LocalDateTime activatedAt;

    public PolicyActivatedEvent(
            String policyKey,
            int version,
            String activatedBy,
            LocalDateTime activatedAt
    ) {
        this.policyKey = Objects.requireNonNull(policyKey);
        this.version = version;
        this.activatedBy = Objects.requireNonNull(activatedBy);
        this.activatedAt = Objects.requireNonNull(activatedAt);
        validateVersion();
    }

    private void validateVersion() {
        if (version < 0) {
            throw new IllegalArgumentException("version must not be negative");
        }
    }

    public String getPolicyKey() { return policyKey; }
    public int getVersion() { return version; }
    public String getActivatedBy() { return activatedBy; }
    public LocalDateTime getActivatedAt() { return activatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolicyActivatedEvent that = (PolicyActivatedEvent) o;
        return version == that.version &&
               Objects.equals(policyKey, that.policyKey) &&
               Objects.equals(activatedBy, that.activatedBy) &&
               Objects.equals(activatedAt, that.activatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyKey, version, activatedBy, activatedAt);
    }

    @Override
    public String toString() {
        return "PolicyActivatedEvent{" +
               "policyKey='" + policyKey + '\'' +
               ", version=" + version +
               ", activatedBy='" + activatedBy + '\'' +
               ", activatedAt=" + activatedAt +
               '}';
    }
}
```

### 주요 문제점

1. **과도한 보일러플레이트**: getter, equals, hashCode, toString 수동 구현
2. **가독성 저하**: 핵심 비즈니스 로직이 보일러플레이트에 묻힘
3. **유지보수 부담**: 필드 추가/수정 시 여러 메서드 동시 수정 필요
4. **실수 가능성**: equals/hashCode 구현 누락 또는 오류

## 해결책: Java Record

### 간결한 정의

```java
public record PolicyActivatedEvent(
    String policyKey,
    int version,
    String activatedBy,
    LocalDateTime activatedAt
) {
    // Compact Constructor: 검증 로직만 작성
    public PolicyActivatedEvent {
        Objects.requireNonNull(policyKey, "policyKey must not be null");
        Objects.requireNonNull(activatedBy, "activatedBy must not be null");
        Objects.requireNonNull(activatedAt, "activatedAt must not be null");

        if (version < 0) {
            throw new IllegalArgumentException("version must not be negative: " + version);
        }
    }
}
```

### Record의 자동 생성 요소

Record는 다음 요소들을 컴파일러가 자동으로 생성합니다:

1. **private final 필드**: 모든 컴포넌트에 대한 불변 필드
2. **public 생성자**: 모든 필드를 초기화하는 생성자
3. **public accessor**: `policyKey()`, `version()` 형태의 getter
4. **equals()**: 모든 필드 기반 동등성 비교
5. **hashCode()**: 모든 필드 기반 해시 코드
6. **toString()**: 모든 필드를 포함하는 문자열 표현

## Value Object 예시

### 1. 단순 Value Object

```java
public record Dimension(int width, int height) {
    public Dimension {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                "Dimensions must be positive: width=%d, height=%d".formatted(width, height)
            );
        }
    }

    public int area() {
        return width * height;
    }

    public Dimension scale(double factor) {
        return new Dimension(
            (int) (width * factor),
            (int) (height * factor)
        );
    }
}
```

### 2. 복합 Value Object

```java
public record RateLimiting(int requestsPerHour, int uploadsPerDay) {
    public RateLimiting {
        if (requestsPerHour <= 0) {
            throw new IllegalArgumentException(
                "requestsPerHour must be positive: " + requestsPerHour
            );
        }
        if (uploadsPerDay <= 0) {
            throw new IllegalArgumentException(
                "uploadsPerDay must be positive: " + uploadsPerDay
            );
        }
    }

    public boolean isAllowed(int currentRequests, int currentUploads) {
        return currentRequests < requestsPerHour && currentUploads < uploadsPerDay;
    }

    public RateLimiting increaseLimit(int additionalRequests, int additionalUploads) {
        return new RateLimiting(
            requestsPerHour + additionalRequests,
            uploadsPerDay + additionalUploads
        );
    }
}
```

### 3. 중첩 Value Object

```java
public record ImagePolicy(
    Dimension maxDimension,
    long maxFileSizeBytes,
    Set<String> allowedFormats
) {
    public ImagePolicy {
        Objects.requireNonNull(maxDimension, "maxDimension must not be null");
        Objects.requireNonNull(allowedFormats, "allowedFormats must not be null");

        if (maxFileSizeBytes <= 0) {
            throw new IllegalArgumentException("maxFileSizeBytes must be positive");
        }

        if (allowedFormats.isEmpty()) {
            throw new IllegalArgumentException("allowedFormats must not be empty");
        }

        // 방어적 복사
        allowedFormats = Set.copyOf(allowedFormats);
    }

    public boolean isValidImage(Dimension dimension, long fileSize, String format) {
        return dimension.width() <= maxDimension.width() &&
               dimension.height() <= maxDimension.height() &&
               fileSize <= maxFileSizeBytes &&
               allowedFormats.contains(format.toLowerCase());
    }
}
```

## Domain Event 예시

### 1. 단순 Event

```java
public record PolicyActivatedEvent(
    String policyKey,
    int version,
    String activatedBy,
    LocalDateTime activatedAt
) {
    public PolicyActivatedEvent {
        Objects.requireNonNull(policyKey, "policyKey must not be null");
        Objects.requireNonNull(activatedBy, "activatedBy must not be null");
        Objects.requireNonNull(activatedAt, "activatedAt must not be null");

        if (version < 0) {
            throw new IllegalArgumentException("version must not be negative");
        }
    }
}
```

### 2. 복합 Event

```java
public record PolicyUpdatedEvent(
    String policyKey,
    int oldVersion,
    int newVersion,
    Map<String, Object> changes,
    String changedBy,
    LocalDateTime changedAt
) {
    public PolicyUpdatedEvent {
        Objects.requireNonNull(policyKey, "policyKey must not be null");
        Objects.requireNonNull(changes, "changes must not be null");
        Objects.requireNonNull(changedBy, "changedBy must not be null");
        Objects.requireNonNull(changedAt, "changedAt must not be null");

        if (oldVersion < 0 || newVersion <= oldVersion) {
            throw new IllegalArgumentException(
                "Invalid version transition: %d -> %d".formatted(oldVersion, newVersion)
            );
        }

        if (changes.isEmpty()) {
            throw new IllegalArgumentException("changes must not be empty");
        }

        // 방어적 복사
        changes = Map.copyOf(changes);
    }

    public int versionDiff() {
        return newVersion - oldVersion;
    }
}
```

## 검증 로직 패턴

### 1. Compact Constructor

```java
public record Email(String value) {
    // 정규식은 static final로 선언
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        Objects.requireNonNull(value, "email must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("invalid email format: " + value);
        }

        // 정규화
        value = value.toLowerCase().trim();
    }
}
```

### 2. Static Factory Method

```java
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must not be negative: " + amount);
        }
    }

    // Static factory methods
    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public static Money of(long amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public static Money of(String amount, String currencyCode) {
        return new Money(
            new BigDecimal(amount),
            Currency.getInstance(currencyCode)
        );
    }

    // 비즈니스 메서드
    public Money add(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(amount.add(other.amount), currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(amount.multiply(factor), currency);
    }
}
```

### 3. 복잡한 검증

```java
public record DateRange(LocalDate startDate, LocalDate endDate) {
    public DateRange {
        Objects.requireNonNull(startDate, "startDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                "endDate must not be before startDate: %s -> %s"
                    .formatted(startDate, endDate)
            );
        }
    }

    public long days() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public boolean overlaps(DateRange other) {
        return !endDate.isBefore(other.startDate) &&
               !other.endDate.isBefore(startDate);
    }
}
```

## DTO (Application Layer) 예시

### Request DTO

```java
public record CreatePolicyRequest(
    String tenantId,
    String userType,
    String serviceType,
    LocalDateTime effectiveFrom,
    LocalDateTime effectiveUntil
) {
    public CreatePolicyRequest {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(userType, "userType must not be null");
        Objects.requireNonNull(serviceType, "serviceType must not be null");
        Objects.requireNonNull(effectiveFrom, "effectiveFrom must not be null");
        Objects.requireNonNull(effectiveUntil, "effectiveUntil must not be null");

        if (effectiveUntil.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException(
                "effectiveUntil must be after effectiveFrom"
            );
        }
    }
}
```

### Response DTO

```java
public record PolicyResponse(
    String policyKey,
    int version,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public PolicyResponse {
        Objects.requireNonNull(policyKey, "policyKey must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");

        if (version < 1) {
            throw new IllegalArgumentException("version must be positive");
        }
    }
}
```

## Record를 사용하지 않는 경우

다음 경우에는 일반 class를 사용합니다:

### 1. Aggregate Root

```java
// ❌ Record 사용 불가 - 가변 상태와 복잡한 행위 보유
public final class UploadPolicy {
    private final PolicyKey policyKey;
    private int version;
    private PolicyStatus status;
    private final FileTypePolicies fileTypePolicies;

    private UploadPolicy(PolicyKey policyKey, FileTypePolicies fileTypePolicies) {
        this.policyKey = policyKey;
        this.version = 1;
        this.status = PolicyStatus.DRAFT;
        this.fileTypePolicies = fileTypePolicies;
    }

    public static UploadPolicy create(PolicyKey policyKey, FileTypePolicies fileTypePolicies) {
        return new UploadPolicy(policyKey, fileTypePolicies);
    }

    public void activate(String activatedBy) {
        if (this.status == PolicyStatus.ACTIVE) {
            throw new IllegalStateException("Policy is already active");
        }
        this.status = PolicyStatus.ACTIVE;
        // 도메인 이벤트 발행 등...
    }

    public void updateVersion() {
        this.version++;
    }

    public PolicyStatus status() {
        return this.status;
    }
}
```

### 2. Domain Service

```java
// ❌ Record 사용 불가 - stateless 서비스
public final class PolicyDomainService {

    public boolean canActivatePolicy(UploadPolicy policy, User user) {
        // 복잡한 비즈니스 규칙 검증
        return user.hasPermission("ACTIVATE_POLICY") &&
               policy.status() == PolicyStatus.DRAFT;
    }

    public PolicyValidationResult validatePolicyConflicts(
            UploadPolicy newPolicy,
            List<UploadPolicy> existingPolicies
    ) {
        // 정책 충돌 검증 로직
        return PolicyValidationResult.valid();
    }
}
```

### 3. 복잡한 비즈니스 로직이 필요한 경우

```java
// ❌ Record 사용 불가 - 복잡한 계산 로직과 캐싱
public final class PricingCalculator {
    private final TaxPolicy taxPolicy;
    private final DiscountPolicy discountPolicy;
    private final Map<String, BigDecimal> priceCache;

    private PricingCalculator(TaxPolicy taxPolicy, DiscountPolicy discountPolicy) {
        this.taxPolicy = taxPolicy;
        this.discountPolicy = discountPolicy;
        this.priceCache = new HashMap<>();
    }

    public static PricingCalculator of(TaxPolicy taxPolicy, DiscountPolicy discountPolicy) {
        return new PricingCalculator(taxPolicy, discountPolicy);
    }

    public Money calculateFinalPrice(Money basePrice, Customer customer) {
        // 복잡한 가격 계산 로직
        String cacheKey = generateCacheKey(basePrice, customer);
        // 캐싱 로직...
        return cachedOrCalculate(cacheKey, basePrice, customer);
    }
}
```

## 마이그레이션 가이드

### Before: final class

```java
public final class RateLimiting {
    private final int requestsPerHour;
    private final int uploadsPerDay;

    public RateLimiting(int requestsPerHour, int uploadsPerDay) {
        if (requestsPerHour <= 0 || uploadsPerDay <= 0) {
            throw new IllegalArgumentException("Rate limits must be positive");
        }
        this.requestsPerHour = requestsPerHour;
        this.uploadsPerDay = uploadsPerDay;
    }

    public int getRequestsPerHour() { return requestsPerHour; }
    public int getUploadsPerDay() { return uploadsPerDay; }

    public boolean isAllowed(int currentRequests, int currentUploads) {
        return currentRequests < requestsPerHour && currentUploads < uploadsPerDay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RateLimiting that = (RateLimiting) o;
        return requestsPerHour == that.requestsPerHour &&
               uploadsPerDay == that.uploadsPerDay;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestsPerHour, uploadsPerDay);
    }

    @Override
    public String toString() {
        return "RateLimiting{requestsPerHour=" + requestsPerHour +
               ", uploadsPerDay=" + uploadsPerDay + '}';
    }
}
```

### After: record

```java
public record RateLimiting(int requestsPerHour, int uploadsPerDay) {
    public RateLimiting {
        if (requestsPerHour <= 0 || uploadsPerDay <= 0) {
            throw new IllegalArgumentException(
                "Rate limits must be positive: requests=%d, uploads=%d"
                    .formatted(requestsPerHour, uploadsPerDay)
            );
        }
    }

    public boolean isAllowed(int currentRequests, int currentUploads) {
        return currentRequests < requestsPerHour && currentUploads < uploadsPerDay;
    }
}
```

### 마이그레이션 체크리스트

1. **불변성 확인**: 모든 필드가 `private final`인가?
2. **setter 없음**: setter 메서드가 없는가?
3. **단순 데이터**: 복잡한 상태 관리나 캐싱이 없는가?
4. **검증 로직**: 생성자 검증 로직을 compact constructor로 이동
5. **비즈니스 메서드**: 기존 비즈니스 메서드를 record에 그대로 추가
6. **getter 호출 변경**: `getXxx()` → `xxx()`로 변경
7. **테스트 업데이트**: 생성자와 getter 호출 변경

## Record 제약사항

### 1. 상속 불가

```java
// ❌ 컴파일 에러 - record는 다른 클래스를 상속할 수 없음
public record SubRecord(String value) extends BaseClass {
}

// ✅ 인터페이스 구현은 가능
public record ValidRecord(String value) implements Serializable {
}
```

### 2. 필드 추가 불가

```java
// ❌ 컴파일 에러 - 추가 인스턴스 필드 선언 불가
public record InvalidRecord(String value) {
    private String additionalField; // 불가능
}

// ✅ static 필드는 가능
public record ValidRecord(String value) {
    private static final Pattern PATTERN = Pattern.compile("...");
}
```

### 3. final 클래스

```java
// Record는 암묵적으로 final
// 다른 클래스가 record를 상속할 수 없음
```

## 모범 사례

### 1. 비즈니스 의미 있는 이름 사용

```java
// ❌ 기술적 접미사
public record DimensionVO(int width, int height) {}
public record EmailVO(String value) {}

// ✅ 비즈니스 도메인 이름
public record Dimension(int width, int height) {}
public record Email(String value) {}
```

### 2. 방어적 복사

```java
public record Policy(
    String name,
    Set<String> allowedFormats,
    Map<String, Object> metadata
) {
    public Policy {
        Objects.requireNonNull(name);
        Objects.requireNonNull(allowedFormats);
        Objects.requireNonNull(metadata);

        // 가변 컬렉션은 방어적 복사
        allowedFormats = Set.copyOf(allowedFormats);
        metadata = Map.copyOf(metadata);
    }
}
```

### 3. 명확한 검증 메시지

```java
public record Dimension(int width, int height) {
    public Dimension {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                "Dimensions must be positive: width=%d, height=%d"
                    .formatted(width, height)  // 명확한 오류 메시지
            );
        }
    }
}
```

### 4. Static Factory Method 활용

```java
public record Money(BigDecimal amount, Currency currency) {
    // 다양한 생성 방법 제공
    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public static Money won(long amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance("KRW"));
    }

    public static Money usd(String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("USD"));
    }
}
```

## 참고 자료

- [JEP 395: Records](https://openjdk.org/jeps/395)
- [JEP 384: Records (Second Preview)](https://openjdk.org/jeps/384)
- [Effective Java 3rd Edition - Item 15: Minimize mutability](https://www.oreilly.com/library/view/effective-java/9780134686097/)
- [Domain-Driven Design Reference by Eric Evans](https://www.domainlanguage.com/ddd/reference/)
- Issue #2: Value Object와 Domain Event에 Java Record 사용 권장

## 관련 문서

- `CODING_STANDARDS.md`: 전체 코딩 표준
- `DDD_AGGREGATE_MIGRATION_GUIDE.md`: DDD Aggregate 패턴 가이드
- `CUSTOMIZATION_GUIDE.md`: 템플릿 커스터마이징 가이드
