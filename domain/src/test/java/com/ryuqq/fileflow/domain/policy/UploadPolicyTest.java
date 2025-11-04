package com.ryuqq.fileflow.domain.policy;

import com.ryuqq.fileflow.domain.policy.fixture.FileMetadataFixture;
import com.ryuqq.fileflow.domain.policy.fixture.UploadPolicyFixture;
import com.ryuqq.fileflow.domain.upload.FileName;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * UploadPolicy Domain 단위 테스트
 *
 * @author Sangwon Ryu
 * @since 2025-10-31
 */
@DisplayName("UploadPolicy Domain 단위 테스트")
class UploadPolicyTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTests {

        @Test
        @DisplayName("기본 UploadPolicy 생성 성공")
        void createDefault_Success() {
            // When
            UploadPolicy policy = UploadPolicyFixture.createDefault();

            // Then
            assertThat(policy.isActive()).isTrue();
            assertThat(policy.getPolicyName()).isNotNull();
        }

        @Test
        @DisplayName("이미지 전용 Policy 생성 성공")
        void createImageOnly_Success() {
            // When
            UploadPolicy policy = UploadPolicyFixture.createImageOnly();

            // Then
            assertThat(policy.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("정책 평가 테스트")
    class EvaluationTests {

        @Test
        @DisplayName("허용된 MIME 타입이면 PASSED")
        void evaluate_PassedWhenAllowedMimeType() {
            // Given
            UploadPolicy policy = UploadPolicyFixture.createImageOnly();
            FileMetadata metadata = FileMetadataFixture.createJpgImage();

            // When
            PolicyEvaluationResult result = policy.evaluate(metadata);

            // Then
            assertThat(result.status()).isEqualTo(EvaluationStatus.PASSED);
            assertThat(result.violations()).isEmpty();
        }

        @Test
        @DisplayName("허용되지 않은 MIME 타입이면 FAILED")
        void evaluate_FailedWhenDisallowedMimeType() {
            // Given
            UploadPolicy policy = UploadPolicyFixture.createImageOnly();
            FileMetadata metadata = FileMetadataFixture.createPdfDocument();

            // When
            PolicyEvaluationResult result = policy.evaluate(metadata);

            // Then
            assertThat(result.status()).isEqualTo(EvaluationStatus.FAILED);
            assertThat(result.violations()).isNotEmpty();
        }

        @Test
        @DisplayName("파일 크기가 최대값을 초과하면 FAILED")
        void evaluate_FailedWhenExceedsMaxFileSize() {
            // Given
            UploadPolicy policy = UploadPolicyFixture.createImageOnly(); // 5MB 제한
            FileMetadata metadata = FileMetadataFixture.createLargeFile(); // 1GB

            // When
            PolicyEvaluationResult result = policy.evaluate(metadata);

            // Then
            assertThat(result.status()).isEqualTo(EvaluationStatus.FAILED);
            assertThat(result.violations()).anyMatch(v -> v.contains("최대 파일 크기"));
        }

        @Test
        @DisplayName("파일 크기가 최소값 미만이면 FAILED")
        void evaluate_FailedWhenBelowMinFileSize() {
            // Given
            UploadPolicy policy = UploadPolicyFixture.createDefault(); // 1KB 최소
            FileMetadata metadata = FileMetadataFixture.create(
                com.ryuqq.fileflow.domain.upload.FileName.of("tiny.txt"),
                MimeType.of("text/plain"),
                com.ryuqq.fileflow.domain.upload.FileSize.of(100L)
            ); // 100 bytes

            // When
            PolicyEvaluationResult result = policy.evaluate(metadata);

            // Then
            assertThat(result.status()).isEqualTo(EvaluationStatus.FAILED);
            assertThat(result.violations()).anyMatch(v -> v.contains("최소 파일 크기"));
        }
    }

    @Nested
    @DisplayName("정책 활성화/비활성화 테스트")
    class ActivationTests {

        @Test
        @DisplayName("정책 활성화 성공")
        void activate_Success() {
            // Given
            UploadPolicy policy = UploadPolicyFixture.createInactive();

            // When
            policy.activate();

            // Then
            assertThat(policy.isActive()).isTrue();
        }

        @Test
        @DisplayName("정책 비활성화 성공")
        void deactivate_Success() {
            // Given
            UploadPolicy policy = UploadPolicyFixture.createDefault();

            // When
            policy.deactivate();

            // Then
            assertThat(policy.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("PolicyRules Builder 테스트")
    class PolicyRulesBuilderTests {

        @Test
        @DisplayName("PolicyRules Builder로 생성 성공")
        void builder_CreatesRules() {
            // When
            UploadPolicy.PolicyRules rules = UploadPolicy.PolicyRules.builder()
                .allowMimeTypes("image/jpeg")
                .maxFileSize(10485760L)
                .minFileSize(1024L)
                .allowExtensions(".jpg")
                .requireScan()
                .build();

            // Then
            assertThat(rules).isNotNull();
            assertThat(rules.getAllowedMimeTypes()).contains("image/jpeg");
            assertThat(rules.getMaxFileSize()).isEqualTo(10485760L);
            assertThat(rules.getScanRequired()).isTrue();
        }
    }
}
