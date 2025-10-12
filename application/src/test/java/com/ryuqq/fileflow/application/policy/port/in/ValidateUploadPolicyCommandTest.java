package com.ryuqq.fileflow.application.policy.port.in;

import com.ryuqq.fileflow.application.policy.port.in.ValidateUploadPolicyUseCase.ValidateUploadPolicyCommand;
import com.ryuqq.fileflow.domain.policy.FileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ValidateUploadPolicyCommand 단위 테스트
 *
 * Command의 생성자 검증 로직과 Rate Limiting 검증 메서드를 테스트합니다.
 *
 * @author sangwon-ryu
 */
class ValidateUploadPolicyCommandTest {

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorValidation {

        @Test
        @DisplayName("정상적인 Command 생성")
        void createCommand_ValidInputs_Success() {
            // When
            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "tenant-1",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    "jpg",
                    1024L,
                    5,
                    10,
                    100
            );

            // Then
            assertThat(command).isNotNull();
            assertThat(command.tenantId()).isEqualTo("tenant-1");
            assertThat(command.userType()).isEqualTo("CONSUMER");
            assertThat(command.serviceType()).isEqualTo("REVIEW");
            assertThat(command.fileType()).isEqualTo(FileType.IMAGE);
            assertThat(command.fileFormat()).isEqualTo("jpg");
            assertThat(command.fileSizeBytes()).isEqualTo(1024L);
            assertThat(command.fileCount()).isEqualTo(5);
            assertThat(command.currentRequestCount()).isEqualTo(10);
            assertThat(command.currentUploadCount()).isEqualTo(100);
        }

        @Test
        @DisplayName("FileType이 null인 경우 예외 발생")
        void createCommand_NullFileType_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> new ValidateUploadPolicyCommand(
                    "tenant-1",
                    "CONSUMER",
                    "REVIEW",
                    null, // null FileType
                    "jpg",
                    1024L,
                    5,
                    null,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FileType must not be null");
        }

        @Test
        @DisplayName("FileSizeBytes가 0인 경우 예외 발생")
        void createCommand_ZeroFileSize_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> new ValidateUploadPolicyCommand(
                    "tenant-1",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    "jpg",
                    0L, // 0 bytes
                    5,
                    null,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FileSizeBytes must be greater than 0");
        }

        @Test
        @DisplayName("FileSizeBytes가 음수인 경우 예외 발생")
        void createCommand_NegativeFileSize_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> new ValidateUploadPolicyCommand(
                    "tenant-1",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    "jpg",
                    -1024L, // negative bytes
                    5,
                    null,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FileSizeBytes must be greater than 0");
        }

        @Test
        @DisplayName("FileCount가 0인 경우 예외 발생")
        void createCommand_ZeroFileCount_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> new ValidateUploadPolicyCommand(
                    "tenant-1",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    "jpg",
                    1024L,
                    0, // 0 files
                    null,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FileCount must be greater than 0");
        }

        @Test
        @DisplayName("FileCount가 음수인 경우 예외 발생")
        void createCommand_NegativeFileCount_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> new ValidateUploadPolicyCommand(
                    "tenant-1",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    "jpg",
                    1024L,
                    -5, // negative count
                    null,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FileCount must be greater than 0");
        }

        @Test
        @DisplayName("Optional 필드(fileFormat, currentRequestCount, currentUploadCount)가 null인 경우 정상 생성")
        void createCommand_NullOptionalFields_Success() {
            // When
            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "tenant-1",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    null, // null fileFormat
                    1024L,
                    5,
                    null, // null currentRequestCount
                    null  // null currentUploadCount
            );

            // Then
            assertThat(command).isNotNull();
            assertThat(command.fileFormat()).isNull();
            assertThat(command.currentRequestCount()).isNull();
            assertThat(command.currentUploadCount()).isNull();
        }
    }

    @Nested
    @DisplayName("Rate Limiting 검증 필요 여부")
    class ShouldValidateRateLimit {

        @Test
        @DisplayName("currentRequestCount와 currentUploadCount가 모두 있으면 true")
        void shouldValidateRateLimit_BothPresent_ReturnsTrue() {
            // Given
            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "tenant-1",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    "jpg",
                    1024L,
                    5,
                    10,   // currentRequestCount present
                    100   // currentUploadCount present
            );

            // When & Then
            assertThat(command.shouldValidateRateLimit()).isTrue();
        }

        @Test
        @DisplayName("currentRequestCount만 있으면 false")
        void shouldValidateRateLimit_OnlyRequestCount_ReturnsFalse() {
            // Given
            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "tenant-1",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    "jpg",
                    1024L,
                    5,
                    10,   // currentRequestCount present
                    null  // currentUploadCount null
            );

            // When & Then
            assertThat(command.shouldValidateRateLimit()).isFalse();
        }

        @Test
        @DisplayName("currentUploadCount만 있으면 false")
        void shouldValidateRateLimit_OnlyUploadCount_ReturnsFalse() {
            // Given
            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "tenant-1",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    "jpg",
                    1024L,
                    5,
                    null, // currentRequestCount null
                    100   // currentUploadCount present
            );

            // When & Then
            assertThat(command.shouldValidateRateLimit()).isFalse();
        }

        @Test
        @DisplayName("currentRequestCount와 currentUploadCount가 모두 없으면 false")
        void shouldValidateRateLimit_BothAbsent_ReturnsFalse() {
            // Given
            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "tenant-1",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    "jpg",
                    1024L,
                    5,
                    null, // currentRequestCount null
                    null  // currentUploadCount null
            );

            // When & Then
            assertThat(command.shouldValidateRateLimit()).isFalse();
        }

        @Test
        @DisplayName("currentRequestCount가 0이고 currentUploadCount가 있으면 true (값이 0이어도 검증 필요)")
        void shouldValidateRateLimit_ZeroRequestCount_ReturnsTrue() {
            // Given
            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "tenant-1",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    "jpg",
                    1024L,
                    5,
                    0,    // currentRequestCount 0 (but not null)
                    100   // currentUploadCount present
            );

            // When & Then
            assertThat(command.shouldValidateRateLimit()).isTrue();
        }

        @Test
        @DisplayName("currentUploadCount가 0이고 currentRequestCount가 있으면 true (값이 0이어도 검증 필요)")
        void shouldValidateRateLimit_ZeroUploadCount_ReturnsTrue() {
            // Given
            ValidateUploadPolicyCommand command = new ValidateUploadPolicyCommand(
                    "tenant-1",
                    "CONSUMER",
                    "REVIEW",
                    FileType.IMAGE,
                    "jpg",
                    1024L,
                    5,
                    10,   // currentRequestCount present
                    0     // currentUploadCount 0 (but not null)
            );

            // When & Then
            assertThat(command.shouldValidateRateLimit()).isTrue();
        }
    }
}
