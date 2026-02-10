package com.ryuqq.fileflow.application.session.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.application.session.dto.command.CreateMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.factory.command.MultipartSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.MultipartUploadManager;
import com.ryuqq.fileflow.application.session.manager.client.PresignedUploadManager;
import com.ryuqq.fileflow.application.session.manager.client.SessionExpirationManager;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.vo.SessionExpiration;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MultipartSessionCreationCoordinatorTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final String BUCKET = "fileflow-bucket";

    @Mock private IdGeneratorPort idGeneratorPort;
    @Mock private PresignedUploadManager presignedUploadManager;
    @Mock private MultipartUploadManager multipartUploadManager;
    @Mock private SessionCommandManager sessionCommandManager;
    @Mock private SessionExpirationManager sessionExpirationManager;

    private MultipartSessionCreationCoordinator coordinator;

    @BeforeEach
    void setUp() {
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(NOW, ZoneOffset.UTC));
        MultipartSessionCommandFactory multipartFactory =
                new MultipartSessionCommandFactory(idGeneratorPort, timeProvider);

        coordinator =
                new MultipartSessionCreationCoordinator(
                        multipartFactory,
                        presignedUploadManager,
                        multipartUploadManager,
                        sessionCommandManager,
                        sessionExpirationManager);
    }

    @Test
    @DisplayName("create - 팩토리 → 외부 데이터 해결 → 세션 반환")
    void createOrchestratesFullFlow() {
        given(idGeneratorPort.generate()).willReturn("multipart-session-001");
        given(presignedUploadManager.getBucket()).willReturn(BUCKET);
        given(
                        multipartUploadManager.createMultipartUpload(
                                eq("public/2026/01/multipart-session-001.jpg"), eq("image/jpeg")))
                .willReturn("upload-id-001");

        CreateMultipartUploadSessionCommand command =
                new CreateMultipartUploadSessionCommand(
                        "large-file.jpg",
                        "image/jpeg",
                        AccessType.PUBLIC,
                        5_242_880L,
                        "product-image",
                        "commerce-service");

        MultipartUploadSession session = coordinator.create(command);

        assertThat(session.idValue()).isEqualTo("multipart-session-001");
        assertThat(session.bucket()).isEqualTo(BUCKET);
        assertThat(session.uploadId()).isEqualTo("upload-id-001");
        assertThat(session.s3Key()).isEqualTo("public/2026/01/multipart-session-001.jpg");
        assertThat(session.partSize()).isEqualTo(5_242_880L);
    }

    @Test
    @DisplayName("create - 세션을 영속화한다")
    void createPersistsSession() {
        given(idGeneratorPort.generate()).willReturn("multipart-session-001");
        given(presignedUploadManager.getBucket()).willReturn(BUCKET);
        given(multipartUploadManager.createMultipartUpload(any(), any()))
                .willReturn("upload-id-001");

        CreateMultipartUploadSessionCommand command =
                new CreateMultipartUploadSessionCommand(
                        "large-file.jpg",
                        "image/jpeg",
                        AccessType.PUBLIC,
                        5_242_880L,
                        "product-image",
                        "commerce-service");

        coordinator.create(command);

        then(sessionCommandManager).should().persist(any(MultipartUploadSession.class));
    }

    @Test
    @DisplayName("create - 만료를 등록한다")
    void createRegistersExpiration() {
        given(idGeneratorPort.generate()).willReturn("multipart-session-001");
        given(presignedUploadManager.getBucket()).willReturn(BUCKET);
        given(multipartUploadManager.createMultipartUpload(any(), any()))
                .willReturn("upload-id-001");

        CreateMultipartUploadSessionCommand command =
                new CreateMultipartUploadSessionCommand(
                        "large-file.jpg",
                        "image/jpeg",
                        AccessType.PUBLIC,
                        5_242_880L,
                        "product-image",
                        "commerce-service");

        coordinator.create(command);

        then(sessionExpirationManager).should().registerExpiration(any(SessionExpiration.class));
    }
}
