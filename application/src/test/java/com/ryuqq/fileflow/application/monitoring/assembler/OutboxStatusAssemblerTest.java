package com.ryuqq.fileflow.application.monitoring.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.application.monitoring.dto.response.OutboxStatusResponse;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxStatusAssembler 단위 테스트")
class OutboxStatusAssemblerTest {

    @InjectMocks private OutboxStatusAssembler sut;
    @Mock private TimeProvider timeProvider;

    private static final Instant NOW = Instant.parse("2026-02-20T10:00:00Z");

    @Nested
    @DisplayName("toResponse 메서드")
    class ToResponseTest {

        @Test
        @DisplayName("OutboxStatusCount를 OutboxStatusResponse로 변환한다")
        void toResponse_ConvertsCountToResponse() {
            given(timeProvider.now()).willReturn(NOW);
            OutboxStatusCount downloadCount = new OutboxStatusCount(5L, 100L, 2L);
            OutboxStatusCount transformCount = new OutboxStatusCount(3L, 80L, 1L);

            OutboxStatusResponse result = sut.toResponse(downloadCount, transformCount);

            assertThat(result.download().pending()).isEqualTo(5L);
            assertThat(result.download().sent()).isEqualTo(100L);
            assertThat(result.download().failed()).isEqualTo(2L);
            assertThat(result.transform().pending()).isEqualTo(3L);
            assertThat(result.transform().sent()).isEqualTo(80L);
            assertThat(result.transform().failed()).isEqualTo(1L);
            assertThat(result.checkedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("빈 카운트이면 0으로 기본값을 설정한다")
        void toResponse_EmptyCount_DefaultsToZero() {
            given(timeProvider.now()).willReturn(NOW);

            OutboxStatusResponse result =
                    sut.toResponse(OutboxStatusCount.empty(), OutboxStatusCount.empty());

            assertThat(result.download().pending()).isZero();
            assertThat(result.download().sent()).isZero();
            assertThat(result.download().failed()).isZero();
            assertThat(result.transform().pending()).isZero();
            assertThat(result.transform().sent()).isZero();
            assertThat(result.transform().failed()).isZero();
        }
    }
}
