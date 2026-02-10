package com.ryuqq.fileflow.application.session.dto.bundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.id.SingleUploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.SessionExpiration;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SingleSessionCreationBundleTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant EXPIRES_AT = NOW.plus(Duration.ofHours(1));

    private SingleSessionCreationBundle createBundle() {
        return SingleSessionCreationBundle.of(
                SingleUploadSessionId.of("session-001"),
                "public/2026/01/session-001.jpg",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg",
                "product-image",
                "commerce-service",
                EXPIRES_AT,
                NOW,
                SessionExpiration.of("session-001", "SINGLE", Duration.ofHours(1)));
    }

    @Nested
    @DisplayName("of 팩토리 메서드")
    class OfMethod {

        @Test
        @DisplayName("bucket과 presignedUrl이 null인 번들을 생성한다")
        void createsBundleWithNullBucketAndPresignedUrl() {
            SingleSessionCreationBundle bundle = createBundle();

            assertThat(bundle.bucket()).isNull();
            assertThat(bundle.presignedUrl()).isNull();
            assertThat(bundle.sessionId().value()).isEqualTo("session-001");
            assertThat(bundle.s3Key()).isEqualTo("public/2026/01/session-001.jpg");
        }
    }

    @Nested
    @DisplayName("withBucket")
    class WithBucket {

        @Test
        @DisplayName("bucket을 설정한 새 번들을 반환한다")
        void returnsNewBundleWithBucket() {
            SingleSessionCreationBundle bundle = createBundle();

            SingleSessionCreationBundle enriched = bundle.withBucket("fileflow-bucket");

            assertThat(enriched.bucket()).isEqualTo("fileflow-bucket");
            assertThat(enriched.presignedUrl()).isNull();
            assertThat(enriched.sessionId()).isEqualTo(bundle.sessionId());
            assertThat(enriched.s3Key()).isEqualTo(bundle.s3Key());
        }

        @Test
        @DisplayName("원본 번들은 변경되지 않는다")
        void doesNotMutateOriginal() {
            SingleSessionCreationBundle bundle = createBundle();

            bundle.withBucket("fileflow-bucket");

            assertThat(bundle.bucket()).isNull();
        }
    }

    @Nested
    @DisplayName("withPresignedUrl")
    class WithPresignedUrl {

        @Test
        @DisplayName("presignedUrl을 설정한 새 번들을 반환한다")
        void returnsNewBundleWithPresignedUrl() {
            SingleSessionCreationBundle bundle = createBundle();

            SingleSessionCreationBundle enriched =
                    bundle.withPresignedUrl("https://s3.presigned-url.com/test");

            assertThat(enriched.presignedUrl()).isEqualTo("https://s3.presigned-url.com/test");
            assertThat(enriched.bucket()).isNull();
        }
    }

    @Nested
    @DisplayName("toSession")
    class ToSession {

        @Test
        @DisplayName("bucket과 presignedUrl이 설정된 경우 세션을 생성한다")
        void createsSessionWhenFullyEnriched() {
            SingleSessionCreationBundle bundle =
                    createBundle()
                            .withBucket("fileflow-bucket")
                            .withPresignedUrl("https://s3.presigned-url.com/test");

            SingleUploadSession session = bundle.toSession();

            assertThat(session.idValue()).isEqualTo("session-001");
            assertThat(session.bucket()).isEqualTo("fileflow-bucket");
            assertThat(session.presignedUrlValue()).isEqualTo("https://s3.presigned-url.com/test");
            assertThat(session.s3Key()).isEqualTo("public/2026/01/session-001.jpg");
            assertThat(session.accessType()).isEqualTo(AccessType.PUBLIC);
            assertThat(session.fileName()).isEqualTo("product-image.jpg");
            assertThat(session.contentType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("bucket이 null이면 NullPointerException을 던진다")
        void throwsWhenBucketIsNull() {
            SingleSessionCreationBundle bundle =
                    createBundle().withPresignedUrl("https://s3.presigned-url.com/test");

            assertThatThrownBy(bundle::toSession)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("bucket");
        }

        @Test
        @DisplayName("presignedUrl이 null이면 NullPointerException을 던진다")
        void throwsWhenPresignedUrlIsNull() {
            SingleSessionCreationBundle bundle = createBundle().withBucket("fileflow-bucket");

            assertThatThrownBy(bundle::toSession)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("presignedUrl");
        }
    }

    @Nested
    @DisplayName("체이닝")
    class Chaining {

        @Test
        @DisplayName("withBucket과 withPresignedUrl을 체이닝하여 세션을 생성할 수 있다")
        void chainsWithMethodsAndCreatesSession() {
            SingleUploadSession session =
                    createBundle()
                            .withBucket("fileflow-bucket")
                            .withPresignedUrl("https://s3.presigned-url.com/test")
                            .toSession();

            assertThat(session.bucket()).isEqualTo("fileflow-bucket");
            assertThat(session.presignedUrlValue()).isEqualTo("https://s3.presigned-url.com/test");
        }
    }
}
