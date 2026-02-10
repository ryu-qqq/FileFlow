package com.ryuqq.fileflow.domain.session.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.event.UploadCompletedEvent;
import com.ryuqq.fileflow.domain.session.exception.SessionErrorCode;
import com.ryuqq.fileflow.domain.session.exception.SessionException;
import com.ryuqq.fileflow.domain.session.id.MultipartUploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.MultipartSessionStatus;
import com.ryuqq.fileflow.domain.session.vo.MultipartUploadSessionUpdateData;
import com.ryuqq.fileflow.domain.session.vo.UploadTargetFixture;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("MultipartUploadSession Aggregate 단위 테스트")
class MultipartUploadSessionTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant EXPIRES_AT = NOW.plus(Duration.ofHours(1));

    private MultipartUploadSession createSession() {
        return MultipartUploadSession.forNew(
                MultipartUploadSessionId.of("multipart-001"),
                UploadTargetFixture.anUploadTarget(),
                "upload-id-001",
                5_242_880L,
                "product-image",
                "commerce-service",
                EXPIRES_AT,
                NOW);
    }

    @Nested
    @DisplayName("forNew - 세션 생성")
    class ForNew {

        @Test
        @DisplayName("새 세션 생성 시 상태가 INITIATED이고 completedParts가 비어있다")
        void createsSessionWithInitiatedStatus() {
            MultipartUploadSession session = createSession();

            assertThat(session.idValue()).isEqualTo("multipart-001");
            assertThat(session.status()).isEqualTo(MultipartSessionStatus.INITIATED);
            assertThat(session.uploadId()).isEqualTo("upload-id-001");
            assertThat(session.partSize()).isEqualTo(5_242_880L);
            assertThat(session.purposeValue()).isEqualTo("product-image");
            assertThat(session.sourceValue()).isEqualTo("commerce-service");
            assertThat(session.expiresAt()).isEqualTo(EXPIRES_AT);
            assertThat(session.createdAt()).isEqualTo(NOW);
            assertThat(session.updatedAt()).isEqualTo(NOW);
            assertThat(session.completedParts()).isEmpty();
            assertThat(session.completedPartCount()).isZero();
            assertThat(session.s3Key()).isEqualTo("public/2026/01/file-001.jpg");
            assertThat(session.bucket()).isEqualTo("fileflow-bucket");
            assertThat(session.accessType()).isEqualTo(AccessType.PUBLIC);
            assertThat(session.fileName()).isEqualTo("product-image.jpg");
            assertThat(session.contentType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("새 세션 생성 시 이벤트가 발행되지 않는다")
        void noEventsOnCreation() {
            MultipartUploadSession session = createSession();

            assertThat(session.pollEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("addCompletedPart - 파트 업로드 완료")
    class AddCompletedPart {

        @Test
        @DisplayName("첫 번째 파트 추가 시 INITIATED에서 UPLOADING으로 전환된다")
        void transitionsToUploadingOnFirstPart() {
            MultipartUploadSession session = createSession();
            Instant partTime = NOW.plusSeconds(10);

            session.addCompletedPart(CompletedPart.of(1, "etag-part-1", 5_242_880L, partTime));

            assertThat(session.status()).isEqualTo(MultipartSessionStatus.UPLOADING);
            assertThat(session.completedPartCount()).isEqualTo(1);
            assertThat(session.completedParts().get(0).partNumber()).isEqualTo(1);
            assertThat(session.completedParts().get(0).etag()).isEqualTo("etag-part-1");
            assertThat(session.completedParts().get(0).size()).isEqualTo(5_242_880L);
            assertThat(session.completedParts().get(0).createdAt()).isEqualTo(partTime);
            assertThat(session.updatedAt()).isEqualTo(partTime);
        }

        @Test
        @DisplayName("여러 파트를 추가할 수 있다")
        void addsMultipleParts() {
            MultipartUploadSession session = createSession();

            session.addCompletedPart(
                    CompletedPart.of(1, "etag-1", 5_242_880L, NOW.plusSeconds(10)));
            session.addCompletedPart(
                    CompletedPart.of(2, "etag-2", 5_242_880L, NOW.plusSeconds(20)));
            session.addCompletedPart(
                    CompletedPart.of(3, "etag-3", 3_000_000L, NOW.plusSeconds(30)));

            assertThat(session.completedPartCount()).isEqualTo(3);
            assertThat(session.status()).isEqualTo(MultipartSessionStatus.UPLOADING);
        }

        @Test
        @DisplayName("중복 partNumber 추가 시 SessionException이 발생한다")
        void throwsOnDuplicatePartNumber() {
            MultipartUploadSession session = createSession();
            session.addCompletedPart(
                    CompletedPart.of(1, "etag-1", 5_242_880L, NOW.plusSeconds(10)));

            assertThatThrownBy(
                            () ->
                                    session.addCompletedPart(
                                            CompletedPart.of(
                                                    1,
                                                    "etag-1-dup",
                                                    5_242_880L,
                                                    NOW.plusSeconds(20))))
                    .isInstanceOf(SessionException.class)
                    .satisfies(
                            e -> {
                                SessionException ex = (SessionException) e;
                                assertThat(ex.getErrorCode())
                                        .isEqualTo(SessionErrorCode.PART_NUMBER_DUPLICATE);
                            });
        }

        @Test
        @DisplayName("COMPLETED 상태에서 addCompletedPart 호출 시 SessionException이 발생한다")
        void throwsWhenCompleted() {
            MultipartUploadSession session = createSession();
            session.addCompletedPart(
                    CompletedPart.of(1, "etag-1", 5_242_880L, NOW.plusSeconds(10)));
            session.complete(
                    MultipartUploadSessionUpdateData.of(10_000_000L, "etag-final"),
                    NOW.plusSeconds(30));

            assertThatThrownBy(
                            () ->
                                    session.addCompletedPart(
                                            CompletedPart.of(
                                                    2, "etag-2", 5_242_880L, NOW.plusSeconds(40))))
                    .isInstanceOf(SessionException.class)
                    .satisfies(
                            e -> {
                                SessionException ex = (SessionException) e;
                                assertThat(ex.getErrorCode())
                                        .isEqualTo(SessionErrorCode.SESSION_ALREADY_COMPLETED);
                            });
        }

        @Test
        @DisplayName("ABORTED 상태에서 addCompletedPart 호출 시 SessionException이 발생한다")
        void throwsWhenAborted() {
            MultipartUploadSession session = createSession();
            session.abort(NOW.plusSeconds(10));

            assertThatThrownBy(
                            () ->
                                    session.addCompletedPart(
                                            CompletedPart.of(
                                                    1, "etag-1", 5_242_880L, NOW.plusSeconds(20))))
                    .isInstanceOf(SessionException.class)
                    .satisfies(
                            e -> {
                                SessionException ex = (SessionException) e;
                                assertThat(ex.getErrorCode())
                                        .isEqualTo(SessionErrorCode.SESSION_ALREADY_ABORTED);
                            });
        }

        @Test
        @DisplayName("EXPIRED 상태에서 addCompletedPart 호출 시 SessionException이 발생한다")
        void throwsWhenExpired() {
            MultipartUploadSession session = createSession();
            session.expire(EXPIRES_AT.plusSeconds(1));

            assertThatThrownBy(
                            () ->
                                    session.addCompletedPart(
                                            CompletedPart.of(
                                                    1,
                                                    "etag-1",
                                                    5_242_880L,
                                                    EXPIRES_AT.plusSeconds(2))))
                    .isInstanceOf(SessionException.class)
                    .satisfies(
                            e -> {
                                SessionException ex = (SessionException) e;
                                assertThat(ex.getErrorCode())
                                        .isEqualTo(SessionErrorCode.SESSION_EXPIRED);
                            });
        }
    }

    @Nested
    @DisplayName("complete - 업로드 완료")
    class Complete {

        @Test
        @DisplayName("UPLOADING 상태에서 complete 호출 시 COMPLETED로 전환된다")
        void transitionsToCompleted() {
            MultipartUploadSession session = createSession();
            session.addCompletedPart(
                    CompletedPart.of(1, "etag-1", 5_242_880L, NOW.plusSeconds(10)));
            Instant completeTime = NOW.plusSeconds(30);

            session.complete(
                    MultipartUploadSessionUpdateData.of(10_485_760L, "etag-final"), completeTime);

            assertThat(session.status()).isEqualTo(MultipartSessionStatus.COMPLETED);
            assertThat(session.updatedAt()).isEqualTo(completeTime);
        }

        @Test
        @DisplayName("complete 호출 시 UploadCompletedEvent가 발행된다")
        void publishesUploadCompletedEvent() {
            MultipartUploadSession session = createSession();
            session.addCompletedPart(
                    CompletedPart.of(1, "etag-1", 5_242_880L, NOW.plusSeconds(10)));
            Instant completeTime = NOW.plusSeconds(30);

            session.complete(
                    MultipartUploadSessionUpdateData.of(10_485_760L, "etag-final"), completeTime);

            List<DomainEvent> events = session.pollEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(UploadCompletedEvent.class);

            UploadCompletedEvent event = (UploadCompletedEvent) events.get(0);
            assertThat(event.sessionId()).isEqualTo("multipart-001");
            assertThat(event.sessionType()).isEqualTo("MULTIPART");
            assertThat(event.s3Key()).isEqualTo("public/2026/01/file-001.jpg");
            assertThat(event.bucket()).isEqualTo("fileflow-bucket");
            assertThat(event.accessType()).isEqualTo(AccessType.PUBLIC);
            assertThat(event.fileSize()).isEqualTo(10_485_760L);
            assertThat(event.etag()).isEqualTo("etag-final");
            assertThat(event.occurredAt()).isEqualTo(completeTime);
        }

        @Test
        @DisplayName("completedParts 없이 complete 호출 시 SessionException이 발생한다")
        void throwsWhenNoCompletedParts() {
            MultipartUploadSession session = createSession();

            assertThatThrownBy(
                            () ->
                                    session.complete(
                                            MultipartUploadSessionUpdateData.of(
                                                    10_000_000L, "etag-final"),
                                            NOW.plusSeconds(30)))
                    .isInstanceOf(SessionException.class)
                    .satisfies(
                            e -> {
                                SessionException ex = (SessionException) e;
                                assertThat(ex.getErrorCode())
                                        .isEqualTo(SessionErrorCode.INVALID_SESSION_STATUS);
                            });
        }

        @Test
        @DisplayName("COMPLETED 상태에서 complete 호출 시 SessionException이 발생한다")
        void throwsWhenAlreadyCompleted() {
            MultipartUploadSession session = createSession();
            session.addCompletedPart(
                    CompletedPart.of(1, "etag-1", 5_242_880L, NOW.plusSeconds(10)));
            session.complete(
                    MultipartUploadSessionUpdateData.of(10_000_000L, "etag-final"),
                    NOW.plusSeconds(30));

            assertThatThrownBy(
                            () ->
                                    session.complete(
                                            MultipartUploadSessionUpdateData.of(
                                                    10_000_000L, "etag-final-2"),
                                            NOW.plusSeconds(60)))
                    .isInstanceOf(SessionException.class)
                    .satisfies(
                            e -> {
                                SessionException ex = (SessionException) e;
                                assertThat(ex.getErrorCode())
                                        .isEqualTo(SessionErrorCode.SESSION_ALREADY_COMPLETED);
                            });
        }

        @Test
        @DisplayName("시간이 만료된 상태에서 complete 호출 시 SessionException이 발생한다")
        void throwsWhenTimeExpired() {
            MultipartUploadSession session = createSession();
            session.addCompletedPart(
                    CompletedPart.of(1, "etag-1", 5_242_880L, NOW.plusSeconds(10)));

            assertThatThrownBy(
                            () ->
                                    session.complete(
                                            MultipartUploadSessionUpdateData.of(
                                                    10_000_000L, "etag-final"),
                                            EXPIRES_AT.plusSeconds(1)))
                    .isInstanceOf(SessionException.class)
                    .satisfies(
                            e -> {
                                SessionException ex = (SessionException) e;
                                assertThat(ex.getErrorCode())
                                        .isEqualTo(SessionErrorCode.SESSION_EXPIRED);
                            });
        }
    }

    @Nested
    @DisplayName("abort - 업로드 중단")
    class Abort {

        @Test
        @DisplayName("INITIATED 상태에서 abort 호출 시 ABORTED로 전환된다")
        void abortsFromInitiated() {
            MultipartUploadSession session = createSession();
            Instant abortTime = NOW.plusSeconds(30);

            session.abort(abortTime);

            assertThat(session.status()).isEqualTo(MultipartSessionStatus.ABORTED);
            assertThat(session.updatedAt()).isEqualTo(abortTime);
        }

        @Test
        @DisplayName("UPLOADING 상태에서 abort 호출 시 ABORTED로 전환된다")
        void abortsFromUploading() {
            MultipartUploadSession session = createSession();
            session.addCompletedPart(
                    CompletedPart.of(1, "etag-1", 5_242_880L, NOW.plusSeconds(10)));
            Instant abortTime = NOW.plusSeconds(30);

            session.abort(abortTime);

            assertThat(session.status()).isEqualTo(MultipartSessionStatus.ABORTED);
            assertThat(session.updatedAt()).isEqualTo(abortTime);
        }

        @Test
        @DisplayName("COMPLETED 상태에서 abort 호출 시 SessionException이 발생한다")
        void throwsWhenCompleted() {
            MultipartUploadSession session = createSession();
            session.addCompletedPart(
                    CompletedPart.of(1, "etag-1", 5_242_880L, NOW.plusSeconds(10)));
            session.complete(
                    MultipartUploadSessionUpdateData.of(10_000_000L, "etag-final"),
                    NOW.plusSeconds(30));

            assertThatThrownBy(() -> session.abort(NOW.plusSeconds(60)))
                    .isInstanceOf(SessionException.class)
                    .satisfies(
                            e -> {
                                SessionException ex = (SessionException) e;
                                assertThat(ex.getErrorCode())
                                        .isEqualTo(SessionErrorCode.SESSION_ALREADY_COMPLETED);
                            });
        }
    }

    @Nested
    @DisplayName("expire - 세션 만료")
    class Expire {

        @Test
        @DisplayName("INITIATED 상태에서 expire 호출 시 EXPIRED로 전환된다")
        void expiresFromInitiated() {
            MultipartUploadSession session = createSession();
            Instant expireTime = EXPIRES_AT.plusSeconds(1);

            session.expire(expireTime);

            assertThat(session.status()).isEqualTo(MultipartSessionStatus.EXPIRED);
            assertThat(session.updatedAt()).isEqualTo(expireTime);
        }

        @Test
        @DisplayName("UPLOADING 상태에서 expire 호출 시 EXPIRED로 전환된다")
        void expiresFromUploading() {
            MultipartUploadSession session = createSession();
            session.addCompletedPart(
                    CompletedPart.of(1, "etag-1", 5_242_880L, NOW.plusSeconds(10)));

            session.expire(EXPIRES_AT.plusSeconds(1));

            assertThat(session.status()).isEqualTo(MultipartSessionStatus.EXPIRED);
        }

        @Test
        @DisplayName("COMPLETED 상태에서 expire 호출 시 상태가 변경되지 않는다")
        void ignoresWhenCompleted() {
            MultipartUploadSession session = createSession();
            session.addCompletedPart(
                    CompletedPart.of(1, "etag-1", 5_242_880L, NOW.plusSeconds(10)));
            session.complete(
                    MultipartUploadSessionUpdateData.of(10_000_000L, "etag-final"),
                    NOW.plusSeconds(30));

            session.expire(EXPIRES_AT.plusSeconds(1));

            assertThat(session.status()).isEqualTo(MultipartSessionStatus.COMPLETED);
        }

        @Test
        @DisplayName("ABORTED 상태에서 expire 호출 시 상태가 변경되지 않는다")
        void ignoresWhenAborted() {
            MultipartUploadSession session = createSession();
            session.abort(NOW.plusSeconds(10));

            session.expire(EXPIRES_AT.plusSeconds(1));

            assertThat(session.status()).isEqualTo(MultipartSessionStatus.ABORTED);
        }
    }

    @Nested
    @DisplayName("isExpired - 만료 여부 확인")
    class IsExpired {

        @Test
        @DisplayName("expiresAt 이전이면 false를 반환한다")
        void returnsFalseBeforeExpiration() {
            MultipartUploadSession session = createSession();

            assertThat(session.isExpired(NOW.plusSeconds(30))).isFalse();
        }

        @Test
        @DisplayName("expiresAt 이후면 true를 반환한다")
        void returnsTrueAfterExpiration() {
            MultipartUploadSession session = createSession();

            assertThat(session.isExpired(EXPIRES_AT.plusSeconds(1))).isTrue();
        }
    }

    @Nested
    @DisplayName("pollEvents - 이벤트 폴링")
    class PollEvents {

        @Test
        @DisplayName("pollEvents 호출 후 이벤트가 비워진다")
        void clearsEventsAfterPoll() {
            MultipartUploadSession session = createSession();
            session.addCompletedPart(
                    CompletedPart.of(1, "etag-1", 5_242_880L, NOW.plusSeconds(10)));
            session.complete(
                    MultipartUploadSessionUpdateData.of(10_485_760L, "etag-final"),
                    NOW.plusSeconds(30));

            List<DomainEvent> firstPoll = session.pollEvents();
            assertThat(firstPoll).hasSize(1);

            List<DomainEvent> secondPoll = session.pollEvents();
            assertThat(secondPoll).isEmpty();
        }
    }

    @Nested
    @DisplayName("reconstitute - 복원")
    class Reconstitute {

        @Test
        @DisplayName("reconstitute로 복원된 세션은 이벤트가 없다")
        void reconstitutedSessionHasNoEvents() {
            MultipartUploadSession session =
                    MultipartUploadSession.reconstitute(
                            MultipartUploadSessionId.of("multipart-002"),
                            UploadTargetFixture.anUploadTarget(),
                            "upload-id-002",
                            5_242_880L,
                            "product-image",
                            "commerce-service",
                            MultipartSessionStatus.COMPLETED,
                            EXPIRES_AT,
                            NOW,
                            NOW.plusSeconds(60),
                            List.of());

            assertThat(session.status()).isEqualTo(MultipartSessionStatus.COMPLETED);
            assertThat(session.pollEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("validateUploadable - 업로드 가능 상태 검증")
    class ValidateUploadable {

        @Test
        @DisplayName("INITIATED 상태에서 만료 전이면 예외가 발생하지 않는다")
        void doesNotThrowWhenInitiatedAndNotExpired() {
            MultipartUploadSession session = createSession();

            assertThatCode(() -> session.validateUploadable(NOW.plusSeconds(30)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("UPLOADING 상태에서 만료 전이면 예외가 발생하지 않는다")
        void doesNotThrowWhenUploadingAndNotExpired() {
            MultipartUploadSession session = createSession();
            session.addCompletedPart(
                    CompletedPart.of(1, "etag-1", 5_242_880L, NOW.plusSeconds(10)));

            assertThatCode(() -> session.validateUploadable(NOW.plusSeconds(30)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("COMPLETED 상태에서 호출 시 SessionException이 발생한다")
        void throwsWhenCompleted() {
            MultipartUploadSession session = createSession();
            session.addCompletedPart(
                    CompletedPart.of(1, "etag-1", 5_242_880L, NOW.plusSeconds(10)));
            session.complete(
                    MultipartUploadSessionUpdateData.of(10_000_000L, "etag-final"),
                    NOW.plusSeconds(30));

            assertThatThrownBy(() -> session.validateUploadable(NOW.plusSeconds(60)))
                    .isInstanceOf(SessionException.class)
                    .satisfies(
                            e -> {
                                SessionException ex = (SessionException) e;
                                assertThat(ex.getErrorCode())
                                        .isEqualTo(SessionErrorCode.SESSION_ALREADY_COMPLETED);
                            });
        }

        @Test
        @DisplayName("ABORTED 상태에서 호출 시 SessionException이 발생한다")
        void throwsWhenAborted() {
            MultipartUploadSession session = createSession();
            session.abort(NOW.plusSeconds(10));

            assertThatThrownBy(() -> session.validateUploadable(NOW.plusSeconds(30)))
                    .isInstanceOf(SessionException.class)
                    .satisfies(
                            e -> {
                                SessionException ex = (SessionException) e;
                                assertThat(ex.getErrorCode())
                                        .isEqualTo(SessionErrorCode.SESSION_ALREADY_ABORTED);
                            });
        }

        @Test
        @DisplayName("EXPIRED 상태에서 호출 시 SessionException이 발생한다")
        void throwsWhenExpiredStatus() {
            MultipartUploadSession session = createSession();
            session.expire(EXPIRES_AT.plusSeconds(1));

            assertThatThrownBy(() -> session.validateUploadable(EXPIRES_AT.plusSeconds(2)))
                    .isInstanceOf(SessionException.class)
                    .satisfies(
                            e -> {
                                SessionException ex = (SessionException) e;
                                assertThat(ex.getErrorCode())
                                        .isEqualTo(SessionErrorCode.SESSION_EXPIRED);
                            });
        }

        @Test
        @DisplayName("시간이 만료된 경우 호출 시 SessionException이 발생한다")
        void throwsWhenTimeExpired() {
            MultipartUploadSession session = createSession();

            assertThatThrownBy(() -> session.validateUploadable(EXPIRES_AT.plusSeconds(1)))
                    .isInstanceOf(SessionException.class)
                    .satisfies(
                            e -> {
                                SessionException ex = (SessionException) e;
                                assertThat(ex.getErrorCode())
                                        .isEqualTo(SessionErrorCode.SESSION_EXPIRED);
                            });
        }
    }

    @Nested
    @DisplayName("equals/hashCode - ID 기반 동등성")
    class EqualsHashCode {

        @Test
        @DisplayName("동일한 ID를 가진 세션은 동등하다")
        void equalsByIdOnly() {
            MultipartUploadSession session1 = createSession();
            MultipartUploadSession session2 =
                    MultipartUploadSession.forNew(
                            MultipartUploadSessionId.of("multipart-001"),
                            UploadTargetFixture.anInternalUploadTarget(),
                            "different-upload-id",
                            10_000_000L,
                            "different-purpose",
                            "different-source",
                            EXPIRES_AT.plus(Duration.ofHours(2)),
                            NOW.plusSeconds(100));

            assertThat(session1).isEqualTo(session2);
            assertThat(session1.hashCode()).isEqualTo(session2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 세션은 동등하지 않다")
        void notEqualWithDifferentId() {
            MultipartUploadSession session1 = createSession();
            MultipartUploadSession session2 =
                    MultipartUploadSession.forNew(
                            MultipartUploadSessionId.of("multipart-002"),
                            UploadTargetFixture.anUploadTarget(),
                            "upload-id-001",
                            5_242_880L,
                            "product-image",
                            "commerce-service",
                            EXPIRES_AT,
                            NOW);

            assertThat(session1).isNotEqualTo(session2);
        }
    }
}
