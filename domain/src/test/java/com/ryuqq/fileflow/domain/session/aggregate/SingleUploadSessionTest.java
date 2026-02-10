package com.ryuqq.fileflow.domain.session.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.event.UploadCompletedEvent;
import com.ryuqq.fileflow.domain.session.exception.SessionErrorCode;
import com.ryuqq.fileflow.domain.session.exception.SessionException;
import com.ryuqq.fileflow.domain.session.id.SingleUploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.SingleSessionStatus;
import com.ryuqq.fileflow.domain.session.vo.SingleUploadSessionUpdateData;
import com.ryuqq.fileflow.domain.session.vo.UploadTargetFixture;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SingleUploadSession Aggregate 단위 테스트")
class SingleUploadSessionTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant EXPIRES_AT = NOW.plus(Duration.ofHours(1));

    private SingleUploadSession createSession() {
        return SingleUploadSession.forNew(
                SingleUploadSessionId.of("session-001"),
                UploadTargetFixture.anUploadTarget(),
                "https://s3.presigned-url.com/test",
                "product-image",
                "commerce-service",
                EXPIRES_AT,
                NOW);
    }

    @Nested
    @DisplayName("forNew - 세션 생성")
    class ForNew {

        @Test
        @DisplayName("새 세션 생성 시 상태가 CREATED이고 속성이 올바르게 설정된다")
        void createsSessionWithCreatedStatus() {
            SingleUploadSession session = createSession();

            assertThat(session.idValue()).isEqualTo("session-001");
            assertThat(session.status()).isEqualTo(SingleSessionStatus.CREATED);
            assertThat(session.presignedUrlValue()).isEqualTo("https://s3.presigned-url.com/test");
            assertThat(session.purposeValue()).isEqualTo("product-image");
            assertThat(session.sourceValue()).isEqualTo("commerce-service");
            assertThat(session.expiresAt()).isEqualTo(EXPIRES_AT);
            assertThat(session.createdAt()).isEqualTo(NOW);
            assertThat(session.updatedAt()).isEqualTo(NOW);
            assertThat(session.s3Key()).isEqualTo("public/2026/01/file-001.jpg");
            assertThat(session.bucket()).isEqualTo("fileflow-bucket");
            assertThat(session.accessType()).isEqualTo(AccessType.PUBLIC);
            assertThat(session.fileName()).isEqualTo("product-image.jpg");
            assertThat(session.contentType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("새 세션 생성 시 이벤트가 발행되지 않는다")
        void noEventsOnCreation() {
            SingleUploadSession session = createSession();

            List<DomainEvent> events = session.pollEvents();
            assertThat(events).isEmpty();
        }
    }

    @Nested
    @DisplayName("complete - 업로드 완료")
    class Complete {

        @Test
        @DisplayName("CREATED 상태에서 complete 호출 시 COMPLETED로 전환된다")
        void transitionsToCompleted() {
            SingleUploadSession session = createSession();
            Instant completeTime = NOW.plusSeconds(30);

            session.complete(SingleUploadSessionUpdateData.of(1024L, "etag-123"), completeTime);

            assertThat(session.status()).isEqualTo(SingleSessionStatus.COMPLETED);
            assertThat(session.updatedAt()).isEqualTo(completeTime);
        }

        @Test
        @DisplayName("complete 호출 시 UploadCompletedEvent가 발행된다")
        void publishesUploadCompletedEvent() {
            SingleUploadSession session = createSession();
            Instant completeTime = NOW.plusSeconds(30);

            session.complete(SingleUploadSessionUpdateData.of(1024L, "etag-123"), completeTime);

            List<DomainEvent> events = session.pollEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(UploadCompletedEvent.class);

            UploadCompletedEvent event = (UploadCompletedEvent) events.get(0);
            assertThat(event.sessionId()).isEqualTo("session-001");
            assertThat(event.sessionType()).isEqualTo("SINGLE");
            assertThat(event.s3Key()).isEqualTo("public/2026/01/file-001.jpg");
            assertThat(event.bucket()).isEqualTo("fileflow-bucket");
            assertThat(event.accessType()).isEqualTo(AccessType.PUBLIC);
            assertThat(event.fileName()).isEqualTo("product-image.jpg");
            assertThat(event.contentType()).isEqualTo("image/jpeg");
            assertThat(event.fileSize()).isEqualTo(1024L);
            assertThat(event.etag()).isEqualTo("etag-123");
            assertThat(event.purpose()).isEqualTo("product-image");
            assertThat(event.source()).isEqualTo("commerce-service");
            assertThat(event.occurredAt()).isEqualTo(completeTime);
        }

        @Test
        @DisplayName("COMPLETED 상태에서 complete 호출 시 SessionException이 발생한다")
        void throwsWhenAlreadyCompleted() {
            SingleUploadSession session = createSession();
            session.complete(
                    SingleUploadSessionUpdateData.of(1024L, "etag-123"), NOW.plusSeconds(30));

            assertThatThrownBy(
                            () ->
                                    session.complete(
                                            SingleUploadSessionUpdateData.of(2048L, "etag-456"),
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
        @DisplayName("EXPIRED 상태에서 complete 호출 시 SessionException이 발생한다")
        void throwsWhenExpired() {
            SingleUploadSession session = createSession();
            session.expire(EXPIRES_AT.plusSeconds(1));

            assertThatThrownBy(
                            () ->
                                    session.complete(
                                            SingleUploadSessionUpdateData.of(1024L, "etag-123"),
                                            EXPIRES_AT.plusSeconds(2)))
                    .isInstanceOf(SessionException.class)
                    .satisfies(
                            e -> {
                                SessionException ex = (SessionException) e;
                                assertThat(ex.getErrorCode())
                                        .isEqualTo(SessionErrorCode.SESSION_EXPIRED);
                            });
        }

        @Test
        @DisplayName("시간이 만료된 상태에서 complete 호출 시 SessionException이 발생한다")
        void throwsWhenTimeExpired() {
            SingleUploadSession session = createSession();

            assertThatThrownBy(
                            () ->
                                    session.complete(
                                            SingleUploadSessionUpdateData.of(1024L, "etag-123"),
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
    @DisplayName("expire - 세션 만료")
    class Expire {

        @Test
        @DisplayName("CREATED 상태에서 expire 호출 시 EXPIRED로 전환된다")
        void transitionsToExpired() {
            SingleUploadSession session = createSession();
            Instant expireTime = EXPIRES_AT.plusSeconds(1);

            session.expire(expireTime);

            assertThat(session.status()).isEqualTo(SingleSessionStatus.EXPIRED);
            assertThat(session.updatedAt()).isEqualTo(expireTime);
        }

        @Test
        @DisplayName("COMPLETED 상태에서 expire 호출 시 상태가 변경되지 않는다")
        void ignoresWhenCompleted() {
            SingleUploadSession session = createSession();
            session.complete(
                    SingleUploadSessionUpdateData.of(1024L, "etag-123"), NOW.plusSeconds(30));
            Instant updatedAtBeforeExpire = session.updatedAt();

            session.expire(EXPIRES_AT.plusSeconds(1));

            assertThat(session.status()).isEqualTo(SingleSessionStatus.COMPLETED);
            assertThat(session.updatedAt()).isEqualTo(updatedAtBeforeExpire);
        }
    }

    @Nested
    @DisplayName("isExpired - 만료 여부 확인")
    class IsExpired {

        @Test
        @DisplayName("expiresAt 이전이면 false를 반환한다")
        void returnsFalseBeforeExpiration() {
            SingleUploadSession session = createSession();

            assertThat(session.isExpired(NOW.plusSeconds(30))).isFalse();
        }

        @Test
        @DisplayName("expiresAt 이후면 true를 반환한다")
        void returnsTrueAfterExpiration() {
            SingleUploadSession session = createSession();

            assertThat(session.isExpired(EXPIRES_AT.plusSeconds(1))).isTrue();
        }

        @Test
        @DisplayName("expiresAt과 동일한 시각이면 false를 반환한다")
        void returnsFalseAtExactExpiration() {
            SingleUploadSession session = createSession();

            assertThat(session.isExpired(EXPIRES_AT)).isFalse();
        }
    }

    @Nested
    @DisplayName("pollEvents - 이벤트 폴링")
    class PollEvents {

        @Test
        @DisplayName("pollEvents 호출 후 이벤트가 비워진다")
        void clearsEventsAfterPoll() {
            SingleUploadSession session = createSession();
            session.complete(
                    SingleUploadSessionUpdateData.of(1024L, "etag-123"), NOW.plusSeconds(30));

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
            SingleUploadSession session =
                    SingleUploadSession.reconstitute(
                            SingleUploadSessionId.of("session-002"),
                            UploadTargetFixture.anUploadTarget(),
                            "https://s3.presigned-url.com/test",
                            "product-image",
                            "commerce-service",
                            SingleSessionStatus.COMPLETED,
                            EXPIRES_AT,
                            NOW,
                            NOW.plusSeconds(30));

            assertThat(session.status()).isEqualTo(SingleSessionStatus.COMPLETED);
            assertThat(session.pollEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("equals/hashCode - ID 기반 동등성")
    class EqualsHashCode {

        @Test
        @DisplayName("동일한 ID를 가진 세션은 동등하다")
        void equalsByIdOnly() {
            SingleUploadSession session1 = createSession();
            SingleUploadSession session2 =
                    SingleUploadSession.forNew(
                            SingleUploadSessionId.of("session-001"),
                            UploadTargetFixture.anInternalUploadTarget(),
                            "https://different-url.com",
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
            SingleUploadSession session1 = createSession();
            SingleUploadSession session2 =
                    SingleUploadSession.forNew(
                            SingleUploadSessionId.of("session-002"),
                            UploadTargetFixture.anUploadTarget(),
                            "https://s3.presigned-url.com/test",
                            "product-image",
                            "commerce-service",
                            EXPIRES_AT,
                            NOW);

            assertThat(session1).isNotEqualTo(session2);
        }
    }
}
