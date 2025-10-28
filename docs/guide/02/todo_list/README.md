# KAN-144: 파일 업로드 시스템 - 작업 목록

## 📋 에픽 개요

**에픽**: KAN-144 - 파일 업로드 시스템
**상태**: 해야 할 일
**우선순위**: Medium

## 🎯 전체 진행 상황

- **총 태스크**: 26개
- **완료**: 0개 (0%)
- **진행 중**: 0개 (0%)
- **대기 중**: 26개 (100%)

## 📊 Phase별 분류

### Phase 2A: Multipart Upload
**완료율**: 0% (0/10)
- [⏳] KAN-310: MultipartUpload Aggregate 구현
- [⏳] KAN-311: UploadPart Value Object 구현
- [⏳] KAN-312: UploadSession Aggregate 확장
- [⏳] KAN-313: MultipartUploadJpaAdapter 구현
- [⏳] KAN-314: UploadSessionJpaAdapter 확장
- [⏳] KAN-315: InitMultipartUploadUseCase 구현
- [⏳] KAN-316: GeneratePartPresignedUrlUseCase 구현
- [⏳] KAN-317: MarkPartUploadedUseCase 구현
- [⏳] KAN-318: CompleteMultipartUploadUseCase 구현
- [⏳] KAN-319: UploadController 확장 (Multipart 엔드포인트 4개)

### Phase 2B: External Download
**완료율**: 0% (0/6)
- [⏳] KAN-320: ExternalDownload Aggregate 구현
- [⏳] KAN-321: UploadPolicy Aggregate 구현
- [⏳] KAN-322: PolicyResolverService 구현
- [⏳] KAN-323: StartExternalDownloadUseCase 구현
- [⏳] KAN-324: ExternalDownloadWorker 구현
- [⏳] KAN-325: ExternalDownloadController 구현

### Phase 2C: Events & Batch
**완료율**: 0% (0/10)
- [⏳] KAN-326: UploadSession AbstractAggregateRoot 확장
- [⏳] KAN-327: Domain Events 정의 (4개)
- [⏳] KAN-328: UploadEventPublisher 구현 (Anti-Corruption Layer)
- [⏳] KAN-329: UploadEventMapper 구현
- [⏳] KAN-330: IdempotencyMiddleware 구현
- [⏳] KAN-331: UploadSessionExpirationBatchJob 구현
- [⏳] KAN-332: Multipart Upload 통합 테스트
- [⏳] KAN-333: External Download 통합 테스트
- [⏳] KAN-334: Policy Evaluation 통합 테스트
- [⏳] KAN-335: Event Publishing 통합 테스트

## 📁 상세 문서

- [Phase 2A 상세 태스크](./phase-2a-multipart-upload.md)
- [Phase 2B 상세 태스크](./phase-2b-external-download.md)
- [Phase 2C 상세 태스크](./phase-2c-events-batch.md)

## 🚨 작업 시작 전 준비사항

### 1. Phase 1 완료 확인
Phase 2 시작 전 다음 Phase 1 태스크들이 완료되어야 합니다:
- [ ] KAN-259: 권한 평가 성능 테스트 및 최적화 (Phase 1B)
- [ ] KAN-264: 관찰성 구축 - 메트릭/로깅 (Phase 1C)
- [ ] KAN-266: 성능 회귀 테스트 및 최적화 (Phase 1C)
- [ ] KAN-267: Phase 1 최종 검증 및 배포 준비 (Phase 1C)

### 2. 아키텍처 의존성
Phase 2는 Phase 1의 다음 컴포넌트에 의존합니다:
- ✅ Tenant/Organization Domain Model
- ✅ UserContext Domain Model
- ✅ Permission/Role System
- ✅ Redis Cache Layer
- ✅ CEL ABAC Engine

### 3. 인프라 요구사항
- **AWS S3**: Multipart Upload API
- **Redis**: Idempotency 키 저장, 세션 만료 추적
- **Spring Batch**: 세션 만료 배치 작업
- **Spring Events**: 도메인 이벤트 발행

## 🎓 아키텍처 준수 사항

### 필수 규칙 (Phase 1과 동일)
- ✅ **Lombok 금지**: Pure Java getter/setter 사용
- ✅ **Law of Demeter**: Getter 체이닝 금지
- ✅ **Long FK 전략**: JPA 관계 어노테이션 미사용
- ✅ **Transaction 경계**: `@Transactional` 내 외부 API 호출 금지
- ✅ **헥사고날 아키텍처**: Domain → Application → Adapter 의존성

### Phase 2 추가 규칙
- ✅ **도메인 이벤트**: AbstractAggregateRoot 확장, 이벤트 발행
- ✅ **Anti-Corruption Layer**: 외부 시스템(Spring Events) 격리
- ✅ **Idempotency**: 중복 요청 방지 (Redis 기반)
- ✅ **Batch Job**: Spring Batch, 트랜잭션 청크 단위 처리

### 테스트 요구사항
- Unit Test: 모든 도메인 로직 및 UseCase
- Integration Test: Repository, Controller, E2E 시나리오
- ArchUnit Test: 아키텍처 의존성 규칙 (Phase 1과 동일)

### 성능 목표
- Presigned URL 생성 P95 < 100ms
- Multipart Complete P95 < 500ms (S3 API 호출 포함)
- External Download 시작 P95 < 200ms
- Batch Job 처리 속도 > 100 세션/초

## 📖 핵심 개념

### Multipart Upload
- **목적**: 대용량 파일(>100MB)을 작은 파트로 나눠 업로드
- **장점**: 재시도 가능, 병렬 업로드, 네트워크 효율성
- **흐름**:
  1. Init → uploadId 발급
  2. 각 파트별 Presigned URL 생성
  3. 클라이언트가 직접 S3에 업로드
  4. Complete → S3가 파트 병합

### External Download
- **목적**: 외부 URL에서 파일을 서버가 다운로드하여 S3에 저장
- **장점**: 클라이언트 대역폭 절약, 서버 측 검증
- **흐름**:
  1. Start → 다운로드 작업 등록
  2. Worker가 비동기로 다운로드
  3. 완료 후 UploadSession 생성

### Upload Policy
- **목적**: 업로드 방식 결정 (Direct/Multipart/External)
- **조건**:
  - fileSize >= 100MB → Multipart
  - externalUrl 제공 → External Download
  - else → Direct Upload

### Domain Events
- **목적**: 업로드 생명주기 이벤트 발행 (감사, 알림)
- **이벤트**:
  - UploadSessionCreated
  - MultipartUploadCompleted
  - ExternalDownloadCompleted
  - UploadSessionExpired

## 📞 참고 링크

- [프로젝트 코딩 규칙](../../coding_convention/)
- [DB 스키마](../schema.sql)
- [Phase 1 완료 체크리스트](../../phase-1-completion-checklist.md)
- [AWS S3 Multipart Upload API](https://docs.aws.amazon.com/AmazonS3/latest/userguide/mpuoverview.html)
