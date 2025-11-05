package com.ryuqq.fileflow.domain.upload;

import com.ryuqq.fileflow.domain.upload.fixture.MultipartUploadFixture;
import com.ryuqq.fileflow.domain.upload.fixture.UploadPartFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * MultipartUpload Domain 단위 테스트
 *
 * <p>테스트 구성: Happy Path, Edge Cases, Exception Cases, Invariant Validation, Law of Demeter Tests</p>
 * <p>Fixture 사용: {@link MultipartUploadFixture}를 활용하여 테스트 데이터 생성</p>
 *
 * @author Sangwon Ryu
 * @since 2025-10-31
 */
@DisplayName("MultipartUpload Domain 단위 테스트")
class MultipartUploadTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreationTests {

        @Test
        @DisplayName("신규 MultipartUpload 생성 성공 (INIT 상태)")
        void create_Success() {
            // Given
            UploadSessionId uploadSessionId = UploadSessionId.of(1L);

            // When
            MultipartUpload upload = MultipartUpload.forNew(uploadSessionId);

            // Then
            assertThat(upload.getUploadSessionId()).isEqualTo(uploadSessionId);
            assertThat(upload.getStatus()).isEqualTo(MultipartUpload.MultipartStatus.INIT);
            assertThat(upload.getProviderUploadId()).isNull();
            assertThat(upload.getTotalParts()).isNull();
            assertThat(upload.getUploadedParts()).isEmpty();
        }

        @Test
        @DisplayName("Fixture를 통한 MultipartUpload 생성")
        void createUsingFixture_Success() {
            // Given & When
            MultipartUpload upload = MultipartUploadFixture.createNew();

            // Then
            assertThat(upload.getStatus()).isEqualTo(MultipartUpload.MultipartStatus.INIT);
        }
    }

    @Nested
    @DisplayName("상태 전환 테스트 (Happy Path)")
    class StateTransitionTests {

        @Test
        @DisplayName("INIT → IN_PROGRESS: initiate() 성공")
        void initiate_Success() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createNew();
            ProviderUploadId providerUploadId = ProviderUploadId.of("aws-upload-id-123");
            TotalParts totalParts = TotalParts.of(3);

            // When
            upload.initiate(providerUploadId, totalParts);

            // Then
            assertThat(upload.getStatus()).isEqualTo(MultipartUpload.MultipartStatus.IN_PROGRESS);
            assertThat(upload.getProviderUploadId()).isEqualTo(providerUploadId);
            assertThat(upload.getTotalParts()).isEqualTo(totalParts);
            assertThat(upload.isInProgress()).isTrue();
        }

        @Test
        @DisplayName("IN_PROGRESS → COMPLETED: 모든 파트 업로드 후 complete() 성공")
        void complete_Success() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createInitiated(
                UploadSessionId.of(1L),
                ProviderUploadId.of("upload-id"),
                TotalParts.of(2)
            );
            upload.addPart(UploadPartFixture.create(1, 5242880L));
            upload.addPart(UploadPartFixture.create(2, 5242880L));

            // When
            upload.complete();

            // Then
            assertThat(upload.getStatus()).isEqualTo(MultipartUpload.MultipartStatus.COMPLETED);
            assertThat(upload.isCompleted()).isTrue();
            assertThat(upload.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("IN_PROGRESS → ABORTED: abort() 성공")
        void abort_Success() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createInitiated();

            // When
            upload.abort();

            // Then
            assertThat(upload.getStatus()).isEqualTo(MultipartUpload.MultipartStatus.ABORTED);
            assertThat(upload.getAbortedAt()).isNotNull();
        }

        @Test
        @DisplayName("IN_PROGRESS → FAILED: fail() 성공")
        void fail_Success() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createInitiated();

            // When
            upload.fail();

            // Then
            assertThat(upload.getStatus()).isEqualTo(MultipartUpload.MultipartStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("파트 업로드 테스트 (Happy Path)")
    class PartUploadTests {

        @Test
        @DisplayName("파트 추가 성공")
        void addPart_Success() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createInitiated(
                UploadSessionId.of(1L),
                ProviderUploadId.of("upload-id"),
                TotalParts.of(3)
            );
            UploadPart part1 = UploadPartFixture.create(1, 5242880L);

            // When
            upload.addPart(part1);

            // Then
            assertThat(upload.getUploadedParts()).hasSize(1);
            assertThat(upload.getUploadedParts()).contains(part1);
        }

        @Test
        @DisplayName("여러 파트 순차 추가 성공")
        void addMultipleParts_Success() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createInitiated(
                UploadSessionId.of(1L),
                ProviderUploadId.of("upload-id"),
                TotalParts.of(3)
            );

            // When
            upload.addPart(UploadPartFixture.create(1, 5242880L));
            upload.addPart(UploadPartFixture.create(2, 5242880L));
            upload.addPart(UploadPartFixture.create(3, 5242880L));

            // Then
            assertThat(upload.getUploadedParts()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("예외 처리 테스트 (Exception Cases)")
    class ExceptionTests {

        @Test
        @DisplayName("forNew() - uploadSessionId가 null이면 예외 발생")
        void forNew_ThrowsException_WhenUploadSessionIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> MultipartUpload.forNew(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upload Session ID는 필수입니다");
        }

        @Test
        @DisplayName("initiate() - INIT 상태가 아니면 예외 발생")
        void initiate_ThrowsException_WhenNotInInitState() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createInitiated();

            // When & Then
            assertThatThrownBy(() -> upload.initiate(
                ProviderUploadId.of("new-upload-id"),
                TotalParts.of(3)
            ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 초기화된 업로드입니다");
        }

        @Test
        @DisplayName("initiate() - providerUploadId가 null이면 예외 발생")
        void initiate_ThrowsException_WhenProviderUploadIdIsNull() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createNew();

            // When & Then
            assertThatThrownBy(() -> upload.initiate(null, TotalParts.of(3)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider Upload ID는 필수입니다");
        }

        @Test
        @DisplayName("initiate() - totalParts가 null이면 예외 발생")
        void initiate_ThrowsException_WhenTotalPartsIsNull() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createNew();

            // When & Then
            assertThatThrownBy(() -> upload.initiate(ProviderUploadId.of("upload-id"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Total Parts는 필수입니다");
        }

        @Test
        @DisplayName("addPart() - IN_PROGRESS 상태가 아니면 예외 발생")
        void addPart_ThrowsException_WhenNotInProgress() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createNew();
            UploadPart part = UploadPartFixture.createDefault();

            // When & Then
            assertThatThrownBy(() -> upload.addPart(part))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("업로드가 시작되지 않았습니다");
        }

        @Test
        @DisplayName("addPart() - 중복 partNumber 추가 시 예외 발생")
        void addPart_ThrowsException_WhenDuplicatePartNumber() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createInitiated(
                UploadSessionId.of(1L),
                ProviderUploadId.of("upload-id"),
                TotalParts.of(3)
            );
            upload.addPart(UploadPartFixture.create(1, 5242880L));

            // When & Then
            assertThatThrownBy(() -> upload.addPart(UploadPartFixture.create(1, 5242880L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 업로드된 파트입니다");
        }

        @Test
        @DisplayName("complete() - 모든 파트가 업로드되지 않았으면 예외 발생")
        void complete_ThrowsException_WhenNotAllPartsUploaded() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createInitiated(
                UploadSessionId.of(1L),
                ProviderUploadId.of("upload-id"),
                TotalParts.of(3)
            );
            upload.addPart(UploadPartFixture.create(1, 5242880L));
            // Part 2, 3 누락

            // When & Then
            assertThatThrownBy(() -> upload.complete())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("완료할 수 없습니다");
        }

        @Test
        @DisplayName("abort() - COMPLETED 상태에서 abort 시 예외 발생")
        void abort_ThrowsException_WhenAlreadyCompleted() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createCompleted();

            // When & Then
            assertThatThrownBy(() -> upload.abort())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("완료된 업로드는 중단할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("Tell, Don't Ask 패턴 테스트")
    class TellDontAskTests {

        @Test
        @DisplayName("canComplete() - 완료 가능 여부 확인")
        void canComplete_Success() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createInitiated(
                UploadSessionId.of(1L),
                ProviderUploadId.of("upload-id"),
                TotalParts.of(2)
            );
            upload.addPart(UploadPartFixture.create(1, 5242880L));
            upload.addPart(UploadPartFixture.create(2, 5242880L));

            // When
            boolean canComplete = upload.canComplete();

            // Then
            assertThat(canComplete).isTrue();
        }

        @Test
        @DisplayName("canComplete() - 파트 누락 시 false 반환")
        void canComplete_ReturnsFalse_WhenPartsAreMissing() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createInitiated(
                UploadSessionId.of(1L),
                ProviderUploadId.of("upload-id"),
                TotalParts.of(3)
            );
            upload.addPart(UploadPartFixture.create(1, 5242880L));
            // Part 2, 3 누락

            // When
            boolean canComplete = upload.canComplete();

            // Then
            assertThat(canComplete).isFalse();
        }

        @Test
        @DisplayName("isInProgress() - 진행 중 상태 확인")
        void isInProgress_Success() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createInitiated();

            // When
            boolean isInProgress = upload.isInProgress();

            // Then
            assertThat(isInProgress).isTrue();
        }

        @Test
        @DisplayName("isCompleted() - 완료 상태 확인")
        void isCompleted_Success() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createCompleted();

            // When
            boolean isCompleted = upload.isCompleted();

            // Then
            assertThat(isCompleted).isTrue();
        }
    }

    @Nested
    @DisplayName("불변식 검증 테스트 (Invariant Validation)")
    class InvariantTests {

        @Test
        @DisplayName("생성된 MultipartUpload는 항상 INIT 상태")
        void created_AlwaysInInitState() {
            // Given & When
            MultipartUpload upload = MultipartUploadFixture.createNew();

            // Then
            assertThat(upload.getStatus()).isEqualTo(MultipartUpload.MultipartStatus.INIT);
        }

        @Test
        @DisplayName("초기화된 MultipartUpload는 항상 startedAt을 가짐")
        void initiated_AlwaysHasStartedAt() {
            // Given & When
            MultipartUpload upload = MultipartUploadFixture.createInitiated();

            // Then
            assertThat(upload.getStartedAt()).isNotNull();
        }

        @Test
        @DisplayName("완료된 MultipartUpload는 항상 completedAt을 가짐")
        void completed_AlwaysHasCompletedAt() {
            // Given & When
            MultipartUpload upload = MultipartUploadFixture.createCompleted();

            // Then
            assertThat(upload.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("uploadedParts는 항상 불변 리스트로 반환")
        void getUploadedParts_AlwaysReturnsImmutableList() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createInitiated();

            // When
            var parts = upload.getUploadedParts();

            // Then
            assertThatThrownBy(() -> parts.add(UploadPartFixture.createDefault()))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Reconstitute 테스트")
    class ReconstituteTests {

        @Test
        @DisplayName("DB에서 MultipartUpload 복원 성공")
        void reconstitute_Success() {
            // Given & When
            MultipartUpload upload = MultipartUploadFixture.reconstituteDefault(100L);

            // Then
            assertThat(upload.getId()).isEqualTo(100L);
            assertThat(upload.getUploadSessionId()).isNotNull();
        }
    }
}
