package com.ryuqq.fileflow.domain.asset.aggregate;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatusHistoryId;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * FileAssetStatusHistory Aggregate 단위 테스트.
 */
@DisplayName("FileAssetStatusHistory Aggregate 단위 테스트")
class FileAssetStatusHistoryTest {

    private static final Long FILE_ASSET_ID = 1L;
    private static final String TEST_MESSAGE = "테스트 메시지";
    private static final Long DURATION_MILLIS = 1000L;

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 새로운 상태 히스토리를 생성할 수 있다")
        void shouldCreateWithForNew() {
            // given & when
            FileAssetStatusHistory history = FileAssetStatusHistory.forNew(
                    FILE_ASSET_ID,
                    FileAssetStatus.PENDING,
                    FileAssetStatus.PROCESSING,
                    TEST_MESSAGE,
                    "system",
                    "SYSTEM",
                    DURATION_MILLIS);

            // then
            assertThat(history).isNotNull();
            assertThat(history.getId()).isNotNull();
            assertThat(history.getFileAssetId()).isEqualTo(FILE_ASSET_ID);
            assertThat(history.getFromStatus()).isEqualTo(FileAssetStatus.PENDING);
            assertThat(history.getToStatus()).isEqualTo(FileAssetStatus.PROCESSING);
            assertThat(history.getMessage()).isEqualTo(TEST_MESSAGE);
            assertThat(history.getActor()).isEqualTo("system");
            assertThat(history.getActorType()).isEqualTo("SYSTEM");
            assertThat(history.getDurationMillis()).isEqualTo(DURATION_MILLIS);
            assertThat(history.getChangedAt()).isNotNull();
        }

        @Test
        @DisplayName("forSystemChange()로 시스템 변경 히스토리를 생성할 수 있다")
        void shouldCreateWithForSystemChange() {
            // given & when
            FileAssetStatusHistory history = FileAssetStatusHistory.forSystemChange(
                    FILE_ASSET_ID,
                    FileAssetStatus.PENDING,
                    FileAssetStatus.PROCESSING,
                    TEST_MESSAGE,
                    DURATION_MILLIS);

            // then
            assertThat(history).isNotNull();
            assertThat(history.getActor()).isEqualTo("system");
            assertThat(history.getActorType()).isEqualTo("SYSTEM");
        }

        @Test
        @DisplayName("forN8nChange()로 n8n 변경 히스토리를 생성할 수 있다")
        void shouldCreateWithForN8nChange() {
            // given & when
            FileAssetStatusHistory history = FileAssetStatusHistory.forN8nChange(
                    FILE_ASSET_ID,
                    FileAssetStatus.PROCESSING,
                    FileAssetStatus.N8N_PROCESSING,
                    TEST_MESSAGE,
                    DURATION_MILLIS);

            // then
            assertThat(history).isNotNull();
            assertThat(history.getActor()).isEqualTo("n8n");
            assertThat(history.getActorType()).isEqualTo("N8N");
        }

        @Test
        @DisplayName("reconstitute()로 DB에서 복원할 수 있다")
        void shouldReconstitute() {
            // given
            FileAssetStatusHistoryId id = FileAssetStatusHistoryId.forNew();
            LocalDateTime changedAt = LocalDateTime.of(2025, 12, 2, 10, 0, 0);

            // when
            FileAssetStatusHistory history = FileAssetStatusHistory.reconstitute(
                    id,
                    FILE_ASSET_ID,
                    FileAssetStatus.PENDING,
                    FileAssetStatus.PROCESSING,
                    TEST_MESSAGE,
                    "system",
                    "SYSTEM",
                    changedAt,
                    DURATION_MILLIS);

            // then
            assertThat(history).isNotNull();
            assertThat(history.getId()).isEqualTo(id);
            assertThat(history.getFileAssetId()).isEqualTo(FILE_ASSET_ID);
            assertThat(history.getChangedAt()).isEqualTo(changedAt);
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트")
    class BusinessMethodTest {

        @Test
        @DisplayName("isFailure()는 FAILED 상태일 때 true를 반환한다")
        void shouldReturnTrueForIsFailure() {
            // given
            FileAssetStatusHistory history = FileAssetStatusHistory.forSystemChange(
                    FILE_ASSET_ID,
                    FileAssetStatus.PROCESSING,
                    FileAssetStatus.FAILED,
                    "처리 실패",
                    DURATION_MILLIS);

            // when & then
            assertThat(history.isFailure()).isTrue();
        }

        @Test
        @DisplayName("isFailure()는 FAILED가 아닌 상태일 때 false를 반환한다")
        void shouldReturnFalseForIsFailureWhenNotFailed() {
            // given
            FileAssetStatusHistory history = FileAssetStatusHistory.forSystemChange(
                    FILE_ASSET_ID,
                    FileAssetStatus.PENDING,
                    FileAssetStatus.PROCESSING,
                    TEST_MESSAGE,
                    DURATION_MILLIS);

            // when & then
            assertThat(history.isFailure()).isFalse();
        }

        @Test
        @DisplayName("isInitialCreation()는 fromStatus가 null일 때 true를 반환한다")
        void shouldReturnTrueForIsInitialCreation() {
            // given
            FileAssetStatusHistory history = FileAssetStatusHistory.forNew(
                    FILE_ASSET_ID,
                    null, // 최초 생성 시 fromStatus는 null
                    FileAssetStatus.PENDING,
                    "파일 생성됨",
                    "system",
                    "SYSTEM",
                    null);

            // when & then
            assertThat(history.isInitialCreation()).isTrue();
        }

        @Test
        @DisplayName("isInitialCreation()는 fromStatus가 있을 때 false를 반환한다")
        void shouldReturnFalseForIsInitialCreationWhenFromStatusExists() {
            // given
            FileAssetStatusHistory history = FileAssetStatusHistory.forSystemChange(
                    FILE_ASSET_ID,
                    FileAssetStatus.PENDING,
                    FileAssetStatus.PROCESSING,
                    TEST_MESSAGE,
                    DURATION_MILLIS);

            // when & then
            assertThat(history.isInitialCreation()).isFalse();
        }

        @Test
        @DisplayName("exceedsSla()는 SLA 초과 시 true를 반환한다")
        void shouldReturnTrueForExceedsSla() {
            // given
            long slaMillis = 5000L; // 5초 SLA
            long actualDuration = 6000L; // 6초 소요

            FileAssetStatusHistory history = FileAssetStatusHistory.forSystemChange(
                    FILE_ASSET_ID,
                    FileAssetStatus.PENDING,
                    FileAssetStatus.PROCESSING,
                    TEST_MESSAGE,
                    actualDuration);

            // when & then
            assertThat(history.exceedsSla(slaMillis)).isTrue();
        }

        @Test
        @DisplayName("exceedsSla()는 SLA 이내일 때 false를 반환한다")
        void shouldReturnFalseForExceedsSlaWhenWithinSla() {
            // given
            long slaMillis = 5000L; // 5초 SLA
            long actualDuration = 3000L; // 3초 소요

            FileAssetStatusHistory history = FileAssetStatusHistory.forSystemChange(
                    FILE_ASSET_ID,
                    FileAssetStatus.PENDING,
                    FileAssetStatus.PROCESSING,
                    TEST_MESSAGE,
                    actualDuration);

            // when & then
            assertThat(history.exceedsSla(slaMillis)).isFalse();
        }

        @Test
        @DisplayName("exceedsSla()는 durationMillis가 null일 때 false를 반환한다")
        void shouldReturnFalseForExceedsSlaWhenDurationIsNull() {
            // given
            FileAssetStatusHistory history = FileAssetStatusHistory.forNew(
                    FILE_ASSET_ID,
                    null,
                    FileAssetStatus.PENDING,
                    "파일 생성됨",
                    "system",
                    "SYSTEM",
                    null); // durationMillis null

            // when & then
            assertThat(history.exceedsSla(5000L)).isFalse();
        }
    }
}
