package com.ryuqq.fileflow.domain.session.aggregate;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import com.ryuqq.fileflow.domain.iam.fixture.UserContextFixture;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;
import com.ryuqq.fileflow.domain.session.exception.ETagMismatchException;
import com.ryuqq.fileflow.domain.session.exception.InvalidSessionStatusException;
import com.ryuqq.fileflow.domain.session.fixture.*;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SingleUploadSession 단위 테스트")
class SingleUploadSessionTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 신규 세션을 생성하면 PREPARING 상태여야 한다")
        void forNew_ShouldCreateSessionWithPreparingStatus() {
            // given & when
            SingleUploadSession session = SingleUploadSessionFixture.defaultSingleUploadSession();

            // then
            assertThat(session.getId()).isNotNull();
            assertThat(session.getStatus()).isEqualTo(SessionStatus.PREPARING);
            assertThat(session.getEtag()).isNull();
            assertThat(session.getCompletedAt()).isNull();
            assertThat(session.getVersion()).isNull();
        }

        @Test
        @DisplayName("of()로 생성 시 ID가 null이면 예외가 발생한다")
        void of_WithNullId_ShouldThrowException() {
            // given & when & then
            Instant now = Instant.now(ClockFixture.defaultClock());
            assertThatThrownBy(
                            () ->
                                    SingleUploadSession.of(
                                            null,
                                            IdempotencyKeyFixture.defaultIdempotencyKey(),
                                            UserContextFixture.defaultAdminUserContext(),
                                            FileNameFixture.defaultFileName(),
                                            FileSizeFixture.defaultFileSize(),
                                            ContentTypeFixture.defaultContentType(),
                                            S3BucketFixture.defaultS3Bucket(),
                                            S3KeyFixture.defaultS3Key(),
                                            ExpirationTimeFixture.defaultExpirationTime(),
                                            now,
                                            SessionStatus.PREPARING,
                                            null,
                                            null,
                                            null,
                                            now, // updatedAt
                                            null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("상태 전이 테스트")
    class StatusTransitionTest {

        @Test
        @DisplayName("PREPARING 상태에서 activate()를 호출하면 ACTIVE 상태로 전환된다")
        void activate_FromPreparing_ShouldTransitionToActive() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.defaultSingleUploadSession();

            // when
            session.activate(
                    PresignedUrlFixture.defaultPresignedUrl(), ClockFixture.defaultClock());

            // then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
            assertThat(session.isActive()).isTrue();
        }

        @Test
        @DisplayName("activate() 시 Presigned URL이 null이면 예외가 발생한다")
        void activate_WithNullPresignedUrl_ShouldThrowException() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.defaultSingleUploadSession();

            // when & then
            assertThatThrownBy(() -> session.activate(null, ClockFixture.defaultClock()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Presigned URL은 null일 수 없습니다");
        }

        @Test
        @DisplayName("COMPLETED 상태에서 activate()를 호출하면 예외가 발생한다")
        void activate_FromCompleted_ShouldThrowException() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.completedSingleUploadSession();

            // when & then
            assertThatThrownBy(
                            () ->
                                    session.activate(
                                            PresignedUrlFixture.defaultPresignedUrl(),
                                            ClockFixture.defaultClock()))
                    .isInstanceOf(InvalidSessionStatusException.class);
        }

        @Test
        @DisplayName("ACTIVE 상태에서 expire()를 호출하면 EXPIRED 상태로 전환된다")
        void expire_FromActive_ShouldTransitionToExpired() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();

            // when
            session.expire(ClockFixture.defaultClock());

            // then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.EXPIRED);
        }

        @Test
        @DisplayName("ACTIVE 상태에서 fail()을 호출하면 FAILED 상태로 전환된다")
        void fail_FromActive_ShouldTransitionToFailed() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();

            // when
            session.fail(ClockFixture.defaultClock());

            // then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("업로드 완료 테스트")
    class CompleteTest {

        @Test
        @DisplayName("ETag가 일치하면 업로드를 완료할 수 있다")
        void complete_WithMatchingETag_ShouldSucceed() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();
            var etag = ETagFixture.defaultETag();

            // when
            session.complete(etag, etag, ClockFixture.defaultClock());

            // then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
            assertThat(session.isCompleted()).isTrue();
            assertThat(session.getCompletedAt()).isNotNull();
            assertThat(session.getEtag()).isEqualTo(etag);
        }

        @Test
        @DisplayName("ETag가 불일치하면 예외가 발생한다")
        void complete_WithMismatchedETag_ShouldThrowException() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();
            var clientETag = ETagFixture.defaultETag();
            var s3ETag = ETagFixture.customETag("different-etag");

            // when & then
            assertThatThrownBy(
                            () -> session.complete(clientETag, s3ETag, ClockFixture.defaultClock()))
                    .isInstanceOf(ETagMismatchException.class);
        }

        @Test
        @DisplayName("클라이언트 ETag가 null이면 예외가 발생한다")
        void complete_WithNullClientETag_ShouldThrowException() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();

            // when & then
            assertThatThrownBy(
                            () ->
                                    session.complete(
                                            null,
                                            ETagFixture.defaultETag(),
                                            ClockFixture.defaultClock()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("클라이언트 ETag는 null일 수 없습니다");
        }

        @Test
        @DisplayName("S3 ETag가 null이면 예외가 발생한다")
        void complete_WithNullS3ETag_ShouldThrowException() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();

            // when & then
            assertThatThrownBy(
                            () ->
                                    session.complete(
                                            ETagFixture.defaultETag(),
                                            null,
                                            ClockFixture.defaultClock()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("S3 ETag는 null일 수 없습니다");
        }

        @Test
        @DisplayName("업로드 완료 시 도메인 이벤트가 발행된다")
        void complete_ShouldPublishDomainEvent() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();
            var etag = ETagFixture.defaultETag();

            // when
            session.complete(etag, etag, ClockFixture.defaultClock());
            List<FileUploadCompletedEvent> events = session.pollDomainEvents();

            // then
            assertThat(events).hasSize(1);
            FileUploadCompletedEvent event = events.get(0);
            assertThat(event.sessionId()).isEqualTo(session.getId());
            assertThat(event.fileName()).isEqualTo(session.getFileName());
            assertThat(event.etag()).isEqualTo(session.getEtag());
        }

        @Test
        @DisplayName("도메인 이벤트를 poll하면 내부 목록이 비워진다")
        void pollDomainEvents_ShouldClearInternalList() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();
            var etag = ETagFixture.defaultETag();
            session.complete(etag, etag, ClockFixture.defaultClock());

            // when
            List<FileUploadCompletedEvent> firstPoll = session.pollDomainEvents();
            List<FileUploadCompletedEvent> secondPoll = session.pollDomainEvents();

            // then
            assertThat(firstPoll).hasSize(1);
            assertThat(secondPoll).isEmpty();
        }
    }

    @Nested
    @DisplayName("Presigned URL 조회 테스트")
    class PresignedUrlTest {

        @Test
        @DisplayName("만료되지 않은 세션은 Presigned URL을 반환한다")
        void getPresignedUrl_WithValidSession_ShouldReturnUrl() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();

            // when & then
            assertThatCode(session::getPresignedUrl).doesNotThrowAnyException();
            assertThat(session.getPresignedUrl()).isNotNull();
        }

        @Test
        @DisplayName("만료된 세션도 Presigned URL을 조회할 수 있다 (Getter는 검증하지 않음)")
        void getPresignedUrl_WithExpiredSession_ShouldReturnValue() {
            // given
            SingleUploadSession session =
                    SingleUploadSession.forNew(
                            IdempotencyKeyFixture.defaultIdempotencyKey(),
                            UserContextFixture.defaultAdminUserContext(),
                            FileNameFixture.defaultFileName(),
                            FileSizeFixture.defaultFileSize(),
                            ContentTypeFixture.defaultContentType(),
                            S3BucketFixture.defaultS3Bucket(),
                            S3KeyFixture.defaultS3Key(),
                            ExpirationTimeFixture.expiredExpirationTime(),
                            ClockFixture.defaultClock());
            session.activate(
                    PresignedUrlFixture.defaultPresignedUrl(), ClockFixture.defaultClock());

            // when & then
            assertThatCode(session::getPresignedUrl).doesNotThrowAnyException();
            assertThat(session.getPresignedUrl()).isNotNull();
        }

        @Test
        @DisplayName("getPresignedUrlValue()는 Law of Demeter를 준수한다")
        void getPresignedUrlValue_ShouldFollowLawOfDemeter() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();

            // when & then
            assertThat(session.getPresignedUrlValue()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("만료 검증 테스트")
    class ExpirationTest {

        @Test
        @DisplayName("isExpired()로 만료 여부를 확인할 수 있다")
        void isExpired_ShouldReturnCorrectStatus() {
            // given
            SingleUploadSession activeSession =
                    SingleUploadSessionFixture.activeSingleUploadSession();
            SingleUploadSession expiredSession =
                    SingleUploadSession.forNew(
                            IdempotencyKeyFixture.defaultIdempotencyKey(),
                            UserContextFixture.defaultAdminUserContext(),
                            FileNameFixture.defaultFileName(),
                            FileSizeFixture.defaultFileSize(),
                            ContentTypeFixture.defaultContentType(),
                            S3BucketFixture.defaultS3Bucket(),
                            S3KeyFixture.defaultS3Key(),
                            ExpirationTimeFixture.expiredExpirationTime(),
                            ClockFixture.defaultClock());

            // when & then
            assertThat(activeSession.isExpired(ClockFixture.defaultClock())).isFalse();
            assertThat(expiredSession.isExpired(ClockFixture.defaultClock())).isTrue();
        }
    }

    @Nested
    @DisplayName("Getter 테스트")
    class GetterTest {

        @Test
        @DisplayName("모든 필드를 올바르게 반환한다")
        void getters_ShouldReturnCorrectValues() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();

            // when & then
            assertThat(session.getId()).isNotNull();
            assertThat(session.getIdempotencyKey()).isNotNull();
            assertThat(session.getUserContext()).isNotNull();
            assertThat(session.getFileName()).isNotNull();
            assertThat(session.getFileSize()).isNotNull();
            assertThat(session.getContentType()).isNotNull();
            assertThat(session.getBucket()).isNotNull();
            assertThat(session.getS3Key()).isNotNull();
            assertThat(session.getExpirationTime()).isNotNull();
            assertThat(session.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Law of Demeter를 준수하는 편의 메서드가 동작한다")
        void convenienceMethods_ShouldWork() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();

            // when & then
            assertThat(session.getIdValue()).isNotBlank();
            assertThat(session.getUserIdentifier()).isNotNull();
            assertThat(session.getOrganizationId()).isNull(); // Admin org ID is null
            assertThat(session.getFileNameValue()).isNotBlank();
            assertThat(session.getFileSizeValue()).isPositive();
            assertThat(session.getContentTypeValue()).isNotBlank();
            assertThat(session.getBucketValue()).isNotBlank();
            assertThat(session.getS3KeyValue()).isNotBlank();
            assertThat(session.getExpiresAt()).isNotNull();
        }

        @Test
        @DisplayName("완료되지 않은 세션의 ETag는 null이다")
        void getETagValue_WithIncompleteSession_ShouldReturnNull() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();

            // when & then
            assertThat(session.getETagValue()).isNull();
        }

        @Test
        @DisplayName("완료된 세션의 ETag는 값을 반환한다")
        void getETagValue_WithCompletedSession_ShouldReturnValue() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.completedSingleUploadSession();

            // when & then
            assertThat(session.getETagValue()).isNotBlank();
        }
    }
}
