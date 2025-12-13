package com.ryuqq.fileflow.domain.asset.service;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.fixture.FileAssetFixture;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAssetUpdateService 단위 테스트")
class FileAssetUpdateServiceTest {

    private FileAssetUpdateService service;

    @BeforeEach
    void setUp() {
        ClockHolder clockHolder = ClockFixture::defaultClock;
        service = new FileAssetUpdateService(clockHolder);
    }

    @Nested
    @DisplayName("startProcessing 테스트")
    class StartProcessingTest {

        @Test
        @DisplayName("PENDING 상태의 FileAsset을 PROCESSING으로 변경한다")
        void startProcessing_ShouldChangeStatusToProcessing() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            assertThat(fileAsset.getStatus()).isEqualTo(FileAssetStatus.PENDING);

            // when
            FileAssetUpdateResult result = service.startProcessing(fileAsset);

            // then
            assertThat(result.fileAsset().getStatus()).isEqualTo(FileAssetStatus.PROCESSING);
        }

        @Test
        @DisplayName("상태 변경 이력을 생성한다")
        void startProcessing_ShouldCreateStatusHistory() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();

            // when
            FileAssetUpdateResult result = service.startProcessing(fileAsset);

            // then
            FileAssetStatusHistory history = result.statusHistory();
            assertThat(history).isNotNull();
            assertThat(history.getFromStatus()).isEqualTo(FileAssetStatus.PENDING);
            assertThat(history.getToStatus()).isEqualTo(FileAssetStatus.PROCESSING);
            assertThat(history.getMessage()).isEqualTo("이미지 처리 시작");
        }

        @Test
        @DisplayName("StatusHistory에 FileAsset ID가 포함된다")
        void startProcessing_StatusHistoryShouldContainFileAssetId() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();

            // when
            FileAssetUpdateResult result = service.startProcessing(fileAsset);

            // then
            FileAssetStatusHistory history = result.statusHistory();
            assertThat(history.getFileAssetId()).isEqualTo(fileAsset.getId());
        }

        @Test
        @DisplayName("결과에 변경된 FileAsset과 StatusHistory가 모두 포함된다")
        void startProcessing_ShouldReturnBothFileAssetAndHistory() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();

            // when
            FileAssetUpdateResult result = service.startProcessing(fileAsset);

            // then
            assertThat(result).isNotNull();
            assertThat(result.fileAsset()).isNotNull();
            assertThat(result.statusHistory()).isNotNull();
            assertThat(result.fileAsset()).isSameAs(fileAsset);
        }
    }

    @Nested
    @DisplayName("markResized 테스트")
    class MarkResizedTest {

        @Test
        @DisplayName("PROCESSING 상태의 FileAsset을 RESIZED로 변경한다")
        void markResized_ShouldChangeStatusToResized() {
            // given
            FileAsset fileAsset = FileAssetFixture.processingFileAsset();
            assertThat(fileAsset.getStatus()).isEqualTo(FileAssetStatus.PROCESSING);

            // when
            FileAssetUpdateResult result = service.markResized(fileAsset, 5);

            // then
            assertThat(result.fileAsset().getStatus()).isEqualTo(FileAssetStatus.RESIZED);
        }

        @Test
        @DisplayName("처리된 이미지 수를 포함한 상태 변경 이력을 생성한다")
        void markResized_ShouldCreateStatusHistoryWithProcessedCount() {
            // given
            FileAsset fileAsset = FileAssetFixture.processingFileAsset();
            int processedCount = 7;

            // when
            FileAssetUpdateResult result = service.markResized(fileAsset, processedCount);

            // then
            FileAssetStatusHistory history = result.statusHistory();
            assertThat(history).isNotNull();
            assertThat(history.getFromStatus()).isEqualTo(FileAssetStatus.PROCESSING);
            assertThat(history.getToStatus()).isEqualTo(FileAssetStatus.RESIZED);
            assertThat(history.getMessage()).isEqualTo("이미지 처리 완료: 7개 생성");
        }

        @Test
        @DisplayName("처리된 이미지 수가 0일 때도 정상 동작한다")
        void markResized_WithZeroProcessedCount_ShouldWork() {
            // given
            FileAsset fileAsset = FileAssetFixture.processingFileAsset();

            // when
            FileAssetUpdateResult result = service.markResized(fileAsset, 0);

            // then
            assertThat(result.statusHistory().getMessage()).isEqualTo("이미지 처리 완료: 0개 생성");
        }

        @Test
        @DisplayName("StatusHistory에 FileAsset ID가 포함된다")
        void markResized_StatusHistoryShouldContainFileAssetId() {
            // given
            FileAsset fileAsset = FileAssetFixture.processingFileAsset();

            // when
            FileAssetUpdateResult result = service.markResized(fileAsset, 3);

            // then
            FileAssetStatusHistory history = result.statusHistory();
            assertThat(history.getFileAssetId()).isEqualTo(fileAsset.getId());
        }
    }

    @Nested
    @DisplayName("markFailed 테스트")
    class MarkFailedTest {

        @Test
        @DisplayName("FileAsset을 FAILED로 변경한다")
        void markFailed_ShouldChangeStatusToFailed() {
            // given
            FileAsset fileAsset = FileAssetFixture.processingFileAsset();
            String errorMessage = "S3 업로드 실패";

            // when
            FileAssetUpdateResult result = service.markFailed(fileAsset, errorMessage);

            // then
            assertThat(result.fileAsset().getStatus()).isEqualTo(FileAssetStatus.FAILED);
        }

        @Test
        @DisplayName("에러 메시지를 포함한 상태 변경 이력을 생성한다")
        void markFailed_ShouldCreateStatusHistoryWithErrorMessage() {
            // given
            FileAsset fileAsset = FileAssetFixture.processingFileAsset();
            String errorMessage = "이미지 형식이 지원되지 않습니다";

            // when
            FileAssetUpdateResult result = service.markFailed(fileAsset, errorMessage);

            // then
            FileAssetStatusHistory history = result.statusHistory();
            assertThat(history).isNotNull();
            assertThat(history.getFromStatus()).isEqualTo(FileAssetStatus.PROCESSING);
            assertThat(history.getToStatus()).isEqualTo(FileAssetStatus.FAILED);
            assertThat(history.getMessage()).isEqualTo("처리 실패: 이미지 형식이 지원되지 않습니다");
        }

        @Test
        @DisplayName("PENDING 상태에서도 실패 처리할 수 있다")
        void markFailed_FromPendingStatus_ShouldWork() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            assertThat(fileAsset.getStatus()).isEqualTo(FileAssetStatus.PENDING);
            String errorMessage = "파일 읽기 실패";

            // when
            FileAssetUpdateResult result = service.markFailed(fileAsset, errorMessage);

            // then
            assertThat(result.fileAsset().getStatus()).isEqualTo(FileAssetStatus.FAILED);
            assertThat(result.statusHistory().getFromStatus()).isEqualTo(FileAssetStatus.PENDING);
        }

        @Test
        @DisplayName("StatusHistory에 FileAsset ID가 포함된다")
        void markFailed_StatusHistoryShouldContainFileAssetId() {
            // given
            FileAsset fileAsset = FileAssetFixture.processingFileAsset();

            // when
            FileAssetUpdateResult result = service.markFailed(fileAsset, "오류 발생");

            // then
            FileAssetStatusHistory history = result.statusHistory();
            assertThat(history.getFileAssetId()).isEqualTo(fileAsset.getId());
        }

        @Test
        @DisplayName("에러 메시지가 null일 때도 정상 동작한다")
        void markFailed_WithNullErrorMessage_ShouldWork() {
            // given
            FileAsset fileAsset = FileAssetFixture.processingFileAsset();

            // when
            FileAssetUpdateResult result = service.markFailed(fileAsset, null);

            // then
            assertThat(result.fileAsset().getStatus()).isEqualTo(FileAssetStatus.FAILED);
            assertThat(result.statusHistory().getMessage()).isEqualTo("처리 실패: null");
        }
    }
}
