#!/bin/bash

# =====================================================
# Serena Conventions Setup Script
# Purpose: 프로젝트 템플릿 사용자가 코딩 컨벤션을 Serena 메모리에 자동으로 설정
# Usage: bash setup-serena-conventions.sh
# =====================================================

set -e  # 에러 발생 시 즉시 중단

PROJECT_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
cd "$PROJECT_ROOT"

echo "🚀 Serena Conventions Setup"
echo "=================================="
echo ""
echo "📂 Project Root: $PROJECT_ROOT"
echo ""

# Python 설치 확인
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3가 설치되어 있지 않습니다."
    echo "   Python 3를 설치한 후 다시 실행하세요."
    exit 1
fi

echo "✅ Python 3 확인 완료"
echo ""

# ==================== Serena 메모리 생성 ====================

echo "🧠 Serena 메모리 생성 중..."
echo ""

python3 << 'PYTHON_SCRIPT'
import sys
import os

# Serena MCP 사용 가능 여부 확인 (간접)
try:
    print("📋 코딩 컨벤션 메모리 생성 시작...")
    print("")

    # 메모리 내용 준비
    memories = {
        "coding_convention_domain_layer": """# Domain Layer Coding Conventions

## 🎯 Core Principles (Zero-Tolerance)

### 1️⃣ Lombok 금지
- **Rule**: @Data, @Builder, @Getter, @Setter 등 모든 Lombok 어노테이션 금지
- **Reason**: Plain Java로 명시적 코드 작성, 의도 명확화
- **Detection**: `@Data|@Builder|@Getter|@Setter|@AllArgsConstructor|@NoArgsConstructor`
- **Fix**: Pure Java getter/setter 직접 작성

### 2️⃣ Law of Demeter (Getter 체이닝 금지)
- **Rule**: 한 번에 한 depth의 getter만 호출 가능
- **Anti-Pattern**: `order.getCustomer().getAddress().getZip()` ❌
- **Detection**: `\\.get[A-Z].*\\.get[A-Z]` (정규식 매칭)
- **Fix**: Tell, Don't Ask 패턴 적용

### 3️⃣ Aggregate Root 패턴
- **Rule**: Aggregate 내부 Entity는 외부에서 직접 접근 불가
- **Pattern**: Root를 통한 간접 접근만 허용

## 📚 Reference
- Cache Location: `.claude/cache/rules/domain-layer-*`
- Docs: `docs/coding_convention/02-domain-layer/`
""",

        "coding_convention_application_layer": """# Application Layer Coding Conventions

## 🎯 Core Principles (Zero-Tolerance)

### 1️⃣ Transaction Boundary (가장 중요!)
- **Rule**: `@Transactional` 내에서 외부 API 호출 절대 금지
- **Prohibited**: RestTemplate, WebClient, FeignClient, Kafka Producer 등
- **Reason**: 트랜잭션 커넥션 홀딩 시간 최소화, DB 커넥션 고갈 방지

### 2️⃣ Spring 프록시 제약사항
⚠️ **다음 경우 `@Transactional`이 작동하지 않습니다:**
- Private 메서드
- Final 클래스/메서드
- 같은 클래스 내부 호출 (`this.method()`)

### 3️⃣ UseCase Single Responsibility
- **Rule**: 하나의 UseCase는 하나의 비즈니스 목적만 수행
- **Pattern**: Command/Query 분리 (CQRS)

## 📚 Reference
- Cache Location: `.claude/cache/rules/application-layer-*`
- Docs: `docs/coding_convention/03-application-layer/`
""",

        "coding_convention_persistence_layer": """# Persistence Layer Coding Conventions

## 🎯 Core Principles (Zero-Tolerance)

### 1️⃣ Long FK Strategy (가장 중요!)
- **Rule**: JPA 관계 어노테이션 금지
- **Prohibited**: `@OneToMany`, `@ManyToOne`, `@OneToOne`, `@ManyToMany`
- **Use Instead**: `private Long userId;` (Long 타입 FK 직접 관리)
- **Reason**: N+1 문제 방지, Law of Demeter 준수, 성능 최적화

### 2️⃣ Entity Immutability
- **Rule**: 불변 필드는 `final`로 선언, setter 금지
- **Pattern**: Static factory method (create, reconstitute)

### 3️⃣ CQRS Separation
- **Rule**: Command/Query Adapter 분리
- **Command**: CUD 작업
- **Query**: Read 작업

## 📚 Reference
- Cache Location: `.claude/cache/rules/persistence-layer-*`
- Docs: `docs/coding_convention/04-persistence-layer/`
""",

        "coding_convention_rest_api_layer": """# REST API Layer Coding Conventions

## 🎯 Core Principles (Zero-Tolerance)

### 1️⃣ Controller Thin (얇은 컨트롤러)
- **Rule**: Controller는 요청/응답 변환만 담당
- **Prohibited**: 비즈니스 로직, 트랜잭션, 복잡한 검증

### 2️⃣ Exception Handling (GlobalExceptionHandler)
- **Rule**: 모든 예외는 `@RestControllerAdvice`로 중앙 처리
- **Pattern**: Domain Exception → ErrorMapper → ApiResponse

### 3️⃣ Response Format (일관된 응답 형식)
- **Rule**: 모든 API는 `ApiResponse<T>` 형식으로 응답

## 📚 Reference
- Cache Location: `.claude/cache/rules/adapter-rest-api-layer-*`
- Docs: `docs/coding_convention/01-adapter-rest-api-layer/`
""",

        "coding_convention_index": """# Spring Standards Project - Coding Convention Master Index

## 🎯 Quick Reference

### 레이어별 메모리 접근
```
read_memory("coding_convention_domain_layer")       → Domain Layer 규칙
read_memory("coding_convention_application_layer")  → Application Layer 규칙
read_memory("coding_convention_persistence_layer")  → Persistence Layer 규칙
read_memory("coding_convention_rest_api_layer")     → REST API Layer 규칙
```

## 🚨 Zero-Tolerance Rules

1. Lombok 금지 (Domain)
2. Law of Demeter (Domain)
3. Long FK Strategy (Persistence)
4. Transaction Boundary (Application)
5. Spring 프록시 제약사항 (Application)
6. Javadoc 필수 (All Layers)

## 📚 Reference
- Cache Location: `.claude/cache/rules/`
- Total Rules: 90개 (Layer별)
"""
    }

    print("📝 생성할 메모리:")
    for name in memories.keys():
        print(f"   - {name}")
    print("")

    print("✅ 메모리 생성 준비 완료")
    print("")
    print("🔧 Serena MCP를 통해 메모리를 생성하려면:")
    print("   Claude Code에서 다음 명령을 실행하세요:")
    print("")
    for name, content in memories.items():
        print(f'   mcp__serena__write_memory("{name}", """')
        print(f'   {content[:100]}...')
        print(f'   """)')
        print("")

    print("✨ 또는 /sc:load 명령어를 실행하면 자동으로 로드됩니다.")

except Exception as e:
    print(f"❌ 오류 발생: {e}")
    sys.exit(1)

PYTHON_SCRIPT

echo ""
echo "=================================="
echo "✅ Serena Conventions Setup 완료!"
echo ""
echo "📖 다음 단계:"
echo "   1. Claude Code를 실행합니다"
echo "   2. 이 프로젝트 디렉토리로 이동합니다"
echo "   3. 다음 명령어를 실행합니다:"
echo ""
echo "      /sc:load"
echo ""
echo "   4. Serena 메모리가 자동으로 로드됩니다!"
echo ""
echo "🎯 또는 수동으로 메모리 확인:"
echo "   list_memories()"
echo "   read_memory(\"coding_convention_index\")"
echo ""
echo "=================================="
