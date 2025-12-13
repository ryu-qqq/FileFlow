package com.ryuqq.fileflow.domain.session.aggregate;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import com.ryuqq.fileflow.domain.session.fixture.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CompletedPart 단위 테스트")
class CompletedPartTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 신규 Part를 생성하면 PENDING 상태여야 한다")
        void forNew_ShouldCreatePartWithPendingStatus() {
            // given & when
            CompletedPart part = CompletedPartFixture.defaultCompletedPart();

            // then
            assertThat(part.getId()).isNull();
            assertThat(part.getSessionId()).isNotNull();
            assertThat(part.getPartNumber()).isNotNull();
            assertThat(part.getPresignedUrl()).isNotNull();
            assertThat(part.isCompleted()).isFalse();
            assertThat(part.getSize()).isZero();
        }

        @Test
        @DisplayName("of()로 영속성 복원 시 모든 필드가 설정된다")
        void of_ShouldRestoreAllFields() {
            // given & when
            CompletedPart part = CompletedPartFixture.existingCompletedPart();

            // then
            assertThat(part.getId()).isNotNull();
            assertThat(part.getSessionId()).isNotNull();
            assertThat(part.getPartNumber()).isNotNull();
            assertThat(part.getPresignedUrl()).isNotNull();
            assertThat(part.getEtag()).isNotNull();
            assertThat(part.getSize()).isPositive();
            assertThat(part.getUploadedAt()).isNotNull();
            assertThat(part.isCompleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("생성자 검증 테스트")
    class ConstructorValidationTest {

        @Test
        @DisplayName("sessionId가 null이면 예외가 발생한다")
        void constructor_WithNullSessionId_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    CompletedPart.forNew(
                                            null,
                                            PartNumberFixture.defaultPartNumber(),
                                            PresignedUrlFixture.defaultPresignedUrl()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("세션 ID는 null일 수 없습니다");
        }

        @Test
        @DisplayName("partNumber가 null이면 예외가 발생한다")
        void constructor_WithNullPartNumber_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    CompletedPart.forNew(
                                            UploadSessionIdFixture.defaultUploadSessionId(),
                                            null,
                                            PresignedUrlFixture.defaultPresignedUrl()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part 번호는 null일 수 없습니다");
        }

        @Test
        @DisplayName("presignedUrl이 null이면 예외가 발생한다")
        void constructor_WithNullPresignedUrl_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    CompletedPart.forNew(
                                            UploadSessionIdFixture.defaultUploadSessionId(),
                                            PartNumberFixture.defaultPartNumber(),
                                            null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Presigned URL은 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("Part 완료 테스트")
    class CompleteTest {

        @Test
        @DisplayName("유효한 ETag와 크기로 Part를 완료할 수 있다")
        void complete_WithValidETagAndSize_ShouldSucceed() {
            // given
            CompletedPart part = CompletedPartFixture.defaultCompletedPart();
            var etag = ETagFixture.defaultETag();
            long size = 10 * 1024 * 1024L; // 10MB

            // when
            part.complete(etag, size, ClockFixture.defaultClock());

            // then
            assertThat(part.isCompleted()).isTrue();
            assertThat(part.getEtag()).isEqualTo(etag);
            assertThat(part.getSize()).isEqualTo(size);
            assertThat(part.getUploadedAt()).isNotNull();
        }

        @Test
        @DisplayName("ETag가 null이면 예외가 발생한다")
        void complete_WithNullETag_ShouldThrowException() {
            // given
            CompletedPart part = CompletedPartFixture.defaultCompletedPart();

            // when & then
            assertThatThrownBy(
                            () ->
                                    part.complete(
                                            null, 10 * 1024 * 1024L, ClockFixture.defaultClock()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ETag는 null이거나 비어있을 수 없습니다");
        }

        @Test
        @DisplayName("ETag가 비어있으면 예외가 발생한다")
        void complete_WithEmptyETag_ShouldThrowException() {
            // given
            CompletedPart part = CompletedPartFixture.defaultCompletedPart();

            // when & then
            assertThatThrownBy(
                            () ->
                                    part.complete(
                                            ETagFixture.emptyETag(),
                                            10 * 1024 * 1024L,
                                            ClockFixture.defaultClock()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ETag는 null이거나 비어있을 수 없습니다");
        }

        @Test
        @DisplayName("크기가 0 이하면 예외가 발생한다")
        void complete_WithInvalidSize_ShouldThrowException() {
            // given
            CompletedPart part = CompletedPartFixture.defaultCompletedPart();

            // when & then
            assertThatThrownBy(
                            () ->
                                    part.complete(
                                            ETagFixture.defaultETag(),
                                            0,
                                            ClockFixture.defaultClock()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part 크기는 0보다 커야 합니다");

            assertThatThrownBy(
                            () ->
                                    part.complete(
                                            ETagFixture.defaultETag(),
                                            -1,
                                            ClockFixture.defaultClock()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part 크기는 0보다 커야 합니다");
        }
    }

    @Nested
    @DisplayName("완료 상태 확인 테스트")
    class CompletionStatusTest {

        @Test
        @DisplayName("신규 생성된 Part는 완료되지 않은 상태다")
        void isCompleted_WithNewPart_ShouldReturnFalse() {
            // given
            CompletedPart part = CompletedPartFixture.defaultCompletedPart();

            // when & then
            assertThat(part.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("complete() 호출 후 Part는 완료 상태다")
        void isCompleted_AfterComplete_ShouldReturnTrue() {
            // given
            CompletedPart part = CompletedPartFixture.defaultCompletedPart();

            // when
            part.complete(
                    ETagFixture.defaultETag(), 10 * 1024 * 1024L, ClockFixture.defaultClock());

            // then
            assertThat(part.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("영속성 복원된 완료 Part는 완료 상태다")
        void isCompleted_WithExistingCompletedPart_ShouldReturnTrue() {
            // given
            CompletedPart part = CompletedPartFixture.existingCompletedPart();

            // when & then
            assertThat(part.isCompleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("Getter 테스트")
    class GetterTest {

        @Test
        @DisplayName("모든 필드를 올바르게 반환한다")
        void getters_ShouldReturnCorrectValues() {
            // given
            CompletedPart part = CompletedPartFixture.completedCompletedPart();

            // when & then
            assertThat(part.getSessionId()).isNotNull();
            assertThat(part.getPartNumber()).isNotNull();
            assertThat(part.getPresignedUrl()).isNotNull();
            assertThat(part.getEtag()).isNotNull();
            assertThat(part.getSize()).isPositive();
            assertThat(part.getUploadedAt()).isNotNull();
        }

        @Test
        @DisplayName("Law of Demeter를 준수하는 편의 메서드가 동작한다")
        void convenienceMethods_ShouldWork() {
            // given
            CompletedPart part = CompletedPartFixture.completedCompletedPart();

            // when & then
            assertThat(part.getSessionIdValue()).isNotBlank();
            assertThat(part.getPartNumberValue()).isPositive();
            assertThat(part.getPresignedUrlValue()).isNotBlank();
            assertThat(part.getETagValue()).isNotBlank();
        }
    }
}
