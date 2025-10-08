package com.ryuqq.fileflow.domain.upload.event;

import com.ryuqq.fileflow.domain.upload.vo.CheckSum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ChecksumVerified Domain Event 테스트")
class ChecksumVerifiedTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 파라미터로 ChecksumVerified 이벤트를 생성할 수 있다")
        void createChecksumVerified() {
            // given
            String sessionId = "session123";
            CheckSum expectedChecksum = CheckSum.sha256("a".repeat(64));
            CheckSum actualChecksum = CheckSum.sha256("a".repeat(64));
            boolean matched = true;
            LocalDateTime verifiedAt = LocalDateTime.now();

            // when
            ChecksumVerified event = ChecksumVerified.of(
                    sessionId, expectedChecksum, actualChecksum, matched, verifiedAt
            );

            // then
            assertThat(event.sessionId()).isEqualTo(sessionId);
            assertThat(event.expectedChecksum()).isEqualTo(expectedChecksum);
            assertThat(event.actualChecksum()).isEqualTo(actualChecksum);
            assertThat(event.matched()).isTrue();
            assertThat(event.verifiedAt()).isEqualTo(verifiedAt);
        }

        @Test
        @DisplayName("success() 팩토리 메서드로 검증 성공 이벤트를 생성할 수 있다")
        void createSuccessEvent() {
            // given
            String sessionId = "session123";
            CheckSum checksum = CheckSum.sha256("a".repeat(64));
            LocalDateTime verifiedAt = LocalDateTime.now();

            // when
            ChecksumVerified event = ChecksumVerified.success(sessionId, checksum, verifiedAt);

            // then
            assertThat(event.sessionId()).isEqualTo(sessionId);
            assertThat(event.expectedChecksum()).isEqualTo(checksum);
            assertThat(event.actualChecksum()).isEqualTo(checksum);
            assertThat(event.matched()).isTrue();
            assertThat(event.isSuccess()).isTrue();
            assertThat(event.isFailure()).isFalse();
        }

        @Test
        @DisplayName("failure() 팩토리 메서드로 검증 실패 이벤트를 생성할 수 있다")
        void createFailureEvent() {
            // given
            String sessionId = "session123";
            CheckSum expectedChecksum = CheckSum.sha256("a".repeat(64));
            CheckSum actualChecksum = CheckSum.sha256("b".repeat(64));
            LocalDateTime verifiedAt = LocalDateTime.now();

            // when
            ChecksumVerified event = ChecksumVerified.failure(
                    sessionId, expectedChecksum, actualChecksum, verifiedAt
            );

            // then
            assertThat(event.sessionId()).isEqualTo(sessionId);
            assertThat(event.expectedChecksum()).isEqualTo(expectedChecksum);
            assertThat(event.actualChecksum()).isEqualTo(actualChecksum);
            assertThat(event.matched()).isFalse();
            assertThat(event.isSuccess()).isFalse();
            assertThat(event.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("검증 실패 테스트")
    class ValidationTest {

        @Test
        @DisplayName("sessionId가 null이면 예외가 발생한다")
        void createWithNullSessionId() {
            CheckSum checksum = CheckSum.sha256("a".repeat(64));
            LocalDateTime now = LocalDateTime.now();

            assertThatThrownBy(() ->
                    ChecksumVerified.of(null, checksum, checksum, true, now)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("SessionId cannot be null or empty");
        }

        @Test
        @DisplayName("expectedChecksum이 null이면 예외가 발생한다")
        void createWithNullExpectedChecksum() {
            CheckSum checksum = CheckSum.sha256("a".repeat(64));
            LocalDateTime now = LocalDateTime.now();

            assertThatThrownBy(() ->
                    ChecksumVerified.of("session123", null, checksum, true, now)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("ExpectedChecksum cannot be null");
        }

        @Test
        @DisplayName("actualChecksum이 null이면 예외가 발생한다")
        void createWithNullActualChecksum() {
            CheckSum checksum = CheckSum.sha256("a".repeat(64));
            LocalDateTime now = LocalDateTime.now();

            assertThatThrownBy(() ->
                    ChecksumVerified.of("session123", checksum, null, true, now)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("ActualChecksum cannot be null");
        }

        @Test
        @DisplayName("verifiedAt이 null이면 예외가 발생한다")
        void createWithNullVerifiedAt() {
            CheckSum checksum = CheckSum.sha256("a".repeat(64));

            assertThatThrownBy(() ->
                    ChecksumVerified.of("session123", checksum, checksum, true, null)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("VerifiedAt cannot be null");
        }

        @Test
        @DisplayName("verifiedAt이 미래 시간이면 예외가 발생한다")
        void createWithFutureVerifiedAt() {
            CheckSum checksum = CheckSum.sha256("a".repeat(64));
            LocalDateTime future = LocalDateTime.now().plusHours(1);

            assertThatThrownBy(() ->
                    ChecksumVerified.of("session123", checksum, checksum, true, future)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("VerifiedAt cannot be in the future");
        }

        @Test
        @DisplayName("matched 플래그가 실제 체크섬 비교 결과와 다르면 예외가 발생한다")
        void createWithInconsistentMatchedFlag() {
            // given
            CheckSum checksum1 = CheckSum.sha256("a".repeat(64));
            CheckSum checksum2 = CheckSum.sha256("b".repeat(64));
            LocalDateTime now = LocalDateTime.now();

            // when & then - 일치하지 않는데 true로 설정
            assertThatThrownBy(() ->
                    ChecksumVerified.of("session123", checksum1, checksum2, true, now)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Matched flag does not reflect actual checksum comparison");

            // when & then - 일치하는데 false로 설정
            assertThatThrownBy(() ->
                    ChecksumVerified.of("session123", checksum1, checksum1, false, now)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Matched flag does not reflect actual checksum comparison");
        }
    }

    @Nested
    @DisplayName("검증 결과 판단 테스트")
    class ResultTest {

        @Test
        @DisplayName("체크섬이 일치하면 isSuccess()가 true를 반환한다")
        void isSuccess_whenMatched() {
            // given
            CheckSum checksum = CheckSum.sha256("a".repeat(64));
            ChecksumVerified event = ChecksumVerified.success(
                    "session123", checksum, LocalDateTime.now()
            );

            // when & then
            assertThat(event.isSuccess()).isTrue();
            assertThat(event.isFailure()).isFalse();
        }

        @Test
        @DisplayName("체크섬이 일치하지 않으면 isFailure()가 true를 반환한다")
        void isFailure_whenNotMatched() {
            // given
            CheckSum expected = CheckSum.sha256("a".repeat(64));
            CheckSum actual = CheckSum.sha256("b".repeat(64));
            ChecksumVerified event = ChecksumVerified.failure(
                    "session123", expected, actual, LocalDateTime.now()
            );

            // when & then
            assertThat(event.isSuccess()).isFalse();
            assertThat(event.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("대소문자 구분 없는 체크섬 비교 테스트")
    class CaseInsensitiveTest {

        @Test
        @DisplayName("대소문자가 다른 체크섬도 일치로 판단된다")
        void matchedWithDifferentCase() {
            // given
            CheckSum lowerCase = CheckSum.sha256("a".repeat(64));
            CheckSum upperCase = CheckSum.sha256("A".repeat(64));
            LocalDateTime now = LocalDateTime.now();

            // when
            ChecksumVerified event = ChecksumVerified.of(
                    "session123", lowerCase, upperCase, true, now
            );

            // then
            assertThat(event.matched()).isTrue();
            assertThat(event.isSuccess()).isTrue();
        }
    }
}
