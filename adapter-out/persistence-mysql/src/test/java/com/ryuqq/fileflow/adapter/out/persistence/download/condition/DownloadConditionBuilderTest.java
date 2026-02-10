package com.ryuqq.fileflow.adapter.out.persistence.download.condition;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("DownloadConditionBuilder 단위 테스트")
class DownloadConditionBuilderTest {

    private final DownloadConditionBuilder conditionBuilder = new DownloadConditionBuilder();

    @Nested
    @DisplayName("idEq 메서드 테스트")
    class IdEqTest {

        @Test
        @DisplayName("ID가 주어지면 BooleanExpression을 반환합니다")
        void idEq_withId_shouldReturnExpression() {
            BooleanExpression result = conditionBuilder.idEq("download-001");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("ID가 null이면 null을 반환합니다")
        void idEq_withNull_shouldReturnNull() {
            BooleanExpression result = conditionBuilder.idEq(null);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("statusEq 메서드 테스트")
    class StatusEqTest {

        @Test
        @DisplayName("status가 주어지면 BooleanExpression을 반환합니다")
        void statusEq_withStatus_shouldReturnExpression() {
            BooleanExpression result = conditionBuilder.statusEq(DownloadTaskStatus.QUEUED);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("status가 null이면 null을 반환합니다")
        void statusEq_withNull_shouldReturnNull() {
            BooleanExpression result = conditionBuilder.statusEq(null);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("createdBefore 메서드 테스트")
    class CreatedBeforeTest {

        @Test
        @DisplayName("시간이 주어지면 BooleanExpression을 반환합니다")
        void createdBefore_withInstant_shouldReturnExpression() {
            BooleanExpression result =
                    conditionBuilder.createdBefore(Instant.parse("2026-01-01T00:00:00Z"));
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("시간이 null이면 null을 반환합니다")
        void createdBefore_withNull_shouldReturnNull() {
            BooleanExpression result = conditionBuilder.createdBefore(null);
            assertThat(result).isNull();
        }
    }
}
