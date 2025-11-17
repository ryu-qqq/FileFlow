package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.GeneratePresignedUrlCommand;
import com.ryuqq.fileflow.application.dto.response.PresignedUrlResponse;
import com.ryuqq.fileflow.application.fixture.GeneratePresignedUrlCommandFixture;
import com.ryuqq.fileflow.application.port.out.command.FilePersistencePort;
import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.exception.InvalidFileSizeException;
import com.ryuqq.fileflow.domain.exception.InvalidMimeTypeException;
import com.ryuqq.fileflow.domain.fixture.FileFixture;
import com.ryuqq.fileflow.domain.vo.FileId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * GeneratePresignedUrlService 테스트
 * <p>
 * Application Layer Service 테스트 규칙:
 * - Mock Port 사용 (Outbound Port)
 * - TestFixture 필수 사용 (Command, Domain)
 * - Transaction 경계 검증 (@Transactional 내 외부 API 호출 금지)
 * - CQRS 준수 검증 (Command UseCase)
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class GeneratePresignedUrlServiceTest {

    @Mock
    private FilePersistencePort filePersistencePort;

    @Mock
    private com.ryuqq.fileflow.application.port.out.external.S3ClientPort s3ClientPort;

    private GeneratePresignedUrlService generatePresignedUrlService;

    @BeforeEach
    void setUp() {
        // Fixed Clock for deterministic testing
        java.time.Clock clock = java.time.Clock.fixed(
                java.time.Instant.parse("2024-11-16T10:00:00Z"),
                java.time.ZoneId.systemDefault()
        );

        generatePresignedUrlService = new GeneratePresignedUrlService(
                filePersistencePort,
                s3ClientPort,
                clock
        );
    }

    /**
     * 🔴 RED Phase: 실패하는 테스트
     * <p>
     * GeneratePresignedUrlService가 존재하지 않으므로 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("파일 메타데이터를 생성하고 저장해야 한다")
    void shouldCreateFileMetadata() {
        // Given: Fixture로 Command 생성
        GeneratePresignedUrlCommand command = GeneratePresignedUrlCommandFixture.create();

        // Given: Domain Fixture로 File Aggregate 생성
        File file = FileFixture.aJpgImage();
        FileId expectedFileId = file.getFileId();

        // Given: Mock FilePersistencePort 동작 정의
        given(filePersistencePort.persist(any(File.class)))
                .willReturn(expectedFileId);

        // Given: Mock S3ClientPort 동작 정의
        String expectedPresignedUrl = "https://s3.amazonaws.com/fileflow-bucket/uploads/test.jpg?signature=abc123";
        given(s3ClientPort.generatePresignedUrl(any(String.class), any(java.time.Duration.class)))
                .willReturn(expectedPresignedUrl);

        // When: UseCase 실행
        PresignedUrlResponse response = generatePresignedUrlService.execute(command);

        // Then: 파일 메타데이터가 저장되었는지 검증
        verify(filePersistencePort).persist(any(File.class));

        // Then: S3 Presigned URL이 생성되었는지 검증
        verify(s3ClientPort).generatePresignedUrl(any(String.class), any(java.time.Duration.class));

        // Then: Response 검증
        assertThat(response).isNotNull();
        assertThat(response.fileId()).isNotNull();
        assertThat(response.presignedUrl()).isEqualTo(expectedPresignedUrl);
        assertThat(response.s3Key()).isNotNull();
        assertThat(response.expiresIn()).isEqualTo(3600L); // 기본 1시간
    }

    /**
     * 🔴 RED Phase: 파일 크기 검증 실패 테스트
     * <p>
     * 1GB를 초과하는 파일 크기일 경우 InvalidFileSizeException이 발생해야 합니다.
     * File.forNew() 메서드에서 Domain 레벨 검증을 수행하므로,
     * Service는 Domain 예외를 전파합니다.
     * </p>
     */
    @Test
    @DisplayName("파일 크기가 1GB를 초과하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenFileSizeExceeds1GB() {
        // Given: 1GB를 초과하는 파일 크기 Command
        long exceedingSize = 1024L * 1024L * 1024L + 1L; // 1GB + 1 byte
        GeneratePresignedUrlCommand command = GeneratePresignedUrlCommandFixture.withFileSize(exceedingSize);

        // When & Then: InvalidFileSizeException 발생 검증
        assertThatThrownBy(() -> generatePresignedUrlService.execute(command))
                .isInstanceOf(InvalidFileSizeException.class)
                .hasMessageContaining("1GB");
    }

    /**
     * 🔴 RED Phase: MIME 타입 검증 실패 테스트
     * <p>
     * 허용되지 않는 MIME 타입일 경우 InvalidMimeTypeException이 발생해야 합니다.
     * File.forNew() 메서드에서 Domain 레벨 검증을 수행하므로,
     * Service는 Domain 예외를 전파합니다.
     * </p>
     */
    @Test
    @DisplayName("허용되지 않는 MIME 타입이면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenInvalidMimeType() {
        // Given: 허용되지 않는 MIME 타입 Command
        GeneratePresignedUrlCommand command = GeneratePresignedUrlCommandFixture.withMimeType("application/invalid");

        // When & Then: InvalidMimeTypeException 발생 검증
        assertThatThrownBy(() -> generatePresignedUrlService.execute(command))
                .isInstanceOf(InvalidMimeTypeException.class)
                .hasMessageContaining("허용되지 않는");
    }
}
