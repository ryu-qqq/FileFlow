package com.ryuqq.fileflow.application.session.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.application.session.dto.command.CreateSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.factory.command.SingleSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.PresignedUploadManager;
import com.ryuqq.fileflow.application.session.manager.client.SessionExpirationManager;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.SessionExpiration;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SingleSessionCreationCoordinatorTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final String BUCKET = "fileflow-bucket";

    @Mock private IdGeneratorPort idGeneratorPort;
    @Mock private PresignedUploadManager presignedUploadManager;
    @Mock private SessionCommandManager sessionCommandManager;
    @Mock private SessionExpirationManager sessionExpirationManager;

    private SingleSessionCreationCoordinator coordinator;

    @BeforeEach
    void setUp() {
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(NOW, ZoneOffset.UTC));
        SingleSessionCommandFactory singleFactory =
                new SingleSessionCommandFactory(idGeneratorPort, timeProvider);

        coordinator =
                new SingleSessionCreationCoordinator(
                        singleFactory, presignedUploadManager,
                        sessionCommandManager, sessionExpirationManager);
    }

    @Test
    @DisplayName("create - 팩토리 → 외부 데이터 해결 → 세션 반환")
    void createOrchestratesFullFlow() {
        given(idGeneratorPort.generate()).willReturn("session-001");
        given(presignedUploadManager.getBucket()).willReturn(BUCKET);
        given(
                        presignedUploadManager.generatePresignedUploadUrl(
                                eq("public/2026/01/session-001.jpg"),
                                eq("image/jpeg"),
                                any(Duration.class)))
                .willReturn("https://s3.presigned-url.com/test");

        CreateSingleUploadSessionCommand command =
                new CreateSingleUploadSessionCommand(
                        "product-image.jpg",
                        "image/jpeg",
                        AccessType.PUBLIC,
                        "product-image",
                        "commerce-service");

        SingleUploadSession session = coordinator.create(command);

        assertThat(session.idValue()).isEqualTo("session-001");
        assertThat(session.bucket()).isEqualTo(BUCKET);
        assertThat(session.presignedUrlValue()).isEqualTo("https://s3.presigned-url.com/test");
        assertThat(session.s3Key()).isEqualTo("public/2026/01/session-001.jpg");
    }

    @Test
    @DisplayName("create - 세션을 영속화한다")
    void createPersistsSession() {
        given(idGeneratorPort.generate()).willReturn("session-001");
        given(presignedUploadManager.getBucket()).willReturn(BUCKET);
        given(presignedUploadManager.generatePresignedUploadUrl(any(), any(), any()))
                .willReturn("https://s3.presigned-url.com/test");

        CreateSingleUploadSessionCommand command =
                new CreateSingleUploadSessionCommand(
                        "product-image.jpg",
                        "image/jpeg",
                        AccessType.PUBLIC,
                        "product-image",
                        "commerce-service");

        coordinator.create(command);

        then(sessionCommandManager).should().persist(any(SingleUploadSession.class));
    }

    @Test
    @DisplayName("create - 만료를 등록한다")
    void createRegistersExpiration() {
        given(idGeneratorPort.generate()).willReturn("session-001");
        given(presignedUploadManager.getBucket()).willReturn(BUCKET);
        given(presignedUploadManager.generatePresignedUploadUrl(any(), any(), any()))
                .willReturn("https://s3.presigned-url.com/test");

        CreateSingleUploadSessionCommand command =
                new CreateSingleUploadSessionCommand(
                        "product-image.jpg",
                        "image/jpeg",
                        AccessType.PUBLIC,
                        "product-image",
                        "commerce-service");

        coordinator.create(command);

        then(sessionExpirationManager).should().registerExpiration(any(SessionExpiration.class));
    }
}
