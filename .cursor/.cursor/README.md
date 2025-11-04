# Cursor Configuration

이 디렉토리는 Cursor IDE의 프로젝트별 설정을 포함합니다.

## 디렉토리 구조

- `docs/` - 코딩 컨벤션 문서 (docs/coding_convention/의 심볼릭 링크)
- `rules/` - 규칙 캐시

## 코딩 컨벤션 문서

`docs/` 디렉토리의 모든 문서는 `docs/coding_convention/`의 심볼릭 링크입니다.
원본 문서를 수정하면 Cursor Docs에서도 자동으로 반영됩니다.

## 프로젝트 규칙

프로젝트 루트의 `.cursorrules` 파일이 Cursor에서 자동으로 인식됩니다.

## 주요 규칙 요약

### Zero-Tolerance (절대 금지)
1. **Lombok 금지** - Pure Java getter/setter 직접 작성
2. **Law of Demeter** - Getter 체이닝 금지, Tell Don't Ask
3. **Long FK Strategy** - JPA 관계 매핑 금지, Long FK 사용
4. **Transaction 경계** - @Transactional 내 외부 API 호출 금지
5. **Javadoc 필수** - 모든 public 클래스/메서드에 Javadoc

### 레이어별 규칙
- **Domain Layer**: Aggregate Root, Value Object, Domain Event, Factory Pattern
- **Application Layer**: UseCase Single Responsibility, Command/Query 분리, @Transactional 경계, Assembler
- **Persistence Layer**: CQRS, QueryDSL, N+1 방지
- **REST API Layer**: Controller Thin, GlobalExceptionHandler, ApiResponse 표준화
