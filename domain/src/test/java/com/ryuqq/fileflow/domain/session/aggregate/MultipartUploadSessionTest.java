package com.ryuqq.fileflow.domain.session.aggregate;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import com.ryuqq.fileflow.domain.iam.fixture.UserContextFixture;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;
import com.ryuqq.fileflow.domain.session.exception.IncompletePartsException;
import com.ryuqq.fileflow.domain.session.exception.InvalidSessionStatusException;
import com.ryuqq.fileflow.domain.session.exception.SessionExpiredException;
import com.ryuqq.fileflow.domain.session.fixture.*;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("MultipartUploadSession 단위 테스트")
class MultipartUploadSessionTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 신규 세션을 생성하면 PREPARING 상태여야 한다")
        void forNew_ShouldCreateSessionWithPreparingStatus() {
            // given & when
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.defaultMultipartUploadSession();

            // then
            assertThat(session.getId()).isNotNull();
            assertThat(session.getStatus()).isEqualTo(SessionStatus.PREPARING);
            assertThat(session.getCompletedAt()).isNull();
            assertThat(session.getMergedETag()).isNull();
            assertThat(session.getVersion()).isNull();
        }

        @Test
        @DisplayName("reconstitute()로 영속성 복원 시 ID가 null이면 예외가 발생한다")
        void reconstitute_WithNullId_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    MultipartUploadSession.reconstitute(
                                            null,
                                            UserContextFixture.defaultAdminUserContext(),
                                            FileNameFixture.defaultFileName(),
                                            FileSizeFixture.largeFileSize(),
                                            ContentTypeFixture.defaultContentType(),
                                            S3BucketFixture.defaultS3Bucket(),
                                            S3KeyFixture.defaultS3Key(),
                                            S3UploadIdFixture.defaultS3UploadId(),
                                            TotalPartsFixture.defaultTotalParts(),
                                            PartSizeFixture.defaultPartSize(),
                                            ExpirationTimeFixture.multipartExpirationTime(),
                                            ClockFixture.defaultClock()
                                                    .instant()
                                                    .atZone(ClockFixture.defaultClock().getZone())
                                                    .toLocalDateTime(),
                                            SessionStatus.PREPARING,
                                            null,
                                            null,
                                            ClockFixture.defaultClock()))
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
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.defaultMultipartUploadSession();

            // when
            session.activate();

            // then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
            assertThat(session.isActive()).isTrue();
        }

        @Test
        @DisplayName("COMPLETED 상태에서 activate()를 호출하면 예외가 발생한다")
        void activate_FromCompleted_ShouldThrowException() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();
            List<CompletedPart> completedParts = createAllCompletedParts(session);
            session.complete(ETagFixture.multipartETag(), completedParts);

            // when & then
            assertThatThrownBy(session::activate).isInstanceOf(InvalidSessionStatusException.class);
        }

        @Test
        @DisplayName("ACTIVE 상태에서 expire()를 호출하면 EXPIRED 상태로 전환된다")
        void expire_FromActive_ShouldTransitionToExpired() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();

            // when
            session.expire();

            // then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.EXPIRED);
        }

        @Test
        @DisplayName("ACTIVE 상태에서 fail()을 호출하면 FAILED 상태로 전환된다")
        void fail_FromActive_ShouldTransitionToFailed() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();

            // when
            session.fail();

            // then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("업로드 완료 테스트")
    class CompleteTest {

        @Test
        @DisplayName("모든 Part가 완료되면 업로드를 완료할 수 있다")
        void complete_WithAllPartsCompleted_ShouldSucceed() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();
            List<CompletedPart> completedParts = createAllCompletedParts(session);

            // when
            session.complete(ETagFixture.multipartETag(), completedParts);

            // then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
            assertThat(session.isCompleted()).isTrue();
            assertThat(session.getCompletedAt()).isNotNull();
            assertThat(session.getMergedETag()).isEqualTo(ETagFixture.multipartETag());
        }

        @Test
        @DisplayName("모든 Part가 완료되지 않으면 예외가 발생한다")
        void complete_WithIncompleteparts_ShouldThrowException() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();
            List<CompletedPart> incompleteParts = createIncompleteparts(session);

            // when & then
            assertThatThrownBy(() -> session.complete(ETagFixture.multipartETag(), incompleteParts))
                    .isInstanceOf(IncompletePartsException.class);
        }

        @Test
        @DisplayName("Part 개수가 부족하면 예외가 발생한다")
        void complete_WithInsufficientParts_ShouldThrowException() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();
            List<CompletedPart> insufficientParts = new ArrayList<>();
            insufficientParts.add(CompletedPartFixture.completedCompletedPart());

            // when & then
            assertThatThrownBy(
                            () -> session.complete(ETagFixture.multipartETag(), insufficientParts))
                    .isInstanceOf(IncompletePartsException.class);
        }

        @Test
        @DisplayName("업로드 완료 시 도메인 이벤트가 발행된다")
        void complete_ShouldPublishDomainEvent() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();
            List<CompletedPart> completedParts = createAllCompletedParts(session);

            // when
            session.complete(ETagFixture.multipartETag(), completedParts);
            List<FileUploadCompletedEvent> events = session.pollDomainEvents();

            // then
            assertThat(events).hasSize(1);
            FileUploadCompletedEvent event = events.get(0);
            assertThat(event.sessionId()).isEqualTo(session.getId());
            assertThat(event.fileName()).isEqualTo(session.getFileName());
            assertThat(event.etag()).isEqualTo(session.getMergedETag());
        }

        @Test
        @DisplayName("도메인 이벤트를 poll하면 내부 목록이 비워진다")
        void pollDomainEvents_ShouldClearInternalList() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();
            List<CompletedPart> completedParts = createAllCompletedParts(session);
            session.complete(ETagFixture.multipartETag(), completedParts);

            // when
            List<FileUploadCompletedEvent> firstPoll = session.pollDomainEvents();
            List<FileUploadCompletedEvent> secondPoll = session.pollDomainEvents();

            // then
            assertThat(firstPoll).hasSize(1);
            assertThat(secondPoll).isEmpty();
        }
    }

    @Nested
    @DisplayName("만료 검증 테스트")
    class ExpirationTest {

        @Test
        @DisplayName("만료되지 않은 세션은 검증을 통과한다")
        void validateNotExpired_WithValidSession_ShouldPass() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();

            // when & then
            assertThatCode(session::validateNotExpired).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("만료된 세션은 예외가 발생한다")
        void validateNotExpired_WithExpiredSession_ShouldThrowException() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSession.forNew(
                            UserContextFixture.defaultAdminUserContext(),
                            FileNameFixture.defaultFileName(),
                            FileSizeFixture.largeFileSize(),
                            ContentTypeFixture.defaultContentType(),
                            S3BucketFixture.defaultS3Bucket(),
                            S3KeyFixture.defaultS3Key(),
                            S3UploadIdFixture.defaultS3UploadId(),
                            TotalPartsFixture.defaultTotalParts(),
                            PartSizeFixture.defaultPartSize(),
                            ExpirationTimeFixture.expiredExpirationTime(),
                            ClockFixture.defaultClock());

            // when & then
            assertThatThrownBy(session::validateNotExpired)
                    .isInstanceOf(SessionExpiredException.class);
        }

        @Test
        @DisplayName("isExpired()로 만료 여부를 확인할 수 있다")
        void isExpired_ShouldReturnCorrectStatus() {
            // given
            MultipartUploadSession activeSession =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();
            MultipartUploadSession expiredSession =
                    MultipartUploadSession.forNew(
                            UserContextFixture.defaultAdminUserContext(),
                            FileNameFixture.defaultFileName(),
                            FileSizeFixture.largeFileSize(),
                            ContentTypeFixture.defaultContentType(),
                            S3BucketFixture.defaultS3Bucket(),
                            S3KeyFixture.defaultS3Key(),
                            S3UploadIdFixture.defaultS3UploadId(),
                            TotalPartsFixture.defaultTotalParts(),
                            PartSizeFixture.defaultPartSize(),
                            ExpirationTimeFixture.expiredExpirationTime(),
                            ClockFixture.defaultClock());

            // when & then
            assertThat(activeSession.isExpired()).isFalse();
            assertThat(expiredSession.isExpired()).isTrue();
        }
    }

    @Nested
    @DisplayName("Part 번호 검증 테스트")
    class PartNumberValidationTest {

        @Test
        @DisplayName("유효한 Part 번호는 검증을 통과한다")
        void isValidPartNumber_WithValidNumber_ShouldReturnTrue() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();

            // when & then
            assertThat(session.isValidPartNumber(1)).isTrue();
            assertThat(session.isValidPartNumber(5)).isTrue();
        }

        @Test
        @DisplayName("범위를 벗어난 Part 번호는 검증에 실패한다")
        void isValidPartNumber_WithInvalidNumber_ShouldReturnFalse() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();

            // when & then
            assertThat(session.isValidPartNumber(0)).isFalse();
            assertThat(session.isValidPartNumber(6)).isFalse();
        }
    }

    @Nested
    @DisplayName("Getter 테스트")
    class GetterTest {

        @Test
        @DisplayName("모든 필드를 올바르게 반환한다")
        void getters_ShouldReturnCorrectValues() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();

            // when & then
            assertThat(session.getId()).isNotNull();
            assertThat(session.getUserContext()).isNotNull();
            assertThat(session.getFileName()).isNotNull();
            assertThat(session.getFileSize()).isNotNull();
            assertThat(session.getContentType()).isNotNull();
            assertThat(session.getBucket()).isNotNull();
            assertThat(session.getS3Key()).isNotNull();
            assertThat(session.getS3UploadId()).isNotNull();
            assertThat(session.getTotalParts()).isNotNull();
            assertThat(session.getPartSize()).isNotNull();
            assertThat(session.getExpirationTime()).isNotNull();
            assertThat(session.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Law of Demeter를 준수하는 편의 메서드가 동작한다")
        void convenienceMethods_ShouldWork() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();

            // when & then
            assertThat(session.getUserIdentifier()).isNotNull();
            assertThat(session.getOrganizationId()).isGreaterThanOrEqualTo(0L); // Admin org ID is 0
            assertThat(session.getFileNameValue()).isNotBlank();
            assertThat(session.getFileSizeValue()).isPositive();
            assertThat(session.getContentTypeValue()).isNotBlank();
            assertThat(session.getBucketValue()).isNotBlank();
            assertThat(session.getS3KeyValue()).isNotBlank();
            assertThat(session.getS3UploadIdValue()).isNotBlank();
            assertThat(session.getTotalPartsValue()).isPositive();
            assertThat(session.getPartSizeValue()).isPositive();
            assertThat(session.getExpiresAt()).isNotNull();
        }
    }

    // ==================== Helper Methods ====================

    private List<CompletedPart> createAllCompletedParts(MultipartUploadSession session) {
        List<CompletedPart> parts = new ArrayList<>();
        for (int i = 1; i <= session.getTotalPartsValue(); i++) {
            CompletedPart part =
                    CompletedPart.forNew(
                            session.getId(),
                            PartNumberFixture.customPartNumber(i),
                            PresignedUrlFixture.defaultPresignedUrl(),
                            ClockFixture.defaultClock());
            part.complete(ETagFixture.defaultETag(), 10 * 1024 * 1024L);
            parts.add(part);
        }
        return parts;
    }

    private List<CompletedPart> createIncompleteparts(MultipartUploadSession session) {
        List<CompletedPart> parts = new ArrayList<>();
        for (int i = 1; i <= session.getTotalPartsValue(); i++) {
            CompletedPart part =
                    CompletedPart.forNew(
                            session.getId(),
                            PartNumberFixture.customPartNumber(i),
                            PresignedUrlFixture.defaultPresignedUrl(),
                            ClockFixture.defaultClock());
            // 첫 번째 Part만 완료하지 않음
            if (i > 1) {
                part.complete(ETagFixture.defaultETag(), 10 * 1024 * 1024L);
            }
            parts.add(part);
        }
        return parts;
    }
}
