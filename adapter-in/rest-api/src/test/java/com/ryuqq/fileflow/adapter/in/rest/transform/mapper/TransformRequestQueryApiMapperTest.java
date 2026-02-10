package com.ryuqq.fileflow.adapter.in.rest.transform.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.transform.TransformRequestApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.transform.dto.response.TransformRequestApiResponse;
import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * TransformRequestQueryApiMapper 단위 테스트.
 *
 * <p>Application Response -> API Response 변환 로직을 검증합니다.
 */
@Tag("unit")
@DisplayName("TransformRequestQueryApiMapper 단위 테스트")
class TransformRequestQueryApiMapperTest {

    private TransformRequestQueryApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransformRequestQueryApiMapper();
    }

    @Nested
    @DisplayName("toResponse(TransformRequestResponse)")
    class ToTransformRequestApiResponseTest {

        @Test
        @DisplayName("TransformRequestResponse를 TransformRequestApiResponse로 변환한다")
        void toResponse_transformRequest_success() {
            // given
            TransformRequestResponse response =
                    TransformRequestApiFixtures.transformRequestResponse();

            // when
            TransformRequestApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.transformRequestId()).isEqualTo(response.transformRequestId());
            assertThat(apiResponse.sourceAssetId()).isEqualTo(response.sourceAssetId());
            assertThat(apiResponse.sourceContentType()).isEqualTo(response.sourceContentType());
            assertThat(apiResponse.transformType()).isEqualTo(response.transformType());
            assertThat(apiResponse.width()).isEqualTo(response.width());
            assertThat(apiResponse.height()).isEqualTo(response.height());
            assertThat(apiResponse.quality()).isEqualTo(response.quality());
            assertThat(apiResponse.targetFormat()).isEqualTo(response.targetFormat());
            assertThat(apiResponse.status()).isEqualTo(response.status());
            assertThat(apiResponse.resultAssetId()).isEqualTo(response.resultAssetId());
            assertThat(apiResponse.lastError()).isEqualTo(response.lastError());
            assertThat(apiResponse.createdAt()).isNotBlank();
        }

        @Test
        @DisplayName("Instant 타입의 날짜가 ISO 8601 형식의 문자열로 변환된다")
        void toResponse_transformRequest_dateFormat() {
            // given
            TransformRequestResponse response =
                    TransformRequestApiFixtures.transformRequestResponse();

            // when
            TransformRequestApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.createdAt()).contains("T");
            assertThat(apiResponse.createdAt()).contains("+");
        }

        @Test
        @DisplayName("null인 날짜 필드는 null로 변환된다")
        void toResponse_transformRequest_nullDates() {
            // given
            TransformRequestResponse response =
                    TransformRequestApiFixtures.transformRequestResponse();

            // when
            TransformRequestApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.completedAt()).isNull();
        }
    }
}
