# Task: Admin Auth, Brand, Category 개발

> 생성일: 2026-02-02
> 브랜치: `feature/admin-auth-brand-category` (dev 기준)

## 개발 범위

### 1. Auth (인증) - 우선 순위 1 ⏳
| 엔드포인트 | v1 (레거시) | v2 (신규) | 상태 |
|-----------|------------|----------|------|
| 로그인 | `/v1/admin/tokens/login` | `/v2/admin/tokens/login` | stub 존재, UseCase 미구현 |
| 로그아웃 | `/v1/admin/tokens/logout` | `/v2/admin/tokens/logout` | stub 존재, UseCase 미구현 |
| 내 정보 | `/v1/admin/users/me` | `/v2/admin/users/me` | 없음, 신규 개발 필요 |

### 2. Brand (브랜드) - 우선 순위 2
| 엔드포인트 | v1 (레거시) | v2 (신규) |
|-----------|------------|----------|
| 목록 조회 | `/v1/admin/brands` | `/v2/admin/brands` |
| 상세 조회 | `/v1/admin/brands/{id}` | `/v2/admin/brands/{id}` |
| 생성 | `/v1/admin/brands` | `/v2/admin/brands` |
| 수정 | `/v1/admin/brands/{id}` | `/v2/admin/brands/{id}` |
| 삭제 | `/v1/admin/brands/{id}` | `/v2/admin/brands/{id}` |

### 3. Category (카테고리) - 우선 순위 3
| 엔드포인트 | v1 (레거시) | v2 (신규) |
|-----------|------------|----------|
| 목록 조회 | `/v1/admin/categories` | `/v2/admin/categories` |
| 상세 조회 | `/v1/admin/categories/{id}` | `/v2/admin/categories/{id}` |
| 생성 | `/v1/admin/categories` | `/v2/admin/categories` |
| 수정 | `/v1/admin/categories/{id}` | `/v2/admin/categories/{id}` |
| 삭제 | `/v1/admin/categories/{id}` | `/v2/admin/categories/{id}` |

## 현재 파일 위치
```
adapter-in/rest-api-admin/src/main/java/com/ryuqq/setof/adapter/in/rest/admin/
├── v2/token/controller/TokenCommandController.java  # Login/Logout stub
└── (TODO) v1/ 레거시 호환 컨트롤러
```

## 다음 작업
1. [ ] Auth UseCase 설계 및 구현
   - LoginUseCase
   - LogoutUseCase
   - GetMyInfoUseCase
2. [ ] v1 레거시 호환 엔드포인트 추가
3. [ ] Brand CRUD 구현
4. [ ] Category CRUD 구현

## 기술 참고
- **Strangler Fig 패턴**: v1 → v2 점진적 마이그레이션
- **API Gateway 라우팅**:
  - `/v1/**` → legacy bootstrap
  - `/v2/**` → 신규 bootstrap

## 세션 재개 명령어
```bash
# 1. 브랜치 확인
git checkout feature/admin-auth-brand-category

# 2. Serena Memory 읽기
# Memory: task-admin-auth-brand-category
```
