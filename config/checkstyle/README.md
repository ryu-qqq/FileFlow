# 📐 Checkstyle 설정 가이드

## 개요

Checkstyle은 코드 스타일과 품질을 자동으로 검증하는 정적 분석 도구입니다.
이 프로젝트는 **Level 3 엄격 규칙**을 적용하여 모든 위반 사항을 빌드 실패로 처리합니다.

## 설정 파일

- **위치**: `config/checkstyle/checkstyle.xml`
- **적용 범위**: 모든 서브프로젝트의 Java 소스 코드
- **실행 시점**: 빌드 시 자동 실행 (`./gradlew build`)

## 주요 검증 규칙

### 1. Hexagonal Architecture 강제

#### JPA 연관관계 금지
```java
// ❌ PROHIBITED
@OneToMany
@ManyToOne
@OneToOne
@ManyToMany

// ✅ ALLOWED - 외래키는 Long 타입 필드로만
@Column(nullable = false)
private Long userId;
```

**이유**: JPA 연관관계는 암묵적 쿼리와 N+1 문제를 유발합니다. 명시적 외래키를 사용하여 쿼리를 투명하게 관리합니다.

#### Request/Response는 Record만 허용
```java
// ❌ PROHIBITED
public class CreateOrderRequest {
    private String userId;
}

// ✅ ALLOWED
public record CreateOrderRequest(
    @NotNull String userId
) {}
```

**이유**: Record는 불변성을 보장하고 boilerplate 코드를 제거합니다.

### 2. Lombok 완전 금지

```java
// ❌ STRICTLY PROHIBITED
import lombok.Data;
import lombok.Builder;
import lombok.Getter;
```

**이유**:
- 숨겨진 동작으로 디버깅 어려움
- IDE 의존성 증가
- 컴파일 시점 바이트코드 조작으로 예측 불가능한 동작

### 3. Javadoc 필수

#### Public API에 대한 완전한 문서화
```java
/**
 * 주문을 생성합니다.
 *
 * @param request 주문 생성 요청
 * @return 생성된 주문
 * @throws InvalidOrderException 주문 검증 실패 시
 * @author 홍길동 (hong.gildong@company.com)
 * @since 2024-01-01
 */
public Order createOrder(CreateOrderRequest request) {
    // implementation
}
```

**필수 요소**:
- `@author` 태그: 이름 + 이메일 형식 (`Name (email@company.com)`)
- `@param`, `@return`, `@throws`: 모든 파라미터와 예외 문서화
- `@since`: 최초 작성일

### 4. 코드 복잡도 제한

| 항목 | 최대값 | 위반 시 |
|------|--------|---------|
| Cyclomatic Complexity | 10 | 빌드 실패 |
| 메서드 길이 | 50줄 | 빌드 실패 |
| 파라미터 개수 | 5개 | 빌드 실패 |

**복잡도가 높을 때 해결 방법**:
- 메서드 분리 (Extract Method)
- Strategy 패턴 적용
- Command 객체로 파라미터 그룹화

### 5. Setter 메서드 금지

```java
// ❌ PROHIBITED
public void setStatus(String status) {
    this.status = status;
}

// ✅ ALLOWED - 새 객체 반환
public Order confirm() {
    return new Order(this.id, OrderStatus.CONFIRMED);
}
```

**이유**: 불변성을 보장하여 side-effect를 제거하고 thread-safety를 확보합니다.

## 실행 방법

### 전체 검사
```bash
./gradlew checkstyleMain
```

### 특정 모듈만 검사
```bash
./gradlew :domain:checkstyleMain
./gradlew :application:checkstyleMain
```

### 빌드 시 자동 실행
```bash
./gradlew build
# Checkstyle이 자동으로 실행되며, 위반 시 빌드 실패
```

## 리포트 확인

**HTML 리포트 위치**:
```
build/reports/checkstyle/main.html
```

각 모듈의 `build/reports/checkstyle/` 디렉토리에서 상세한 위반 내역을 확인할 수 있습니다.

## 설정 커스터마이징

### Checkstyle 버전 변경
`gradle/libs.versions.toml`:
```toml
[versions]
checkstyle = "10.12.5"  # 원하는 버전으로 변경
```

### 규칙 제외
특정 규칙을 제외하려면 `config/checkstyle/checkstyle.xml`에서 해당 모듈을 주석 처리:
```xml
<!--
<module name="CyclomaticComplexity">
    <property name="max" value="10"/>
</module>
-->
```

### 경고를 오류로 처리 비활성화 (권장하지 않음)
`build.gradle.kts`:
```kotlin
checkstyle {
    isIgnoreFailures = true  // ⚠️ 품질 저하 주의
}
```

## 문제 해결

### 자주 발생하는 위반

#### 1. Import 순서
```bash
Error: Wrong order for import
```

**해결**: IDE의 Import 자동 정렬 기능 사용 (IntelliJ: Ctrl+Alt+O)

#### 2. Javadoc 누락
```bash
Error: Missing a Javadoc comment
```

**해결**: Public 클래스/메서드에 Javadoc 추가

#### 3. 메서드 길이 초과
```bash
Error: Method length is 65 lines (max allowed is 50)
```

**해결**: Extract Method 리팩토링으로 메서드 분리

## 통합 도구

### IntelliJ IDEA
1. Settings → Editor → Code Style
2. Scheme → Import Scheme → Checkstyle Configuration
3. `config/checkstyle/checkstyle.xml` 선택

### VS Code
1. Extension: Checkstyle for Java 설치
2. Settings → Checkstyle Configuration Path
3. `config/checkstyle/checkstyle.xml` 설정

## 참고 자료

- [Checkstyle 공식 문서](https://checkstyle.org/)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [프로젝트 코딩 표준](../../docs/CODING_STANDARDS.md)
