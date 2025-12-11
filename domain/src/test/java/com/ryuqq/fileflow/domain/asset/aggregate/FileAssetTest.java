package com.ryuqq.fileflow.domain.asset.aggregate;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.asset.fixture.*;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.asset.vo.ImageDimension;
import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
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
            // given & when & then - fileName이 null인 경우
            assertThatThrownBy(
                            () ->
                                    FileAsset.forNew(
                                            UploadSessionIdFixture.fixedUploadSessionId(),
                                            null, // fileName
                                            FileSizeFixture.defaultFileSize(),
                                            ContentTypeFixture.defaultContentType(),
                                            FileCategory.IMAGE,
                                            null, // ImageDimension (nullable)
                                            S3BucketFixture.defaultS3Bucket(),
                                            S3KeyFixture.defaultS3Key(),
                                            ETagFixture.defaultETag(),
                                            UserId.generate(),
                                            OrganizationId.generate(),
                                            TenantId.generate(),
                                            ClockFixture.defaultClock()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null일 수 없습니다");
        }

        @Test
        @DisplayName("sessionId가 null이어도 ExternalDownload 경우에는 허용된다")
        void constructor_WithNullSessionId_ShouldBeAllowedForExternalDownload() {
            // given & when - sessionId가 null인 경우 (ExternalDownload)
            FileAsset asset =
                    FileAsset.forNew(
                            null, // sessionId (ExternalDownload의 경우 null 허용)
                            FileNameFixture.defaultFileName(),
                            FileSizeFixture.defaultFileSize(),
                            ContentTypeFixture.defaultContentType(),
                            FileCategory.IMAGE,
                            null,
                            S3BucketFixture.defaultS3Bucket(),
                            S3KeyFixture.defaultS3Key(),
                            ETagFixture.defaultETag(),
                            null, // userId (ExternalDownload의 경우 null)
                            OrganizationId.generate(),
                            TenantId.generate(),
                            ClockFixture.defaultClock());

            // then
            assertThat(asset).isNotNull();
            assertThat(asset.getSessionId()).isNull();
        }
    }

    @Nested
    @DisplayName("가공 처리 가능 검증 테스트")
    class ValidateCanProcessTest {

        @Test
        @DisplayName("PENDING 상태면 검증을 통과한다")
        void shouldValidateCanProcessWhenPending() {
            // given
            FileAsset asset = FileAssetFixture.defaultFileAsset();

            // when & then (예외 없이 통과)
            assertThatCode(asset::validateCanProcess).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PENDING이 아닌 상태면 예외가 발생한다")
        void shouldThrowWhenValidateCanProcessButNotPending() {
            // given
            FileAsset processingAsset = FileAssetFixture.processingFileAsset();
            FileAsset completedAsset = FileAssetFixture.completedFileAsset();

            // when & then
            assertThatThrownBy(processingAsset::validateCanProcess)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PENDING 상태에서만 가공을 시작할 수 있습니다");

            assertThatThrownBy(completedAsset::validateCanProcess)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PENDING 상태에서만 가공을 시작할 수 있습니다");
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
            asset.completeProcessing(ClockFixture.defaultClock());

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
            asset.completeProcessing(ClockFixture.defaultClock());

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
            assertThatThrownBy(() -> asset.completeProcessing(ClockFixture.defaultClock()))
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
            pendingAsset.failProcessing(ClockFixture.defaultClock());
            processingAsset.failProcessing(ClockFixture.defaultClock());

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
            asset.delete(ClockFixture.defaultClock());

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
            asset.delete(ClockFixture.defaultClock());

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
            asset.delete(ClockFixture.defaultClock());

            // then
            assertThat(asset.getStatus()).isEqualTo(FileAssetStatus.DELETED);
            assertThat(asset.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("FAILED 상태에서 삭제할 수 있다")
        void delete_FromFailed_ShouldSucceed() {
            // given
            FileAsset asset = FileAssetFixture.defaultFileAsset();
            asset.failProcessing(ClockFixture.defaultClock());

            // when
            asset.delete(ClockFixture.defaultClock());

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
            assertThatThrownBy(() -> asset.delete(ClockFixture.defaultClock()))
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
                            null, // ImageDimension (nullable)
                            S3BucketFixture.defaultS3Bucket(),
                            S3KeyFixture.defaultS3Key(),
                            ETagFixture.defaultETag(),
                            null, // userId can be null
                            OrganizationId.generate(),
                            TenantId.generate(),
                            ClockFixture.defaultClock());

            // when & then
            assertThat(asset.getUserId()).isNull();
        }
    }

    @Nested
    @DisplayName("Dimension 업데이트 테스트")
    class UpdateDimensionTest {

        @Test
        @DisplayName("dimension이 null인 FileAsset에 dimension을 업데이트할 수 있다")
        void updateDimension_WhenNullDimension_ShouldUpdate() {
            // given
            FileAsset asset = FileAssetFixture.defaultFileAsset(); // dimension이 null인 상태

            // when
            asset.updateDimension(ImageDimension.of(1920, 1080));

            // then
            assertThat(asset.hasImageDimension()).isTrue();
            assertThat(asset.getWidth()).isEqualTo(1920);
            assertThat(asset.getHeight()).isEqualTo(1080);
        }

        @Test
        @DisplayName("이미 dimension이 있는 FileAsset은 업데이트하면 예외가 발생한다")
        void updateDimension_WhenAlreadyHasDimension_ShouldThrowException() {
            // given
            FileAsset asset = FileAssetFixture.existingFileAsset(); // dimension이 있는 상태

            // when & then
            assertThatThrownBy(() -> asset.updateDimension(ImageDimension.of(800, 600)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 dimension이 설정되어 있습니다");
        }

        @Test
        @DisplayName("null dimension으로 업데이트하면 예외가 발생한다")
        void updateDimension_WithNullDimension_ShouldThrowException() {
            // given
            FileAsset asset = FileAssetFixture.defaultFileAsset();

            // when & then
            assertThatThrownBy(() -> asset.updateDimension(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dimension은 null일 수 없습니다");
        }
    }
}
