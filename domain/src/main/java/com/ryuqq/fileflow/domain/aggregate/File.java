package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.exception.InvalidFileSizeException;
import com.ryuqq.fileflow.domain.exception.InvalidMimeTypeException;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.FileStatus;
import com.ryuqq.fileflow.domain.vo.UploaderId;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 파일 Aggregate Root
 * <p>
 * 파일 메타데이터와 S3 저장 정보를 관리하는 도메인 엔티티입니다.
 * </p>
 */
public class File {

    /**
     * 최대 파일 크기: 1GB
     */
    private static final long MAX_FILE_SIZE = 1024L * 1024L * 1024L; // 1GB

    /**
     * CDN URL 베이스 경로
     */
    private static final String CDN_BASE_URL = "https://cdn.fileflow.com/";

    /**
     * 허용되는 MIME 타입 목록
     */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            // 이미지
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/svg+xml",
            // 문서
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            // 엑셀
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            // HTML
            "text/html",
            "text/plain",
            "text/csv"
    );

    private final Clock clock;
    private final FileId fileId;
    private final String fileName;
    private final long fileSize;
    private final String mimeType;
    private final FileStatus status;
    private final String s3Key;
    private final String s3Bucket;
    private final String cdnUrl;
    private final UploaderId uploaderId;
    private final String category;
    private final String tags;
    private final int retryCount;
    private final int version;
    private final LocalDateTime deletedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * File Aggregate 생성자
     * <p>
     * Private 생성자: 외부에서 직접 생성 불가, 팩토리 메서드 사용 필수
     * </p>
     *
     * @param clock      시간 의존성 (테스트 가능한 시간 제어)
     * @param fileId     파일 고유 ID (FileId VO)
     * @param fileName   파일명
     * @param fileSize   파일 크기 (바이트)
     * @param mimeType   MIME 타입
     * @param status     파일 상태
     * @param s3Key      S3 객체 키
     * @param s3Bucket   S3 버킷명
     * @param cdnUrl     CDN URL
     * @param uploaderId 업로더 사용자 ID (UploaderId VO, Long FK 전략)
     * @param category   파일 카테고리
     * @param tags       태그 (콤마 구분)
     * @param retryCount 재시도 횟수
     * @param version    낙관적 락 버전
     * @param deletedAt  소프트 삭제 시각
     * @param createdAt  생성 시각
     * @param updatedAt  수정 시각
     */
    private File(
            Clock clock,
            FileId fileId,
            String fileName,
            long fileSize,
            String mimeType,
            FileStatus status,
            String s3Key,
            String s3Bucket,
            String cdnUrl,
            UploaderId uploaderId,
            String category,
            String tags,
            int retryCount,
            int version,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.clock = clock;
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.status = status;
        this.s3Key = s3Key;
        this.s3Bucket = s3Bucket;
        this.cdnUrl = cdnUrl;
        this.uploaderId = uploaderId;
        this.category = category;
        this.tags = tags;
        this.retryCount = retryCount;
        this.version = version;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 파일 고유 ID 조회
     */
    public FileId getFileId() {
        return fileId;
    }

    /**
     * 파일명 조회
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 파일 크기 조회 (바이트)
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * MIME 타입 조회
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 파일 상태 조회
     */
    public FileStatus getStatus() {
        return status;
    }

    /**
     * S3 객체 키 조회
     */
    public String getS3Key() {
        return s3Key;
    }

    /**
     * S3 버킷명 조회
     */
    public String getS3Bucket() {
        return s3Bucket;
    }

    /**
     * CDN URL 조회
     */
    public String getCdnUrl() {
        return cdnUrl;
    }

    /**
     * 업로더 사용자 ID 조회 (UploaderId VO)
     */
    public UploaderId getUploaderId() {
        return uploaderId;
    }

    /**
     * 파일 카테고리 조회
     */
    public String getCategory() {
        return category;
    }

    /**
     * 태그 조회 (콤마 구분)
     */
    public String getTags() {
        return tags;
    }

    /**
     * 재시도 횟수 조회
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * 낙관적 락 버전 조회
     */
    public int getVersion() {
        return version;
    }

    /**
     * 소프트 삭제 시각 조회
     */
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    /**
     * 생성 시각 조회
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 수정 시각 조회
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 신규 파일 생성 팩토리 메서드 (애플리케이션 레이어용)
     * <p>
     * ID가 null인 새로운 파일을 생성합니다. 영속화 시점에 ID가 생성됩니다.
     * </p>
     *
     * @param fileName    파일명
     * @param fileSize    파일 크기 (바이트)
     * @param mimeType    MIME 타입
     * @param s3Key       S3 객체 키
     * @param s3Bucket    S3 버킷명
     * @param uploaderId  업로더 사용자 ID (UploaderId VO)
     * @param category    파일 카테고리
     * @param tags        태그 (콤마 구분, nullable)
     * @param clock       시간 의존성 (테스트 가능한 시간 제어)
     * @return 생성된 File Aggregate (ID가 null)
     * @throws InvalidFileSizeException 파일 크기가 유효하지 않을 때
     * @throws InvalidMimeTypeException MIME 타입이 허용되지 않을 때
     */
    public static File forNew(
            String fileName,
            long fileSize,
            String mimeType,
            String s3Key,
            String s3Bucket,
            UploaderId uploaderId,
            String category,
            String tags,
            Clock clock
    ) {
        // 파일 크기 검증
        validateFileSize(fileSize);

        // MIME 타입 검증
        validateMimeType(mimeType);

        // 현재 시각 (Clock 사용)
        LocalDateTime now = LocalDateTime.now(clock);

        // CDN URL 생성 (S3 키 기반)
        String cdnUrl = CDN_BASE_URL + s3Key;

        return new File(
                clock, // Clock 주입
                FileId.forNew(), // ID는 영속화 시점에 생성
                fileName,
                fileSize,
                mimeType,
                FileStatus.PENDING, // 초기 상태는 PENDING
                s3Key,
                s3Bucket,
                cdnUrl,
                uploaderId,
                category,
                tags,
                0, // 초기 재시도 횟수 0
                1, // 초기 버전 1
                null, // deletedAt은 null
                now, // createdAt
                now  // updatedAt
        );
    }

    /**
     * 파일 생성 팩토리 메서드 (비즈니스 로직용)
     * <p>
     * ID가 필수인 파일을 생성합니다. 비즈니스 로직에서 사용합니다.
     * </p>
     *
     * @param clock       시간 의존성 (테스트 가능한 시간 제어)
     * @param fileId      파일 고유 ID (필수, null 불가)
     * @param fileName    파일명
     * @param fileSize    파일 크기 (바이트)
     * @param mimeType    MIME 타입
     * @param status      파일 상태
     * @param s3Key       S3 객체 키
     * @param s3Bucket    S3 버킷명
     * @param cdnUrl      CDN URL
     * @param uploaderId  업로더 사용자 ID (UploaderId VO)
     * @param category    파일 카테고리
     * @param tags        태그 (콤마 구분, nullable)
     * @param retryCount  재시도 횟수
     * @param version     낙관적 락 버전
     * @param deletedAt   소프트 삭제 시각
     * @param createdAt   생성 시각
     * @param updatedAt   수정 시각
     * @return 생성된 File Aggregate
     * @throws IllegalArgumentException ID가 null이거나 새로운 ID일 때
     */
    public static File of(
            Clock clock,
            FileId fileId,
            String fileName,
            long fileSize,
            String mimeType,
            FileStatus status,
            String s3Key,
            String s3Bucket,
            String cdnUrl,
            UploaderId uploaderId,
            String category,
            String tags,
            int retryCount,
            int version,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        validateIdNotNullOrNew(fileId, "ID는 null이거나 새로운 ID일 수 없습니다");

        return new File(
                clock,
                fileId,
                fileName,
                fileSize,
                mimeType,
                status,
                s3Key,
                s3Bucket,
                cdnUrl,
                uploaderId,
                category,
                tags,
                retryCount,
                version,
                deletedAt,
                createdAt,
                updatedAt
        );
    }

    /**
     * 파일 재구성 팩토리 메서드 (영속성 계층용)
     * <p>
     * 영속성 계층에서 조회한 데이터로 Aggregate를 재구성합니다.
     * </p>
     *
     * @param clock       시간 의존성 (테스트 가능한 시간 제어)
     * @param fileId      파일 고유 ID (필수, null 불가)
     * @param fileName    파일명
     * @param fileSize    파일 크기 (바이트)
     * @param mimeType    MIME 타입
     * @param status      파일 상태
     * @param s3Key       S3 객체 키
     * @param s3Bucket    S3 버킷명
     * @param cdnUrl      CDN URL
     * @param uploaderId  업로더 사용자 ID (UploaderId VO)
     * @param category    파일 카테고리
     * @param tags        태그 (콤마 구분, nullable)
     * @param retryCount  재시도 횟수
     * @param version     낙관적 락 버전
     * @param deletedAt   소프트 삭제 시각
     * @param createdAt   생성 시각
     * @param updatedAt   수정 시각
     * @return 재구성된 File Aggregate
     * @throws IllegalArgumentException ID가 null이거나 새로운 ID일 때
     */
    public static File reconstitute(
            Clock clock,
            FileId fileId,
            String fileName,
            long fileSize,
            String mimeType,
            FileStatus status,
            String s3Key,
            String s3Bucket,
            String cdnUrl,
            UploaderId uploaderId,
            String category,
            String tags,
            int retryCount,
            int version,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        validateIdNotNullOrNew(fileId, "재구성을 위한 ID는 null이거나 새로운 ID일 수 없습니다");

        return new File(
                clock,
                fileId,
                fileName,
                fileSize,
                mimeType,
                status,
                s3Key,
                s3Bucket,
                cdnUrl,
                uploaderId,
                category,
                tags,
                retryCount,
                version,
                deletedAt,
                createdAt,
                updatedAt
        );
    }

    /**
     * ID 검증 헬퍼 메서드
     * <p>
     * ID가 null이거나 새로운 ID(값이 null)인 경우 예외를 발생시킵니다.
     * </p>
     *
     * @param fileId       검증할 ID
     * @param errorMessage 에러 메시지
     * @throws IllegalArgumentException ID가 null이거나 새로운 ID일 때
     */
    private static void validateIdNotNullOrNew(FileId fileId, String errorMessage) {
        if (fileId == null || fileId.isNew()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * 파일 생성 팩토리 메서드
     * <p>
     * UUID v7을 자동 생성하고 초기 상태를 PENDING으로 설정합니다.
     * </p>
     *
     * @deprecated {@link #forNew(String, long, String, String, String, UploaderId, String, String, Clock)} 사용을 권장합니다.
     *
     * @param fileName    파일명
     * @param fileSize    파일 크기 (바이트)
     * @param mimeType    MIME 타입
     * @param s3Key       S3 객체 키
     * @param s3Bucket    S3 버킷명
     * @param uploaderId  업로더 사용자 ID
     * @param category    파일 카테고리
     * @param tags        태그 (콤마 구분, nullable)
     * @return 생성된 File Aggregate
     * @throws InvalidFileSizeException 파일 크기가 유효하지 않을 때
     * @throws InvalidMimeTypeException MIME 타입이 허용되지 않을 때
     */
    @Deprecated
    public static File create(
            String fileName,
            long fileSize,
            String mimeType,
            String s3Key,
            String s3Bucket,
            Long uploaderId,
            String category,
            String tags
    ) {
        // UploaderId VO로 변환하여 forNew() 호출 (Clock.systemUTC() 사용)
        return forNew(
                fileName,
                fileSize,
                mimeType,
                s3Key,
                s3Bucket,
                UploaderId.of(uploaderId),
                category,
                tags,
                Clock.systemUTC()
        );
    }

    /**
     * 파일 크기 검증
     *
     * @param fileSize 파일 크기 (바이트)
     * @throws InvalidFileSizeException 파일 크기가 0 이하이거나 1GB 초과일 때
     */
    private static void validateFileSize(long fileSize) {
        if (fileSize <= 0) {
            throw new InvalidFileSizeException("파일 크기는 0보다 커야 합니다. 현재 크기: " + fileSize + " bytes");
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new InvalidFileSizeException(
                    "파일 크기는 1GB를 초과할 수 없습니다. 현재 크기: " + fileSize + " bytes, 최대 크기: " + MAX_FILE_SIZE + " bytes"
            );
        }
    }

    /**
     * MIME 타입 검증
     *
     * @param mimeType MIME 타입
     * @throws InvalidMimeTypeException MIME 타입이 허용 목록에 없을 때
     */
    private static void validateMimeType(String mimeType) {
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new InvalidMimeTypeException(
                    "허용되지 않는 MIME 타입입니다. 제공된 타입: " + mimeType
            );
        }
    }

    /**
     * 상태 전환 헬퍼 메서드
     * <p>
     * 새로운 상태로 File 객체를 생성하며 updatedAt을 자동으로 갱신합니다.
     * </p>
     *
     * @param newStatus 새로운 파일 상태
     * @return 새로운 File 객체
     */
    private File withStatus(FileStatus newStatus) {
        return new File(
                this.clock,
                this.fileId,
                this.fileName,
                this.fileSize,
                this.mimeType,
                newStatus,
                this.s3Key,
                this.s3Bucket,
                this.cdnUrl,
                this.uploaderId,
                this.category,
                this.tags,
                this.retryCount,
                this.version,
                this.deletedAt,
                this.createdAt,
                LocalDateTime.now(clock) // updatedAt 갱신 (Clock 사용)
        );
    }

    /**
     * 파일 상태를 UPLOADING으로 변경
     *
     * @return 새로운 File 객체 (UPLOADING 상태)
     */
    public File markAsUploading() {
        return withStatus(FileStatus.UPLOADING);
    }

    /**
     * 파일 상태를 COMPLETED로 변경
     * <p>
     * PENDING 또는 UPLOADING 상태에서만 전환 가능합니다.
     * </p>
     *
     * @return 새로운 File 객체 (COMPLETED 상태)
     */
    public File markAsCompleted() {
        return withStatus(FileStatus.COMPLETED);
    }

    /**
     * 파일 상태를 FAILED로 변경
     *
     * @param errorMessage 실패 사유
     * @return 새로운 File 객체 (FAILED 상태)
     */
    public File markAsFailed(String errorMessage) {
        return withStatus(FileStatus.FAILED);
    }

    /**
     * 파일 상태를 PROCESSING으로 변경
     * <p>
     * COMPLETED 상태에서만 전환 가능합니다.
     * </p>
     *
     * @return 새로운 File 객체 (PROCESSING 상태)
     * @throws IllegalStateException COMPLETED 상태가 아닐 때
     */
    public File markAsProcessing() {
        if (this.status != FileStatus.COMPLETED) {
            throw new IllegalStateException(
                    "COMPLETED 상태에서만 PROCESSING으로 전환할 수 있습니다. 현재 상태: " + this.status
            );
        }

        return withStatus(FileStatus.PROCESSING);
    }

    /**
     * 재시도 횟수 증가
     *
     * @return 새로운 File 객체 (retryCount + 1)
     */
    public File incrementRetryCount() {
        return new File(
                this.clock,
                this.fileId,
                this.fileName,
                this.fileSize,
                this.mimeType,
                this.status,
                this.s3Key,
                this.s3Bucket,
                this.cdnUrl,
                this.uploaderId,
                this.category,
                this.tags,
                this.retryCount + 1,
                this.version,
                this.deletedAt,
                this.createdAt,
                LocalDateTime.now(clock) // updatedAt 갱신 (Clock 사용)
        );
    }

    /**
     * 파일 소프트 삭제
     * <p>
     * deletedAt을 현재 시각으로 설정합니다.
     * </p>
     *
     * @return 새로운 File 객체 (deletedAt 설정)
     * @throws IllegalStateException 이미 삭제된 파일일 때
     */
    public File softDelete() {
        if (this.deletedAt != null) {
            throw new IllegalStateException("이미 삭제된 파일입니다. 삭제 시각: " + this.deletedAt);
        }

        LocalDateTime now = LocalDateTime.now(clock); // Clock 사용
        return new File(
                this.clock,
                this.fileId,
                this.fileName,
                this.fileSize,
                this.mimeType,
                this.status,
                this.s3Key,
                this.s3Bucket,
                this.cdnUrl,
                this.uploaderId,
                this.category,
                this.tags,
                this.retryCount,
                this.version,
                now, // deletedAt 설정
                this.createdAt,
                now  // updatedAt 갱신
        );
    }
}
