---
description: Hook 로그를 LangFuse로 업로드하여 실시간 모니터링
tags: [project]
---

# Upload LangFuse Hooks - Hook 로그 실시간 모니터링

당신은 `.claude/hooks/logs/hook-execution.jsonl` 로그를 LangFuse Trace로 변환하여 업로드하는 작업을 수행합니다.

## 목적

**실제 개발 환경에서 지속적으로** 다음 시스템의 효과를 측정합니다:

1. **Hook 시스템 일관성** - 평상시 개발에서 Hook이 제대로 작동하는가?
2. **프롬프트 효과** - LangFuse 프롬프트가 실제로 컨벤션을 준수하는가?
3. **Cache 주입 성공률** - 규칙이 제대로 주입되는가?
4. **위반 패턴 분석** - 어떤 레이어에서 위반이 자주 발생하는가?

## 입력 형식

사용자는 다음과 같이 명령합니다:

```bash
/upload-langfuse-hooks

# 전체 로그 업로드
/upload-langfuse-hooks --full

# 특정 세션만
/upload-langfuse-hooks --session 1761875155-77368

# 업로드 후 로그 보존 (기본: 삭제)
/upload-langfuse-hooks --keep-logs
```

**⚠️ 기본 동작**: 업로드 성공 후 **로그 파일 자동 삭제**
**✅ 안전장치**: LangFuse 업로드 성공 확인 후에만 삭제

## 실행 단계

### 1. 환경 변수 확인

**.env 파일 확인**:
```bash
LANGFUSE_PUBLIC_KEY=pk-lf-...
LANGFUSE_SECRET_KEY=sk-lf-...
LANGFUSE_HOST=https://us.cloud.langfuse.com
```

환경 변수가 없으면 사용자에게 설정 요청.

### 2. 스크립트 실행

**기본 실행** (증분 업로드 + 자동 삭제):
```bash
python3 scripts/langfuse/upload-hook-logs.py
# → 업로드 성공 후 로그 파일 자동 삭제 ✅
```

**전체 로그 업로드**:
```bash
python3 scripts/langfuse/upload-hook-logs.py --full
```

**특정 세션만 업로드**:
```bash
python3 scripts/langfuse/upload-hook-logs.py --session 1761875155-77368
```

**로그 보존 (삭제 비활성화)**:
```bash
python3 scripts/langfuse/upload-hook-logs.py --keep-logs
# → 업로드 후 로그 파일 보존 (기본 동작 비활성화)
```

### 3. 업로드 결과 확인

**출력 예시**:
```
✅ LangFuse 클라이언트 초기화 완료 (Host: https://us.cloud.langfuse.com)
📊 Hook 로그 파싱 중... (시작 라인: 0)
📤 15개 세션의 로그를 LangFuse로 업로드 중...
✅ Trace 생성 완료: hook-execution-1761875155-77368
   - Detected Layers: ['application', 'enterprise']
   - Rules Injected: 24
   - Violations: 0
...
✅ 업로드 완료! (236 라인까지 처리)
🗑️ 로그 파일 삭제 완료: .claude/hooks/logs/hook-execution.jsonl
♻️ 업로드 상태 리셋 완료
```

**`--keep-logs` 사용 시**:
```
✅ 업로드 완료! (236 라인까지 처리)
# → 로그 파일 삭제하지 않음
```

### 4. LangFuse 대시보드 확인

**LangFuse UI**:
- URL: https://us.cloud.langfuse.com
- Project: `claude-spring-standards`

**확인할 메트릭**:
1. **Hook 실행률**: Hook이 실행된 세션 비율
2. **Layer 감지 정확도**: 올바른 Layer 감지 비율
3. **Cache 주입 성공률**: Cache 규칙 주입 성공 비율
4. **위반 패턴**: Layer별 컨벤션 위반 건수

### 5. 분석 및 개선

**주간 리뷰**:
1. LangFuse에서 지난 주 데이터 확인
2. 위반이 자주 발생하는 레이어/규칙 식별
3. 프롬프트 개선 (v1.0 → v1.1)
4. Hook 로직 조정 (키워드 매핑, Layer 감지)

## LangFuse Trace 구조

### Trace: hook-execution-{session_id}

**Input**:
```json
{
  "session_id": "1761875155-77368",
  "context_score": 75,
  "detected_keywords": ["domain", "aggregate"],
  "detected_layers": ["domain", "application"]
}
```

**Output**:
```json
{
  "cache_injection_success": true,
  "total_rules_injected": 24,
  "layers_injected": 2,
  "serena_memory_loaded": true,
  "validation_passed": true
}
```

**Metadata**:
```json
{
  "threshold": 25,
  "decision": "cache_injection",
  "serena_layers_loaded": 2,
  "estimated_tokens": 2505,
  "total_violations": 0
}
```

### Observation: cache-injection-{layer}

**Input**:
```json
{
  "layer": "application",
  "priority_filter": "all"
}
```

**Output**:
```json
{
  "rules_loaded": 14,
  "estimated_tokens": 2505,
  "cache_files": [
    "application-layer-assembler-pattern-01_assembler-responsibility.json",
    ...
  ]
}
```

### Observation: validation

**Input**:
```json
{
  "validation_type": "zero-tolerance",
  "file_path": "domain/src/main/java/.../OrderDomain.java",
  "layer": "domain"
}
```

**Output**:
```json
{
  "violations": [],
  "passed_rules": ["lombok-prohibition", "law-of-demeter", "long-fk-strategy"],
  "validation_time_ms": 148
}
```

## 측정 메트릭

### 1. Hook 시스템 메트릭

| 메트릭 | 목표 | 측정 방법 |
|--------|------|----------|
| Hook 실행률 | > 80% | Hook 실행 세션 / 전체 세션 |
| Layer 감지 정확도 | > 90% | 정확한 Layer 감지 / 전체 Layer 감지 |
| Cache 주입 성공률 | > 95% | 성공한 주입 / 시도한 주입 |
| 평균 규칙 주입 수 | 10-30개 | 총 주입 규칙 수 / 전체 세션 |

### 2. 프롬프트 효과 메트릭

| 메트릭 | 목표 | 측정 방법 |
|--------|------|----------|
| 위반률 (Layer별) | < 5% | Layer 위반 / Layer 작업 |
| Zero-Tolerance 준수율 | 100% | 위반 0건 |
| 프롬프트 버전 성능 | v1.0 < v0.9 | 버전별 평균 위반 건수 |

### 3. 대시보드 알람 조건

**알람 발생 조건**:
- ⚠️ Hook 실행률 < 80%
- 🚨 Cache 주입 실패 > 5%
- 🚨 Zero-Tolerance 위반 발생
- ⚠️ 위반률 > 5%

## 자동화 워크플로우

### 옵션 1: Cron Job (권장)

**매시간 자동 업로드**:
```bash
# crontab -e
0 * * * * cd /Users/sangwon-ryu/claude-spring-standards && python3 scripts/langfuse/upload-hook-logs.py >> logs/langfuse-upload.log 2>&1
```

### 옵션 2: Git Pre-commit Hook

**커밋 시 자동 업로드**:
```bash
# hooks/pre-commit
python3 scripts/langfuse/upload-hook-logs.py
```

### 옵션 3: GitHub Actions

**PR 생성 시 자동 업로드**:
```yaml
# .github/workflows/langfuse-upload.yml
name: Upload to LangFuse
on: [pull_request]
jobs:
  upload:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Upload Hook Logs
        env:
          LANGFUSE_PUBLIC_KEY: ${{ secrets.LANGFUSE_PUBLIC_KEY }}
          LANGFUSE_SECRET_KEY: ${{ secrets.LANGFUSE_SECRET_KEY }}
        run: python3 scripts/langfuse/upload-hook-logs.py
```

## 출력 형식

```markdown
✅ Hook 로그 업로드 완료!

**업로드 통계**:
- 세션 수: 15개
- 총 라인 수: 236라인
- Hook 실행률: 85% (13/15 세션)
- 평균 규칙 주입: 22개
- 총 위반 건수: 2건

**LangFuse Dashboard**:
https://us.cloud.langfuse.com/project/claude-spring-standards/traces

**다음 단계**:
1. LangFuse Dashboard에서 실시간 모니터링
2. 위반 패턴 분석
3. 프롬프트 개선 (v1.0 → v1.1)
4. Hook 로직 조정
```

## 에러 처리

### 환경 변수 없음

```
❌ 오류: LANGFUSE_PUBLIC_KEY 및 LANGFUSE_SECRET_KEY 환경 변수가 필요합니다

.env 파일에 다음을 추가하세요:
LANGFUSE_PUBLIC_KEY=pk-lf-...
LANGFUSE_SECRET_KEY=sk-lf-...
LANGFUSE_HOST=https://us.cloud.langfuse.com
```

### 로그 파일 없음

```
⚠️ Hook 로그 파일이 없습니다: /Users/sangwon-ryu/claude-spring-standards/.claude/hooks/logs/hook-execution.jsonl

Hook 시스템이 활성화되어 있는지 확인하세요.
```

### LangFuse API 오류

```
❌ Trace 생성 실패 (1761875155-77368): 401 Unauthorized

API 키가 올바른지 확인하세요.
```

## 참고 문서

- [Measurement Strategy](../../langfuse/MEASUREMENT_STRATEGY.md) - 측정 전략 전체 가이드
- [Hook System](../../docs/DYNAMIC_HOOKS_GUIDE.md) - Hook 시스템 상세
- [LangFuse Docs](https://langfuse.com/docs) - LangFuse 공식 문서
