package com.ryuqq.fileflow.domain.asset.aggregate;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.asset.fixture.*;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import com.ryuqq.fileflow.domain.session.fixture.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAsset 단위 테스트")
class FileAssetTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 신규 자산을 생성하면 PENDING 상태여야 한다")
        void forNew_ShouldCreateAssetWithPendingStatus() {
            // given & when
            FileAsset asset = FileAssetFixture.defaultFileAsset();

            // then
            assertThat(asset.getId()).isNotNull();
            assertThat(asset.getSessionId()).isNotNull();
            assertThat(asset.getStatus()).isEqualTo(FileAssetStatus.PENDING);
            assertThat(asset.getCreatedAt()).isNotNull();
            assertThat(asset.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("reconstitute()로 영속성 복원 시 모든 필드가 설정된다")
        void reconstitute_ShouldRestoreAllFields() {
            // given & when
            FileAsset asset = FileAssetFixture.existingFileAsset();

            // then
            assertThat(asset.getId()).isNotNull();
            assertThat(asset.getSessionId()).isNotNull();
            assertThat(asset.getFileName()).isNotNull();
            assertThat(asset.getFileSize()).isNotNull();
            assertThat(asset.getContentType()).isNotNull();
            assertThat(asset.getCategory()).isNotNull();
            assertThat(asset.getBucket()).isNotNull();
            assertThat(asset.getS3Key()).isNotNull();
            assertThat(asset.getEtag()).isNotNull();
            assertThat(asset.getUserId()).isNotNull();
            assertThat(asset.getOrganizationId()).isNotNull();
            assertThat(asset.getTenantId()).isNotNull();
            assertThat(asset.getStatus()).isEqualTo(FileAssetStatus.COMPLETED);
            assertThat(asset.getCreatedAt()).isNotNull();
            assertThat(asset.getProcessedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("생성자 검증 테스트")
    class ConstructorValidationTest {

        @Test
        @DisplayName("필수 필드가 null이면 예외가 발생한다")
        void constructor_WithNullFields_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    FileAsset.forNew(
                                            null, // sessionId
                                            FileNameFixture.defaultFileName(),
                                            FileSizeFixture.defaultFileSize(),
                                            ContentTypeFixture.defaultContentType(),
                                            FileCategory.IMAGE,
                                            S3BucketFixture.defaultS3Bucket(),
                                            S3KeyFixture.defaultS3Key(),
                                            ETagFixture.defaultETag(),
                                            1000L,
                                            1L,
                                            1L,
                                            ClockFixture.defaultClock()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("가공 처리 시작 테스트")
    class StartProcessingTest {

        @Test
        @DisplayName("PENDING 상태에서 가공을 시작할 수 있다")
        void startProcessing_FromPending_ShouldSucceed() {
            // given
            FileAsset asset = FileAssetFixture.defaultFileAsset();

            // when
            asset.startProcessing();

            // then
            assertThat(asset.getStatus()).isEqualTo(FileAssetStatus.PROCESSING);
        }

        @Test
        @DisplayName("PENDING이 아닌 상태에서 가공 시작 시 예외가 발생한다")
        void startProcessing_FromNonPendingStatus_ShouldThrowException() {
            // given
            FileAsset asset = FileAssetFixture.processingFileAsset();

            // when & then
            assertThatThrownBy(asset::startProcessing)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PENDING 상태에서만 가공을 시작할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("가공 완료 테스트")
    class CompleteProcessingTest {

        @Test
        @DisplayName("PROCESSING 상태에서 가공을 완료할 수 있다")
        void completeProcessing_FromProcessing_ShouldSucceed() {
            // given
            FileAsset asset = FileAssetFixture.processingFileAsset();

            // when
            asset.completeProcessing();

            // then
            assertThat(asset.getStatus()).isEqualTo(FileAssetStatus.COMPLETED);
            assertThat(asset.getProcessedAt()).isNotNull();
        }

        @Test
        @DisplayName("PENDING 상태에서도 가공을 완료할 수 있다")
        void completeProcessing_FromPending_ShouldSucceed() {
            // given
            FileAsset asset = FileAssetFixture.defaultFileAsset();

            // when
            asset.completeProcessing();

            // then
            assertThat(asset.getStatus()).isEqualTo(FileAssetStatus.COMPLETED);
            assertThat(asset.getProcessedAt()).isNotNull();
        }

        @Test
        @DisplayName("COMPLETED 상태에서 가공 완료 시 예외가 발생한다")
        void completeProcessing_FromCompleted_ShouldThrowException() {
            // given
            FileAsset asset = FileAssetFixture.completedFileAsset();

            // when & then
            assertThatThrownBy(asset::completeProcessing)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PENDING 또는 PROCESSING 상태에서만 완료할 수 있습니다");
        }
    }

    @Nested
    @DisplayName("가공 실패 테스트")
    class FailProcessingTest {

        @Test
        @DisplayName("어떤 상태에서든 가공을 실패 처리할 수 있다")
        void failProcessing_FromAnyStatus_ShouldSucceed() {
            // given
            FileAsset pendingAsset = FileAssetFixture.defaultFileAsset();
            FileAsset processingAsset = FileAssetFixture.processingFileAsset();

            // when
            pendingAsset.failProcessing();
            processingAsset.failProcessing();

            // then
            assertThat(pendingAsset.getStatus()).isEqualTo(FileAssetStatus.FAILED);
            assertThat(pendingAsset.getProcessedAt()).isNotNull();
            assertThat(processingAsset.getStatus()).isEqualTo(FileAssetStatus.FAILED);
            assertThat(processingAsset.getProcessedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("삭제 테스트")
    class DeleteTest {

        @Test
        @DisplayName("PENDING 상태에서 삭제할 수 있다")
        void delete_FromPending_ShouldSucceed() {
            // given
            FileAsset asset = FileAssetFixture.defaultFileAsset();

            // when
            asset.delete();

            // then
            assertThat(asset.getStatus()).isEqualTo(FileAssetStatus.DELETED);
            assertThat(asset.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("PROCESSING 상태에서 삭제할 수 있다")
        void delete_FromProcessing_ShouldSucceed() {
            // given
            FileAsset asset = FileAssetFixture.processingFileAsset();

            // when
            asset.delete();

            // then
            assertThat(asset.getStatus()).isEqualTo(FileAssetStatus.DELETED);
            assertThat(asset.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("COMPLETED 상태에서 삭제할 수 있다")
        void delete_FromCompleted_ShouldSucceed() {
            // given
            FileAsset asset = FileAssetFixture.completedFileAsset();

            // when
            asset.delete();

            // then
            assertThat(asset.getStatus()).isEqualTo(FileAssetStatus.DELETED);
            assertThat(asset.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("FAILED 상태에서 삭제할 수 있다")
        void delete_FromFailed_ShouldSucceed() {
            // given
            FileAsset asset = FileAssetFixture.defaultFileAsset();
            asset.failProcessing();

            // when
            asset.delete();

            // then
            assertThat(asset.getStatus()).isEqualTo(FileAssetStatus.DELETED);
            assertThat(asset.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 DELETED 상태에서 삭제 시 예외가 발생한다")
        void delete_FromDeleted_ShouldThrowException() {
            // given
            FileAsset asset = FileAssetFixture.deletedFileAsset();

            // when & then
            assertThatThrownBy(asset::delete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 삭제된 FileAsset입니다");
        }
    }

    @Nested
    @DisplayName("Getter 테스트")
    class GetterTest {

        @Test
        @DisplayName("모든 필드를 올바르게 반환한다")
        void getters_ShouldReturnCorrectValues() {
            // given
            FileAsset asset = FileAssetFixture.existingFileAsset();

            // when & then
            assertThat(asset.getId()).isNotNull();
            assertThat(asset.getSessionId()).isNotNull();
            assertThat(asset.getFileName()).isNotNull();
            assertThat(asset.getFileSize()).isNotNull();
            assertThat(asset.getContentType()).isNotNull();
            assertThat(asset.getCategory()).isNotNull();
            assertThat(asset.getBucket()).isNotNull();
            assertThat(asset.getS3Key()).isNotNull();
            assertThat(asset.getEtag()).isNotNull();
            assertThat(asset.getUserId()).isNotNull();
            assertThat(asset.getOrganizationId()).isNotNull();
            assertThat(asset.getTenantId()).isNotNull();
            assertThat(asset.getStatus()).isNotNull();
            assertThat(asset.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Law of Demeter를 준수하는 편의 메서드가 동작한다")
        void convenienceMethods_ShouldWork() {
            // given
            FileAsset asset = FileAssetFixture.existingFileAsset();

            // when & then
            assertThat(asset.getIdValue()).isNotBlank();
            assertThat(asset.getSessionIdValue()).isNotBlank();
            assertThat(asset.getFileNameValue()).isNotBlank();
            assertThat(asset.getFileSizeValue()).isPositive();
            assertThat(asset.getContentTypeValue()).isNotBlank();
            assertThat(asset.getBucketValue()).isNotBlank();
            assertThat(asset.getS3KeyValue()).isNotBlank();
            assertThat(asset.getEtagValue()).isNotBlank();
        }

        @Test
        @DisplayName("userId가 null일 수 있다 (Customer의 경우)")
        void getUserId_CanBeNull() {
            // given
            FileAsset asset =
                    FileAsset.forNew(
                            UploadSessionIdFixture.defaultUploadSessionId(),
                            FileNameFixture.defaultFileName(),
                            FileSizeFixture.defaultFileSize(),
                            ContentTypeFixture.defaultContentType(),
                            FileCategory.IMAGE,
                            S3BucketFixture.defaultS3Bucket(),
                            S3KeyFixture.defaultS3Key(),
                            ETagFixture.defaultETag(),
                            null, // userId can be null
                            1L,
                            1L,
                            ClockFixture.defaultClock());

            // when & then
            assertThat(asset.getUserId()).isNull();
        }
    }
}
