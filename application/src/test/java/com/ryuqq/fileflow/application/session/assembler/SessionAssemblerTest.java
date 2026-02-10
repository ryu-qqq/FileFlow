package com.ryuqq.fileflow.application.session.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.SingleUploadSessionResponse;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSessionFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SessionAssembler 단위 테스트")
class SessionAssemblerTest {

    private SessionAssembler sut;

    @BeforeEach
    void setUp() {
        sut = new SessionAssembler();
    }

    @Nested
    @DisplayName("toResponse(SingleUploadSession) 메서드")
    class ToSingleResponseTest {

        @Test
        @DisplayName("SingleUploadSession을 SingleUploadSessionResponse로 변환한다")
        void toResponse_CreatedSession_ReturnsCorrectResponse() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.aCreatedSession();

            // when
            SingleUploadSessionResponse result = sut.toResponse(session);

            // then
            assertThat(result.sessionId()).isEqualTo(session.idValue());
            assertThat(result.presignedUrl()).isEqualTo(session.presignedUrlValue());
            assertThat(result.s3Key()).isEqualTo(session.s3Key());
            assertThat(result.bucket()).isEqualTo(session.bucket());
            assertThat(result.accessType()).isEqualTo(session.accessType());
            assertThat(result.fileName()).isEqualTo(session.fileName());
            assertThat(result.contentType()).isEqualTo(session.contentType());
            assertThat(result.status()).isEqualTo(session.status().name());
            assertThat(result.expiresAt()).isEqualTo(session.expiresAt());
            assertThat(result.createdAt()).isEqualTo(session.createdAt());
        }

        @Test
        @DisplayName("완료된 SingleUploadSession의 상태를 COMPLETED로 변환한다")
        void toResponse_CompletedSession_ReturnsCompletedStatus() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.aCompletedSession();

            // when
            SingleUploadSessionResponse result = sut.toResponse(session);

            // then
            assertThat(result.status()).isEqualTo("COMPLETED");
        }
    }

    @Nested
    @DisplayName("toResponse(MultipartUploadSession) 메서드")
    class ToMultipartResponseTest {

        @Test
        @DisplayName("MultipartUploadSession을 MultipartUploadSessionResponse로 변환한다")
        void toResponse_InitiatedSession_ReturnsCorrectResponse() {
            // given
            MultipartUploadSession session = MultipartUploadSessionFixture.anInitiatedSession();

            // when
            MultipartUploadSessionResponse result = sut.toResponse(session);

            // then
            assertThat(result.sessionId()).isEqualTo(session.idValue());
            assertThat(result.uploadId()).isEqualTo(session.uploadId());
            assertThat(result.s3Key()).isEqualTo(session.s3Key());
            assertThat(result.bucket()).isEqualTo(session.bucket());
            assertThat(result.accessType()).isEqualTo(session.accessType());
            assertThat(result.fileName()).isEqualTo(session.fileName());
            assertThat(result.contentType()).isEqualTo(session.contentType());
            assertThat(result.partSize()).isEqualTo(session.partSize());
            assertThat(result.status()).isEqualTo(session.status().name());
            assertThat(result.completedPartCount()).isEqualTo(session.completedPartCount());
            assertThat(result.completedParts()).isEmpty();
            assertThat(result.expiresAt()).isEqualTo(session.expiresAt());
            assertThat(result.createdAt()).isEqualTo(session.createdAt());
        }

        @Test
        @DisplayName("파트가 추가된 세션의 CompletedPart를 올바르게 변환한다")
        void toResponse_UploadingSession_ReturnsCompletedParts() {
            // given
            MultipartUploadSession session = MultipartUploadSessionFixture.anUploadingSession();

            // when
            MultipartUploadSessionResponse result = sut.toResponse(session);

            // then
            assertThat(result.completedPartCount()).isEqualTo(1);
            assertThat(result.completedParts()).hasSize(1);

            MultipartUploadSessionResponse.CompletedPartResponse partResponse =
                    result.completedParts().get(0);
            assertThat(partResponse.partNumber()).isEqualTo(1);
            assertThat(partResponse.etag()).isEqualTo("etag-part-1");
            assertThat(partResponse.size()).isEqualTo(5_242_880L);
        }

        @Test
        @DisplayName("INITIATED 상태의 빈 파트 목록을 올바르게 변환한다")
        void toResponse_InitiatedSession_ReturnsEmptyParts() {
            // given
            MultipartUploadSession session = MultipartUploadSessionFixture.anInitiatedSession();

            // when
            MultipartUploadSessionResponse result = sut.toResponse(session);

            // then
            assertThat(result.status()).isEqualTo("INITIATED");
            assertThat(result.completedPartCount()).isZero();
            assertThat(result.completedParts()).isEmpty();
        }
    }
}
