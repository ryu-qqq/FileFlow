# LangFuse 텔레메트리 가이드

이 가이드는 Spring Standards 템플릿의 텔레메트리 시스템을 설명합니다.

---

## 🎯 텔레메트리란?

**텔레메트리**는 이 템플릿이 얼마나 효과적인지 측정하기 위해 **익명화된 사용 통계**를 수집하는 것입니다.

### 수집되는 데이터 (익명화됨)

#### ✅ 수집됨 (익명화)
- 토큰 사용량 (Layer별)
- 검증 시간 (ms)
- 컨벤션 위반 건수
- Cascade 작업 성공률
- 템플릿 버전

#### ❌ 수집 안 됨
- 사용자 이름 (익명 해시로 변환: `user-a1b2c3`)
- 파일 이름 (모두 `*.java`로 변환)
- 프로젝트 이름 (모두 `project-001`로 변환)
- 실제 코드 내용
- IP 주소
- 회사/조직 정보

### 왜 수집하나요?

템플릿 메인테이너(개발자)가 다음을 측정하기 위해:

1. **효과성 측정**
   - Cache 시스템이 실제로 토큰을 절감하는가?
   - Serena Memory가 위반을 줄이는가?
   - Layer별로 어떤 규칙이 자주 위반되는가?

2. **개선 방향 파악**
   - 어떤 Layer에서 문제가 많은가?
   - 어떤 규칙이 너무 엄격한가?
   - 어떤 기능이 실제로 사용되는가?

3. **A/B 테스트**
   - 새 기능이 실제로 효과가 있는가?
   - 버전 업그레이드 시 성능이 개선되는가?

---

## 🔐 개인정보 보호

### 익명화 보장

모든 데이터는 **자동으로 익명화**됩니다:

```python
# 사용자명 익명화
"sangwon-ryu" → "user-a1b2c3d4" (SHA256 해시)

# 파일명 익명화
"OrderDomain.java" → "*.java"
"PaymentService.kt" → "*.kt"

# 프로젝트명 익명화
"my-ecommerce-project" → "project-001"
"company-internal-app" → "project-002"
```

### 전송되는 데이터 예시

**실제 전송 데이터**:
```json
{
  "trace": {
    "id": "session-2025-10-29T10:30:00Z",
    "name": "Claude Session",
    "tags": ["domain"],
    "metadata": {
      "user": "user-a1b2c3d4",
      "project": "project-001",
      "template_version": "v1.0.0"
    }
  },
  "observations": [
    {
      "name": "Cache Injection: domain",
      "metadata": {
        "rules_loaded": 5,
        "estimated_tokens": 2500
      }
    },
    {
      "name": "Code Validation",
      "metadata": {
        "file": "*.java",
        "validation_time_ms": 148,
        "status": "passed"
      }
    }
  ]
}
```

**민감한 정보는 포함되지 않습니다**!

---

## ⚙️ 활성화 방법

### Option 1: 설치 시 선택 (권장)

설치 스크립트 실행 시 물어봅니다:

```bash
bash scripts/install-claude-hooks.sh

# 출력:
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# 📊 텔레메트리 (익명 사용 통계)
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#
# Spring Standards 템플릿 개선을 위해 익명화된 사용 통계를
# 수집하도록 허용하시겠습니까?
#
# 수집 데이터:
#   ✅ 토큰 사용량 (익명)
#   ✅ 검증 시간 (익명)
#   ✅ 컨벤션 위반 통계 (익명)
#   ❌ 사용자 이름 (수집 안 됨)
#   ❌ 파일 이름 (수집 안 됨)
#   ❌ 코드 내용 (수집 안 됨)
#
# 자세한 내용: docs/LANGFUSE_TELEMETRY_GUIDE.md
#
# 텔레메트리를 활성화하시겠습니까? (y/N):
```

### Option 2: 수동 활성화

```bash
# 1. .langfuse.telemetry 파일 생성
echo "enabled=true" > .langfuse.telemetry

# 2. 환경 변수 설정 (선택 사항)
export LANGFUSE_TELEMETRY_ENABLED=true
```

### Option 3: 수동 업로드 (일회성)

```bash
# 텔레메트리 설정으로 로그 집계 및 업로드
python3 scripts/langfuse/aggregate-logs.py --telemetry
python3 scripts/langfuse/upload-to-langfuse.py --telemetry

# 또는 한 번에
python3 scripts/langfuse/aggregate-logs.py --telemetry --output /tmp/data.json && \
python3 scripts/langfuse/upload-to-langfuse.py --telemetry --input /tmp/data.json
```

### Option 4: 실시간 모니터링 (선택사항)

```bash
# 백그라운드에서 5분마다 자동 업로드
bash scripts/langfuse/monitor.sh

# 출력:
# 🚀 LangFuse Monitor Started (Telemetry Mode)
#    Claude logs:  .claude/hooks/logs/hook-execution.jsonl
#    Cascade logs: .pipeline-metrics/metrics.jsonl
#    Interval:     300s
#    Telemetry:    enabled (anonymized)
#    Host:         https://us.cloud.langfuse.com
```

---

## 🚫 비활성화 방법

### 완전 비활성화

```bash
# .langfuse.telemetry 파일 삭제
rm -f .langfuse.telemetry

# 또는
echo "enabled=false" > .langfuse.telemetry
```

### 환경 변수로 비활성화

```bash
export LANGFUSE_TELEMETRY_ENABLED=false
```

---

## 📊 텔레메트리 대시보드

템플릿 메인테이너는 다음 통계를 확인할 수 있습니다:

### 1. 토큰 효율성
```
평균 토큰 사용량:
- Cache 사용 전: 50,000 tokens
- Cache 사용 후: 5,000 tokens (90% 절감)
```

### 2. Layer별 위반 통계
```
컨벤션 위반 Top 5:
1. Domain Layer - Law of Demeter: 23%
2. Application Layer - Transaction 경계: 18%
3. Persistence Layer - Long FK: 15%
4. Domain Layer - Lombok: 12%
5. REST Layer - Validation: 8%
```

### 3. 템플릿 버전별 비교
```
v1.0.0 → v1.1.0 개선:
- 위반 건수: 23회 → 5회 (78% ↓)
- 검증 시간: 561ms → 148ms (73% ↑)
```

---

## ❓ FAQ

### Q1: 텔레메트리 없이도 템플릿을 사용할 수 있나요?

**A**: 물론입니다! 텔레메트리는 **완전히 선택 사항**입니다.

활성화하지 않아도 모든 기능이 정상 작동합니다:
- ✅ Dynamic Hooks
- ✅ Cache 시스템
- ✅ Serena Memory
- ✅ 자동 검증
- ✅ Cascade 통합

### Q2: 텔레메트리를 활성화하면 어떤 이점이 있나요?

**A**: 직접적인 이점은 없지만, **템플릿 개선에 기여**합니다.

더 많은 사용자가 텔레메트리를 활성화할수록:
- ✅ 템플릿이 더 빠르게 개선됩니다
- ✅ 실제 사용 패턴에 기반한 기능이 추가됩니다
- ✅ 버그가 더 빨리 발견됩니다

### Q3: 회사 보안 정책상 외부 데이터 전송이 금지되어 있습니다.

**A**: 문제없습니다. **텔레메트리를 비활성화**하세요.

```bash
echo "enabled=false" > .langfuse.telemetry
```

모든 데이터는 로컬에만 저장되고, 외부로 전송되지 않습니다.

### Q4: 텔레메트리가 성능에 영향을 주나요?

**A**: **거의 없습니다.**

- 로그 집계는 5분 주기로 백그라운드에서 실행
- 네트워크 전송은 비동기로 처리
- 개발 작업에 영향 없음

### Q5: 나중에 텔레메트리를 비활성화할 수 있나요?

**A**: 언제든지 가능합니다.

```bash
# 즉시 비활성화
rm -f .langfuse.telemetry

# 또는
echo "enabled=false" > .langfuse.telemetry
```

### Q6: 어떤 데이터가 전송되는지 직접 확인할 수 있나요?

**A**: 물론입니다!

```bash
# 1. 로그 집계 (업로드 안 함)
python3 scripts/langfuse/aggregate-logs.py \
  --telemetry \
  --output /tmp/telemetry-preview.json

# 2. 전송될 데이터 확인
cat /tmp/telemetry-preview.json | jq
```

---

## 📜 라이선스 및 약관

### 데이터 사용 약관

1. **수집 목적**: 템플릿 개선 및 효과성 측정
2. **보관 기간**: 90일 (LangFuse 기본 정책)
3. **제3자 공유**: 없음
4. **익명화**: 모든 개인정보 자동 제거
5. **철회 권리**: 언제든지 비활성화 가능

### GDPR 준수

이 텔레메트리 시스템은 **GDPR을 준수**합니다:

- ✅ **투명성**: 명확한 안내 제공
- ✅ **동의**: 명시적 옵트인
- ✅ **최소화**: 필요한 데이터만 수집
- ✅ **익명화**: 개인정보 자동 제거
- ✅ **철회**: 언제든지 비활성화 가능

---

## 🙏 감사의 말

텔레메트리를 활성화해주셔서 감사합니다!

당신의 기여로 Spring Standards 템플릿이 더 나아집니다.

---

**생성일**: 2025-10-29
**버전**: 1.0.0
