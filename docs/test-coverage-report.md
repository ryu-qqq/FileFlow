# FileFlow 테스트 커버리지 상세 보고서

**작성일**: 2025-11-27
**기준**: JaCoCo Report (최신 빌드)

---

## 1. 전체 커버리지 요약

### 1.1 모듈별 Instruction 커버리지

| 모듈 | Instruction | Line | Branch | JaCoCo 기준 | 상태 | 차이 |
|------|-------------|------|--------|-------------|------|------|
| **domain** | 89.7% | 91.5% | 82.9% | 90% | ❌ 미달 | -0.3% |
| **application** | 70.0% | 73.5% | 60.1% | 70% | ✅ 통과 | 0.0% |
| **rest-api** | 29.2% | 27.7% | 13.4% | 30% | ❌ 미달 | -0.8% |
| **persistence-mysql** | 79.7% | 81.0% | 55.4% | 70% | ✅ 통과 | +9.7% |

### 1.2 전체 통계

| 메트릭 | 값 |
|--------|-----|
| **전체 Instruction 커버리지** | 69.4% |
| **전체 Line 커버리지** | 71.5% |
| **전체 Branch 커버리지** | 55.3% |
| **총 테스트 케이스** | 150+ |
| **아키텍처 테스트** | ✅ 전체 통과 |

---

## 2. 모듈별 상세 분석

### 2.1 Domain Layer (89.7%)

**상태**: ⚠️ 목표 미달 (90% 기준, 0.3% 부족)

#### 테스트가 부족한 영역

| 클래스 | 현재 커버리지 | 미달 원인 |
|--------|--------------|----------|
| `FileAsset.delete()` | 0% | 새로 추가된 DELETED 상태 전환 메서드 |
| `FileAsset.getDeletedAt()` | 0% | delete() 호출 후 반환되는 getter |
| `DomainException` (Map 생성자) | 0% | args가 있는 생성자 미사용 |
| `S3Key.isSecure()` 일부 브랜치 | ~50% | 부분적 경로 검증 로직 |

#### 개선 방안

```java
// FileAssetTest에 추가 필요한 테스트
@Test
@DisplayName("FileAsset delete 호출 시 DELETED 상태로 전환된다")
void delete_ShouldTransitionToDeletedStatus() {
    FileAsset fileAsset = createCompletedFileAsset();
    fileAsset.delete();
    assertThat(fileAsset.getStatus()).isEqualTo(FileAssetStatus.DELETED);
    assertThat(fileAsset.getDeletedAt()).isNotNull();
}
```

---

### 2.2 Application Layer (70.0%)

**상태**: ✅ 목표 달성 (70% 기준)

#### 2.2.1 테스트 없는 신규 UseCase 서비스

| 서비스 | 커버리지 | 설명 |
|--------|----------|------|
| `GetUploadSessionService` | 0% | 업로드 세션 단건 조회 |
| `GetUploadSessionsService` | 0% | 업로드 세션 목록 조회 |
| `DeleteFileAssetService` | 0% | 파일 자산 삭제 (Soft Delete) |
| `GenerateDownloadUrlService` | 0% | S3 Presigned Download URL 생성 |
| `BatchGenerateDownloadUrlService` | 0% | 다중 파일 Download URL 일괄 생성 |

#### 2.2.2 테스트 없는 DTO

| DTO | 커버리지 | 설명 |
|-----|----------|------|
| `DeleteFileAssetResponse` | 0% | 삭제 응답 DTO |
| `DownloadUrlResponse` | 0% | 다운로드 URL 응답 DTO |
| `BatchDownloadUrlResponse` | 0% | 일괄 다운로드 URL 응답 DTO |
| `BatchDownloadUrlResponse.FailedDownloadUrl` | 0% | 실패 항목 중첩 클래스 |

#### 2.2.3 커버리지 양호한 영역

| 패키지 | 커버리지 | 비고 |
|--------|----------|------|
| `session.service` | 85%+ | 기존 업로드 세션 서비스 |
| `session.scheduler` | 90%+ | 만료 세션 처리 스케줄러 |
| `download.scheduler` | 100% | Outbox 재시도 스케줄러 |
| `asset.manager` | 66.7% | 2개 중 1개 메서드 미테스트 |

#### 개선 우선순위 (Application)

1. **높음**: 신규 UseCase 서비스 5개 테스트 작성
2. **중간**: Assembler 테스트 강화
3. **낮음**: DTO 팩토리 메서드 테스트

---

### 2.3 REST API Layer (29.2%)

**상태**: ❌ 목표 미달 (30% 기준, 0.8% 부족)

#### 2.3.1 테스트 없는 Controller

| Controller | 커버리지 | 설명 |
|------------|----------|------|
| `UploadSessionQueryController` | 0% | 세션 조회 API (신규) |
| `FileAssetCommandController` | 0% | 파일 삭제/다운로드 URL API (신규) |
| `ExternalDownloadController` | 0% | 외부 다운로드 API |

#### 2.3.2 테스트 없는 Mapper 메서드

| Mapper | 메서드 | 커버리지 |
|--------|--------|----------|
| `UploadSessionApiMapper` | `toGetUploadSessionQuery` | 0% |
| `UploadSessionApiMapper` | `toListUploadSessionsQuery` | 0% |
| `UploadSessionApiMapper` | `toUploadSessionApiResponse` | 0% |
| `UploadSessionApiMapper` | `toUploadSessionDetailApiResponse` | 0% |
| `FileAssetApiMapper` | 전체 메서드 | 0% |
| `ExternalDownloadApiMapper` | 전체 메서드 | 0% |

#### 2.3.3 테스트 없는 Config

| Config | 커버리지 | 설명 |
|--------|----------|------|
| `FilterConfig` | 0% | 필터 등록 설정 |
| `ErrorHandlingConfig` | 0% | 에러 처리 설정 |
| `UserContextSupplierConfig` | 0% | 사용자 컨텍스트 공급자 |

#### 개선 우선순위 (REST API)

1. **높음**: Controller 테스트 (MockMvc 사용)
2. **높음**: Mapper 테스트 (단위 테스트)
3. **중간**: Config 클래스 통합 테스트

---

### 2.4 Persistence Layer (79.7%)

**상태**: ✅ 목표 초과 달성 (70% 기준, +9.7%)

#### 2.4.1 테스트 부족 영역

| Adapter | 메서드 | 커버리지 | 설명 |
|---------|--------|----------|------|
| `FindUploadSessionQueryAdapter` | `findByIdAndTenantId` | 0% | 테넌트별 세션 조회 |
| `FindUploadSessionQueryAdapter` | `findByCriteria` | 0% | 조건별 세션 목록 조회 |
| `FindUploadSessionQueryAdapter` | `countByCriteria` | 0% | 조건별 세션 수 카운트 |
| `FileAssetJpaEntity` | `update()` | 0% | 엔티티 업데이트 메서드 |

#### 2.4.2 커버리지 양호한 영역

| Adapter | 커버리지 | 비고 |
|---------|----------|------|
| `PersistSingleUploadSessionAdapter` | 100% | 단일 업로드 세션 영속화 |
| `PersistMultipartUploadSessionAdapter` | 100% | 멀티파트 업로드 세션 영속화 |
| `PersistCompletedPartAdapter` | 100% | 완료된 파트 영속화 |
| `FindCompletedPartQueryAdapter` | 100% | 파트 조회 |
| `FileAssetQueryAdapter` | 90%+ | 파일 자산 조회 |

---

## 3. 테스트 작성 가이드

### 3.1 UseCase 서비스 테스트 템플릿

```java
@ExtendWith(MockitoExtension.class)
class GetUploadSessionServiceTest {

    @Mock
    private FindUploadSessionQueryPort findUploadSessionQueryPort;

    @Mock
    private FindCompletedPartQueryPort findCompletedPartQueryPort;

    @InjectMocks
    private GetUploadSessionService sut;

    @Nested
    @DisplayName("execute 메서드")
    class Execute {

        @Test
        @DisplayName("존재하는 세션 ID로 조회하면 세션 상세 정보를 반환한다")
        void shouldReturnSessionDetail_whenSessionExists() {
            // given
            var query = GetUploadSessionQuery.of("session-123", 1L);
            var session = createTestSession();
            given(findUploadSessionQueryPort.findByIdAndTenantId(any(), anyLong()))
                .willReturn(Optional.of(session));

            // when
            var result = sut.execute(query);

            // then
            assertThat(result.sessionId()).isEqualTo("session-123");
        }

        @Test
        @DisplayName("존재하지 않는 세션 ID로 조회하면 예외를 던진다")
        void shouldThrowException_whenSessionNotFound() {
            // given
            var query = GetUploadSessionQuery.of("not-exist", 1L);
            given(findUploadSessionQueryPort.findByIdAndTenantId(any(), anyLong()))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.execute(query))
                .isInstanceOf(UploadSessionNotFoundException.class);
        }
    }
}
```

### 3.2 Mapper 테스트 템플릿

```java
class FileAssetApiMapperTest {

    private final FileAssetApiMapper sut = new FileAssetApiMapper();

    @Test
    @DisplayName("GetFileAssetQuery로 변환한다")
    void toGetFileAssetQuery_shouldMapCorrectly() {
        // when
        var result = sut.toGetFileAssetQuery("asset-123", 1L, 2L);

        // then
        assertThat(result.id()).isEqualTo("asset-123");
        assertThat(result.organizationId()).isEqualTo(1L);
        assertThat(result.tenantId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("삭제 Command 변환 시 reason이 null이면 null을 유지한다")
    void toDeleteFileAssetCommand_whenRequestIsNull_shouldUseNullReason() {
        // when
        var result = sut.toDeleteFileAssetCommand("asset-123", null, 1L, 2L);

        // then
        assertThat(result.reason()).isNull();
    }
}
```

### 3.3 Controller 테스트 템플릿 (MockMvc)

```java
@WebMvcTest(FileAssetQueryController.class)
class FileAssetQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetFileAssetUseCase getFileAssetUseCase;

    @MockBean
    private FileAssetApiMapper mapper;

    @Test
    @DisplayName("GET /api/v1/file-assets/{id} - 성공")
    void getFileAsset_shouldReturn200() throws Exception {
        // given
        given(mapper.toGetFileAssetQuery(anyString(), anyLong(), anyLong()))
            .willReturn(GetFileAssetQuery.of("asset-123", 1L, 2L));
        given(getFileAssetUseCase.execute(any()))
            .willReturn(createTestResponse());

        // when & then
        mockMvc.perform(get("/api/v1/file-assets/asset-123")
                .header("X-Tenant-Id", "2")
                .header("X-Organization-Id", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value("asset-123"));
    }
}
```

---

## 4. 커버리지 개선 계획

### Phase 1: 즉시 필요 (JaCoCo 빌드 통과)

**목표**: Domain 90%, REST API 30% 달성

| 작업 | 예상 영향 | 우선순위 |
|------|----------|----------|
| `FileAsset.delete()` 테스트 추가 | Domain +0.5% | 🔴 높음 |
| `FileAssetApiMapper` 테스트 추가 | REST API +2% | 🔴 높음 |
| `UploadSessionApiMapper` 신규 메서드 테스트 | REST API +3% | 🔴 높음 |

### Phase 2: UseCase 테스트 (커버리지 향상)

**목표**: Application 75%+

| 작업 | 예상 영향 | 우선순위 |
|------|----------|----------|
| `GetUploadSessionService` 테스트 | Application +1% | 🟡 중간 |
| `GetUploadSessionsService` 테스트 | Application +1% | 🟡 중간 |
| `DeleteFileAssetService` 테스트 | Application +1% | 🟡 중간 |
| `GenerateDownloadUrlService` 테스트 | Application +1% | 🟡 중간 |
| `BatchGenerateDownloadUrlService` 테스트 | Application +2% | 🟡 중간 |

### Phase 3: Controller 테스트 (품질 향상)

**목표**: REST API 50%+

| 작업 | 예상 영향 | 우선순위 |
|------|----------|----------|
| `UploadSessionQueryController` 테스트 | REST API +5% | 🟡 중간 |
| `FileAssetCommandController` 테스트 | REST API +5% | 🟡 중간 |
| `ExternalDownloadController` 테스트 | REST API +5% | 🟡 중간 |

---

## 5. 테스트 파일 위치 가이드

```
project-root/
├── domain/
│   └── src/test/java/
│       └── com/ryuqq/fileflow/domain/
│           ├── asset/
│           │   ├── aggregate/FileAssetTest.java      ← 추가 필요
│           │   └── vo/FileAssetStatusTest.java       ✅ 존재
│           └── session/
│               └── ...                                ✅ 존재
│
├── application/
│   └── src/test/java/
│       └── com/ryuqq/fileflow/application/
│           ├── asset/
│           │   ├── service/
│           │   │   ├── GetFileAssetServiceTest.java   ✅ 존재
│           │   │   ├── GetFileAssetsServiceTest.java  ✅ 존재
│           │   │   ├── DeleteFileAssetServiceTest.java     ← 신규 필요
│           │   │   ├── GenerateDownloadUrlServiceTest.java ← 신규 필요
│           │   │   └── BatchGenerateDownloadUrlServiceTest.java ← 신규 필요
│           │   └── assembler/
│           │       └── FileAssetQueryAssemblerTest.java    ← 신규 필요
│           └── session/
│               └── service/
│                   ├── GetUploadSessionServiceTest.java    ← 신규 필요
│                   └── GetUploadSessionsServiceTest.java   ← 신규 필요
│
├── adapter-in/rest-api/
│   └── src/test/java/
│       └── com/ryuqq/fileflow/adapter/in/rest/
│           ├── asset/
│           │   ├── controller/
│           │   │   ├── FileAssetQueryControllerTest.java   ✅ 존재
│           │   │   └── FileAssetCommandControllerTest.java ← 신규 필요
│           │   └── mapper/
│           │       └── FileAssetApiMapperTest.java         ← 신규 필요
│           └── session/
│               ├── controller/
│               │   └── UploadSessionQueryControllerTest.java ← 신규 필요
│               └── mapper/
│                   └── UploadSessionApiMapperTest.java     (일부 추가 필요)
│
└── adapter-out/persistence-mysql/
    └── src/test/java/
        └── ...                                             ✅ 대부분 존재
```

---

## 6. 결론 및 권장 사항

### 현재 상태 요약

- **전체 커버리지**: 69.4% (양호)
- **JaCoCo 빌드**: ❌ 실패 (Domain 0.3%, REST API 0.8% 부족)
- **아키텍처 테스트**: ✅ 전체 통과

### 즉시 조치 필요 사항

1. **Domain Layer**: `FileAsset.delete()` 메서드 테스트 1개 추가
2. **REST API Layer**: Mapper 테스트 2개 추가 (`FileAssetApiMapper`, `UploadSessionApiMapper` 신규 메서드)

### 중기 개선 사항

1. 신규 UseCase 서비스 5개 테스트 작성
2. 신규 Controller 2개 테스트 작성
3. Assembler 테스트 강화

### 권장 테스트 작성 순서

```
1. FileAsset.delete() 테스트 (Domain 90% 달성)
   ↓
2. FileAssetApiMapper 테스트 (REST API 30% 달성)
   ↓
3. UseCase 서비스 테스트 5개 (Application 75%+)
   ↓
4. Controller 테스트 (REST API 50%+)
```

---

## 부록: 테스트 실행 명령어

```bash
# 전체 테스트 실행
./gradlew test

# JaCoCo 리포트 생성
./gradlew jacocoTestReport

# 특정 모듈만 테스트
./gradlew :domain:test
./gradlew :application:test
./gradlew :adapter-in:rest-api:test

# 특정 테스트 클래스만 실행
./gradlew :domain:test --tests "*.FileAssetTest"

# 커버리지 검증
./gradlew jacocoTestCoverageVerification
```
