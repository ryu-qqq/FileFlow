package com.ryuqq.fileflow.adapter.rest.file.fixture;

import com.ryuqq.fileflow.adapter.rest.file.dto.response.FileDetailApiResponse;
import com.ryuqq.fileflow.adapter.rest.file.dto.response.FileVariantInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * FileDetailApiResponse 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see FileDetailApiResponse
 */
public class FileDetailApiResponseFixture {

    /**
     * 기본값으로 FileDetailApiResponse 생성
     *
     * @return 기본값을 가진 FileDetailApiResponse
     */
    public static FileDetailApiResponse create() {
        return FileDetailApiResponse.of(
            1L,                                                 // fileId
            "test-image.jpg",                                   // fileName
            1048576L,                                           // fileSize
            "image/jpeg",                                       // contentType
            "AVAILABLE",                                        // status
            "PRIVATE",                                          // visibility
            100L,                                               // ownerUserId
            "tenant-1",                                         // tenantId
            200L,                                               // organizationId
            "session-key-123",                                  // uploadSessionKey
            "checksum-abc",                                     // checksum
            FileVariantInfoFixture.createOriginal(),            // original
            List.of(FileVariantInfoFixture.createOriginal()),   // variants
            Map.of("key", "value"),                             // metadata
            LocalDateTime.of(2024, 1, 1, 0, 0),                // uploadedAt
            LocalDateTime.of(2024, 1, 1, 0, 1),                // processedAt
            LocalDateTime.of(2024, 12, 31, 23, 59),            // expiresAt
            365                                                 // retentionDays
        );
    }

    /**
     * 특정 ID로 FileDetailApiResponse 생성
     *
     * @param fileId File ID
     * @return 지정된 ID를 가진 FileDetailApiResponse
     */
    public static FileDetailApiResponse createWithId(Long fileId) {
        return FileDetailApiResponse.of(
            fileId,                                             // fileId
            "test-image.jpg",                                   // fileName
            1048576L,                                           // fileSize
            "image/jpeg",                                       // contentType
            "AVAILABLE",                                        // status
            "PRIVATE",                                          // visibility
            100L,                                               // ownerUserId
            "tenant-1",                                         // tenantId
            200L,                                               // organizationId
            "session-key-123",                                  // uploadSessionKey
            "checksum-abc",                                     // checksum
            FileVariantInfoFixture.createOriginal(),            // original
            List.of(FileVariantInfoFixture.createOriginal()),   // variants
            Map.of("key", "value"),                             // metadata
            LocalDateTime.of(2024, 1, 1, 0, 0),                // uploadedAt
            LocalDateTime.of(2024, 1, 1, 0, 1),                // processedAt
            LocalDateTime.of(2024, 12, 31, 23, 59),            // expiresAt
            365                                                 // retentionDays
        );
    }

    /**
     * 여러 변형본을 가진 FileDetailApiResponse 생성
     *
     * @return 여러 변형본을 가진 FileDetailApiResponse
     */
    public static FileDetailApiResponse createWithVariants() {
        return FileDetailApiResponse.of(
            1L,                                                 // fileId
            "test-image.jpg",                                   // fileName
            1048576L,                                           // fileSize
            "image/jpeg",                                       // contentType
            "AVAILABLE",                                        // status
            "PRIVATE",                                          // visibility
            100L,                                               // ownerUserId
            "tenant-1",                                         // tenantId
            200L,                                               // organizationId
            "session-key-123",                                  // uploadSessionKey
            "checksum-abc",                                     // checksum
            FileVariantInfoFixture.createOriginal(),            // original
            List.of(
                FileVariantInfoFixture.createOriginal(),
                FileVariantInfoFixture.createThumbnail()
            ),                                                  // variants
            Map.of("key", "value"),                             // metadata
            LocalDateTime.of(2024, 1, 1, 0, 0),                // uploadedAt
            LocalDateTime.of(2024, 1, 1, 0, 1),                // processedAt
            LocalDateTime.of(2024, 12, 31, 23, 59),            // expiresAt
            365                                                 // retentionDays
        );
    }

    /**
     * 모든 필드를 지정하여 FileDetailApiResponse 생성
     *
     * @param fileId File ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType Content Type
     * @param status 상태
     * @param visibility 가시성
     * @param ownerUserId 소유자 User ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param uploadSessionKey 업로드 세션 키
     * @param checksum 체크섬
     * @param original 원본 정보
     * @param variants 변형본 목록
     * @param metadata 메타데이터
     * @param uploadedAt 업로드 시간
     * @param processedAt 처리 완료 시간
     * @param expiresAt 만료 시간
     * @param retentionDays 보존 기간 (일)
     * @return FileDetailApiResponse
     */
    public static FileDetailApiResponse createWith(
        Long fileId,
        String fileName,
        Long fileSize,
        String contentType,
        String status,
        String visibility,
        Long ownerUserId,
        String tenantId,
        Long organizationId,
        String uploadSessionKey,
        String checksum,
        FileVariantInfo original,
        List<FileVariantInfo> variants,
        Map<String, String> metadata,
        LocalDateTime uploadedAt,
        LocalDateTime processedAt,
        LocalDateTime expiresAt,
        Integer retentionDays
    ) {
        return FileDetailApiResponse.of(
            fileId,
            fileName,
            fileSize,
            contentType,
            status,
            visibility,
            ownerUserId,
            tenantId,
            organizationId,
            uploadSessionKey,
            checksum,
            original,
            variants,
            metadata,
            uploadedAt,
            processedAt,
            expiresAt,
            retentionDays
        );
    }

    // Private 생성자
    private FileDetailApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
