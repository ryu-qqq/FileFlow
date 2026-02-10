package com.ryuqq.fileflow.application.session.factory.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.application.session.dto.bundle.SingleSessionCreationBundle;
import com.ryuqq.fileflow.application.session.dto.command.CreateSingleUploadSessionCommand;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
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
class SingleSessionCommandFactoryTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock private IdGeneratorPort idGeneratorPort;

    private SingleSessionCommandFactory factory;

    @BeforeEach
    void setUp() {
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(NOW, ZoneOffset.UTC));
        factory = new SingleSessionCommandFactory(idGeneratorPort, timeProvider);
    }

    @Test
    @DisplayName("create - 순수 도메인 계산으로 번들을 생성한다")
    void createReturnsBundleWithDomainComputations() {
        given(idGeneratorPort.generate()).willReturn("session-001");

        CreateSingleUploadSessionCommand command =
                new CreateSingleUploadSessionCommand(
                        "product-image.jpg",
                        "image/jpeg",
                        AccessType.PUBLIC,
                        "product-image",
                        "commerce-service");

        SingleSessionCreationBundle bundle = factory.create(command);

        assertThat(bundle.sessionId().value()).isEqualTo("session-001");
        assertThat(bundle.s3Key()).isEqualTo("public/2026/01/session-001.jpg");
        assertThat(bundle.accessType()).isEqualTo(AccessType.PUBLIC);
        assertThat(bundle.fileName()).isEqualTo("product-image.jpg");
        assertThat(bundle.contentType()).isEqualTo("image/jpeg");
        assertThat(bundle.purpose()).isEqualTo("product-image");
        assertThat(bundle.source()).isEqualTo("commerce-service");
        assertThat(bundle.expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(1)));
        assertThat(bundle.createdAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("create - bucket과 presignedUrl은 null이다 (Coordinator에서 설정)")
    void createReturnsBundleWithNullExternalFields() {
        given(idGeneratorPort.generate()).willReturn("session-001");

        CreateSingleUploadSessionCommand command =
                new CreateSingleUploadSessionCommand(
                        "product-image.jpg",
                        "image/jpeg",
                        AccessType.PUBLIC,
                        "product-image",
                        "commerce-service");

        SingleSessionCreationBundle bundle = factory.create(command);

        assertThat(bundle.bucket()).isNull();
        assertThat(bundle.presignedUrl()).isNull();
    }

    @Test
    @DisplayName("create - 만료 정보를 올바르게 생성한다")
    void createReturnsCorrectExpiration() {
        given(idGeneratorPort.generate()).willReturn("session-001");

        CreateSingleUploadSessionCommand command =
                new CreateSingleUploadSessionCommand(
                        "product-image.jpg",
                        "image/jpeg",
                        AccessType.PUBLIC,
                        "product-image",
                        "commerce-service");

        SingleSessionCreationBundle bundle = factory.create(command);

        assertThat(bundle.expiration().sessionId()).isEqualTo("session-001");
        assertThat(bundle.expiration().sessionType()).isEqualTo("SINGLE");
        assertThat(bundle.expiration().ttl()).isEqualTo(Duration.ofHours(1));
    }
}
