package com.ryuqq.fileflow.adapter.in.rest.download.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.download.DownloadTaskApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.response.DownloadTaskApiResponse;
import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * DownloadTaskQueryApiMapper 단위 테스트.
 *
 * <p>Application Response -> API Response 변환 로직을 검증합니다.
 */
@Tag("unit")
@DisplayName("DownloadTaskQueryApiMapper 단위 테스트")
class DownloadTaskQueryApiMapperTest {

    private DownloadTaskQueryApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DownloadTaskQueryApiMapper();
    }

    @Nested
    @DisplayName("toResponse(DownloadTaskResponse)")
    class ToDownloadTaskApiResponseTest {

        @Test
        @DisplayName("DownloadTaskResponse를 DownloadTaskApiResponse로 변환한다")
        void toResponse_downloadTask_success() {
            // given
            DownloadTaskResponse response = DownloadTaskApiFixtures.downloadTaskResponse();

            // when
            DownloadTaskApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.downloadTaskId()).isEqualTo(response.downloadTaskId());
            assertThat(apiResponse.sourceUrl()).isEqualTo(response.sourceUrl());
            assertThat(apiResponse.s3Key()).isEqualTo(response.s3Key());
            assertThat(apiResponse.bucket()).isEqualTo(response.bucket());
            assertThat(apiResponse.accessType()).isEqualTo(response.accessType().name());
            assertThat(apiResponse.purpose()).isEqualTo(response.purpose());
            assertThat(apiResponse.source()).isEqualTo(response.source());
            assertThat(apiResponse.status()).isEqualTo(response.status());
            assertThat(apiResponse.retryCount()).isEqualTo(response.retryCount());
            assertThat(apiResponse.maxRetries()).isEqualTo(response.maxRetries());
            assertThat(apiResponse.callbackUrl()).isEqualTo(response.callbackUrl());
            assertThat(apiResponse.lastError()).isEqualTo(response.lastError());
            assertThat(apiResponse.createdAt()).isNotBlank();
        }

        @Test
        @DisplayName("Instant 타입의 날짜가 ISO 8601 형식의 문자열로 변환된다")
        void toResponse_downloadTask_dateFormat() {
            // given
            DownloadTaskResponse response = DownloadTaskApiFixtures.downloadTaskResponse();

            // when
            DownloadTaskApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.createdAt()).contains("T");
            assertThat(apiResponse.createdAt()).contains("+");
        }

        @Test
        @DisplayName("null인 날짜 필드는 null로 변환된다")
        void toResponse_downloadTask_nullDates() {
            // given
            DownloadTaskResponse response = DownloadTaskApiFixtures.downloadTaskResponse();

            // when
            DownloadTaskApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.startedAt()).isNull();
            assertThat(apiResponse.completedAt()).isNull();
        }
    }
}
