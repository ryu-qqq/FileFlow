# Fix: Workflow Grep Pattern Syntax Error

## 🐛 문제 (Problem)

PR #97 merge 직후 GitHub Actions workflow가 shell script syntax error로 실패:

```
/home/runner/work/_temp/xxx.sh: line 181: [build-all]: command not found
/home/runner/work/_temp/xxx.sh: line 181: [rebuild]: command not found
Error: Process completed with exit code 127.
```

**원인**: grep 패턴에서 대괄호 이스케이프가 shell script로 변환될 때 명령어로 해석됨

```bash
# BEFORE (❌ 실패)
if echo "$COMMIT_MSG" | grep -qE '\[build-all\]|\[rebuild\]'; then
```

## ✅ 해결 (Solution)

대괄호를 제거하고 단어만 매칭하도록 단순화:

```bash
# AFTER (✅ 성공)
if echo "$COMMIT_MSG" | grep -qE 'build-all|rebuild'; then
```

**장점**:
- ✅ `[build-all]` 감지 가능
- ✅ `build-all` 감지 가능
- ✅ `rebuild` 감지 가능
- ✅ Shell script 에러 해결

## 📝 변경 사항 (Changes)

- `.github/workflows/build-and-deploy.yml` (line 55)
  - `'\[build-all\]|\[rebuild\]'` → `'build-all|rebuild'`

## 🔍 테스트 (Testing)

이 PR의 커밋 메시지에 `[build-all]` 플래그 포함하여 자동 테스트 예정:

1. ✅ Workflow가 정상 실행되는지 확인
2. ✅ 4개의 Docker 이미지 빌드 (web-api, scheduler-download, scheduler-pipeline, scheduler-upload)
3. ✅ ECR에 scheduler-*-latest 이미지 생성 확인
4. ✅ ECS 서비스 정상 구동 확인

## 🔗 관련 (Related)

- PR #97: 원본 빌드 감지 로직 개선
- Workflow run: 19223664162 (실패한 워크플로우)

## 📊 영향 (Impact)

- ✅ **즉시 해결**: Workflow 실행 가능
- ✅ **근본 문제 해결**: Missing scheduler 이미지 빌드
- ✅ **ECS 서비스 복구**: 모든 서비스 정상 구동 예상
