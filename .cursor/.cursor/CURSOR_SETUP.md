# Cursor IDE 설정 가이드

## Rules 추가 방법

Cursor IDE에서 Rules를 추가하려면:

1. **Cursor Settings 열기**
   - `Cmd + ,` (Mac) 또는 `Ctrl + ,` (Windows/Linux)
   - 또는 `Cursor` → `Settings`

2. **Features → Rules로 이동**
   - 왼쪽 메뉴에서 `Features` 클릭
   - `Rules` 섹션 선택

3. **Project Rules 추가**
   - "Add Rule" 또는 "+" 버튼 클릭
   - 파일 경로 선택: `.cursorrules`
   - 또는 직접 텍스트로 추가

4. **User Rules에 추가 (선택사항)**
   - User Rules 섹션에서도 동일하게 `.cursorrules` 내용 추가 가능

## Docs 추가 방법

Cursor IDE에서 Docs를 추가하려면:

1. **Cursor Settings 열기**
   - `Cmd + ,` (Mac) 또는 `Ctrl + ,` (Windows/Linux)

2. **Features → Docs로 이동**
   - 왼쪽 메뉴에서 `Features` 클릭
   - `Docs` 섹션 선택

3. **디렉토리 추가**
   - "Add Directory" 버튼 클릭
   - `.cursor/docs` 디렉토리 선택
   - 또는 프로젝트 루트의 `docs/coding_convention/` 디렉토리 선택

4. **자동 인덱싱 확인**
   - Cursor가 마크다운 파일을 자동으로 스캔합니다
   - 인덱싱이 완료되면 Docs 패널에서 확인 가능

## 빠른 확인 방법

### Rules 확인
- Cursor Chat에서 `@rules` 입력
- 또는 Settings → Features → Rules에서 목록 확인

### Docs 확인
- Cursor Chat에서 `@docs` 입력
- 또는 Settings → Features → Docs에서 목록 확인
- 또는 사이드바의 "Docs" 아이콘 클릭

## 트러블슈팅

### Rules가 보이지 않는 경우
1. `.cursorrules` 파일이 프로젝트 루트에 있는지 확인
2. 파일 이름이 정확히 `.cursorrules`인지 확인 (확장자 없음)
3. Cursor를 완전히 재시작 (종료 후 다시 시작)
4. Settings → Features → Rules에서 수동으로 추가

### Docs가 보이지 않는 경우
1. `.cursor/docs` 디렉토리가 존재하는지 확인
2. 마크다운 파일이 있는지 확인 (`find .cursor/docs -name "*.md"`)
3. Settings → Features → Docs에서 수동으로 디렉토리 추가
4. Cursor를 완전히 재시작

## 현재 설정된 파일

- **Rules 파일**: `.cursorrules` (프로젝트 루트)
- **Docs 디렉토리**: `.cursor/docs/` (151개 마크다운 파일 심볼릭 링크)
- **원본 Docs**: `docs/coding_convention/`

## 참고

- `.cursor/docs/`의 모든 파일은 `docs/coding_convention/`의 심볼릭 링크입니다
- 원본 파일을 수정하면 Cursor Docs에도 자동으로 반영됩니다
- 심볼릭 링크 대신 복사를 원하면 `cp -r docs/coding_convention .cursor/docs` 실행







