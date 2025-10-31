# LangFuse 통합 스크립트

이 디렉토리는 Claude Code 및 Cascade 로그를 LangFuse로 전송하는 스크립트를 포함합니다.

## 📋 파일 구조

```
scripts/langfuse/
├── README.md (이 파일)
├── aggregate-logs.py      # JSONL → LangFuse 형식 변환
├── upload-to-langfuse.py  # LangFuse API 전송
└── monitor.sh             # 실시간 모니터링 (선택)
```

## 🚀 빠른 시작

### 템플릿 사용자 (Telemetry Mode)

이 템플릿은 템플릿 효과성 측정을 위해 **익명화된 텔레메트리**를 지원합니다.

#### 1. 설치 시 텔레메트리 활성화

```bash
bash scripts/install-claude-hooks.sh

# 프롬프트:
# 📊 텔레메트리 (익명 사용 통계)
# 텔레메트리를 활성화하시겠습니까? (y/N):
```

#### 2. 수동 업로드 (일회성)

```bash
# 텔레메트리 설정으로 로그 집계 및 업로드
python3 scripts/langfuse/aggregate-logs.py --telemetry
python3 scripts/langfuse/upload-to-langfuse.py --telemetry
```

#### 3. 실시간 모니터링 (선택사항)

```bash
# 5분마다 자동 업로드
bash scripts/langfuse/monitor.sh
```

### 팀/회사 사용 (독립 LangFuse 프로젝트)

독립적인 LangFuse 프로젝트를 사용하려면:

#### 1. 환경 변수 설정

```bash
# .env 또는 ~/.bashrc
export LANGFUSE_PUBLIC_KEY="pk-lf-your-key-..."
export LANGFUSE_SECRET_KEY="sk-lf-your-secret-..."
export LANGFUSE_HOST="https://cloud.langfuse.com"
```

#### 2. Python 의존성 설치

```bash
pip install requests  # HTTP 요청용
```

**참고**: LangFuse Python SDK는 **필요하지 않습니다**. 이 스크립트는 LangFuse Ingestion API를 직접 사용합니다.

#### 3. 로그 집계 및 업로드

```bash
# 1. 로그 집계 (JSONL → LangFuse 형식)
python3 scripts/langfuse/aggregate-logs.py \
  --claude-logs .claude/hooks/logs/hook-execution.jsonl \
  --cascade-logs .pipeline-metrics/metrics.jsonl \
  --output langfuse-data.json \
  --anonymize  # 개인정보 익명화 (선택)

# 2. LangFuse 업로드
python3 scripts/langfuse/upload-to-langfuse.py \
  --input langfuse-data.json
```

## 📊 스크립트 상세

### aggregate-logs.py

**기능**: Claude Code 및 Cascade 로그를 LangFuse Trace/Observation 형식으로 변환

**입력**:
- `.claude/hooks/logs/hook-execution.jsonl`
- `.pipeline-metrics/metrics.jsonl`

**출력**:
- `langfuse-data.json` (LangFuse API 호환 형식)

**옵션**:
- `--anonymize`: 사용자명, 파일명 등 익명화
- `--telemetry`: 텔레메트리 모드 (`.langfuse.telemetry` 자동 읽기, 익명화 강제)

### upload-to-langfuse.py

**기능**: LangFuse Ingestion API로 Trace/Observation 배치 전송

**기술 스택**:
- `requests` 라이브러리 사용
- LangFuse Ingestion API (`/api/public/ingestion`) 직접 호출
- LangFuse Python SDK 불필요

**환경 변수**:
- `LANGFUSE_PUBLIC_KEY`
- `LANGFUSE_SECRET_KEY`
- `LANGFUSE_HOST` (기본: https://cloud.langfuse.com)

**옵션**:
- `--telemetry`: 텔레메트리 모드 (`.langfuse.telemetry`에서 credentials 자동 읽기)

**API 형식**:
```python
# Batch 형식으로 전송
{
  'batch': [
    {
      'type': 'trace-create',
      'timestamp': '2025-10-30T12:00:00Z',
      'body': { 'id': '...', 'name': '...', ... }
    },
    {
      'type': 'event-create',
      'timestamp': '2025-10-30T12:00:01Z',
      'body': { 'traceId': '...', 'name': '...', ... }
    }
  ]
}
```

### monitor.sh

**기능**: 실시간 로그 모니터링 및 자동 업로드

**실행**:
```bash
bash scripts/langfuse/monitor.sh
```

**동작**:
- 5분마다 로그 집계
- 자동으로 LangFuse 업로드

## 🔒 개인정보 보호

### 익명화 옵션

`--anonymize` 플래그를 사용하면 다음 항목이 익명화됩니다:

- **사용자명**: `sangwon-ryu` → `user-a1b2c3` (SHA256 해시)
- **파일명**: `Order.java` → `*.java`
- **프로젝트명**: `my-ecommerce` → `project-001`

### 권장 사용 방식

```bash
# 개발 환경 (익명화 없음)
python3 scripts/langfuse/aggregate-logs.py

# 공유 환경 (익명화 활성화)
python3 scripts/langfuse/aggregate-logs.py --anonymize
```

## 🎯 멀티 테넌트 전략

### 시나리오별 권장 사항

#### 시나리오 1: 템플릿 메인테이너 (개발)
- **LangFuse 프로젝트**: `spring-standards-template` (중앙 집중)
- **익명화**: 필수 (`--anonymize`)
- **목적**: 템플릿 효과성 측정

#### 시나리오 2: 팀/회사 사용
- **LangFuse 프로젝트**: `{team-name}-spring-project` (독립)
- **익명화**: 선택 사항
- **목적**: 팀별 개발 효율 분석

#### 시나리오 3: 개인 사용
- **LangFuse 프로젝트**: `{username}-project` (개인)
- **익명화**: 불필요
- **목적**: 개인 개발 패턴 분석

## 📈 LangFuse 대시보드

### 추적 가능한 메트릭

1. **토큰 사용량**
   - Layer별 토큰 사용량
   - 시간대별 추이

2. **컨벤션 위반**
   - 위반 규칙 통계
   - Layer별 위반 건수

3. **성능**
   - 검증 시간 (ms)
   - Cascade 작업 시간

4. **품질**
   - 테스트 통과율
   - 커버리지

## 🔧 CI/CD 통합

### GitHub Actions 예시

```yaml
# .github/workflows/langfuse-upload.yml
name: LangFuse Upload

on:
  push:
    branches: [main]

jobs:
  upload:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Upload to LangFuse
        env:
          LANGFUSE_PUBLIC_KEY: ${{ secrets.LANGFUSE_PUBLIC_KEY }}
          LANGFUSE_SECRET_KEY: ${{ secrets.LANGFUSE_SECRET_KEY }}
        run: |
          python3 scripts/langfuse/aggregate-logs.py --anonymize
          python3 scripts/langfuse/upload-to-langfuse.py
```

## 💡 자주 묻는 질문

### Q: LangFuse Python SDK를 설치해야 하나요?

**A**: **아니요**. 이 스크립트는 LangFuse Ingestion API를 직접 사용하므로 SDK가 필요하지 않습니다. `requests` 라이브러리만 있으면 됩니다.

### Q: 여러 프로젝트에서 같은 LangFuse 프로젝트를 사용해도 되나요?

**A**: 권장하지 않습니다. 팀별/프로젝트별 독립 LangFuse 프로젝트를 생성하세요.

### Q: 로그가 너무 많으면 비용이 많이 나오나요?

**A**: LangFuse 무료 플랜은 월 50K Traces까지 무료입니다. 샘플링 또는 집계로 최적화 가능합니다.

### Q: Self-Hosted LangFuse를 사용할 수 있나요?

**A**: 가능합니다. `LANGFUSE_HOST` 환경 변수를 Self-Hosted URL로 설정하세요.

### Q: Timestamp 형식 오류가 발생하면?

**A**: `aggregate-logs.py`가 자동으로 ISO 8601 UTC 형식 (`2025-10-30T12:00:00Z`)으로 변환합니다. 만약 오류가 발생하면 `aggregate-logs.py`를 최신 버전으로 업데이트하세요.

## 📚 참고 문서

- [LangFuse 모니터링 가이드](../../docs/LANGFUSE_MONITORING_GUIDE.md) - 전체 시스템 설명
- [LangFuse 공식 문서](https://langfuse.com/docs)
- [LangFuse API 문서](https://langfuse.com/docs/api)

---

**생성일**: 2025-10-29
**버전**: 1.0.0
