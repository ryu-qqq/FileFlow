package com.ryuqq.fileflow.application.dto.response;

import com.ryuqq.fileflow.application.fixture.FileDetailResponseFixture;
import com.ryuqq.fileflow.domain.aggregate.FileProcessingJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileDetailResponse Record 테스트
 * <p>
 * Response DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Response
 * - 패키지: ..application..dto.response..
 * - 불변 객체 (final fields)
 * </p>
 */
class FileDetailResponseTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * FileDetailResponse Record가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("FileDetailResponse는 Record 타입이어야 한다")
    void shouldBeRecordType() {
        // Given: Fixture로 Response 생성
        FileDetailResponse response = FileDetailResponseFixture.aResponse();

        // When & Then: Record 타입 검증
        assertThat(response).isNotNull();
        assertThat(response.getClass().isRecord()).isTrue();
    }

    @Test
    @DisplayName("FileDetailResponse는 fileId 필드를 가져야 한다")
    void shouldHaveFileIdField() {
        // Given: Fixture로 커스텀 fileId Response 생성
        Long expectedFileId = 100L;
        FileDetailResponse response = FileDetailResponseFixture.withFileId(expectedFileId);

        // When & Then: fileId 필드 검증
        assertThat(response.fileId()).isEqualTo(expectedFileId);
    }

    @Test
    @DisplayName("FileDetailResponse는 status 필드를 가져야 한다")
    void shouldHaveStatusField() {
        // Given: Fixture로 커스텀 status Response 생성
        String expectedStatus = "COMPLETED";
        FileDetailResponse response = FileDetailResponseFixture.withStatus(expectedStatus);

        // When & Then: status 필드 검증
        assertThat(response.status()).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("FileDetailResponse는 s3Url 필드를 가져야 한다")
    void shouldHaveS3UrlField() {
        // Given: Fixture로 커스텀 s3Url Response 생성
        String expectedS3Url = "https://s3.amazonaws.com/bucket/uploads/file.jpg";
        FileDetailResponse response = FileDetailResponseFixture.withS3Url(expectedS3Url);

        // When & Then: s3Url 필드 검증
        assertThat(response.s3Url()).isEqualTo(expectedS3Url);
    }

    @Test
    @DisplayName("FileDetailResponse는 cdnUrl 필드를 가져야 한다")
    void shouldHaveCdnUrlField() {
        // Given: Fixture로 커스텀 cdnUrl Response 생성
        String expectedCdnUrl = "https://cdn.example.com/uploads/file.jpg";
        FileDetailResponse response = FileDetailResponseFixture.withCdnUrl(expectedCdnUrl);

        // When & Then: cdnUrl 필드 검증
        assertThat(response.cdnUrl()).isEqualTo(expectedCdnUrl);
    }

    @Test
    @DisplayName("FileDetailResponse는 processingJobs 필드를 가져야 한다")
    void shouldHaveProcessingJobsField() {
        // Given: Fixture로 processingJobs가 포함된 Response 생성
        FileDetailResponse response = FileDetailResponseFixture.withProcessingJobs();

        // When & Then: processingJobs 필드 검증
        assertThat(response.processingJobs()).isNotNull();
        assertThat(response.processingJobs()).isInstanceOf(List.class);
    }

    @Test
    @DisplayName("FileDetailResponse는 processingJobs 목록이 비어있을 수 있다")
    void shouldHaveEmptyProcessingJobs() {
        // Given: Fixture로 processingJobs가 비어있는 Response 생성
        FileDetailResponse response = FileDetailResponseFixture.withoutProcessingJobs();

        // When & Then: processingJobs가 비어있음을 검증
        assertThat(response.processingJobs()).isEmpty();
    }

    @Test
    @DisplayName("FileDetailResponse는 processingJobs 목록이 여러 개일 수 있다")
    void shouldHaveMultipleProcessingJobs() {
        // Given: Fixture로 processingJobs가 포함 가능한 Response 생성
        FileDetailResponse response = FileDetailResponseFixture.withMultipleJobs();

        // When & Then: processingJobs 필드 존재 검증 (리스트 타입)
        assertThat(response.processingJobs()).isNotNull();
        assertThat(response.processingJobs()).isInstanceOf(List.class);
    }
}
