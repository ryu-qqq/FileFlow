package com.ryuqq.fileflow.adapter.in.rest.transform.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.fileflow.domain.session.exception.SessionErrorCode;
import com.ryuqq.fileflow.domain.session.exception.SessionException;
import com.ryuqq.fileflow.domain.transform.exception.TransformErrorCode;
import com.ryuqq.fileflow.domain.transform.exception.TransformException;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@Tag("unit")
@DisplayName("TransformRequestErrorMapper")
class TransformRequestErrorMapperTest {

    private final TransformRequestErrorMapper sut = new TransformRequestErrorMapper();

    @Nested
    @DisplayName("supports")
    class SupportsTest {

        @Test
        @DisplayName("TransformException이면 true를 반환한다")
        void shouldReturnTrueForTransformException() {
            var ex = new TransformException(TransformErrorCode.TRANSFORM_NOT_FOUND);
            assertThat(sut.supports(ex)).isTrue();
        }

        @Test
        @DisplayName("다른 도메인 예외이면 false를 반환한다")
        void shouldReturnFalseForOtherDomainException() {
            var ex = new SessionException(SessionErrorCode.SESSION_NOT_FOUND);
            assertThat(sut.supports(ex)).isFalse();
        }
    }

    @Nested
    @DisplayName("map")
    class MapTest {

        @Test
        @DisplayName("404 에러 코드를 올바르게 매핑한다")
        void shouldMapNotFoundError() {
            var ex = new TransformException(TransformErrorCode.TRANSFORM_NOT_FOUND);

            ErrorMapper.MappedError result = sut.map(ex, Locale.KOREA);

            assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.title()).isEqualTo("TRANSFORM-001");
            assertThat(result.detail()).isEqualTo("변환 요청을 찾을 수 없습니다");
            assertThat(result.type().toString()).isEqualTo("/errors/transform");
        }

        @Test
        @DisplayName("400 에러 코드를 올바르게 매핑한다")
        void shouldMapBadRequestError() {
            var ex = new TransformException(TransformErrorCode.NOT_IMAGE_FILE);

            ErrorMapper.MappedError result = sut.map(ex, Locale.KOREA);

            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.title()).isEqualTo("TRANSFORM-002");
        }
    }
}
