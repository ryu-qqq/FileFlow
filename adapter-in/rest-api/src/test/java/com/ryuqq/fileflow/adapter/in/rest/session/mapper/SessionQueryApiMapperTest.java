package com.ryuqq.fileflow.adapter.in.rest.session.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.session.SessionApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.MultipartUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.PresignedPartUrlApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.SingleUploadSessionApiResponse;
import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.PresignedPartUrlResponse;
import com.ryuqq.fileflow.application.session.dto.response.SingleUploadSessionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * SessionQueryApiMapper 단위 테스트.
 *
 * <p>Application Response -> API Response 변환 로직을 검증합니다.
 */
@Tag("unit")
@DisplayName("SessionQueryApiMapper 단위 테스트")
class SessionQueryApiMapperTest {

    private SessionQueryApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SessionQueryApiMapper();
    }

    @Nested
    @DisplayName("toResponse(SingleUploadSessionResponse)")
    class ToSingleUploadSessionApiResponseTest {

        @Test
        @DisplayName("SingleUploadSessionResponse를 SingleUploadSessionApiResponse로 변환한다")
        void toResponse_singleUploadSession_success() {
            // given
            SingleUploadSessionResponse response = SessionApiFixtures.singleUploadSessionResponse();

            // when
            SingleUploadSessionApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo(response.sessionId());
            assertThat(apiResponse.presignedUrl()).isEqualTo(response.presignedUrl());
            assertThat(apiResponse.s3Key()).isEqualTo(response.s3Key());
            assertThat(apiResponse.bucket()).isEqualTo(response.bucket());
            assertThat(apiResponse.accessType()).isEqualTo(response.accessType().name());
            assertThat(apiResponse.fileName()).isEqualTo(response.fileName());
            assertThat(apiResponse.contentType()).isEqualTo(response.contentType());
            assertThat(apiResponse.status()).isEqualTo(response.status());
            assertThat(apiResponse.expiresAt()).isNotBlank();
            assertThat(apiResponse.createdAt()).isNotBlank();
        }

        @Test
        @DisplayName("Instant 타입의 날짜가 ISO 8601 형식의 문자열로 변환된다")
        void toResponse_singleUploadSession_dateFormat() {
            // given
            SingleUploadSessionResponse response = SessionApiFixtures.singleUploadSessionResponse();

            // when
            SingleUploadSessionApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.expiresAt()).contains("T");
            assertThat(apiResponse.expiresAt()).contains("+");
            assertThat(apiResponse.createdAt()).contains("T");
            assertThat(apiResponse.createdAt()).contains("+");
        }
    }

    @Nested
    @DisplayName("toResponse(MultipartUploadSessionResponse)")
    class ToMultipartUploadSessionApiResponseTest {

        @Test
        @DisplayName("MultipartUploadSessionResponse를 MultipartUploadSessionApiResponse로 변환한다")
        void toResponse_multipartUploadSession_success() {
            // given
            MultipartUploadSessionResponse response =
                    SessionApiFixtures.multipartUploadSessionResponse();

            // when
            MultipartUploadSessionApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo(response.sessionId());
            assertThat(apiResponse.uploadId()).isEqualTo(response.uploadId());
            assertThat(apiResponse.s3Key()).isEqualTo(response.s3Key());
            assertThat(apiResponse.bucket()).isEqualTo(response.bucket());
            assertThat(apiResponse.accessType()).isEqualTo(response.accessType().name());
            assertThat(apiResponse.fileName()).isEqualTo(response.fileName());
            assertThat(apiResponse.contentType()).isEqualTo(response.contentType());
            assertThat(apiResponse.partSize()).isEqualTo(response.partSize());
            assertThat(apiResponse.status()).isEqualTo(response.status());
            assertThat(apiResponse.completedPartCount()).isEqualTo(response.completedPartCount());
            assertThat(apiResponse.expiresAt()).isNotBlank();
            assertThat(apiResponse.createdAt()).isNotBlank();
        }

        @Test
        @DisplayName("완료된 파트 목록이 올바르게 변환된다")
        void toResponse_multipartUploadSession_completedParts() {
            // given
            MultipartUploadSessionResponse response =
                    SessionApiFixtures.multipartUploadSessionResponse();

            // when
            MultipartUploadSessionApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.completedParts()).hasSize(2);
            assertThat(apiResponse.completedParts().get(0).partNumber()).isEqualTo(1);
            assertThat(apiResponse.completedParts().get(0).etag())
                    .isEqualTo(SessionApiFixtures.ETAG);
            assertThat(apiResponse.completedParts().get(0).size())
                    .isEqualTo(SessionApiFixtures.PART_SIZE);
            assertThat(apiResponse.completedParts().get(1).partNumber()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("toResponse(PresignedPartUrlResponse)")
    class ToPresignedPartUrlApiResponseTest {

        @Test
        @DisplayName("PresignedPartUrlResponse를 PresignedPartUrlApiResponse로 변환한다")
        void toResponse_presignedPartUrl_success() {
            // given
            PresignedPartUrlResponse response = SessionApiFixtures.presignedPartUrlResponse();

            // when
            PresignedPartUrlApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.presignedUrl()).isEqualTo(response.presignedUrl());
            assertThat(apiResponse.partNumber()).isEqualTo(response.partNumber());
            assertThat(apiResponse.expiresInSeconds()).isEqualTo(response.expiresInSeconds());
        }
    }
}
