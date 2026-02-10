package com.ryuqq.fileflow.application.session.factory.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.application.session.dto.bundle.MultipartSessionCreationBundle;
import com.ryuqq.fileflow.application.session.dto.command.CreateMultipartUploadSessionCommand;
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
class MultipartSessionCommandFactoryTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock private IdGeneratorPort idGeneratorPort;

    private MultipartSessionCommandFactory factory;

    @BeforeEach
    void setUp() {
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(NOW, ZoneOffset.UTC));
        factory = new MultipartSessionCommandFactory(idGeneratorPort, timeProvider);
    }

    @Test
    @DisplayName("create - 순수 도메인 계산으로 멀티파트 번들을 생성한다")
    void createReturnsBundleWithDomainComputations() {
        given(idGeneratorPort.generate()).willReturn("multipart-session-001");

        CreateMultipartUploadSessionCommand command =
                new CreateMultipartUploadSessionCommand(
                        "large-file.jpg",
                        "image/jpeg",
                        AccessType.PUBLIC,
                        5_242_880L,
                        "product-image",
                        "commerce-service");

        MultipartSessionCreationBundle bundle = factory.create(command);

        assertThat(bundle.sessionId().value()).isEqualTo("multipart-session-001");
        assertThat(bundle.s3Key()).isEqualTo("public/2026/01/multipart-session-001.jpg");
        assertThat(bundle.accessType()).isEqualTo(AccessType.PUBLIC);
        assertThat(bundle.fileName()).isEqualTo("large-file.jpg");
        assertThat(bundle.contentType()).isEqualTo("image/jpeg");
        assertThat(bundle.partSize()).isEqualTo(5_242_880L);
        assertThat(bundle.purpose()).isEqualTo("product-image");
        assertThat(bundle.source()).isEqualTo("commerce-service");
        assertThat(bundle.expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(24)));
        assertThat(bundle.createdAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("create - bucket과 uploadId는 null이다 (Coordinator에서 설정)")
    void createReturnsBundleWithNullExternalFields() {
        given(idGeneratorPort.generate()).willReturn("multipart-session-001");

        CreateMultipartUploadSessionCommand command =
                new CreateMultipartUploadSessionCommand(
                        "large-file.jpg",
                        "image/jpeg",
                        AccessType.PUBLIC,
                        5_242_880L,
                        "product-image",
                        "commerce-service");

        MultipartSessionCreationBundle bundle = factory.create(command);

        assertThat(bundle.bucket()).isNull();
        assertThat(bundle.uploadId()).isNull();
    }

    @Test
    @DisplayName("create - 만료 정보를 올바르게 생성한다")
    void createReturnsCorrectExpiration() {
        given(idGeneratorPort.generate()).willReturn("multipart-session-001");

        CreateMultipartUploadSessionCommand command =
                new CreateMultipartUploadSessionCommand(
                        "large-file.jpg",
                        "image/jpeg",
                        AccessType.PUBLIC,
                        5_242_880L,
                        "product-image",
                        "commerce-service");

        MultipartSessionCreationBundle bundle = factory.create(command);

        assertThat(bundle.expiration().sessionId()).isEqualTo("multipart-session-001");
        assertThat(bundle.expiration().sessionType()).isEqualTo("MULTIPART");
        assertThat(bundle.expiration().ttl()).isEqualTo(Duration.ofHours(24));
    }
}
