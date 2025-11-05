package com.ryuqq.fileflow.application.upload.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ryuqq.fileflow.application.upload.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.upload.manager.MultipartUploadStateManager;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadMultipartUploadPort;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.ProviderUploadId;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.TotalParts;
import com.ryuqq.fileflow.domain.upload.UploadPart;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;
import com.ryuqq.fileflow.domain.upload.fixture.MultipartUploadFixture;
import com.ryuqq.fileflow.domain.upload.fixture.UploadSessionFixture;

/**
 * MarkPartUploadedService 단위 테스트
 *
 * <p>테스트 구성: Happy Path, Edge Cases, Exception Cases</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MarkPartUploadedService 단위 테스트")
class MarkPartUploadedServiceTest {

    @Mock
    private LoadUploadSessionPort loadUploadSessionPort;

    @Mock
    private LoadMultipartUploadPort loadMultipartUploadPort;

    @Mock
    private MultipartUploadStateManager multipartUploadStateManager;

    @InjectMocks
    private MarkPartUploadedService service;

    @Nested
    @DisplayName("Happy Path 테스트")
    class HappyPathTests {

        @Test
        @DisplayName("execute_Success - 파트 업로드 완료 처리 성공")
        void execute_Success() {
            // Given
            String sessionKey = "session-key-123";
            MarkPartUploadedCommand command = MarkPartUploadedCommand.of(
                sessionKey,
                1,
                "etag-123",
                5242880L
            );

            UploadSession tempSession = UploadSessionFixture.createMultipart();
            UploadSession session = UploadSessionFixture.reconstitute(
                1L,
                SessionKey.of(sessionKey),
                tempSession.getTenantId(),
                tempSession.getFileName(),
                tempSession.getFileSize(),
                tempSession.getUploadType(),
                tempSession.getStorageKey(),
                tempSession.getStatus(),
                null,
                null,
                tempSession.getCreatedAt(),
                tempSession.getUpdatedAt(),
                null,
                null
            );

            MultipartUpload multipart = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(session.getIdValue()))
                .providerUploadId(ProviderUploadId.of("aws-upload-id-123"))
                .totalParts(TotalParts.of(3))
                .initiate()
                .build();

            MultipartUpload savedMultipart = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(session.getIdValue()))
                .providerUploadId(ProviderUploadId.of("aws-upload-id-123"))
                .totalParts(TotalParts.of(3))
                .initiate()
                .addParts(1)
                .build();

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));
            when(loadMultipartUploadPort.findByUploadSessionId(session.getIdValue()))
                .thenReturn(Optional.of(multipart));
            when(multipartUploadStateManager.addPart(eq(multipart), any(UploadPart.class)))
                .thenReturn(savedMultipart);

            // When
            service.execute(command);

            // Then
            verify(loadUploadSessionPort).findBySessionKey(SessionKey.of(sessionKey));
            verify(loadMultipartUploadPort).findByUploadSessionId(session.getIdValue());
            verify(multipartUploadStateManager).addPart(eq(multipart), any(UploadPart.class));
        }

        @Test
        @DisplayName("execute_Success_MultipleParts - 여러 파트 순차 업로드")
        void execute_Success_MultipleParts() {
            // Given
            String sessionKey = "session-key-456";
            UploadSession tempSession = UploadSessionFixture.createMultipart();
            UploadSession session = UploadSessionFixture.reconstitute(
                1L,
                SessionKey.of(sessionKey),
                tempSession.getTenantId(),
                tempSession.getFileName(),
                tempSession.getFileSize(),
                tempSession.getUploadType(),
                tempSession.getStorageKey(),
                tempSession.getStatus(),
                null,
                null,
                tempSession.getCreatedAt(),
                tempSession.getUpdatedAt(),
                null,
                null
            );

            MultipartUpload multipart = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(session.getIdValue()))
                .totalParts(TotalParts.of(3))
                .initiate()
                .build();

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));
            when(loadMultipartUploadPort.findByUploadSessionId(session.getIdValue()))
                .thenReturn(Optional.of(multipart));

            // 첫 번째 파트
            MarkPartUploadedCommand command1 = MarkPartUploadedCommand.of(sessionKey, 1, "etag-1", 5242880L);
            when(multipartUploadStateManager.addPart(eq(multipart), any(UploadPart.class)))
                .thenReturn(multipart);

            // When
            service.execute(command1);

            // Then
            verify(multipartUploadStateManager).addPart(eq(multipart), any(UploadPart.class));
        }
    }

    @Nested
    @DisplayName("Exception Cases 테스트")
    class ExceptionCasesTests {

        @Test
        @DisplayName("execute_ThrowsException_WhenSessionNotFound - 세션 조회 실패")
        void execute_ThrowsException_WhenSessionNotFound() {
            // Given
            String sessionKey = "non-existent-session";
            MarkPartUploadedCommand command = MarkPartUploadedCommand.of(
                sessionKey,
                1,
                "etag-123",
                5242880L
            );

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upload session not found");

            verify(loadMultipartUploadPort, never()).findByUploadSessionId(any(Long.class));
            verify(multipartUploadStateManager, never()).addPart(any(), any());
        }

        @Test
        @DisplayName("execute_ThrowsException_WhenNotMultipartUpload - Multipart 업로드가 아님")
        void execute_ThrowsException_WhenNotMultipartUpload() {
            // Given
            String sessionKey = "session-key-single";
            MarkPartUploadedCommand command = MarkPartUploadedCommand.of(
                sessionKey,
                1,
                "etag-123",
                5242880L
            );

            UploadSession session = UploadSessionFixture.createSingle();
            session = UploadSessionFixture.reconstitute(
                session.getIdValue(),
                SessionKey.of(sessionKey),
                session.getTenantId(),
                session.getFileName(),
                session.getFileSize(),
                session.getUploadType(),
                session.getStorageKey(),
                session.getStatus(),
                null,
                null,
                session.getCreatedAt(),
                session.getUpdatedAt(),
                null,
                null
            );

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));
            when(loadMultipartUploadPort.findByUploadSessionId(session.getIdValue()))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not a multipart upload");

            verify(multipartUploadStateManager, never()).addPart(any(), any());
        }

        @Test
        @DisplayName("execute_ThrowsException_WhenDuplicatePart - 중복 파트 번호")
        void execute_ThrowsException_WhenDuplicatePart() {
            // Given
            String sessionKey = "session-key-duplicate";
            MarkPartUploadedCommand command = MarkPartUploadedCommand.of(
                sessionKey,
                1,
                "etag-123",
                5242880L
            );

            UploadSession tempSession = UploadSessionFixture.createMultipart();
            UploadSession session = UploadSessionFixture.reconstitute(
                1L,
                SessionKey.of(sessionKey),
                tempSession.getTenantId(),
                tempSession.getFileName(),
                tempSession.getFileSize(),
                tempSession.getUploadType(),
                tempSession.getStorageKey(),
                tempSession.getStatus(),
                null,
                null,
                tempSession.getCreatedAt(),
                tempSession.getUpdatedAt(),
                null,
                null
            );

            MultipartUpload multipart = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(session.getIdValue()))
                .totalParts(TotalParts.of(3))
                .initiate()
                .addParts(1) // 이미 파트 1이 추가됨
                .build();

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));
            when(loadMultipartUploadPort.findByUploadSessionId(session.getIdValue()))
                .thenReturn(Optional.of(multipart));
            when(multipartUploadStateManager.addPart(eq(multipart), any(UploadPart.class)))
                .thenThrow(new com.ryuqq.fileflow.domain.upload.exception.DuplicatePartNumberException(1));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(com.ryuqq.fileflow.domain.upload.exception.DuplicatePartNumberException.class);
        }
    }
}

