# Epic 3 & Epic 4 분석 및 실행 계획

## 📋 Executive Summary

- **총 이슈 수**: 80개 (완료 제외)
- **실제 작업**: 68개 (중복 12개 제거)
- **중복 태스크**: KAN-100 ~ KAN-111 (Epic 4 중복)
- **핵심 의존성**: Epic 4 → Epic 3 (이미지 파이프라인)

---

## 🚨 Critical Issues

### 1. 중복 태스크 발견 (12개)

Epic 4의 태스크가 완전히 중복되어 있습니다:

| 원본 (보존) | 중복 (삭제 필요) | 태스크명 |
|-----------|----------------|---------|
| KAN-52 | KAN-100 | HTML 처리 Domain 모델 구현 |
| KAN-53 | KAN-101 | HTML 처리 Port 인터페이스 정의 |
| KAN-54 | KAN-102 | HTML 파싱 UseCase 구현 |
| KAN-55 | KAN-103 | 외부 이미지 다운로드 UseCase 구현 |
| KAN-56 | KAN-104 | HTML 이미지 최적화 UseCase 구현 |
| KAN-57 | KAN-105 | 최적화된 HTML 생성 UseCase 구현 |
| KAN-58 | KAN-106 | Jsoup HTML 파서 Adapter 구현 |
| KAN-59 | KAN-107 | HTTP 이미지 다운로더 Adapter 구현 |
| KAN-60 | KAN-108 | S3 HTML 저장소 Adapter 구현 |
| KAN-61 | KAN-109 | 데이터베이스 스키마 설계 및 구현 |
| KAN-62 | KAN-110 | HTML 처리 API Controller 구현 |
| KAN-63 | KAN-111 | 통합 테스트 및 문서화 |

**권장 조치**:
- KAN-100 ~ KAN-111 삭제
- KAN-52 ~ KAN-63 유지

---

## 📊 Epic 구조 분석

### Epic 3: 이미지 처리 파이프라인 (23개 태스크)

**범위**: KAN-27 ~ KAN-50
**목적**: 독립적인 이미지 파일 처리 엔진 (재사용 가능한 컴포넌트)

#### 태스크 분류

**1. Foundation (2개)**
- KAN-28: 이미지 최적화 Domain 모델 설계
- KAN-43: processing_job 테이블 설계 및 생성

**2. 이미지 최적화 (3개)**
- KAN-29: WebP 변환 서비스 구현
- KAN-30: 이미지 압축 로직 구현 (품질 90%)
- KAN-31: 이미지 메타데이터 처리 (EXIF 제거/유지)

**3. 썸네일 생성 (4개)**
- KAN-32: 썸네일 생성 전략 설계 및 구현
- KAN-33: 다중 크기 썸네일 생성 (Small: 300x300, Medium: 800x800)
- KAN-34: 리사이징 알고리즘 최적화
- KAN-35: 썸네일 파일 관계 저장 (file_relationship)

**4. OCR 처리 (4개) - 선택적**
- KAN-36: AWS Textract 통합 설정
- KAN-37: 이미지 텍스트 추출 서비스 구현
- KAN-38: 상품 정보 파싱 로직 (재질, 원산지, 세탁방법, 사이즈)
- KAN-39: OCR 결과 저장 및 메타데이터 연동

**5. CDN 배포 (3개)**
- KAN-40: CloudFront 배포 설정 (인프라)
- KAN-41: CDN 자동 배포 로직 구현
- KAN-42: 캐시 무효화 서비스 구현

**6. 파이프라인 오케스트레이션 (5개)**
- KAN-44: 파이프라인 오케스트레이션 구현
- KAN-45: 작업 상태 추적 및 관리
- KAN-46: 재시도 및 에러 핸들링
- KAN-47: SQS/SNS 파이프라인 설정
- KAN-48: 비동기 메시지 핸들러 구현

**7. 테스트 & 최적화 (2개)**
- KAN-49: 통합 테스트 작성
- KAN-50: 성능 테스트 및 최적화

---

### Epic 4: HTML 처리 파이프라인 (16개 태스크)

**범위**: KAN-51 ~ KAN-63, KAN-96 ~ KAN-99
**목적**: HTML 내 임베디드 이미지 처리 엔진 (Epic 3 재사용)

#### 태스크 분류

**1. Foundation (2개)**
- KAN-52: HTML 처리 Domain 모델 구현
- KAN-53: HTML 처리 Port 인터페이스 정의

**2. Core UseCase (4개)**
- KAN-54: HTML 파싱 UseCase 구현
- KAN-55: 외부 이미지 다운로드 UseCase 구현
- KAN-56: HTML 이미지 최적화 UseCase 구현
- KAN-57: 최적화된 HTML 생성 UseCase 구현

**3. Adapter Layer (3개)**
- KAN-58: Jsoup HTML 파서 Adapter 구현
- KAN-59: HTTP 이미지 다운로더 Adapter 구현
- KAN-60: S3 HTML 저장소 Adapter 구현

**4. Persistence & API (2개)**
- KAN-61: 데이터베이스 스키마 설계 및 구현
- KAN-62: HTML 처리 API Controller 구현

**5. Testing (1개)**
- KAN-63: 통합 테스트 및 문서화

**6. Advanced Features (4개)**
- KAN-96: HTML 처리 오케스트레이터 UseCase 구현
- KAN-97: **Epic 3 이미지 파이프라인 연동 어댑터 구현** ⚠️ CRITICAL
- KAN-98: HTML 처리 상태 전이 관리 컴포넌트 구현
- KAN-99: HTML 처리 실패 복구 메커니즘 구현

---

## 🔗 Epic 3 ↔ Epic 4 연관성

### ✅ 중복 없음 (역할 분리)

Epic 3과 Epic 4는 **기능적으로 중복되지 않습니다**:

- **Epic 3**: 이미지 처리 **엔진** (재사용 가능한 라이브러리)
- **Epic 4**: HTML 처리 **클라이언트** (Epic 3 호출하여 사용)

### 🔑 핵심 연동 포인트

**KAN-97: Epic 3 이미지 파이프라인 연동 어댑터**

```
HTML 처리 흐름:
1. HTML 파싱 (KAN-54, KAN-58)
2. 이미지 URL 추출
3. 이미지 다운로드 (KAN-55, KAN-59)
4. → Epic 3 파이프라인 호출 (KAN-97) ⚠️
   - WebP 변환 (KAN-29)
   - 압축 (KAN-30)
   - 썸네일 생성 (KAN-32~35)
5. 최적화된 HTML 재생성 (KAN-57)
6. S3 저장 (KAN-60)
```

### 📦 공유 인프라

| 리소스 | Epic 3 | Epic 4 | 공유 방식 |
|--------|--------|--------|----------|
| **CDN** | KAN-40~42 | ✓ | CloudFront 배포 공통 사용 |
| **메타데이터** | KAN-17 | ✓ | file_metadata 테이블 공유 |
| **오케스트레이션** | KAN-43~48 | KAN-96~99 | processing_job 기반 상태 관리 |
| **이미지 처리** | KAN-29~35 | KAN-97 | Epic 3 API 호출 방식 |

---

## 🎯 최적 처리 순서

### 전체 실행 계획

```
Phase 1: Epic 3 (필수 선행)
  ↓
Phase 2: Epic 4 (Epic 3 의존)
  ↓
Phase 3: Epic 6 (병렬 가능)
```

---

### Phase 1: Epic 3 - 이미지 처리 파이프라인

**전제조건**: Epic 1 (정책), Epic 2 (업로드) 완료

#### Step 1: Foundation (병렬 가능)
- **KAN-28**: Domain 모델 설계
- **KAN-43**: processing_job 테이블 생성

**예상 기간**: 2일
**의존성**: 없음 (병렬 진행 가능)

#### Step 2: Core Processing (순차)
1. **KAN-29**: WebP 변환 서비스
2. **KAN-30**: 이미지 압축 (품질 90%)
3. **KAN-31**: EXIF 메타데이터 처리
4. **KAN-32**: 썸네일 생성 전략
5. **KAN-33**: 다중 크기 썸네일 생성
6. **KAN-34**: 리사이징 알고리즘 최적화
7. **KAN-35**: 썸네일 파일 관계 저장

**예상 기간**: 5일
**의존성**: Step 1 완료 후

#### Step 3: Infrastructure (병렬 가능)

**Group A: CDN**
- **KAN-40**: CloudFront 배포 설정
- **KAN-41**: CDN 자동 배포 로직
- **KAN-42**: 캐시 무효화 서비스

**Group B: Orchestration**
- **KAN-44**: 파이프라인 오케스트레이션
- **KAN-45**: 작업 상태 추적
- **KAN-46**: 재시도 및 에러 핸들링
- **KAN-47**: SQS/SNS 파이프라인 설정
- **KAN-48**: 비동기 메시지 핸들러

**예상 기간**: 4일
**의존성**: Step 2 완료 후, Group A/B 병렬 진행

#### Step 4: Advanced Features (선택적, 후순위)
- **KAN-36**: AWS Textract 통합
- **KAN-37**: 텍스트 추출 서비스
- **KAN-38**: 상품 정보 파싱
- **KAN-39**: OCR 결과 저장

**예상 기간**: 3일
**의존성**: Step 3 완료 후 (나중에 추가 가능)

#### Step 5: Validation
- **KAN-49**: 통합 테스트
- **KAN-50**: 성능 테스트

**예상 기간**: 2일
**의존성**: Step 3 완료 후 (Step 4 없이도 진행 가능)

**Epic 3 총 예상 기간**: 13일 (OCR 제외 시 10일)

---

### Phase 2: Epic 4 - HTML 처리 파이프라인

**전제조건**: Epic 3 완료 (최소 KAN-28~35, KAN-40~48)

#### Step 1: Foundation (순차)
1. **KAN-52**: HTML Domain 모델
2. **KAN-53**: Port 인터페이스

**예상 기간**: 1일
**의존성**: Epic 3 Step 1 완료

#### Step 2: Core Implementation (순차)

**Group A: Parsing**
- **KAN-54**: HTML 파싱 UseCase
- **KAN-58**: Jsoup Adapter

**Group B: Download**
- **KAN-55**: 외부 이미지 다운로드 UseCase
- **KAN-59**: HTTP 다운로더 Adapter

**예상 기간**: 3일
**의존성**: Step 1 완료 후

#### Step 3: Epic 3 Integration ⚠️ CRITICAL
- **KAN-97**: Epic 3 이미지 파이프라인 연동 어댑터

이 태스크는 Epic 3의 다음 기능을 재사용:
- KAN-29~31: 이미지 최적화
- KAN-32~35: 썸네일 생성
- KAN-40~42: CDN 배포

**예상 기간**: 2일
**의존성**:
- Epic 3 Step 2, 3 완료 필수
- Epic 4 Step 2 완료

#### Step 4: HTML Optimization (순차)
1. **KAN-56**: HTML 이미지 최적화 UseCase
2. **KAN-57**: 최적화된 HTML 생성 UseCase
3. **KAN-60**: S3 HTML 저장소 Adapter

**예상 기간**: 2일
**의존성**: Step 3 완료 후

#### Step 5: Orchestration (순차)
1. **KAN-96**: HTML 처리 오케스트레이터
2. **KAN-98**: 상태 전이 관리 컴포넌트
3. **KAN-99**: 실패 복구 메커니즘

**예상 기간**: 3일
**의존성**: Step 4 완료 후

#### Step 6: Persistence & API (병렬 가능)
- **KAN-61**: 데이터베이스 스키마
- **KAN-62**: API Controller

**예상 기간**: 2일
**의존성**: Step 5 완료 후

#### Step 7: Validation
- **KAN-63**: 통합 테스트 및 문서화

**예상 기간**: 2일
**의존성**: Step 6 완료 후

**Epic 4 총 예상 기간**: 15일

---

### Phase 3: Epic 6 - 인프라 & 모니터링 (병렬 가능)

**범위**: KAN-113 ~ KAN-130
**특징**: Epic 3/4와 독립적, 인프라팀 별도 진행 가능

**예상 기간**: 10일 (병렬 진행 시)

---

## 📅 타임라인 시뮬레이션

### 시나리오 1: 순차 진행 (보수적)

```
Week 1-2:   Epic 3 Step 1-2 (7일)
Week 2-3:   Epic 3 Step 3 (4일)
Week 3:     Epic 3 Step 5 (2일) - OCR 제외
Week 4:     Epic 4 Step 1-2 (4일)
Week 4-5:   Epic 4 Step 3-4 (4일)
Week 5-6:   Epic 4 Step 5-7 (7일)

총 소요 기간: 6주 (30 영업일)
```

### 시나리오 2: 최적화 진행 (권장)

```
Week 1-2:   Epic 3 Step 1-3 (10일) + Epic 6 시작 (병렬)
Week 3:     Epic 3 Step 5 (2일) + Epic 6 진행
Week 3-4:   Epic 4 Step 1-3 (6일)
Week 4-5:   Epic 4 Step 4-7 (9일)
Week 5-6:   Epic 6 완료 + Epic 3 Step 4 (OCR, 선택)

총 소요 기간: 5주 (25 영업일)
```

---

## 🚦 의존성 체크리스트

### Epic 4 시작 전 필수 완료 항목

- [ ] **KAN-28**: Domain 모델 (Epic 3)
- [ ] **KAN-29~31**: 이미지 최적화 (Epic 3)
- [ ] **KAN-32~35**: 썸네일 생성 (Epic 3)
- [ ] **KAN-40~42**: CDN 배포 (Epic 3)
- [ ] **KAN-43~48**: 파이프라인 오케스트레이션 (Epic 3)

### Epic 4 KAN-97 시작 전 필수 확인

- [ ] Epic 3 이미지 파이프라인 API 문서화 완료
- [ ] Epic 3 통합 테스트 통과 (KAN-49)
- [ ] Epic 3 성능 검증 완료 (KAN-50)

---

## 💡 권장 사항

### 1. 즉시 조치 필요

**중복 태스크 정리**
```bash
# Jira에서 삭제할 이슈
KAN-100, KAN-101, KAN-102, KAN-103, KAN-104, KAN-105,
KAN-106, KAN-107, KAN-108, KAN-109, KAN-110, KAN-111

# 또는 "Duplicate" 상태로 전환
```

**의존성 링크 설정**
```
KAN-97 → blocks → KAN-56
KAN-97 → is blocked by → KAN-29, KAN-30, KAN-31, KAN-32, KAN-33, KAN-34, KAN-35
Epic 4 → is blocked by → Epic 3
```

### 2. 우선순위 재조정

| Priority | Epic | 시작 조건 |
|----------|------|---------|
| **P1** | Epic 3 | Epic 1, 2 완료 |
| **P2** | Epic 4 | Epic 3 완료 |
| **P3** | Epic 6 | 병렬 진행 가능 |

### 3. OCR 기능 후순위 처리

**KAN-36~39 (OCR 처리)는 선택적**:
- Epic 3 핵심 기능과 독립적
- Epic 4 시작에 영향 없음
- 나중에 추가해도 파이프라인 변경 최소화

**권장**: Epic 4 완료 후 추가

### 4. 팀 분리 전략

```
Team A (Backend): Epic 3 → Epic 4
Team B (Infra):   Epic 6 (병렬)
```

### 5. 리스크 관리

**High Risk Areas**:
- **KAN-97**: Epic 3/4 통합 포인트 (충분한 테스트 필요)
- **KAN-44~48**: 파이프라인 오케스트레이션 (복잡도 높음)
- **KAN-96~99**: HTML 오케스트레이션 (Epic 3 의존성)

**Mitigation**:
- KAN-97 프로토타입 조기 작성
- Epic 3 완료 후 1주일 버퍼 추가
- Epic 3/4 간 API 인터페이스 사전 합의

---

## 📊 진행률 추적

### 현재 상태 (2025-10-13 기준)

| Epic | 완료 | 진행중 | 대기 | 총계 | 진행률 |
|------|------|--------|------|------|--------|
| Epic 1 | 8 | 0 | 0 | 8 | 100% ✅ |
| Epic 2 | 16 | 0 | 0 | 16 | 100% ✅ |
| Epic 3 | 0 | 0 | 23 | 23 | 0% |
| Epic 4 | 0 | 0 | 16 | 16 | 0% |
| Epic 6 | 0 | 0 | 18 | 18 | 0% |
| **Total** | **24** | **0** | **57** | **81** | **30%** |

*주: 중복 제거 후 실제 작업 68개*

### 목표 마일스톤

- **Week 3**: Epic 3 Core 완료 (KAN-28~35)
- **Week 4**: Epic 3 Infrastructure 완료 (KAN-40~48)
- **Week 6**: Epic 4 Core 완료 (KAN-52~60)
- **Week 8**: Epic 4 완료 (KAN-61~63, KAN-96~99)

---

## 🔍 추가 분석 필요 사항

### 1. Epic 3 이미지 처리 API 스펙

KAN-97 개발을 위해 Epic 3에서 제공해야 할 API:

```java
// 예상 인터페이스
public interface ImageProcessingService {
    // 이미지 최적화
    OptimizedImageResult optimizeImage(ImageFile source, OptimizationConfig config);

    // 썸네일 생성
    List<ThumbnailResult> generateThumbnails(ImageFile source, List<ThumbnailSize> sizes);

    // OCR 처리 (선택적)
    OcrResult extractText(ImageFile source);

    // CDN 배포
    CdnUrl deployCdn(ImageFile optimized);
}
```

### 2. processing_job 스키마 확장

Epic 4를 위한 HTML 처리 상태 추가 필요:

```sql
ALTER TABLE processing_job ADD COLUMN job_type VARCHAR(50);
-- 'IMAGE_PROCESSING', 'HTML_PROCESSING'

ALTER TABLE processing_job ADD COLUMN parent_job_id BIGINT;
-- HTML 처리 → 여러 이미지 처리 jobs 관계
```

### 3. 테스트 전략

**Epic 3 단위 테스트**:
- 각 이미지 처리 단계 개별 테스트
- 파이프라인 통합 테스트
- 성능 테스트 (처리 시간, 메모리)

**Epic 4 통합 테스트**:
- Epic 3 API Mock 테스트
- Epic 3 실제 연동 테스트
- E2E 시나리오 테스트

---

## 📞 Stakeholder Communication

### 개발팀 공유 사항

1. **Epic 3 우선 진행** 필수 (Epic 4 블로킹)
2. **중복 태스크 12개** 정리 필요
3. **KAN-97**이 Epic 3/4 통합의 핵심
4. OCR 기능은 후순위 가능

### Product Owner 확인 필요

- [ ] Epic 4 시작 전 Epic 3 완료 대기 가능 여부
- [ ] OCR 기능(KAN-36~39) 필수 여부
- [ ] 중복 태스크(KAN-100~111) 삭제 승인
- [ ] Epic 6 병렬 진행 리소스 확보 가능 여부

---

## 📚 참고 문서

- Epic 1: 테넌트 정책 관리 시스템 (완료)
- Epic 2: 파일 업로드 & 저장 (완료)
- [동시성 및 중복 메시지 처리](./concurrency-and-duplicate-message-handling.md)

---

**문서 작성일**: 2025-10-13
**분석 대상**: KAN 프로젝트 Epic 3, Epic 4
**총 이슈 수**: 80개 (중복 제거 후 68개)
