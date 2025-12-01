package com.ryuqq.fileflow.application.download.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.application.download.port.out.command.ExternalDownloadOutboxPersistencePort;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadOutboxFixture;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalDownloadOutboxManager 테스트")
class ExternalDownloadOutboxManagerTest {

    @Mock private ExternalDownloadOutboxPersistencePort persistencePort;

    @Mock private ClockHolder clockHolder;

    @InjectMocks private ExternalDownloadOutboxManager manager;

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-11-26T12:00:00Z"), ZoneId.of("UTC"));

    @Nested
    @DisplayName("save 메서드")
    class SaveTest {

        @Test
        @DisplayName("Outbox를 저장하고 ID를 반환한다")
        void shouldSaveAndReturnId() {
            // given
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownloadOutboxId expectedId =
                    ExternalDownloadOutboxId.of("00000000-0000-0000-0000-000000000001");

            given(persistencePort.persist(outbox)).willReturn(expectedId);

            // when
            ExternalDownloadOutboxId result = manager.save(outbox);

            // then
            assertThat(result).isEqualTo(expectedId);
            verify(persistencePort).persist(outbox);
        }

        @Test
        @DisplayName("신규 Outbox를 저장할 수 있다")
        void shouldSaveNewOutbox() {
            // given
            ExternalDownloadOutbox newOutbox = ExternalDownloadOutboxFixture.defaultOutbox();
            ExternalDownloadOutboxId generatedId =
                    ExternalDownloadOutboxId.of("00000000-0000-0000-0000-0000000003e7");

            given(persistencePort.persist(any(ExternalDownloadOutbox.class)))
                    .willReturn(generatedId);

            // when
            ExternalDownloadOutboxId result = manager.save(newOutbox);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getValue()).isEqualTo("00000000-0000-0000-0000-0000000003e7");
            verify(persistencePort).persist(newOutbox);
        }
    }

    @Nested
    @DisplayName("markAsPublished 메서드")
    class MarkAsPublishedTest {

        @Test
        @DisplayName("Outbox를 발행 완료 상태로 업데이트한다")
        void shouldMarkAsPublished() {
            // given
            given(clockHolder.getClock()).willReturn(FIXED_CLOCK);
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();

            given(persistencePort.persist(any(ExternalDownloadOutbox.class)))
                    .willReturn(outbox.getId());

            // when
            manager.markAsPublished(outbox);

            // then
            ArgumentCaptor<ExternalDownloadOutbox> captor =
                    ArgumentCaptor.forClass(ExternalDownloadOutbox.class);
            verify(persistencePort).persist(captor.capture());

            ExternalDownloadOutbox savedOutbox = captor.getValue();
            assertThat(savedOutbox.isPublished()).isTrue();
            assertThat(savedOutbox.getPublishedAt()).isNotNull();
        }

        @Test
        @DisplayName("markAsPublished 호출 시 Clock의 현재 시간이 사용된다")
        void shouldUseClockForPublishedAt() {
            // given
            given(clockHolder.getClock()).willReturn(FIXED_CLOCK);
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();

            given(persistencePort.persist(any(ExternalDownloadOutbox.class)))
                    .willReturn(outbox.getId());

            // when
            manager.markAsPublished(outbox);

            // then
            ArgumentCaptor<ExternalDownloadOutbox> captor =
                    ArgumentCaptor.forClass(ExternalDownloadOutbox.class);
            verify(persistencePort).persist(captor.capture());

            ExternalDownloadOutbox savedOutbox = captor.getValue();
            assertThat(savedOutbox.getPublishedAt()).isEqualTo(Instant.now(FIXED_CLOCK));
        }
    }

    @Nested
    @DisplayName("markAsFailed 메서드")
    class MarkAsFailedTest {

        @Test
        @DisplayName("Outbox 발행 실패 시 published 상태를 유지한다")
        void shouldKeepUnpublishedState() {
            // given
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();

            // when
            manager.markAsFailed(outbox);

            // then
            // published=false 상태 유지 (재시도 스케줄러에서 처리)
            assertThat(outbox.isPublished()).isFalse();
        }
    }
}
