package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.UploadFromExternalUrlCommand;
import com.ryuqq.fileflow.application.fixture.UploadFromExternalUrlCommandFixture;
import com.ryuqq.fileflow.application.port.in.command.UploadFromExternalUrlPort;
import com.ryuqq.fileflow.application.port.out.command.MessageOutboxPersistencePort;
import com.ryuqq.fileflow.application.port.out.command.SaveFilePort;
import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.aggregate.MessageOutbox;
import com.ryuqq.fileflow.domain.fixture.FileFixture;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.MessageOutboxId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * UploadFromExternalUrlService 테스트
 * <p>
 * Application Layer Service 테스트 규칙:
 * - Mock Port 사용 (Outbound Port)
 * - TestFixture 필수 사용 (Command, Domain)
 * - Transaction 경계 검증 (@Transactional 내 외부 API 호출 금지)
 * - CQRS 준수 검증 (Command UseCase)
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class UploadFromExternalUrlServiceTest {

    @Mock
    private SaveFilePort saveFilePort;

    @Mock
    private MessageOutboxPersistencePort messageOutboxPersistencePort;

    private UploadFromExternalUrlPort uploadFromExternalUrlPort;

    @BeforeEach
    void setUp() {
        java.time.Clock clock = java.time.Clock.fixed(
                java.time.Instant.parse("2024-11-16T10:00:00Z"),
                java.time.ZoneId.systemDefault()
        );

        uploadFromExternalUrlPort = new UploadFromExternalUrlService(
                saveFilePort,
                messageOutboxPersistencePort,
                clock
        );
    }

    /**
     * 🔴 RED Phase: URL 검증 테스트
     * <p>
     * HTTP URL은 허용하지 않습니다. HTTPS만 허용합니다.
     * </p>
     */
    @Test
    @DisplayName("HTTP URL은 업로드할 수 없다 (HTTPS만 허용)")
    void shouldThrowExceptionWhenInvalidUrl() {
        // Given: HTTP URL (보안상 허용하지 않음)
        UploadFromExternalUrlCommand command = UploadFromExternalUrlCommandFixture
                .withExternalUrl("http://example.com/image.jpg");

        // When & Then: IllegalArgumentException 발생 (InvalidUrlException)
        assertThatThrownBy(() -> uploadFromExternalUrlPort.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTPS");
    }

    /**
     * 🔴 RED Phase: MessageOutbox 생성 테스트
     * <p>
     * 외부 URL 다운로드 요청 시 FILE_DOWNLOAD_REQUESTED 이벤트를 MessageOutbox에 저장해야 합니다.
     * Transaction 경계: File 메타데이터 생성 + Outbox 생성 (트랜잭션 내부)
     * </p>
     */
    @Test
    @DisplayName("외부 URL 다운로드 요청 시 FILE_DOWNLOAD_REQUESTED 이벤트를 MessageOutbox에 저장해야 한다")
    void shouldCreateMessageOutboxForExternalDownload() {
        // Given: 유효한 HTTPS URL
        UploadFromExternalUrlCommand command = UploadFromExternalUrlCommandFixture.create();

        // Given: File 메타데이터 생성 성공 (PENDING 상태)
        File pendingFile = FileFixture.aFile()
                .fileName("external-image.jpg")
                .build();
        given(saveFilePort.save(any(File.class)))
                .willReturn(FileId.of("file-123"));

        // Given: MessageOutbox 저장 성공
        given(messageOutboxPersistencePort.persist(any(MessageOutbox.class)))
                .willReturn(MessageOutboxId.of("outbox-456"));

        // When: 외부 URL 다운로드 요청 실행
        uploadFromExternalUrlPort.execute(command);

        // Then: File이 저장되었는지 검증
        verify(saveFilePort).save(any(File.class));

        // Then: MessageOutbox가 persist 되었는지 검증
        verify(messageOutboxPersistencePort).persist(any(MessageOutbox.class));
    }
}
