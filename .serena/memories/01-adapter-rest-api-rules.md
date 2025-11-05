# REST API Layer 규칙 요약본 (2025-11-05)

> **용도**: `/cc:load` 초기 로딩용 REST API Layer 핵심 요약본
> **상세 규칙**: Hook이 자동으로 27개 Cache Rules를 실시간 주입 (O(1) 검색)

---

## 🚨 Zero-Tolerance (절대 금지)

- ❌ **Lombok 사용**: `@RequiredArgsConstructor`, `@Data` 등 모두 금지
- ❌ **Controller에 비즈니스 로직**: Controller는 Thin Layer (HTTP 처리만)
- ❌ **Port 직접 의존**: Controller → UseCase (Port Interface) 의존
- ❌ **Entity 직접 노출**: API Response에 JPA Entity 반환 금지
- ❌ **Exception을 Controller에서 처리**: GlobalExceptionHandler로 중앙 집중화

---

## ✅ 필수 규칙

### 1️⃣ Controller 설계 (Thin Layer)
- ✅ **Constructor Injection**: `@RequiredArgsConstructor` 대신 생성자 직접 작성
- ✅ **RESTful API 설계**: HTTP Method (GET/POST/PUT/DELETE) 의미론적 사용
- ✅ **Request Validation**: `@Valid` + `@Validated` 활용
- ✅ **Response Handling**: `ApiResponse<T>` 표준 포맷 사용

### 2️⃣ DTO 패턴 (API ↔ UseCase)
- ✅ **API Request DTO**: HTTP 요청 → API DTO (Controller)
- ✅ **API Response DTO**: UseCase 결과 → API DTO (Controller)
- ✅ **Error Response**: `ErrorResponse` 표준 포맷
- ✅ **Naming Convention**: `XxxApiRequest`, `XxxApiResponse`

### 3️⃣ Exception Handling (중앙 집중화)
- ✅ **GlobalExceptionHandler**: `@RestControllerAdvice`로 중앙 처리
- ✅ **Custom Error Codes**: `ErrorCode` Enum 정의
- ✅ **Validation Exception**: Bean Validation 예외 처리
- ✅ **Error Mapper Pattern**: Domain Exception → HTTP Status 매핑

### 4️⃣ Mapper 패턴 (API ↔ UseCase 변환)
- ✅ **ApiToUseCaseMapper**: API DTO → Command/Query DTO
- ✅ **Mapper Responsibility**: 단순 변환만 담당 (비즈니스 로직 금지)
- ✅ **Static Method**: Mapper는 Static Factory Method 패턴 사용

### 5️⃣ Configuration (Swagger/OpenAPI)
- ✅ **Swagger 설정**: OpenAPI 3.0 기반 API 문서 자동 생성
- ✅ **Security Schema**: JWT, OAuth2 인증 설정
- ✅ **Tag & Operation**: Controller별 Tag, Operation 명시

### 6️⃣ Resources (application.yml, i18n)
- ✅ **application.yml**: 환경별 설정 분리 (dev, prod)
- ✅ **Message i18n**: `messages.properties`로 다국어 지원

### 7️⃣ Testing (ArchUnit, Integration, Unit)
- ✅ **ArchUnit Test**: Controller 레이어 의존성 검증
- ✅ **Controller Unit Test**: `@WebMvcTest`로 단위 테스트
- ✅ **Integration Test**: `@SpringBootTest`로 통합 테스트
- ✅ **REST Docs**: Spring REST Docs로 API 문서 자동 생성

---

## 📊 레이어 통계

- **총 규칙 수**: 27개
- **Zero-Tolerance**: 5개
- **필수 규칙**: 22개
- **Cache Rules**: 27개 (Hook 자동 주입)

---

## 🔗 상세 문서

**Hook이 자동으로 주입하는 Cache Rules (27개)**:

### Controller Design
- `01_restful-api-design.md` - RESTful API 설계 원칙
- `02_constructor-injection-pattern.md` - 생성자 주입 패턴
- `02_request-validation.md` - 요청 검증 규칙
- `03_response-handling.md` - 응답 처리 규칙

### DTO Patterns
- `01_api-request-dto.md` - API Request DTO 설계
- `02_api-response-dto.md` - API Response DTO 설계
- `03_error-response.md` - Error Response 표준 포맷
- `03_naming-conventions.md` - DTO 네이밍 규칙

### Exception Handling
- `01_global-exception-handler.md` - 전역 예외 처리
- `02_custom-error-codes.md` - 커스텀 에러 코드
- `03_error-mapper-pattern.md` - 에러 매퍼 패턴
- `03_validation-exception.md` - Validation 예외 처리

### Mapper Patterns
- `01_api-to-usecase-mapper.md` - API → UseCase 매퍼
- `02_mapper-responsibility.md` - 매퍼 책임 분리

### Config
- `01_swagger-openapi-guide.md` - Swagger/OpenAPI 설정

### Resources
- `01_application-yml-guide.md` - application.yml 설정
- `02_message-i18n-guide.md` - 다국어 메시지 설정

### Testing
- `01_archunit-test-guide.md` - ArchUnit 테스트 가이드
- `01_controller-unit-test.md` - Controller 단위 테스트
- `01_openapi-swagger-conventions.md` - OpenAPI 규칙
- `02_integration-test-guide.md` - 통합 테스트 가이드
- `02_integration-test.md` - 통합 테스트 작성
- `03_rest-docs.md` - Spring REST Docs
- `03_unit-test-guide.md` - 단위 테스트 가이드
- `04_rest-docs-guide.md` - REST Docs 상세 가이드

**완전한 규칙은 Hook 시스템이 실시간으로 제공합니다!**

---

## 🎯 핵심 패턴

### Controller 계층 구조
```
HTTP Request
    ↓
Controller (Thin Layer)
    ├─ @RestController
    ├─ Constructor Injection (Pure Java)
    └─ ApiToUseCaseMapper
        ↓
UseCase (Port Interface)
```

### DTO 변환 흐름
```
API Request DTO → ApiToUseCaseMapper → Command/Query DTO
    ↓
UseCase 실행
    ↓
Result DTO → ApiToUseCaseMapper → API Response DTO
```

### Exception 처리 흐름
```
Domain Exception
    ↓
GlobalExceptionHandler (@RestControllerAdvice)
    ├─ Error Mapper (Domain Exception → HTTP Status)
    └─ ErrorResponse (표준 포맷)
        ↓
HTTP Response (4xx/5xx + Error Body)
```

---

**✅ 이 요약본은 REST API Layer 27개 규칙의 핵심만 포함합니다.**

**🔥 상세 규칙은 Hook이 키워드 감지 시 자동으로 주입합니다!**
