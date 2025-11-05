# Hook 실행 로그

Dynamic Hooks 시스템의 실행 로그가 저장되는 디렉토리입니다.

## 📂 로그 파일

- **hook-execution.jsonl**: Hook 실행 로그 (JSONL 형식, LangFuse 호환)
- **current-session.json**: 현재 세션 메타데이터 (LangFuse 세션 추적용)

## 🔍 로그 확인

```bash
# 실시간 모니터링
tail -f .claude/hooks/logs/hook-execution.jsonl

# 로그 분석
python3 .claude/hooks/scripts/summarize-hook-logs.py
```

**관련 문서**: [Hook README](../README.md) | [LangFuse Guide](../../../langfuse/README.md)
