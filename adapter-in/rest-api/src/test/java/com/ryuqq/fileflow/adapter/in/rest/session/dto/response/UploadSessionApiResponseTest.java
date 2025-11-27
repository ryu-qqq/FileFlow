package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UploadSessionApiResponse 단위 테스트")
class UploadSessionApiResponseTest {

    private static final String SESSION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String FILE_NAME = "document.pdf";
    private static final long FILE_SIZE = 1024 * 1024L;
    private static final String CONTENT_TYPE = "application/pdf";
    private static final String UPLOAD_TYPE = "SINGLE";
    private static final SessionStatus STATUS = SessionStatus.COMPLETED;
    private static final String BUCKET = "test-bucket";
    private static final String KEY = "uploads/document.pdf";

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("of() 팩토리 메서드로 Response를 생성할 수 있다")
        void of_ShouldCreateResponse() {
            // given
            LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(23);

            // when
            UploadSessionApiResponse response =
                    UploadSessionApiResponse.of(
                            SESSION_ID,
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            UPLOAD_TYPE,
                            STATUS,
                            BUCKET,
                            KEY,
                            createdAt,
                            expiresAt);

            // then
            assertThat(response.sessionId()).isEqualTo(SESSION_ID);
            assertThat(response.fileName()).isEqualTo(FILE_NAME);
            assertThat(response.fileSize()).isEqualTo(FILE_SIZE);
            assertThat(response.contentType()).isEqualTo(CONTENT_TYPE);
            assertThat(response.uploadType()).isEqualTo(UPLOAD_TYPE);
            assertThat(response.status()).isEqualTo(STATUS);
            assertThat(response.bucket()).isEqualTo(BUCKET);
            assertThat(response.key()).isEqualTo(KEY);
            assertThat(response.createdAt()).isEqualTo(createdAt);
            assertThat(response.expiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("MULTIPART 업로드 타입으로 Response를 생성할 수 있다")
        void of_MultipartType_ShouldCreateResponse() {
            // given
            LocalDateTime createdAt = LocalDateTime.now();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

            // when
            UploadSessionApiResponse response =
                    UploadSessionApiResponse.of(
                            SESSION_ID,
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            "MULTIPART",
                            SessionStatus.ACTIVE,
                            BUCKET,
                            KEY,
                            createdAt,
                            expiresAt);

            // then
            assertThat(response.uploadType()).isEqualTo("MULTIPART");
            assertThat(response.status()).isEqualTo(SessionStatus.ACTIVE);
        }

        @Test
        @DisplayName("다양한 SessionStatus로 Response를 생성할 수 있다")
        void of_VariousStatuses_ShouldCreateResponse() {
            // given
            LocalDateTime now = LocalDateTime.now();

            for (SessionStatus status : SessionStatus.values()) {
                // when
                UploadSessionApiResponse response =
                        UploadSessionApiResponse.of(
                                SESSION_ID,
                                FILE_NAME,
                                FILE_SIZE,
                                CONTENT_TYPE,
                                UPLOAD_TYPE,
                                status,
                                BUCKET,
                                KEY,
                                now,
                                now.plusHours(24));

                // then
                assertThat(response.status()).isEqualTo(status);
            }
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("동일한 값으로 생성된 두 Response는 동등해야 한다")
        void equals_SameValues_ShouldBeEqual() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2025, 11, 27, 10, 0);
            LocalDateTime expiresAt = LocalDateTime.of(2025, 11, 28, 10, 0);

            UploadSessionApiResponse response1 =
                    UploadSessionApiResponse.of(
                            SESSION_ID,
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            UPLOAD_TYPE,
                            STATUS,
                            BUCKET,
                            KEY,
                            createdAt,
                            expiresAt);
            UploadSessionApiResponse response2 =
                    UploadSessionApiResponse.of(
                            SESSION_ID,
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            UPLOAD_TYPE,
                            STATUS,
                            BUCKET,
                            KEY,
                            createdAt,
                            expiresAt);

            // then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 세션 ID를 가진 두 Response는 동등하지 않아야 한다")
        void equals_DifferentSessionId_ShouldNotBeEqual() {
            // given
            LocalDateTime now = LocalDateTime.now();

            UploadSessionApiResponse response1 =
                    UploadSessionApiResponse.of(
                            SESSION_ID,
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            UPLOAD_TYPE,
                            STATUS,
                            BUCKET,
                            KEY,
                            now,
                            now.plusHours(24));
            UploadSessionApiResponse response2 =
                    UploadSessionApiResponse.of(
                            "different-session-id",
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            UPLOAD_TYPE,
                            STATUS,
                            BUCKET,
                            KEY,
                            now,
                            now.plusHours(24));

            // then
            assertThat(response1).isNotEqualTo(response2);
        }
    }
}
