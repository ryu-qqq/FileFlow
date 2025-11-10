---
description: Layer별 프롬프트를 LangFuse에 등록하여 버전 관리 및 효과 측정
tags: [project]
---

# LangFuse Register Prompt - Prompt Version Management

당신은 Layer별 프롬프트를 LangFuse에 등록하여 버전 관리 및 A/B/C/D 테스트를 준비하는 작업을 수행합니다.

## 목적

1. **프롬프트 버전 관리**: Layer별 프롬프트를 v1.0, v1.1 등으로 버전 관리
2. **메타데이터 저장**: Zero-Tolerance 규칙, Layer 정보 등을 LangFuse에 저장
3. **A/B/C/D 테스트 준비**: 여러 프롬프트 버전을 등록하여 효과 비교 준비

## 입력 형식

사용자는 다음과 같이 Layer와 버전을 제공합니다:

```bash
/langfuse-register-prompt domain v1.0
/langfuse-register-prompt application v1.1
/langfuse-register-prompt all v1.0
```

## 실행 단계

### 1. 환경 변수 확인

**필수 환경 변수**:
```bash
LANGFUSE_PUBLIC_KEY="pk-lf-..."
LANGFUSE_SECRET_KEY="sk-lf-..."
LANGFUSE_HOST="https://us.cloud.langfuse.com"
```

**확인 방법**:
```bash
if [ -z "$LANGFUSE_PUBLIC_KEY" ]; then
  echo "❌ LANGFUSE_PUBLIC_KEY 환경 변수가 설정되지 않았습니다."
  echo "설정 방법: export LANGFUSE_PUBLIC_KEY='pk-lf-...'"
  exit 1
fi
```

### 2. Layer별 프롬프트 정의

#### 2.1 Domain Layer 프롬프트 (v1.0)

**파일**: `.claude/prompts/domain-layer-v1.0.md`

```markdown
# Domain Layer Code Generation Prompt (v1.0)

당신은 Spring DDD Domain Layer 전문가입니다.

## Zero-Tolerance 규칙 (필수)

- ✅ **Lombok 금지**: Pure Java 또는 Record 패턴 사용
- ✅ **Law of Demeter**: Getter 체이닝 금지 (`order.getCustomer().getAddress()` ❌)
- ✅ **Long FK 전략**: JPA 관계 어노테이션 금지 (`@ManyToOne`, `@OneToMany` 등)
- ✅ **Tell, Don't Ask**: 비즈니스 메서드로 상태 변경

## 코드 생성 템플릿

### Aggregate
\```java
public class {AggregateName}Domain {
    private final Long {aggregate}Id;
    private final String name;
    // ... 필드 선언 (final 키워드 사용)

    // Private Constructor
    private {AggregateName}Domain(...) {
        // Validation
        // Field initialization
    }

    // Factory Method
    public static {AggregateName}Domain create(...) {
        return new {AggregateName}Domain(...);
    }

    // Business Methods (Tell, Don't Ask)
    public void executeBusinessLogic() {
        // 비즈니스 규칙 구현
    }

    // Getters (Pure Java, no Lombok)
    public Long get{Aggregate}Id() { return {aggregate}Id; }
    // ...
}
\```

### Value Object (Record)
\```java
public record Email(String value) {
    public Email {
        if (value == null || value.isBlank()) {
            throw new InvalidEmailException("Email cannot be empty");
        }
        // Validation logic
    }
}
\```

## 검증 체크리스트

- [ ] Lombok 어노테이션 미사용
- [ ] Getter 체이닝 없음
- [ ] JPA 관계 어노테이션 없음
- [ ] 비즈니스 메서드 구현
- [ ] Value Object는 Record 패턴
```

#### 2.2 Application Layer 프롬프트 (v1.0)

**파일**: `.claude/prompts/application-layer-v1.0.md`

```markdown
# Application Layer Code Generation Prompt (v1.0)

당신은 Spring Application Layer 전문가입니다.

## Zero-Tolerance 규칙 (필수)

- ✅ **Command/Query 분리**: CQRS 패턴 준수
- ✅ **Transaction 경계**: `@Transactional` 내 외부 API 호출 금지
- ✅ **UseCase 단일 책임**: 하나의 UseCase는 하나의 비즈니스 기능만

## 코드 생성 템플릿

### Command UseCase
\```java
@UseCase
@Transactional
public class {Command}UseCase implements {Command}Port {

    private final {Domain}Repository repository;

    @Override
    public {Response} execute({Command}Command command) {
        // 1. Validation
        // 2. Domain 조회/생성
        // 3. 비즈니스 로직 실행
        // 4. Repository 저장
        // 5. Response 반환
    }

    // 외부 API 호출은 트랜잭션 밖에서
    private void callExternalApi() {
        // @Async 또는 @TransactionalEventListener 사용
    }
}
\```

### Query UseCase
\```java
@UseCase
@Transactional(readOnly = true)
public class {Query}UseCase implements {Query}Port {

    private final {Domain}Repository repository;

    @Override
    public {Response} execute({Query}Query query) {
        // 1. Repository 조회
        // 2. DTO 변환
        // 3. Response 반환
    }
}
\```

## 검증 체크리스트

- [ ] Command/Query 분리
- [ ] `@Transactional` 내 외부 API 호출 없음
- [ ] UseCase는 Port 인터페이스 구현
- [ ] DTO는 record 패턴
```

#### 2.3 Persistence Layer 프롬프트 (v1.0)

**파일**: `.claude/prompts/persistence-layer-v1.0.md`

```markdown
# Persistence Layer Code Generation Prompt (v1.0)

당신은 Spring Persistence Layer 전문가입니다.

## Zero-Tolerance 규칙 (필수)

- ✅ **Long FK 전략**: JPA 관계 어노테이션 금지
- ✅ **QueryDSL 최적화**: N+1 방지, Fetch Join 활용
- ✅ **Entity-Domain 분리**: Entity는 Persistence 전용

## 코드 생성 템플릿

### JPA Entity
\```java
@Entity
@Table(name = "{table_name}")
public class {Entity}Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "{entity}_id")
    private Long id;

    // Long FK (관계 어노테이션 금지)
    @Column(name = "customer_id")
    private Long customerId;

    // ... 필드 선언

    // Constructor, Getters, Setters (Pure Java)
}
\```

### Repository
\```java
public interface {Entity}Repository extends JpaRepository<{Entity}Entity, Long> {
    Optional<{Entity}Entity> findById(Long id);
    List<{Entity}Entity> findByCustomerId(Long customerId);
}
\```

### QueryDSL
\```java
@Repository
@RequiredArgsConstructor
public class {Entity}QueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<{Entity}Entity> findWithOptimization() {
        return queryFactory
            .selectFrom(q{Entity})
            .join(...).fetchJoin() // N+1 방지
            .fetch();
    }
}
\```

## 검증 체크리스트

- [ ] JPA 관계 어노테이션 없음 (`@ManyToOne`, `@OneToMany` 등)
- [ ] Long FK 사용
- [ ] QueryDSL Fetch Join 활용
- [ ] N+1 문제 없음
```

#### 2.4 REST API Layer 프롬프트 (v1.0)

**파일**: `.claude/prompts/adapter-rest-v1.0.md`

```markdown
# REST API Layer Code Generation Prompt (v1.0)

당신은 Spring REST API Layer 전문가입니다.

## Zero-Tolerance 규칙 (필수)

- ✅ **RESTful 설계**: HTTP Method, Status Code 올바른 사용
- ✅ **일관된 Error Response**: 모든 에러는 동일한 형식
- ✅ **Validation**: `@Valid`, `@NotNull` 등 적극 활용

## 코드 생성 템플릿

### Controller
\```java
@RestController
@RequestMapping("/api/v1/{resource}")
@RequiredArgsConstructor
public class {Resource}Controller {

    private final {Command}Port commandPort;
    private final {Query}Port queryPort;

    @PostMapping
    public ResponseEntity<{Response}> create(
        @Valid @RequestBody {Request} request
    ) {
        {Response} response = commandPort.execute(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<{Response}> getById(@PathVariable Long id) {
        {Response} response = queryPort.execute(new {Query}Query(id));
        return ResponseEntity.ok(response);
    }
}
\```

### Request/Response DTO
\```java
public record {Request}(
    @NotNull String name,
    @Min(0) Integer quantity
) {
    public {Command}Command toCommand() {
        return new {Command}Command(name, quantity);
    }
}

public record {Response}(
    Long id,
    String name,
    Integer quantity
) {}
\```

### Global Exception Handler
\```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException e
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getErrorCode(), e.getMessage()));
    }
}
\```

## 검증 체크리스트

- [ ] RESTful 설계 (POST → 201, GET → 200)
- [ ] `@Valid` Validation 적용
- [ ] 일관된 Error Response
- [ ] DTO는 record 패턴
```

### 3. LangFuse API 호출

**Python 스크립트**: `.claude/commands/lib/register_prompt_to_langfuse.py`

```python
#!/usr/bin/env python3
import os
import sys
import json
import requests
from datetime import datetime

def register_prompt(layer, version):
    """
    Layer별 프롬프트를 LangFuse에 등록

    Args:
        layer: domain, application, persistence, adapter-rest
        version: v1.0, v1.1 등
    """

    # 환경 변수 확인
    public_key = os.getenv("LANGFUSE_PUBLIC_KEY")
    secret_key = os.getenv("LANGFUSE_SECRET_KEY")
    host = os.getenv("LANGFUSE_HOST", "https://us.cloud.langfuse.com")

    if not public_key or not secret_key:
        print("❌ LangFuse 환경 변수가 설정되지 않았습니다.")
        print("설정 방법:")
        print("  export LANGFUSE_PUBLIC_KEY='pk-lf-...'")
        print("  export LANGFUSE_SECRET_KEY='sk-lf-...'")
        sys.exit(1)

    # 프롬프트 파일 읽기
    prompt_file = f".claude/prompts/{layer}-layer-{version}.md"
    if not os.path.exists(prompt_file):
        print(f"❌ 프롬프트 파일이 없습니다: {prompt_file}")
        sys.exit(1)

    with open(prompt_file, "r") as f:
        prompt_content = f.read()

    # LangFuse API 요청
    url = f"{host}/api/public/v2/prompts"
    headers = {
        "Content-Type": "application/json"
    }
    auth = (public_key, secret_key)

    data = {
        "name": f"{layer}-layer-prompt",
        "prompt": prompt_content,
        "config": {
            "model": "claude-sonnet-4-5",
            "temperature": 0.7,
            "max_tokens": 8000
        },
        "labels": [layer, version, "zero-tolerance"],
        "tags": [f"layer:{layer}", f"version:{version}"]
    }

    response = requests.post(url, headers=headers, auth=auth, json=data)

    if response.status_code == 200:
        print(f"✅ 프롬프트 등록 완료: {layer} {version}")
        print(f"   Prompt ID: {response.json().get('id')}")
    else:
        print(f"❌ 프롬프트 등록 실패: {response.status_code}")
        print(f"   Error: {response.text}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("사용법: python3 register_prompt_to_langfuse.py <layer> <version>")
        print("예시: python3 register_prompt_to_langfuse.py domain v1.0")
        sys.exit(1)

    layer = sys.argv[1]
    version = sys.argv[2]

    if layer == "all":
        # 모든 Layer 등록
        layers = ["domain", "application", "persistence", "adapter-rest"]
        for l in layers:
            register_prompt(l, version)
    else:
        register_prompt(layer, version)
```

### 4. 출력 형식

```markdown
✅ LangFuse 프롬프트 등록 완료!

**Layer**: {layer}
**Version**: {version}
**Prompt ID**: lf-prompt-{id}

**등록된 내용**:
- Zero-Tolerance 규칙: {규칙 개수}개
- 코드 템플릿: {템플릿 개수}개
- 검증 체크리스트: {체크리스트 항목}개

**LangFuse 대시보드**: {host}/prompts/{id}

**다음 단계**:
1. `/abcd-test {layer} {version}` - A/B/C/D 테스트 실행
2. `/langfuse-analyze {layer} {version}` - 결과 분석
```

## 사용 예시

```bash
# Domain Layer v1.0 등록
/langfuse-register-prompt domain v1.0

# Application Layer v1.1 등록 (개선 버전)
/langfuse-register-prompt application v1.1

# 모든 Layer v1.0 등록
/langfuse-register-prompt all v1.0
```

## 에러 처리

- **환경 변수 없음**: 설정 방법 안내 및 종료
- **프롬프트 파일 없음**: 파일 생성 안내
- **LangFuse API 실패**: 에러 메시지 및 재시도 안내

## 고급 기능

### 1. 프롬프트 버전 비교

**목적**: v1.0 vs v1.1 차이점 분석

**구현**:
```python
def compare_prompts(layer, version1, version2):
    # LangFuse API에서 두 버전 조회
    # Diff 알고리즘으로 차이점 분석
    # 개선 사항 요약
```

### 2. 자동 버전 증가

**목적**: v1.0 → v1.1 자동 증가

**구현**:
```python
def auto_increment_version(layer):
    # LangFuse에서 최신 버전 조회
    # 버전 번호 자동 증가
    # 새 버전 등록
```

### 3. 프롬프트 템플릿 생성

**목적**: Layer별 프롬프트 파일 자동 생성

**구현**:
```bash
# 템플릿에서 프롬프트 파일 생성
python3 generate_prompt_template.py domain v1.0
```

## 참고 문서

- [LangFuse Prompt Management](https://langfuse.com/docs/prompts)
- [TDD_LANGFUSE_SYSTEM_DESIGN.md](../../langfuse/TDD_LANGFUSE_SYSTEM_DESIGN.md)
- [COMMAND_PRIORITY.md](../../langfuse/COMMAND_PRIORITY.md)
