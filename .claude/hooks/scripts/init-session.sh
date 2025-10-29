#!/bin/bash

# init-session.sh - Claude Code SessionStart Hook
# 세션 시작 시 Git 브랜치에서 Jira 태스크를 파싱하고 핵심 규칙을 주입합니다.

set -euo pipefail

# ===== 브랜치 및 Jira 태스크 파싱 =====
BRANCH=$(git branch --show-current 2>/dev/null || echo "unknown")
JIRA_TASK=$(echo "$BRANCH" | grep -oE '[A-Z][A-Z0-9]*-[0-9]+' || echo "")

# ===== 규칙 문서 경로 =====
# 성능 최적화를 위해 요약본 사용 (186줄)
# 전체 문서 참조 필요 시: docs/ENTERPRISE_SPRING_STANDARDS_PROMPT.md (3361줄)
RULES_DOC="docs/ENTERPRISE_SPRING_STANDARDS_SUMMARY.md"
SESSION_CONTEXT="/tmp/claude-session-context.md"

# ===== 세션 컨텍스트 생성 =====
cat > "$SESSION_CONTEXT" <<CONTEXT
# 🚀 Claude Code Session Context

## 📋 현재 작업 정보
- **Git Branch**: \`$BRANCH\`
- **Jira Task**: ${JIRA_TASK:-"N/A (브랜치명에 Jira 패턴 없음)"}
- **Project**: claude-spring-standards (Hexagonal Architecture)
- **Stack**: Spring Boot 3.3.x + Java 21

---

## 🔴 CRITICAL 규칙 요약

### Domain Layer
❌ **ABSOLUTELY FORBIDDEN**:
- NO Spring Framework imports (\`org.springframework.*\`)
- NO JPA/Hibernate annotations (\`@Entity\`, \`@Table\`, etc.)
- NO Lombok (\`@Getter\`, \`@Setter\`, \`@Data\`, etc.)
- NO mutable fields (모든 필드는 \`private final\`)

✅ **REQUIRED**:
- Pure Java only (Apache Commons Lang3만 허용)
- Immutable value objects with factory methods
- Javadoc + \`@author\` 태그 필수
- 90%+ test coverage

### Application Layer
❌ **ABSOLUTELY FORBIDDEN**:
- NO Adapter 직접 참조 (Controller, Repository 구현체)
- NO Lombok
- NO JPA entities in use case logic

✅ **REQUIRED**:
- UseCase suffix for use case classes
- Port interfaces only (의존성 역전)
- \`@Service\`, \`@Transactional\` 허용
- 80%+ test coverage

### Adapter Layer
❌ **ABSOLUTELY FORBIDDEN**:
- NO Lombok
- NO business logic (비즈니스 로직은 Domain/Application)
- NO direct domain manipulation

✅ **REQUIRED**:
- Controller/Repository suffix
- Testcontainers for integration tests
- 70%+ test coverage

---

## 🚨 금지어 (즉시 거부해야 하는 표현)
- "일단", "나중에", "TODO로 남기고"
- "임시로", "테스트는 나중에"
- "Lombok으로 간단하게", "setter 추가"

---

## 🎯 품질 게이트
- **ArchUnit 테스트**: \`HexagonalArchitectureTest.java\` 필수 통과
- **Checkstyle**: Javadoc + @author 검증
- **Git Pre-commit Hooks**: 8개 validator 자동 실행
- **커버리지**: Domain 90%, Application 80%, Adapter 70%

---

## 📚 참고 문서

### 요약본 (세션 로딩용 - 경량)
- **엔터프라이즈 표준**: \`$RULES_DOC\` (핵심 아키텍처 및 DDD 전략)
- **코딩 표준**: \`docs/CODING_STANDARDS_SUMMARY.md\` (SOLID, Law of Demeter, Transaction 경계)

### 전체 문서 (상세 참조용)
- **엔터프라이즈 표준**: \`docs/ENTERPRISE_SPRING_STANDARDS_PROMPT.md\` (96개 규칙, 3361줄)
- **코딩 표준**: \`docs/CODING_STANDARDS.md\` (87개 규칙, 2676줄)

### 특화 가이드
- **DDD Aggregate**: \`docs/DDD_AGGREGATE_MIGRATION_GUIDE.md\`
- **DTO 패턴**: \`docs/DTO_PATTERNS_GUIDE.md\`
- **예외 처리**: \`docs/EXCEPTION_HANDLING_GUIDE.md\`
- **Java Record**: \`docs/JAVA_RECORD_GUIDE.md\`
- **Gemini 리뷰**: \`docs/GEMINI_REVIEW_GUIDE.md\`

### 훅 시스템
- **Dynamic Hooks**: \`.claude/hooks/README.md\`
- **Git Hooks**: \`hooks/README.md\`

### 작업 추적
- **TODO 구현**: \`TODO_IMPLEMENTATION.md\`

---

**⚡ 이 컨텍스트는 세션 시작 시 자동 생성되었습니다.**
**📍 상세 내용 필요 시 전체 문서를 참조하세요.**

CONTEXT

# ===== 출력 메시지 =====
echo ""
echo "✅ 세션 초기화 완료"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 브랜치: $BRANCH"
if [ -n "$JIRA_TASK" ]; then
    echo "🎫 Jira Task: $JIRA_TASK"
else
    echo "⚠️  Jira Task: 없음 (브랜치명에 PROJ-123 형식 패턴 필요)"
fi
echo "📄 세션 컨텍스트: $SESSION_CONTEXT"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

exit 0
