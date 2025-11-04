package com.ryuqq.fileflow.adapter.rest.file.fixture;

import com.ryuqq.fileflow.adapter.rest.file.dto.response.FileMetadataApiResponse;

import java.time.LocalDateTime;

/**
 * FileMetadataApiResponse 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see FileMetadataApiResponse
 */
public class FileMetadataApiResponseFixture {

    /**
     * 기본값으로 FileMetadataApiResponse 생성
     *
     * @return 기본값을 가진 FileMetadataApiResponse
     */
    public static FileMetadataApiResponse create() {
        return FileMetadataApiResponse.of(
            1L,                                      // fileId
            "test-file.pdf",                         // fileName
            1048576L,                                // fileSize
            "application/pdf",                       // contentType
            "AVAILABLE",                             // status
            "PRIVATE",                               // visibility
            "storage/key/test-file.pdf",            // storageKey
            LocalDateTime.of(2024, 1, 1, 0, 0),     // uploadedAt
            LocalDateTime.of(2024, 12, 31, 23, 59)  // expiresAt
        );
    }

    /**
     * 특정 ID로 FileMetadataApiResponse 생성
     *
     * @param fileId File ID
     * @return 지정된 ID를 가진 FileMetadataApiResponse
     */
    public static FileMetadataApiResponse createWithId(Long fileId) {
        return FileMetadataApiResponse.of(
            fileId,                                  // fileId
            "test-file.pdf",                         // fileName
            1048576L,                                // fileSize
            "application/pdf",                       // contentType
            "AVAILABLE",                             // status
            "PRIVATE",                               // visibility
            "storage/key/test-file.pdf",            // storageKey
            LocalDateTime.of(2024, 1, 1, 0, 0),     // uploadedAt
            LocalDateTime.of(2024, 12, 31, 23, 59)  // expiresAt
        );
    }

    /**
     * 특정 상태로 FileMetadataApiResponse 생성
     *
     * @param status 파일 상태
     * @return 지정된 상태를 가진 FileMetadataApiResponse
     */
    public static FileMetadataApiResponse createWithStatus(String status) {
        return FileMetadataApiResponse.of(
            1L,                                      // fileId
            "test-file.pdf",                         // fileName
            1048576L,                                // fileSize
            "application/pdf",                       // contentType
            status,                                  // status
            "PRIVATE",                               // visibility
            "storage/key/test-file.pdf",            // storageKey
            LocalDateTime.of(2024, 1, 1, 0, 0),     // uploadedAt
            LocalDateTime.of(2024, 12, 31, 23, 59)  // expiresAt
        );
    }

    /**
     * 모든 필드를 지정하여 FileMetadataApiResponse 생성
     *
     * @param fileId File ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType Content Type
     * @param status 상태
     * @param visibility 가시성
     * @param storageKey 스토리지 키
     * @param uploadedAt 업로드 시간
     * @param expiresAt 만료 시간
     * @return FileMetadataApiResponse
     */
    public static FileMetadataApiResponse createWith(
        Long fileId,
        String fileName,
        Long fileSize,
        String contentType,
        String status,
        String visibility,
        String storageKey,
        LocalDateTime uploadedAt,
        LocalDateTime expiresAt
    ) {
        return FileMetadataApiResponse.of(
            fileId,
            fileName,
            fileSize,
            contentType,
            status,
            visibility,
            storageKey,
            uploadedAt,
            expiresAt
        );
    }

    // Private 생성자
    private FileMetadataApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
