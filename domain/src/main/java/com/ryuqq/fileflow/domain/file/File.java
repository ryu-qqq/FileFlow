package com.ryuqq.fileflow.domain.file;

import java.time.Clock;
import java.time.LocalDateTime;

import com.ryuqq.fileflow.domain.file.vo.FileName;
import com.ryuqq.fileflow.domain.file.vo.FileSize;
import com.ryuqq.fileflow.domain.file.vo.MimeType;
import com.ryuqq.fileflow.domain.file.vo.S3Path;
import com.ryuqq.fileflow.domain.file.vo.UploadType;
import com.ryuqq.fileflow.domain.session.UploadSession;
import com.ryuqq.fileflow.domain.session.vo.SessionId;
import com.ryuqq.fileflow.domain.session.vo.UserRole;

/**
 * 파일 Aggregate Root.
 *
 * <p>
 * 업로드된 파일 정보를 관리하는 도메인 엔티티입니다.
 * 파일 생성, 논리 삭제 등의 비즈니스 로직을 캡슐화합니다.
 * </p>
 *
 * <p>
 * <strong>생성 패턴</strong>:
 * </p>
 * <ul>
 *     <li>forNew(): 신규 파일 생성 (ID 자동 생성, UploadSession에서 정보 추출)</li>
 *     <li>of(): ID 기반 파일 생성 (비즈니스 로직용)</li>
 *     <li>reconstitute(): 영속성 복원용 (Repository 전용)</li>
 * </ul>
 */
public class File {

    private final SessionId fileId;
    private final Long userId;
    private final Long tenantId;
    private final UserRole role;
    private final FileName fileName;
    private final FileSize fileSize;
    private final MimeType mimeType;
    private final S3Path s3Path;
    private final UploadType uploadType;
    private final Clock clock;
    private final LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
    private boolean deleted;
    private LocalDateTime deletedAt;

    private File(
        SessionId fileId,
        Long userId,
        Long tenantId,
        UserRole role,
        FileName fileName,
        FileSize fileSize,
        MimeType mimeType,
        S3Path s3Path,
        UploadType uploadType,
        Clock clock,
        LocalDateTime uploadedAt,
        LocalDateTime updatedAt,
        boolean deleted,
        LocalDateTime deletedAt
    ) {
        this.fileId = fileId;
        this.userId = userId;
        this.tenantId = tenantId;
        this.role = role;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.s3Path = s3Path;
        this.uploadType = uploadType;
        this.clock = clock;
        this.uploadedAt = uploadedAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
    }

    /**
     * 신규 파일 생성 (ID 자동 생성, UploadSession에서 정보 추출).
     *
     * <p>
     * UploadSession에서 파일 정보를 추출하여 새 파일을 생성합니다.
     * </p>
     *
     * @param session 업로드 세션 (파일 정보 추출용)
     * @param clock 시간 의존성 (테스트 가능성)
     * @return 신규 File
     */
    public static File forNew(UploadSession session, Clock clock) {
        SessionId fileId = SessionId.forNew();
        
        LocalDateTime now = LocalDateTime.now(clock);
        
        return new File(
            fileId,
            session.getUserId(),
            session.getTenantId(),
            session.getRole(),
            session.getFileName(),
            session.getFileSize(),
            session.getMimeType(),
            session.getS3Path(),
            session.getUploadType(),
            clock,
            now,
            now,
            false,
            null
        );
    }

    /**
     * ID 기반 파일 생성 (비즈니스 로직용).
     *
     * <p>
     * 기존 fileId로 파일을 생성하며, UploadSession에서 정보를 추출합니다.
     * </p>
     *
     * @param fileId 파일 ID (null 불가)
     * @param session 업로드 세션 (파일 정보 추출용)
     * @param clock 시간 의존성 (테스트 가능성)
     * @return File
     * @throws IllegalArgumentException fileId가 null인 경우
     */
    public static File of(SessionId fileId, UploadSession session, Clock clock) {
        if (fileId == null) {
            throw new IllegalArgumentException("FileId는 null일 수 없습니다.");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        
        return new File(
            fileId,
            session.getUserId(),
            session.getTenantId(),
            session.getRole(),
            session.getFileName(),
            session.getFileSize(),
            session.getMimeType(),
            session.getS3Path(),
            session.getUploadType(),
            clock,
            now,
            now,
            false,
            null
        );
    }

    /**
     * 영속성 복원용 파일 생성 (Repository 전용).
     *
     * <p>
     * Repository에서 Entity → Domain 변환 시 사용합니다.
     * 검증 로직을 실행하지 않으며, 모든 필드를 그대로 전달합니다.
     * </p>
     *
     * @param fileId 파일 ID
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param role 사용자 역할
     * @param fileName 파일 이름 VO
     * @param fileSize 파일 크기 VO
     * @param mimeType MIME 타입 VO
     * @param s3Path S3 경로 VO
     * @param uploadType 업로드 타입
     * @param clock 시간 의존성 (테스트 가능성)
     * @param uploadedAt 업로드 시각
     * @param updatedAt 수정 시각
     * @param deleted 삭제 여부
     * @param deletedAt 삭제 시각 (Nullable)
     * @return File
     */
    public static File reconstitute(
        SessionId fileId,
        Long userId,
        Long tenantId,
        UserRole role,
        FileName fileName,
        FileSize fileSize,
        MimeType mimeType,
        S3Path s3Path,
        UploadType uploadType,
        Clock clock,
        LocalDateTime uploadedAt,
        LocalDateTime updatedAt,
        boolean deleted,
        LocalDateTime deletedAt
    ) {
        return new File(
            fileId,
            userId,
            tenantId,
            role,
            fileName,
            fileSize,
            mimeType,
            s3Path,
            uploadType,
            clock,
            uploadedAt,
            updatedAt,
            deleted,
            deletedAt
        );
    }

    // ==================== 비즈니스 메서드 ====================

    /**
     * 파일 논리 삭제.
     *
     * <p>
     * 파일을 논리적으로 삭제하며, deletedAt과 updatedAt을 현재 시각으로 설정합니다.
     * </p>
     */
    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    // ==================== Getter 메서드 ====================

    public SessionId getFileId() {
        return fileId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public UserRole getRole() {
        return role;
    }

    public FileName getFileName() {
        return fileName;
    }

    public FileSize getFileSize() {
        return fileSize;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public S3Path getS3Path() {
        return s3Path;
    }

    public UploadType getUploadType() {
        return uploadType;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    // ==================== 상태 조회 메서드 (Tell Don't Ask) ====================

    /**
     * 파일이 삭제되었는지 확인한다.
     *
     * @return 삭제되었으면 true
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * 파일을 삭제할 수 있는지 확인한다.
     *
     * @return 삭제되지 않은 경우 true, 이미 삭제된 경우 false
     */
    public boolean canDelete() {
        return !deleted;
    }

    // ==================== Law of Demeter 준수 메서드 ====================

    /**
     * FileId의 원시 값을 반환한다 (Law of Demeter 준수).
     *
     * <p>
     * 외부 레이어에서 FileId VO를 직접 노출하지 않고 원시 값을 반환합니다.
     * </p>
     *
     * @return FileId의 UUID 문자열 값
     */
    public String getFileIdValue() {
        return fileId.value();
    }
}

