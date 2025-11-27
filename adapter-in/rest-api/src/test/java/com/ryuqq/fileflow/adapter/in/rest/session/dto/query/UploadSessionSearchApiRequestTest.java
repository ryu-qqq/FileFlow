package com.ryuqq.fileflow.adapter.in.rest.session.dto.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.session.dto.query.UploadSessionSearchApiRequest.UploadTypeFilter;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UploadSessionSearchApiRequest 단위 테스트")
class UploadSessionSearchApiRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드를 지정하여 Request를 생성할 수 있다")
        void constructor_WithAllFields_ShouldCreateRequest() {
            // given & when
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(
                            SessionStatus.COMPLETED, UploadTypeFilter.SINGLE, 2, 50);

            // then
            assertThat(request.status()).isEqualTo(SessionStatus.COMPLETED);
            assertThat(request.uploadType()).isEqualTo(UploadTypeFilter.SINGLE);
            assertThat(request.page()).isEqualTo(2);
            assertThat(request.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("page와 size가 null이면 기본값을 설정한다")
        void constructor_WithNullPageAndSize_ShouldSetDefaults() {
            // given & when
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(
                            SessionStatus.COMPLETED, UploadTypeFilter.MULTIPART, null, null);

            // then
            assertThat(request.page()).isEqualTo(0);
            assertThat(request.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("status와 uploadType이 null인 경우에도 생성할 수 있다")
        void constructor_WithNullStatusAndUploadType_ShouldCreateRequest() {
            // given & when
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, 0, 10);

            // then
            assertThat(request.status()).isNull();
            assertThat(request.uploadType()).isNull();
            assertThat(request.page()).isEqualTo(0);
            assertThat(request.size()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Validation 테스트")
    class ValidationTest {

        @Test
        @DisplayName("page가 음수이면 validation 실패한다")
        void validate_NegativePage_ShouldFail() {
            // given
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, -1, 20);

            // when
            Set<ConstraintViolation<UploadSessionSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("0 이상");
        }

        @Test
        @DisplayName("size가 0이면 validation 실패한다")
        void validate_ZeroSize_ShouldFail() {
            // given
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, 0, 0);

            // when
            Set<ConstraintViolation<UploadSessionSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("1 이상");
        }

        @Test
        @DisplayName("size가 100을 초과하면 validation 실패한다")
        void validate_SizeExceeds100_ShouldFail() {
            // given
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, 0, 101);

            // when
            Set<ConstraintViolation<UploadSessionSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("100 이하");
        }

        @Test
        @DisplayName("유효한 값이면 validation 성공한다")
        void validate_ValidRequest_ShouldPass() {
            // given
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(
                            SessionStatus.ACTIVE, UploadTypeFilter.SINGLE, 0, 100);

            // when
            Set<ConstraintViolation<UploadSessionSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("UploadTypeFilter 테스트")
    class UploadTypeFilterTest {

        @Test
        @DisplayName("SINGLE 필터를 사용할 수 있다")
        void single_ShouldWork() {
            // given & when
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, UploadTypeFilter.SINGLE, null, null);

            // then
            assertThat(request.uploadType()).isEqualTo(UploadTypeFilter.SINGLE);
            assertThat(request.uploadType().name()).isEqualTo("SINGLE");
        }

        @Test
        @DisplayName("MULTIPART 필터를 사용할 수 있다")
        void multipart_ShouldWork() {
            // given & when
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, UploadTypeFilter.MULTIPART, null, null);

            // then
            assertThat(request.uploadType()).isEqualTo(UploadTypeFilter.MULTIPART);
            assertThat(request.uploadType().name()).isEqualTo("MULTIPART");
        }

        @Test
        @DisplayName("UploadTypeFilter에는 2개의 값이 있다")
        void values_ShouldHaveTwoElements() {
            // given & when
            UploadTypeFilter[] values = UploadTypeFilter.values();

            // then
            assertThat(values).hasSize(2);
            assertThat(values).containsExactly(UploadTypeFilter.SINGLE, UploadTypeFilter.MULTIPART);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("동일한 값으로 생성된 두 Request는 동등해야 한다")
        void equals_SameValues_ShouldBeEqual() {
            // given
            UploadSessionSearchApiRequest request1 =
                    new UploadSessionSearchApiRequest(
                            SessionStatus.COMPLETED, UploadTypeFilter.SINGLE, 1, 20);
            UploadSessionSearchApiRequest request2 =
                    new UploadSessionSearchApiRequest(
                            SessionStatus.COMPLETED, UploadTypeFilter.SINGLE, 1, 20);

            // then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }
    }
}
