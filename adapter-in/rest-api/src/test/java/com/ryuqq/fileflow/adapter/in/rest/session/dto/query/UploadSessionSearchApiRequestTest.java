package com.ryuqq.fileflow.adapter.in.rest.session.dto.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.session.dto.query.UploadSessionSearchApiRequest.SessionStatusFilter;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.query.UploadSessionSearchApiRequest.UploadTypeFilter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * UploadSessionSearchApiRequest 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("UploadSessionSearchApiRequest 단위 테스트")
class UploadSessionSearchApiRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 요청을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            SessionStatusFilter status = SessionStatusFilter.PENDING;
            UploadTypeFilter uploadType = UploadTypeFilter.SINGLE;
            Integer page = 1;
            Integer size = 50;

            // when
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(status, uploadType, page, size);

            // then
            assertThat(request.status()).isEqualTo(status);
            assertThat(request.uploadType()).isEqualTo(uploadType);
            assertThat(request.page()).isEqualTo(page);
            assertThat(request.size()).isEqualTo(size);
        }

        @Test
        @DisplayName("필터가 null인 요청을 생성할 수 있다")
        void create_WithNullFilters_ShouldSucceed() {
            // when
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, 0, 20);

            // then
            assertThat(request.status()).isNull();
            assertThat(request.uploadType()).isNull();
        }
    }

    @Nested
    @DisplayName("기본값 테스트")
    class DefaultValuesTest {

        @Test
        @DisplayName("page가 null이면 기본값 0이 설정된다")
        void create_WithNullPage_ShouldSetDefaultValue() {
            // when
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, null, 20);

            // then
            assertThat(request.page()).isZero();
        }

        @Test
        @DisplayName("size가 null이면 기본값 20이 설정된다")
        void create_WithNullSize_ShouldSetDefaultValue() {
            // when
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, 0, null);

            // then
            assertThat(request.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("page와 size가 모두 null이면 기본값이 설정된다")
        void create_WithNullPageAndSize_ShouldSetDefaultValues() {
            // when
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, null, null);

            // then
            assertThat(request.page()).isZero();
            assertThat(request.size()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("유효한 요청은 검증을 통과한다")
        void validate_WithValidRequest_ShouldPass() {
            // given
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(
                            SessionStatusFilter.PENDING, UploadTypeFilter.SINGLE, 0, 20);

            // when
            Set<ConstraintViolation<UploadSessionSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -10, -100})
        @DisplayName("페이지 번호가 음수면 검증에 실패한다")
        void validate_WithNegativePage_ShouldFail(int page) {
            // given
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, page, 20);

            // when
            Set<ConstraintViolation<UploadSessionSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("page"));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("페이지 크기가 1 미만이면 검증에 실패한다")
        void validate_WithSizeLessThanOne_ShouldFail(int size) {
            // given
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, 0, size);

            // when
            Set<ConstraintViolation<UploadSessionSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("size"));
        }

        @ParameterizedTest
        @ValueSource(ints = {101, 200, 1000})
        @DisplayName("페이지 크기가 100을 초과하면 검증에 실패한다")
        void validate_WithSizeGreaterThan100_ShouldFail(int size) {
            // given
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, 0, size);

            // when
            Set<ConstraintViolation<UploadSessionSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("size"));
        }

        @Test
        @DisplayName("페이지 크기가 경계값(1)일 때 검증을 통과한다")
        void validate_WithMinSize_ShouldPass() {
            // given
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, 0, 1);

            // when
            Set<ConstraintViolation<UploadSessionSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("페이지 크기가 경계값(100)일 때 검증을 통과한다")
        void validate_WithMaxSize_ShouldPass() {
            // given
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, 0, 100);

            // when
            Set<ConstraintViolation<UploadSessionSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("SessionStatusFilter 테스트")
    class SessionStatusFilterTest {

        @ParameterizedTest
        @EnumSource(SessionStatusFilter.class)
        @DisplayName("모든 세션 상태 필터로 요청을 생성할 수 있다")
        void create_WithAllStatusFilters_ShouldSucceed(SessionStatusFilter status) {
            // when
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(status, null, 0, 20);

            // then
            assertThat(request.status()).isEqualTo(status);
        }

        @Test
        @DisplayName("SessionStatusFilter는 5개의 상태를 가진다")
        void sessionStatusFilter_ShouldHave5Values() {
            // then
            assertThat(SessionStatusFilter.values()).hasSize(5);
            assertThat(SessionStatusFilter.values())
                    .containsExactly(
                            SessionStatusFilter.PENDING,
                            SessionStatusFilter.IN_PROGRESS,
                            SessionStatusFilter.COMPLETED,
                            SessionStatusFilter.EXPIRED,
                            SessionStatusFilter.CANCELLED);
        }
    }

    @Nested
    @DisplayName("UploadTypeFilter 테스트")
    class UploadTypeFilterTest {

        @ParameterizedTest
        @EnumSource(UploadTypeFilter.class)
        @DisplayName("모든 업로드 타입 필터로 요청을 생성할 수 있다")
        void create_WithAllUploadTypeFilters_ShouldSucceed(UploadTypeFilter uploadType) {
            // when
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, uploadType, 0, 20);

            // then
            assertThat(request.uploadType()).isEqualTo(uploadType);
        }

        @Test
        @DisplayName("UploadTypeFilter는 2개의 타입을 가진다")
        void uploadTypeFilter_ShouldHave2Values() {
            // then
            assertThat(UploadTypeFilter.values()).hasSize(2);
            assertThat(UploadTypeFilter.values())
                    .containsExactly(UploadTypeFilter.SINGLE, UploadTypeFilter.MULTIPART);
        }
    }

    @Nested
    @DisplayName("페이지네이션 테스트")
    class PaginationTest {

        @Test
        @DisplayName("첫 번째 페이지(0)로 요청을 생성할 수 있다")
        void create_WithFirstPage_ShouldSucceed() {
            // when
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, 0, 20);

            // then
            assertThat(request.page()).isZero();
        }

        @Test
        @DisplayName("큰 페이지 번호로 요청을 생성할 수 있다")
        void create_WithLargePage_ShouldSucceed() {
            // when
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, 1000, 20);

            // then
            assertThat(request.page()).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 요청은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            UploadSessionSearchApiRequest request1 =
                    new UploadSessionSearchApiRequest(
                            SessionStatusFilter.PENDING, UploadTypeFilter.SINGLE, 0, 20);
            UploadSessionSearchApiRequest request2 =
                    new UploadSessionSearchApiRequest(
                            SessionStatusFilter.PENDING, UploadTypeFilter.SINGLE, 0, 20);

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 status를 가진 요청은 동등하지 않다")
        void equals_WithDifferentStatus_ShouldNotBeEqual() {
            // given
            UploadSessionSearchApiRequest request1 =
                    new UploadSessionSearchApiRequest(SessionStatusFilter.PENDING, null, 0, 20);
            UploadSessionSearchApiRequest request2 =
                    new UploadSessionSearchApiRequest(SessionStatusFilter.COMPLETED, null, 0, 20);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("다른 uploadType을 가진 요청은 동등하지 않다")
        void equals_WithDifferentUploadType_ShouldNotBeEqual() {
            // given
            UploadSessionSearchApiRequest request1 =
                    new UploadSessionSearchApiRequest(null, UploadTypeFilter.SINGLE, 0, 20);
            UploadSessionSearchApiRequest request2 =
                    new UploadSessionSearchApiRequest(null, UploadTypeFilter.MULTIPART, 0, 20);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("다른 page를 가진 요청은 동등하지 않다")
        void equals_WithDifferentPage_ShouldNotBeEqual() {
            // given
            UploadSessionSearchApiRequest request1 =
                    new UploadSessionSearchApiRequest(null, null, 0, 20);
            UploadSessionSearchApiRequest request2 =
                    new UploadSessionSearchApiRequest(null, null, 1, 20);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("다른 size를 가진 요청은 동등하지 않다")
        void equals_WithDifferentSize_ShouldNotBeEqual() {
            // given
            UploadSessionSearchApiRequest request1 =
                    new UploadSessionSearchApiRequest(null, null, 0, 20);
            UploadSessionSearchApiRequest request2 =
                    new UploadSessionSearchApiRequest(null, null, 0, 50);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
