# 📤 업로드 관리 테이블 명세

## 1. upload_sessions (업로드 세션)

### 테이블 설명
파일 업로드 프로세스를 관리하는 핵심 테이블입니다. 각 업로드 요청에 대한 세션을 생성하고 상태를 추적합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 세션 ID |
| session_id | VARCHAR(36) | UK, NOT NULL | UUID() | 세션 고유 식별자 (UUID) |
| tenant_id | VARCHAR(50) | NOT NULL | - | 테넌트 ID (tenants 참조) |
| organization_id | BIGINT | NOT NULL | - | 조직 ID (organizations 참조) |
| user_context_id | BIGINT | NOT NULL | - | 사용자 컨텍스트 ID (user_contexts 참조) |
| policy_id | BIGINT | NOT NULL | - | 업로드 정책 ID (upload_policies 참조) |
| upload_type | ENUM('DIRECT_PRESIGNED', 'DIRECT_API', 'EXTERNAL_URL', 'BATCH') | NOT NULL | - | 업로드 타입 |
| upload_method | ENUM('SINGLE', 'MULTIPART', 'CHUNKED', 'STREAMING') | NOT NULL | 'SINGLE' | 업로드 방식 |
| status | ENUM('INITIALIZED', 'UPLOADING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'EXPIRED') | NOT NULL | 'INITIALIZED' | 세션 상태 |
| external_url | VARCHAR(2048) | NULL | NULL | 외부 URL (EXTERNAL_URL 타입) |
| external_headers | JSON | NULL | NULL | 외부 URL 요청 헤더 |
| source_info | JSON | NULL | '{}' | 소스 정보 (IP, User-Agent 등) |
| session_config | JSON | NULL | '{}' | 세션 설정 |
| presigned_url | TEXT | NULL | NULL | Presigned URL |
| presigned_url_expires_at | DATETIME | NULL | NULL | Presigned URL 만료 시각 |
| total_files | INT | NOT NULL | 0 | 전체 파일 수 |
| uploaded_files | INT | NOT NULL | 0 | 업로드된 파일 수 |
| total_size | BIGINT | NOT NULL | 0 | 전체 크기 (bytes) |
| uploaded_size | BIGINT | NOT NULL | 0 | 업로드된 크기 (bytes) |
| multipart_upload_id | VARCHAR(255) | NULL | NULL | S3 멀티파트 업로드 ID |
| total_parts | INT | NULL | NULL | 전체 파트 수 (멀티파트) |
| uploaded_parts | INT | NULL | 0 | 업로드된 파트 수 |
| checksum_algorithm | ENUM('MD5', 'SHA256', 'SHA1', 'CRC32') | NULL | 'SHA256' | 체크섬 알고리즘 |
| expected_checksum | VARCHAR(64) | NULL | NULL | 예상 체크섬 |
| idempotency_key | VARCHAR(255) | UK, NULL | NULL | 멱등성 키 |
| retry_count | INT | NOT NULL | 0 | 재시도 횟수 |
| error_message | TEXT | NULL | NULL | 에러 메시지 |
| error_code | VARCHAR(50) | NULL | NULL | 에러 코드 |
| callback_url | VARCHAR(500) | NULL | NULL | 완료 콜백 URL |
| callback_status | ENUM('PENDING', 'SUCCESS', 'FAILED', 'NONE') | NULL | 'NONE' | 콜백 상태 |
| metadata | JSON | NULL | '{}' | 추가 메타데이터 |
| version | INT | NOT NULL | 1 | 버전 (낙관적 락) |
| started_at | DATETIME | NULL | NULL | 업로드 시작 시각 |
| completed_at | DATETIME | NULL | NULL | 완료 시각 |
| expires_at | DATETIME | NOT NULL | - | 세션 만료 시각 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_session_id (session_id)
UNIQUE KEY uk_idempotency_key (idempotency_key)
INDEX idx_tenant_org (tenant_id, organization_id, created_at DESC)
INDEX idx_user_context_id (user_context_id, status, created_at DESC)
INDEX idx_policy_id (policy_id)
INDEX idx_status (status, created_at DESC)
INDEX idx_upload_type (upload_type, status)
INDEX idx_expires_at (expires_at)
INDEX idx_created_at (created_at DESC)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

---

## 2. upload_policies (업로드 정책)

### 테이블 설명
테넌트 및 조직별 업로드 정책을 정의합니다. 파일 크기, 타입, 처리 규칙 등을 설정합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 정책 ID |
| tenant_id | VARCHAR(50) | NOT NULL | - | 테넌트 ID (tenants 참조) |
| organization_id | BIGINT | NULL | NULL | 조직 ID (organizations 참조, NULL=테넌트 기본) |
| policy_code | VARCHAR(50) | UK, NOT NULL | - | 정책 코드 |
| policy_name | VARCHAR(100) | NOT NULL | - | 정책명 |
| description | TEXT | NULL | NULL | 정책 설명 |
| policy_type | ENUM('DEFAULT', 'CUSTOM', 'OVERRIDE') | NOT NULL | 'CUSTOM' | 정책 타입 |
| priority | INT | NOT NULL | 100 | 우선순위 (낮을수록 높음) |
| allowed_file_types | JSON | NOT NULL | '[]' | 허용 파일 타입 |
| blocked_file_types | JSON | NULL | '[]' | 차단 파일 타입 |
| allowed_mime_types | JSON | NULL | '[]' | 허용 MIME 타입 |
| max_file_size | BIGINT | NOT NULL | 104857600 | 최대 파일 크기 (bytes, 기본 100MB) |
| min_file_size | BIGINT | NULL | 1 | 최소 파일 크기 (bytes) |
| max_total_size | BIGINT | NULL | 1073741824 | 세션당 최대 총 크기 (1GB) |
| max_files_per_session | INT | NULL | 100 | 세션당 최대 파일 수 |
| allowed_sources | JSON | NULL | '["PRESIGNED","EXTERNAL_URL"]' | 허용 업로드 소스 |
| allowed_ip_ranges | JSON | NULL | NULL | 허용 IP 범위 |
| require_virus_scan | BOOLEAN | NOT NULL | TRUE | 바이러스 스캔 필수 여부 |
| require_checksum | BOOLEAN | NOT NULL | TRUE | 체크섬 검증 필수 여부 |
| auto_process | BOOLEAN | NOT NULL | TRUE | 자동 처리 활성화 |
| processing_pipeline | VARCHAR(100) | NULL | NULL | 기본 처리 파이프라인 |
| processing_priority | ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') | NOT NULL | 'NORMAL' | 처리 우선순위 |
| storage_class | ENUM('STANDARD', 'INFREQUENT_ACCESS', 'ARCHIVE', 'GLACIER') | NOT NULL | 'STANDARD' | 스토리지 클래스 |
| retention_days | INT | NULL | 365 | 보관 기간 (일) |
| auto_delete | BOOLEAN | NOT NULL | FALSE | 자동 삭제 활성화 |
| metadata_rules | JSON | NULL | '{}' | 메타데이터 규칙 |
| naming_convention | VARCHAR(255) | NULL | NULL | 파일명 규칙 (정규식) |
| duplicate_handling | ENUM('ALLOW', 'REJECT', 'RENAME', 'REPLACE') | NOT NULL | 'RENAME' | 중복 파일 처리 |
| rate_limits | JSON | NULL | '{}' | Rate limiting 설정 |
| notification_config | JSON | NULL | '{}' | 알림 설정 |
| is_active | BOOLEAN | NOT NULL | TRUE | 활성화 상태 |
| version | INT | NOT NULL | 1 | 버전 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |
| created_by | VARCHAR(100) | NULL | NULL | 생성자 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_policy_code (policy_code)
UNIQUE KEY uk_tenant_org_code (tenant_id, organization_id, policy_code)
INDEX idx_tenant_id (tenant_id)
INDEX idx_organization_id (organization_id)
INDEX idx_is_active (is_active)
INDEX idx_priority (priority)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

### 샘플 데이터
```sql
-- B2C 이미지 업로드 정책
INSERT INTO upload_policies (tenant_id, policy_code, policy_name, allowed_file_types, max_file_size, processing_pipeline) VALUES
('b2c_kr', 'B2C_IMAGE_STANDARD', '상품 이미지 표준', '["jpg","jpeg","png","webp","gif"]', 52428800, 'image_optimization'),
('b2c_kr', 'B2C_HTML_STANDARD', '상품 상세 HTML', '["html","htm"]', 10485760, 'html_processing'),

-- B2B Excel 업로드 정책
('b2b_global', 'B2B_EXCEL_STANDARD', 'Excel 문서 표준', '["xlsx","xls","csv"]', 104857600, 'excel_ai_mapping'),
('b2b_global', 'B2B_PDF_STANDARD', 'PDF 문서 표준', '["pdf"]', 209715200, 'pdf_processing');
```

---

## 3. upload_parts (멀티파트 업로드 파트)

### 테이블 설명
대용량 파일의 멀티파트 업로드를 위한 파트 정보를 관리합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 파트 ID |
| session_id | VARCHAR(36) | NOT NULL | - | 세션 ID (upload_sessions 참조) |
| part_number | INT | NOT NULL | - | 파트 번호 |
| etag | VARCHAR(255) | NULL | NULL | ETag (S3) |
| size | BIGINT | NOT NULL | - | 파트 크기 (bytes) |
| checksum | VARCHAR(64) | NULL | NULL | 파트 체크섬 |
| status | ENUM('PENDING', 'UPLOADING', 'COMPLETED', 'FAILED') | NOT NULL | 'PENDING' | 파트 상태 |
| retry_count | INT | NOT NULL | 0 | 재시도 횟수 |
| error_message | TEXT | NULL | NULL | 에러 메시지 |
| started_at | DATETIME | NULL | NULL | 업로드 시작 시각 |
| completed_at | DATETIME | NULL | NULL | 완료 시각 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_session_part (session_id, part_number)
INDEX idx_session_id (session_id, status)
INDEX idx_status (status)
-- 외래키 제거: FK constraint 미사용
-- CASCADE 삭제는 애플리케이션 레벨에서 처리
```

---

## 4. upload_chunks (청크 업로드) - 선택적

### 테이블 설명
청크 단위 업로드를 지원하기 위한 테이블입니다. 브라우저 기반 대용량 파일 업로드에 사용됩니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 청크 ID |
| session_id | VARCHAR(36) | NOT NULL | - | 세션 ID (upload_sessions 참조) |
| chunk_index | INT | NOT NULL | - | 청크 인덱스 |
| chunk_size | BIGINT | NOT NULL | - | 청크 크기 (bytes) |
| offset | BIGINT | NOT NULL | - | 파일 내 오프셋 |
| checksum | VARCHAR(64) | NULL | NULL | 청크 체크섬 |
| storage_path | VARCHAR(1024) | NULL | NULL | 임시 저장 경로 |
| status | ENUM('PENDING', 'UPLOADED', 'VERIFIED', 'MERGED') | NOT NULL | 'PENDING' | 청크 상태 |
| uploaded_at | DATETIME | NULL | NULL | 업로드 시각 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_session_chunk (session_id, chunk_index)
INDEX idx_session_id (session_id, status)
-- 외래키 제거: FK constraint 미사용
-- CASCADE 삭제는 애플리케이션 레벨에서 처리
```

---

## 5. external_downloads (외부 다운로드 작업)

### 테이블 설명
외부 URL로부터 파일을 다운로드하는 작업을 관리합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 다운로드 ID |
| session_id | VARCHAR(36) | NOT NULL | - | 세션 ID (upload_sessions 참조) |
| external_url | VARCHAR(2048) | NOT NULL | - | 외부 URL |
| http_method | VARCHAR(10) | NOT NULL | 'GET' | HTTP 메서드 |
| request_headers | JSON | NULL | NULL | 요청 헤더 |
| auth_type | ENUM('NONE', 'BASIC', 'BEARER', 'API_KEY', 'OAUTH2') | NOT NULL | 'NONE' | 인증 타입 |
| auth_credentials | TEXT | NULL | NULL | 인증 정보 (암호화) |
| status | ENUM('PENDING', 'DOWNLOADING', 'COMPLETED', 'FAILED', 'CANCELLED') | NOT NULL | 'PENDING' | 상태 |
| response_code | INT | NULL | NULL | HTTP 응답 코드 |
| response_headers | JSON | NULL | NULL | 응답 헤더 |
| content_type | VARCHAR(100) | NULL | NULL | Content-Type |
| content_length | BIGINT | NULL | NULL | Content-Length |
| downloaded_size | BIGINT | NOT NULL | 0 | 다운로드된 크기 |
| download_speed | BIGINT | NULL | NULL | 다운로드 속도 (bytes/sec) |
| retry_count | INT | NOT NULL | 0 | 재시도 횟수 |
| max_retries | INT | NOT NULL | 3 | 최대 재시도 횟수 |
| error_message | TEXT | NULL | NULL | 에러 메시지 |
| started_at | DATETIME | NULL | NULL | 시작 시각 |
| completed_at | DATETIME | NULL | NULL | 완료 시각 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
INDEX idx_session_id (session_id)
INDEX idx_status (status, created_at DESC)
INDEX idx_external_url (external_url(255))
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

---

## 6. batch_uploads (배치 업로드)

### 테이블 설명
여러 파일을 한번에 업로드하는 배치 작업을 관리합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 배치 ID |
| batch_id | VARCHAR(36) | UK, NOT NULL | UUID() | 배치 고유 식별자 |
| tenant_id | VARCHAR(50) | NOT NULL | - | 테넌트 ID (tenants 참조) |
| organization_id | BIGINT | NOT NULL | - | 조직 ID (organizations 참조) |
| user_context_id | BIGINT | NOT NULL | - | 사용자 컨텍스트 ID (user_contexts 참조) |
| batch_name | VARCHAR(200) | NULL | NULL | 배치명 |
| source_type | ENUM('ZIP', 'FOLDER', 'CSV_LIST', 'API') | NOT NULL | - | 소스 타입 |
| total_files | INT | NOT NULL | 0 | 전체 파일 수 |
| processed_files | INT | NOT NULL | 0 | 처리된 파일 수 |
| successful_files | INT | NOT NULL | 0 | 성공한 파일 수 |
| failed_files | INT | NOT NULL | 0 | 실패한 파일 수 |
| status | ENUM('PREPARING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED') | NOT NULL | 'PREPARING' | 상태 |
| manifest | JSON | NULL | NULL | 배치 매니페스트 |
| results | JSON | NULL | NULL | 처리 결과 |
| started_at | DATETIME | NULL | NULL | 시작 시각 |
| completed_at | DATETIME | NULL | NULL | 완료 시각 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_batch_id (batch_id)
INDEX idx_tenant_org (tenant_id, organization_id, created_at DESC)
INDEX idx_user_context_id (user_context_id, created_at DESC)
INDEX idx_status (status, created_at DESC)
-- 외래키 제거: FK constraint 미사용
-- 참조 무결성은 애플리케이션 레벨에서 검증
```

---

## 7. 참조 무결성 검증 (Application Level)

외래키 제약조건을 제거했기 때문에 애플리케이션 레벨에서 참조 무결성을 보장해야 합니다.

### 7.1 업로드 세션 생성 시 검증

```java
@Service
@RequiredArgsConstructor
public class UploadSessionService {

    private final UploadSessionRepository uploadSessionRepository;
    private final TenantRepository tenantRepository;
    private final OrganizationRepository organizationRepository;
    private final UserContextRepository userContextRepository;
    private final UploadPolicyRepository uploadPolicyRepository;

    @Transactional
    public UploadSession createUploadSession(UploadSessionCreateRequest request) {

        // 1. 참조 무결성 검증
        validateReferences(request);

        // 2. 정책 적용
        UploadPolicy policy = uploadPolicyRepository.findById(request.getPolicyId())
            .orElseThrow(() -> new PolicyNotFoundException(request.getPolicyId()));

        // 3. 정책 검증
        validateAgainstPolicy(request, policy);

        // 4. 세션 생성
        UploadSession session = UploadSession.builder()
            .sessionId(UUID.randomUUID().toString())
            .tenantId(request.getTenantId())
            .organizationId(request.getOrganizationId())
            .userContextId(request.getUserContextId())
            .policyId(request.getPolicyId())
            .uploadType(request.getUploadType())
            .expiresAt(LocalDateTime.now().plusHours(24))
            // ... 나머지 필드
            .build();

        return uploadSessionRepository.save(session);
    }

    private void validateReferences(UploadSessionCreateRequest request) {

        // Tenant 존재 여부 검증
        if (!tenantRepository.existsByTenantId(request.getTenantId())) {
            throw new ReferenceNotFoundException(
                "Tenant not found: " + request.getTenantId()
            );
        }

        // Organization 존재 여부 검증
        if (!organizationRepository.existsById(request.getOrganizationId())) {
            throw new ReferenceNotFoundException(
                "Organization not found: " + request.getOrganizationId()
            );
        }

        // UserContext 존재 여부 검증
        if (!userContextRepository.existsById(request.getUserContextId())) {
            throw new ReferenceNotFoundException(
                "User context not found: " + request.getUserContextId()
            );
        }

        // UploadPolicy 존재 여부 검증
        if (!uploadPolicyRepository.existsById(request.getPolicyId())) {
            throw new ReferenceNotFoundException(
                "Upload policy not found: " + request.getPolicyId()
            );
        }

        // Tenant-Organization 관계 검증
        Organization org = organizationRepository.findById(request.getOrganizationId())
            .orElseThrow(() -> new ReferenceNotFoundException(
                "Organization not found: " + request.getOrganizationId()
            ));

        if (!org.getTenantId().equals(request.getTenantId())) {
            throw new InvalidReferenceException(
                "Organization does not belong to the specified tenant"
            );
        }
    }

    private void validateAgainstPolicy(UploadSessionCreateRequest request, UploadPolicy policy) {

        // 파일 크기 검증
        if (request.getTotalSize() > policy.getMaxTotalSize()) {
            throw new PolicyViolationException(
                "Total size exceeds policy limit: " + policy.getMaxTotalSize()
            );
        }

        // 파일 개수 검증
        if (request.getTotalFiles() > policy.getMaxFilesPerSession()) {
            throw new PolicyViolationException(
                "Number of files exceeds policy limit: " + policy.getMaxFilesPerSession()
            );
        }

        // 업로드 소스 검증
        List<String> allowedSources = policy.getAllowedSources();
        if (!allowedSources.contains(request.getUploadType().name())) {
            throw new PolicyViolationException(
                "Upload type not allowed by policy: " + request.getUploadType()
            );
        }
    }
}
```

### 7.2 세션 삭제 시 CASCADE 처리

```java
@Service
@RequiredArgsConstructor
public class UploadSessionService {

    private final UploadSessionRepository uploadSessionRepository;
    private final UploadPartRepository uploadPartRepository;
    private final UploadChunkRepository uploadChunkRepository;
    private final ExternalDownloadRepository externalDownloadRepository;
    private final FileAssetRepository fileAssetRepository;

    @Transactional
    public void deleteUploadSession(String sessionId) {

        UploadSession session = uploadSessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // 1. 멀티파트 파트 삭제
        uploadPartRepository.deleteBySessionId(sessionId);

        // 2. 청크 삭제
        uploadChunkRepository.deleteBySessionId(sessionId);

        // 3. 외부 다운로드 작업 취소 및 삭제
        List<ExternalDownload> downloads = externalDownloadRepository.findBySessionId(sessionId);
        downloads.forEach(download -> {
            if (download.getStatus() == DownloadStatus.DOWNLOADING) {
                // 다운로드 작업 취소 로직
                downloadCancellationService.cancel(download.getId());
            }
        });
        externalDownloadRepository.deleteBySessionId(sessionId);

        // 4. 완료되지 않은 파일 자산 정리
        List<FileAsset> incompleteFiles = fileAssetRepository.findBySessionIdAndStatusNot(
            sessionId, FileAssetStatus.AVAILABLE
        );
        incompleteFiles.forEach(file -> {
            // 스토리지에서 파일 삭제
            storageService.deleteFile(file.getStoragePath());
        });
        fileAssetRepository.deleteBySessionIdAndStatusNot(sessionId, FileAssetStatus.AVAILABLE);

        // 5. 세션 삭제
        uploadSessionRepository.delete(session);
    }
}
```

### 7.3 배치 검증 (정기 실행)

```java
@Service
@RequiredArgsConstructor
public class ReferenceIntegrityCheckService {

    @Scheduled(cron = "0 0 3 * * ?")  // 매일 오전 3시
    public void checkUploadSessionReferences() {

        // 1. 존재하지 않는 Tenant 참조
        List<UploadSessionOrphanCheck> orphanTenants = uploadSessionRepository.findOrphanTenants();
        if (!orphanTenants.isEmpty()) {
            log.warn("Found {} upload_sessions with invalid tenant_id", orphanTenants.size());
            alertService.sendAlert("Upload Sessions with invalid tenant_id", orphanTenants);
        }

        // 2. 존재하지 않는 Organization 참조
        List<UploadSessionOrphanCheck> orphanOrgs = uploadSessionRepository.findOrphanOrganizations();
        if (!orphanOrgs.isEmpty()) {
            log.warn("Found {} upload_sessions with invalid organization_id", orphanOrgs.size());
            alertService.sendAlert("Upload Sessions with invalid organization_id", orphanOrgs);
        }

        // 3. 존재하지 않는 UserContext 참조
        List<UploadSessionOrphanCheck> orphanUsers = uploadSessionRepository.findOrphanUserContexts();
        if (!orphanUsers.isEmpty()) {
            log.warn("Found {} upload_sessions with invalid user_context_id", orphanUsers.size());
            alertService.sendAlert("Upload Sessions with invalid user_context_id", orphanUsers);
        }

        // 4. 존재하지 않는 UploadPolicy 참조
        List<UploadSessionOrphanCheck> orphanPolicies = uploadSessionRepository.findOrphanPolicies();
        if (!orphanPolicies.isEmpty()) {
            log.warn("Found {} upload_sessions with invalid policy_id", orphanPolicies.size());
            alertService.sendAlert("Upload Sessions with invalid policy_id", orphanPolicies);
        }

        // 5. 만료된 세션 정리
        List<UploadSession> expiredSessions = uploadSessionRepository.findExpiredSessions();
        expiredSessions.forEach(session -> {
            log.info("Cleaning up expired session: {}", session.getSessionId());
            deleteUploadSession(session.getSessionId());
        });
    }
}
```

### 7.4 고아 레코드 검증 쿼리

```sql
-- 1. 존재하지 않는 Tenant를 참조하는 upload_sessions
SELECT 'upload_sessions_orphan_tenant' AS issue_type, COUNT(*) AS count
FROM upload_sessions us
LEFT JOIN tenants t ON us.tenant_id = t.tenant_id
WHERE t.tenant_id IS NULL;

-- 2. 존재하지 않는 Organization을 참조하는 upload_sessions
SELECT 'upload_sessions_orphan_organization' AS issue_type, COUNT(*) AS count
FROM upload_sessions us
LEFT JOIN organizations o ON us.organization_id = o.id
WHERE o.id IS NULL;

-- 3. 존재하지 않는 UserContext를 참조하는 upload_sessions
SELECT 'upload_sessions_orphan_user_context' AS issue_type, COUNT(*) AS count
FROM upload_sessions us
LEFT JOIN user_contexts uc ON us.user_context_id = uc.id
WHERE uc.id IS NULL;

-- 4. 존재하지 않는 UploadPolicy를 참조하는 upload_sessions
SELECT 'upload_sessions_orphan_policy' AS issue_type, COUNT(*) AS count
FROM upload_sessions us
LEFT JOIN upload_policies up ON us.policy_id = up.id
WHERE up.id IS NULL;

-- 5. 존재하지 않는 세션을 참조하는 upload_parts
SELECT 'upload_parts_orphan' AS issue_type, COUNT(*) AS count
FROM upload_parts part
LEFT JOIN upload_sessions us ON part.session_id = us.session_id
WHERE us.session_id IS NULL;

-- 6. 존재하지 않는 세션을 참조하는 upload_chunks
SELECT 'upload_chunks_orphan' AS issue_type, COUNT(*) AS count
FROM upload_chunks chunk
LEFT JOIN upload_sessions us ON chunk.session_id = us.session_id
WHERE us.session_id IS NULL;

-- 7. 존재하지 않는 세션을 참조하는 external_downloads
SELECT 'external_downloads_orphan' AS issue_type, COUNT(*) AS count
FROM external_downloads ed
LEFT JOIN upload_sessions us ON ed.session_id = us.session_id
WHERE us.session_id IS NULL;

-- 8. 존재하지 않는 Tenant를 참조하는 upload_policies
SELECT 'upload_policies_orphan_tenant' AS issue_type, COUNT(*) AS count
FROM upload_policies up
LEFT JOIN tenants t ON up.tenant_id = t.tenant_id
WHERE t.tenant_id IS NULL;

-- 9. 존재하지 않는 Organization을 참조하는 upload_policies
SELECT 'upload_policies_orphan_organization' AS issue_type, COUNT(*) AS count
FROM upload_policies up
LEFT JOIN organizations o ON up.organization_id = o.id
WHERE up.organization_id IS NOT NULL AND o.id IS NULL;
```

### 7.5 Orphan 레코드 정리 스크립트

```sql
-- 1. upload_parts의 고아 레코드 정리
DELETE FROM upload_parts
WHERE session_id NOT IN (SELECT session_id FROM upload_sessions);

-- 2. upload_chunks의 고아 레코드 정리
DELETE FROM upload_chunks
WHERE session_id NOT IN (SELECT session_id FROM upload_sessions);

-- 3. external_downloads의 고아 레코드 정리
DELETE FROM external_downloads
WHERE session_id NOT IN (SELECT session_id FROM upload_sessions);

-- 4. 만료된 세션 정리
DELETE FROM upload_sessions
WHERE expires_at < NOW()
  AND status IN ('INITIALIZED', 'FAILED', 'CANCELLED', 'EXPIRED');

-- 5. 완료 후 90일 경과한 세션 정리
DELETE FROM upload_sessions
WHERE status = 'COMPLETED'
  AND completed_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

---

## 8. 관계 다이어그램

```
upload_policies ──< upload_sessions
                          │
                          ├──< upload_parts
                          ├──< upload_chunks
                          ├──< external_downloads
                          └──< file_assets

batch_uploads ──< upload_sessions (via batch_id)
```

---

## 9. 비즈니스 로직

### 9.1 Presigned URL 업로드 플로우
1. 업로드 세션 생성 요청
2. 참조 무결성 검증 (tenant, organization, user_context, policy)
3. 정책 검증 (파일 크기, 타입, 개수 등)
4. Presigned URL 생성
5. 클라이언트 직접 업로드
6. 업로드 완료 콜백
7. 파일 검증 및 등록
8. 파이프라인 트리거

### 9.2 외부 URL 다운로드 플로우
1. 외부 URL 제공
2. URL 유효성 검증
3. external_downloads 레코드 생성
4. 비동기 다운로드 시작
5. 파일 저장 및 검증
6. file_assets 등록
7. 파이프라인 트리거

### 9.3 멀티파트 업로드 플로우
1. 멀티파트 세션 초기화
2. 파트별 Presigned URL 생성
3. 병렬 파트 업로드
4. 파트 완료 추적
5. 모든 파트 완료 시 병합
6. 최종 파일 검증

---

## 10. 정책 적용 규칙

### 10.1 정책 우선순위
1. 조직별 오버라이드 정책
2. 조직별 커스텀 정책
3. 테넌트 기본 정책
4. 시스템 기본값

### 10.2 정책 상속
- 조직 정책이 없으면 테넌트 정책 상속
- 특정 항목만 오버라이드 가능
- 정책 변경 시 버전 관리

### 10.3 정책 검증 로직
```java
@Service
public class UploadPolicyResolver {

    public UploadPolicy resolvePolicy(String tenantId, Long organizationId) {

        // 1. 조직별 오버라이드 정책 조회
        Optional<UploadPolicy> orgOverridePolicy = uploadPolicyRepository
            .findByTenantIdAndOrganizationIdAndPolicyType(
                tenantId, organizationId, PolicyType.OVERRIDE
            );
        if (orgOverridePolicy.isPresent() && orgOverridePolicy.get().isActive()) {
            return orgOverridePolicy.get();
        }

        // 2. 조직별 커스텀 정책 조회
        Optional<UploadPolicy> orgCustomPolicy = uploadPolicyRepository
            .findByTenantIdAndOrganizationIdAndPolicyType(
                tenantId, organizationId, PolicyType.CUSTOM
            );
        if (orgCustomPolicy.isPresent() && orgCustomPolicy.get().isActive()) {
            return orgCustomPolicy.get();
        }

        // 3. 테넌트 기본 정책 조회
        Optional<UploadPolicy> tenantDefaultPolicy = uploadPolicyRepository
            .findByTenantIdAndOrganizationIdNullAndPolicyType(
                tenantId, PolicyType.DEFAULT
            );
        if (tenantDefaultPolicy.isPresent() && tenantDefaultPolicy.get().isActive()) {
            return tenantDefaultPolicy.get();
        }

        // 4. 시스템 기본값 반환
        return getSystemDefaultPolicy();
    }
}
```

---

## 11. 성능 최적화

### 11.1 캐싱 전략
- 업로드 세션 Redis 캐싱 (TTL: 세션 만료 시간)
- Presigned URL 배치 생성
- 정책 결과 캐싱 (TTL: 10분)

### 11.2 병렬 처리
- 멀티파트 병렬 처리
- 외부 다운로드 큐잉
- 배치 업로드 병렬 처리

### 11.3 인덱스 전략
```sql
-- 세션 조회 성능 최적화
CREATE INDEX idx_session_lookup
ON upload_sessions(tenant_id, organization_id, user_context_id, status, created_at DESC);

-- 만료 세션 정리 성능 최적화
CREATE INDEX idx_expired_sessions
ON upload_sessions(expires_at, status)
WHERE status IN ('INITIALIZED', 'UPLOADING');

-- 정책 조회 성능 최적화
CREATE INDEX idx_policy_lookup
ON upload_policies(tenant_id, organization_id, policy_type, is_active, priority);
```

---

## 12. 보안 고려사항

### 12.1 Presigned URL 보안
- 짧은 만료 시간 (기본 1시간)
- IP 제한 가능 (정책 설정)
- 일회성 사용 추적

### 12.2 외부 다운로드 보안
- 허용 도메인 화이트리스트
- 인증 정보 암호화 저장
- SSL/TLS 검증 필수

### 12.3 Rate Limiting
- 사용자별 업로드 속도 제한
- IP별 요청 제한
- 정책 기반 동적 조정
