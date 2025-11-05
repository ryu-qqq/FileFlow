# LangFuse 통합 시스템

Claude Code의 개발 효율을 LangFuse로 추적하고 분석합니다.

---

## 🎯 목적

1. **Hook 로그 추적**: 규칙 주입, 검증 등 Hook 실행 이력
2. **큐 메트릭 추적**: 작업 시간, 코드 생산성, 예측 정확도
3. **통합 분석**: Hook + 큐 데이터 병합하여 완전한 개발 효율 분석
4. **시각화**: LangFuse 대시보드에서 트렌드 및 개선 사항 확인

---

## 📁 파일 구조

```
langfuse/
├── scripts/
│   ├── log-to-langfuse.py          # Hook 로그 → JSONL + LangFuse (실시간)
│   ├── upload-to-langfuse.py       # Hook 로그 → LangFuse (배치)
│   └── aggregate-queue-metrics.py  # Hook + 큐 → LangFuse (통합) ⭐ NEW
│
└── data/
    ├── langfuse-data.json          # Hook 로그 변환 결과
    └── queue-metrics.json          # Hook + 큐 통합 메트릭 ⭐ NEW
```

---

## 🚀 사용 방법

### 1. 환경 변수 설정

```bash
export LANGFUSE_PUBLIC_KEY="pk-lf-..."
export LANGFUSE_SECRET_KEY="sk-lf-..."
export LANGFUSE_HOST="https://us.cloud.langfuse.com"
```

### 2. 통합 메트릭 업로드 (권장) ⭐

Hook 로그 + 큐 데이터를 병합하여 완전한 개발 효율 분석:

```bash
# Slash Command 사용
/upload-queue-metrics

# 또는 직접 실행
python3 langfuse/scripts/aggregate-queue-metrics.py

# Dry-run (업로드 X, 집계만)
python3 langfuse/scripts/aggregate-queue-metrics.py --dry-run
```

---

## 📊 효율성 점수 (0-100)

### 계산 요소

1. **규칙 주입 효율** (25점) - 이벤트당 주입된 규칙 수
2. **검증 통과율** (25점) - 전체 이벤트 대비 검증 통과 비율
3. **예상 시간 정확도** (30점) - Claude 예측 vs 실제 소요 시간
4. **코드 생산성** (20점) - 시간당 생성 코드 라인 수

### 점수 해석

- **90-100점**: 🌟 탁월
- **80-89점**: ✅ 우수
- **70-79점**: 👍 양호
- **60-69점**: ⚠️ 보통
- **0-59점**: ❌ 미흡

---

**✅ 이 시스템으로 Claude Code의 개발 효율을 정량적으로 측정하고 개선할 수 있습니다!**
