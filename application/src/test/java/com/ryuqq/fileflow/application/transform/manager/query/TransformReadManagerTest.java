package com.ryuqq.fileflow.application.transform.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.transform.port.out.query.TransformRequestQueryPort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
import com.ryuqq.fileflow.domain.transform.exception.TransformRequestNotFoundException;
import com.ryuqq.fileflow.domain.transform.id.TransformRequestId;
import com.ryuqq.fileflow.domain.transform.vo.TransformStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
@DisplayName("TransformReadManager 단위 테스트")
class TransformReadManagerTest {

    @InjectMocks private TransformReadManager sut;
    @Mock private TransformRequestQueryPort queryPort;

    @Nested
    @DisplayName("getTransformRequest 메서드")
    class GetTransformRequestTest {

        @Test
        @DisplayName("존재하는 ID로 TransformRequest를 반환한다")
        void getTransformRequest_ExistingId_ReturnsRequest() {
            // given
            String transformRequestId = "transform-001";
            TransformRequest expectedRequest = TransformRequestFixture.aResizeRequest();

            given(queryPort.findById(TransformRequestId.of(transformRequestId)))
                    .willReturn(Optional.of(expectedRequest));

            // when
            TransformRequest result = sut.getTransformRequest(transformRequestId);

            // then
            assertThat(result).isEqualTo(expectedRequest);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 TransformRequestNotFoundException을 던진다")
        void getTransformRequest_NonExistingId_ThrowsNotFoundException() {
            // given
            String transformRequestId = "non-existing-id";

            given(queryPort.findById(TransformRequestId.of(transformRequestId)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.getTransformRequest(transformRequestId))
                    .isInstanceOf(TransformRequestNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getStaleQueuedRequests 메서드")
    class GetStaleQueuedRequestsTest {

        @Test
        @DisplayName("QUEUED 상태의 오래된 요청 목록을 반환한다")
        void getStaleQueuedRequests_WithStaleRequests_ReturnsList() {
            // given
            Instant createdBefore = Instant.parse("2026-01-01T00:00:00Z");
            int limit = 100;
            List<TransformRequest> expectedRequests =
                    List.of(TransformRequestFixture.aResizeRequest());

            given(
                            queryPort.findByStatusAndCreatedBefore(
                                    TransformStatus.QUEUED, createdBefore, limit))
                    .willReturn(expectedRequests);

            // when
            List<TransformRequest> result = sut.getStaleQueuedRequests(createdBefore, limit);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).isEqualTo(expectedRequests);
        }

        @Test
        @DisplayName("오래된 요청이 없으면 빈 목록을 반환한다")
        void getStaleQueuedRequests_NoStaleRequests_ReturnsEmptyList() {
            // given
            Instant createdBefore = Instant.parse("2026-01-01T00:00:00Z");
            int limit = 100;

            given(
                            queryPort.findByStatusAndCreatedBefore(
                                    TransformStatus.QUEUED, createdBefore, limit))
                    .willReturn(List.of());

            // when
            List<TransformRequest> result = sut.getStaleQueuedRequests(createdBefore, limit);

            // then
            assertThat(result).isEmpty();
        }
    }
}
