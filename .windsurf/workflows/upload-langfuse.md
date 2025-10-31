---
description: langfuse upload
---

# LangFuse 메트릭 업로드

Claude Code 및 Cascade 로그를 LangFuse로 업로드하여 개발 효율성 메트릭을 추적합니다.

## 📊 목적

- **토큰 사용량 분석**: Layer별 토큰 소비 패턴
- **성능 메트릭**: 검증 시간, Cache Hit Rate
- **품질 지표**: 컨벤션 위반, 테스트 커버리지
- **세션 통계**: 평균 세션 시간, 생산성 향상률

## 🚀 실행 방법

### Cascade에서 실행

```
@upload-langfuse
```

또는

```
LangFuse에 로그를 업로드해줘
```

## 📋 실행 단계

### 1단계: 로그 집계

```bash
python3 scripts/langfuse/aggregate-logs.py \
  --claude-logs .claude/hooks/logs/hook-execution.jsonl \
  --cascade-logs .pipeline-metrics/metrics.jsonl \
  --output langfuse-data.json \
  --anonymize
```

**anonymize 옵션**:
- 사용자명: `sangwon-ryu` → `user-a1b2c3d4`
- 파일명: `Order.java` → `*.java`
- 프로젝트명: 익명화

### 2단계: LangFuse 업로드

```bash
python3 scripts/langfuse/upload-to-langfuse.py \
  --input langfuse-data.json
```

**필수 환경 변수**:
```bash
export LANGFUSE_PUBLIC_KEY="pk-lf-..."
export LANGFUSE_SECRET_KEY="sk-lf-..."
export LANGFUSE_HOST="https://cloud.langfuse.com"
```

### 3단계: 결과 확인

LangFuse 대시보드에서 확인:
- https://cloud.langfuse.com

## 🔧 환경 변수 설정

### macOS/Linux

```bash
# ~/.bashrc 또는 ~/.zshrc에 추가
export LANGFUSE_PUBLIC_KEY="pk-lf-your-key"
export LANGFUSE_SECRET_KEY="sk-lf-your-secret"
export LANGFUSE_HOST="https://cloud.langfuse.com"

# 적용
source ~/.bashrc  # 또는 source ~/.zshrc
```

### 환경 변수 확인

```bash
echo $LANGFUSE_PUBLIC_KEY
# 출력: pk-lf-...
```

## 📊 예상 출력

```
🚀 LangFuse Log Aggregator
   Claude logs: .claude/hooks/logs/hook-execution.jsonl
   Cascade logs: .pipeline-metrics/metrics.jsonl
   Anonymize: True

✅ Export complete!
   Output: langfuse-data.json
   Traces: 21
   Observations: 66

📤 Uploading to LangFuse (https://cloud.langfuse.com)
   Traces: 21
   Observations: 66

📊 Uploading Traces...
   ✅ Trace: session-1761205485-56330
   ✅ Trace: session-1761206123-78945
   ...

📊 Uploading Observations...
   ✅ Observation: Keyword Analysis
   ✅ Observation: Cache Injection: domain
   ✅ Observation: Code Validation
   ...

✅ Upload complete!
   Traces: 21/21
   Observations: 66/66
```

## 🎯 LangFuse 대시보드 메트릭

업로드 후 다음 메트릭을 확인할 수 있습니다:

### 토큰 사용량
- Domain Layer: 15,234 tokens
- Application Layer: 12,890 tokens
- Persistence Layer: 8,456 tokens
- REST API Layer: 6,123 tokens

### 성능 메트릭
- 평균 검증 시간: 148ms (↓73% vs baseline)
- Cache Hit Rate: 92%
- 규칙 주입 시간: <100ms

### 품질 지표
- 컨벤션 위반: 5건 (↓78% vs baseline)
- 테스트 커버리지: 87%
- 빌드 성공률: 95%

### 세션 통계
- 평균 세션 시간: 8분 (↓47% vs baseline)
- 총 세션 수: 234
- 활성 사용자: 3

## ⚠️ 문제 해결

### 환경 변수 미설정

```
❌ Error: LangFuse credentials required

해결:
export LANGFUSE_PUBLIC_KEY="pk-lf-..."
export LANGFUSE_SECRET_KEY="sk-lf-..."
```

### 로그 파일 없음

```
⚠️  Claude logs not found: .claude/hooks/logs/hook-execution.jsonl

해결:
- Claude Code로 코드를 생성하면 자동으로 로그가 기록됩니다
- 최소 1회 이상 Hook이 실행되어야 로그가 생성됩니다
```

### Python 모듈 없음

```
❌ Error: 'requests' module not found

해결:
pip install requests
```

## 🔐 보안 및 개인정보

### 익명화 (--anonymize)

개인정보 보호를 위해 다음 항목이 자동으로 익명화됩니다:

- **사용자명**: SHA256 해시 → `user-a1b2c3d4`
- **파일명**: 확장자만 유지 → `*.java`
- **프로젝트명**: 번호로 대체 → `project-001`

### 권장 사항

- **개인 개발**: `--anonymize` 선택 사항
- **팀 공유**: `--anonymize` 필수
- **공개 템플릿**: `--anonymize` 필수

## 📚 관련 문서

- [LangFuse 모니터링 가이드](../../docs/LANGFUSE_MONITORING_GUIDE.md)
- [Dynamic Hooks 시스템](../../docs/DYNAMIC_HOOKS_GUIDE.md)
- [LangFuse 공식 문서](https://langfuse.com/docs)

## 🎓 사용 시나리오

### 시나리오 1: 하루 끝 메트릭 확인

```
# IntelliJ Cascade에서
@upload-langfuse

# 또는 직접 명령
LangFuse에 오늘 로그를 업로드해줘
```

### 시나리오 2: 주간 리포트

```
# 일주일치 데이터 업로드 후 LangFuse 대시보드에서 분석
- 토큰 사용 트렌드
- 가장 많이 사용한 Layer
- 컨벤션 위반 감소율
```

### 시나리오 3: A/B 테스트

```
# Before: Cache 시스템 도입 전
- 평균 토큰: 50,000
- 평균 세션: 15분
- 위반: 23건

# After: Cache 시스템 도입 후 (LangFuse로 측정)
- 평균 토큰: 5,000 (↓90%)
- 평균 세션: 8분 (↓47%)
- 위반: 5건 (↓78%)
```

---

**생성일**: 2025-10-30
**버전**: 1.0.0
**작성자**: Claude Code
