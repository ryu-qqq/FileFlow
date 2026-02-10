package com.ryuqq.fileflow.application.session.dto.bundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.id.MultipartUploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.SessionExpiration;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MultipartSessionCreationBundleTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant EXPIRES_AT = NOW.plus(Duration.ofHours(24));

    private MultipartSessionCreationBundle createBundle() {
        return MultipartSessionCreationBundle.of(
                MultipartUploadSessionId.of("multipart-session-001"),
                "public/2026/01/multipart-session-001.jpg",
                AccessType.PUBLIC,
                "large-file.jpg",
                "image/jpeg",
                5_242_880L,
                "product-image",
                "commerce-service",
                EXPIRES_AT,
                NOW,
                SessionExpiration.of("multipart-session-001", "MULTIPART", Duration.ofHours(24)));
    }

    @Nested
    @DisplayName("of 팩토리 메서드")
    class OfMethod {

        @Test
        @DisplayName("bucket과 uploadId가 null인 번들을 생성한다")
        void createsBundleWithNullBucketAndUploadId() {
            MultipartSessionCreationBundle bundle = createBundle();

            assertThat(bundle.bucket()).isNull();
            assertThat(bundle.uploadId()).isNull();
            assertThat(bundle.sessionId().value()).isEqualTo("multipart-session-001");
            assertThat(bundle.partSize()).isEqualTo(5_242_880L);
        }
    }

    @Nested
    @DisplayName("withBucket")
    class WithBucket {

        @Test
        @DisplayName("bucket을 설정한 새 번들을 반환한다")
        void returnsNewBundleWithBucket() {
            MultipartSessionCreationBundle bundle = createBundle();

            MultipartSessionCreationBundle enriched = bundle.withBucket("fileflow-bucket");

            assertThat(enriched.bucket()).isEqualTo("fileflow-bucket");
            assertThat(enriched.uploadId()).isNull();
            assertThat(enriched.sessionId()).isEqualTo(bundle.sessionId());
        }

        @Test
        @DisplayName("원본 번들은 변경되지 않는다")
        void doesNotMutateOriginal() {
            MultipartSessionCreationBundle bundle = createBundle();

            bundle.withBucket("fileflow-bucket");

            assertThat(bundle.bucket()).isNull();
        }
    }

    @Nested
    @DisplayName("withUploadId")
    class WithUploadId {

        @Test
        @DisplayName("uploadId를 설정한 새 번들을 반환한다")
        void returnsNewBundleWithUploadId() {
            MultipartSessionCreationBundle bundle = createBundle();

            MultipartSessionCreationBundle enriched = bundle.withUploadId("upload-id-001");

            assertThat(enriched.uploadId()).isEqualTo("upload-id-001");
            assertThat(enriched.bucket()).isNull();
        }
    }

    @Nested
    @DisplayName("toSession")
    class ToSession {

        @Test
        @DisplayName("bucket과 uploadId가 설정된 경우 세션을 생성한다")
        void createsSessionWhenFullyEnriched() {
            MultipartSessionCreationBundle bundle =
                    createBundle().withBucket("fileflow-bucket").withUploadId("upload-id-001");

            MultipartUploadSession session = bundle.toSession();

            assertThat(session.idValue()).isEqualTo("multipart-session-001");
            assertThat(session.bucket()).isEqualTo("fileflow-bucket");
            assertThat(session.uploadId()).isEqualTo("upload-id-001");
            assertThat(session.s3Key()).isEqualTo("public/2026/01/multipart-session-001.jpg");
            assertThat(session.partSize()).isEqualTo(5_242_880L);
        }

        @Test
        @DisplayName("bucket이 null이면 NullPointerException을 던진다")
        void throwsWhenBucketIsNull() {
            MultipartSessionCreationBundle bundle = createBundle().withUploadId("upload-id-001");

            assertThatThrownBy(bundle::toSession)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("bucket");
        }

        @Test
        @DisplayName("uploadId가 null이면 NullPointerException을 던진다")
        void throwsWhenUploadIdIsNull() {
            MultipartSessionCreationBundle bundle = createBundle().withBucket("fileflow-bucket");

            assertThatThrownBy(bundle::toSession)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("uploadId");
        }
    }

    @Nested
    @DisplayName("체이닝")
    class Chaining {

        @Test
        @DisplayName("withBucket과 withUploadId를 체이닝하여 세션을 생성할 수 있다")
        void chainsWithMethodsAndCreatesSession() {
            MultipartUploadSession session =
                    createBundle()
                            .withBucket("fileflow-bucket")
                            .withUploadId("upload-id-001")
                            .toSession();

            assertThat(session.bucket()).isEqualTo("fileflow-bucket");
            assertThat(session.uploadId()).isEqualTo("upload-id-001");
        }
    }
}
