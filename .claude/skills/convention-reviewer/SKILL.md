---
name: "convention-reviewer"
description: "Spring DDD 헥사고날 아키텍처 코딩 컨벤션 리뷰어. 기존 코드의 컨벤션 위반 사항을 스캔하고 Cursor AI가 리팩토링할 수 있는 구조화된 TODO 문서를 생성합니다. Zero-Tolerance 규칙(Lombok 금지, Law of Demeter, Long FK, Transaction 경계) 위반을 우선 감지합니다."
---

# Spring DDD Convention Reviewer

이 Skill은 Spring Boot 헥사고날 아키텍처 프로젝트의 코딩 컨벤션 준수 여부를 검토하고, 위반 사항을 Cursor AI가 리팩토링할 수 있는 TODO 목록으로 생성합니다.

## 주요 기능

1. **레이어별 컨벤션 스캔**: Domain, Application, Persistence, REST API 레이어 검증
2. **Zero-Tolerance 우선 감지**: Lombok, Law of Demeter, Long FK, Transaction 경계 위반 우선 처리
3. **TODO 문서 생성**: `.claude/work-orders/refactoring-todos.md`에 구조화된 리팩토링 작업 목록 생성
4. **우선순위 자동 분류**: 🔴 Critical, 🟡 Important, 🟢 Recommended

## 사용 시점

- 기존 코드에 컨벤션이 적용되지 않은 경우
- 레거시 코드 리팩토링 계획 수립 시
- 프로젝트 코드 품질 감사 전
- Cursor AI를 활용한 대규모 리팩토링 전

## 워크플로우

### 1. 스캔 대상 결정

사용자 요청에 따라 스캔 범위를 결정합니다:

```bash
# 전체 프로젝트 스캔
Skill: convention-reviewer
Input: "전체 프로젝트를 스캔해줘"

# 특정 레이어만 스캔
Skill: convention-reviewer
Input: "Domain layer만 스캔해줘"

# 특정 모듈 스캔
Skill: convention-reviewer
Input: "application 모듈만 스캔해줘"
```

### 2. 컨벤션 규칙 로드

`docs/coding_convention/` 디렉토리의 레이어별 규칙을 참조합니다:

```bash
# 레이어별 컨벤션 디렉토리
docs/coding_convention/
├── 01-adapter-rest-api-layer/  # 18개 규칙
├── 02-domain-layer/             # 15개 규칙
├── 03-application-layer/        # 18개 규칙
├── 04-persistence-layer/        # 10개 규칙
├── 05-testing/                  # 12개 규칙
├── 06-java21-patterns/          # 8개 규칙
├── 07-enterprise-patterns/      # 5개 규칙
├── 08-error-handling/           # 5개 규칙
└── 09-orchestration-patterns/   # 8개 규칙
```

상세 규칙은 `REFERENCE.md`를 읽어서 확인하세요.

### 3. 위반 사항 스캔

다음 순서로 위반 사항을 감지합니다:

**🔴 Priority 1: Zero-Tolerance 규칙**
1. Lombok 사용 검사 (`@Data`, `@Builder`, `@Getter`, `@Setter`)
2. Law of Demeter 위반 (Getter 체이닝: `.get().get()`)
3. JPA 관계 어노테이션 (`@ManyToOne`, `@OneToMany`)
4. Transaction 경계 위반 (`@Transactional` 내 외부 API 호출)

**🟡 Priority 2: 레이어별 규칙**
1. Domain: Tell Don't Ask, Aggregate 설계
2. Application: Transaction 관리, DTO 패턴
3. Persistence: Long FK 전략, QueryDSL 최적화
4. REST API: Controller 설계, Exception 처리

**🟢 Priority 3: Best Practices**
1. Java 21 패턴 (Record, Sealed Class, Virtual Threads)
2. Enterprise 패턴 (Caching, Event-Driven)
3. Orchestration 패턴 (3-Phase Lifecycle, Idempotency)

스캔 실행은 `scripts/scan-violations.sh`를 사용하세요:

```bash
bash .claude/skills/convention-reviewer/scripts/scan-violations.sh [target_path]
```

### 4. TODO 문서 생성

스캔 결과를 `.claude/work-orders/refactoring-todos.md`에 작성합니다.

TODO 형식은 `TODO-TEMPLATE.md`를 참조하세요:

```bash
cat .claude/skills/convention-reviewer/TODO-TEMPLATE.md
```

각 TODO 항목은 다음 정보를 포함합니다:
- 파일 경로 및 라인 번호
- 위반 규칙 설명
- 수정 방법 (Before/After 예시)
- 관련 문서 링크
- 우선순위 (🔴/🟡/🟢)

### 5. Cursor AI 리팩토링 지시

생성된 TODO 문서를 Cursor AI에게 전달합니다:

```
Cursor AI에게 전달할 메시지:
"`.claude/work-orders/refactoring-todos.md`를 읽고 우선순위 순으로 리팩토링을 진행해줘.
각 항목을 완료한 후 체크박스를 업데이트해줘."
```

## 출력 형식

### 요약 리포트

```
🔍 코딩 컨벤션 스캔 결과
==================================================

📊 전체 통계:
  - 스캔한 파일 수: 142개
  - 발견된 위반 사항: 87개
  - 🔴 Critical: 23개
  - 🟡 Important: 45개
  - 🟢 Recommended: 19개

📁 레이어별 위반 분포:
  - Domain: 15개
  - Application: 28개
  - Persistence: 22개
  - REST API: 18개
  - Test: 4개

⏱️ 예상 리팩토링 시간: 약 4-6시간
```

### TODO 문서 구조

```markdown
# Spring DDD Convention Refactoring TODOs

생성일시: 2025-11-04 10:30:00
스캔 범위: 전체 프로젝트

## 🔴 Critical (Zero-Tolerance 위반) - 23개

### [ ] Lombok 금지 위반 - Order.java
- **파일**: `domain/src/main/java/com/ryuqq/domain/order/Order.java:15`
- **현재 코드**: `@Data` 사용
- **문제**: Domain layer에서 Lombok 절대 금지
- **수정 방법**: Pure Java getter/setter로 변경
- **참고 문서**: `docs/coding_convention/02-domain-layer/.../lombok-prohibition.md`

[나머지 항목들...]

## 🟡 Important (레이어 규칙 위반) - 45개

[항목들...]

## 🟢 Recommended (Best Practices) - 19개

[항목들...]
```

## 주의사항

1. **False Positive 최소화**: 스캔 결과가 불확실하면 TODO에 "검토 필요" 표시
2. **컨텍스트 유지**: 파일 경로와 라인 번호를 정확히 기록
3. **우선순위 엄격 적용**: Zero-Tolerance 규칙은 반드시 🔴 Critical
4. **Before/After 필수**: 모든 TODO에 수정 전후 코드 예시 포함

## 추가 리소스

- 상세 규칙: `cat REFERENCE.md`
- TODO 템플릿: `cat TODO-TEMPLATE.md`
- 스캔 스크립트: `bash scripts/scan-violations.sh`
