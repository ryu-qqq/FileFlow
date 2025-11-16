package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.GeneratePresignedUrlCommand;
import com.ryuqq.fileflow.application.dto.response.PresignedUrlResponse;
import com.ryuqq.fileflow.application.fixture.GeneratePresignedUrlCommandFixture;
import com.ryuqq.fileflow.application.port.out.command.FilePersistencePort;
import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.fixture.FileFixture;
import com.ryuqq.fileflow.domain.vo.FileId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
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

    private GeneratePresignedUrlService generatePresignedUrlService;

    @BeforeEach
    void setUp() {
        // GeneratePresignedUrlService가 존재하지 않으므로 컴파일 에러 발생
        generatePresignedUrlService = new GeneratePresignedUrlService(filePersistencePort);
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
        File file = FileFixture.create();
        FileId expectedFileId = file.getFileId();

        // Given: Mock Port 동작 정의
        given(filePersistencePort.persist(any(File.class)))
                .willReturn(expectedFileId);

        // When: UseCase 실행
        PresignedUrlResponse response = generatePresignedUrlService.execute(command);

        // Then: 파일 메타데이터가 저장되었는지 검증
        verify(filePersistencePort).persist(any(File.class));

        // Then: Response 검증
        assertThat(response).isNotNull();
        assertThat(response.fileId()).isNotNull();
        assertThat(response.presignedUrl()).isNotNull();
        assertThat(response.s3Key()).isNotNull();
        assertThat(response.expiresIn()).isEqualTo(3600L); // 기본 1시간
    }
}
