package com.ryuqq.fileflow.integration.test.e2e.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.adapter.out.persistence.session.SingleUploadSessionJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.SingleUploadSessionJpaRepository;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.vo.SingleSessionStatus;
import com.ryuqq.fileflow.sdk.exception.FileFlowConflictException;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.session.CompleteSingleUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.CreateSingleUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.SingleUploadSessionResponse;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * SDK를 통한 단건 업로드 세션 통합 테스트.
 *
 * <p>FileFlowClient SDK를 사용하여 실제 서버에 요청을 보내고 SDK 모델의 직렬화/역직렬화, 에러 핸들링을 검증합니다.
 */
@DisplayName("SDK - Single Upload Session 통합 테스트")
class SingleUploadSessionSdkTest extends SdkTestBase {

    @Autowired private SingleUploadSessionJpaRepository singleUploadSessionJpaRepository;

    @BeforeEach
    void setUp() {
        singleUploadSessionJpaRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("세션 생성")
    class CreateTest {

        @Test
        @DisplayName("유효한 요청으로 세션을 생성하면 세션 정보가 반환된다")
        void shouldCreateSession() {
            // given
            var request =
                    new CreateSingleUploadSessionRequest(
                            "test-image.jpg",
                            "image/jpeg",
                            "PUBLIC",
                            "PRODUCT_IMAGE",
                            "commerce-api");

            // when
            ApiResponse<SingleUploadSessionResponse> response =
                    client.singleUploadSession().create(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.data()).isNotNull();

            SingleUploadSessionResponse session = response.data();
            assertThat(session.sessionId()).isNotBlank();
            assertThat(session.presignedUrl()).isNotBlank();
            assertThat(session.s3Key()).isNotBlank();
            assertThat(session.bucket()).isNotBlank();
            assertThat(session.accessType()).isEqualTo("PUBLIC");
            assertThat(session.fileName()).isEqualTo("test-image.jpg");
            assertThat(session.contentType()).isEqualTo("image/jpeg");
            assertThat(session.status()).isEqualTo("CREATED");
            assertThat(session.expiresAt()).isNotBlank();
            assertThat(session.createdAt()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("세션 조회")
    class GetTest {

        @Test
        @DisplayName("존재하는 세션을 조회하면 세션 정보가 반환된다")
        void shouldGetSession() {
            // given
            SingleUploadSessionJpaEntity entity =
                    singleUploadSessionJpaRepository.save(
                            SingleUploadSessionJpaEntityFixture.aCreatedEntity());

            // when
            ApiResponse<SingleUploadSessionResponse> response =
                    client.singleUploadSession().get(entity.getId());

            // then
            SingleUploadSessionResponse session = response.data();
            assertThat(session.sessionId()).isEqualTo(entity.getId());
            assertThat(session.s3Key()).isEqualTo(entity.getS3Key());
            assertThat(session.bucket()).isEqualTo(entity.getBucket());
            assertThat(session.status()).isEqualTo("CREATED");
        }

        @Test
        @DisplayName("존재하지 않는 세션을 조회하면 FileFlowNotFoundException이 발생한다")
        void shouldThrowNotFoundWhenSessionNotExists() {
            assertThatThrownBy(() -> client.singleUploadSession().get("non-existent-id"))
                    .isInstanceOf(FileFlowNotFoundException.class)
                    .satisfies(
                            ex -> {
                                FileFlowNotFoundException e = (FileFlowNotFoundException) ex;
                                assertThat(e.getStatusCode()).isEqualTo(404);
                                assertThat(e.getErrorCode()).isEqualTo("SESSION-001");
                            });
        }
    }

    @Nested
    @DisplayName("세션 완료")
    class CompleteTest {

        @Test
        @DisplayName("CREATED 상태 세션을 완료하면 정상 처리된다")
        void shouldCompleteSession() {
            // given
            Instant now = Instant.now();
            SingleUploadSessionJpaEntity entity =
                    singleUploadSessionJpaRepository.save(
                            SingleUploadSessionJpaEntity.create(
                                    "sdk-complete-test",
                                    "public/2026/01/file.jpg",
                                    "fileflow-test-bucket",
                                    AccessType.PUBLIC,
                                    "test.jpg",
                                    "image/jpeg",
                                    "https://s3.presigned-url.com/test",
                                    "product-image",
                                    "commerce-service",
                                    SingleSessionStatus.CREATED,
                                    now.plus(Duration.ofHours(1)),
                                    now,
                                    now));

            var request = new CompleteSingleUploadSessionRequest(1048576L, "\"abc123\"");

            // when - 예외 없이 정상 완료
            client.singleUploadSession().complete(entity.getId(), request);

            // then - DB 상태 검증
            var updated = singleUploadSessionJpaRepository.findById(entity.getId()).orElseThrow();
            assertThat(updated.getStatus().name()).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("이미 완료된 세션을 재완료하면 FileFlowConflictException이 발생한다")
        void shouldThrowConflictWhenAlreadyCompleted() {
            // given
            SingleUploadSessionJpaEntity entity =
                    singleUploadSessionJpaRepository.save(
                            SingleUploadSessionJpaEntityFixture.aCompletedEntity());

            var request = new CompleteSingleUploadSessionRequest(1048576L, "\"abc123\"");

            // when & then
            assertThatThrownBy(() -> client.singleUploadSession().complete(entity.getId(), request))
                    .isInstanceOf(FileFlowConflictException.class)
                    .satisfies(
                            ex -> {
                                FileFlowConflictException e = (FileFlowConflictException) ex;
                                assertThat(e.getStatusCode()).isEqualTo(409);
                                assertThat(e.getErrorCode()).isEqualTo("SESSION-002");
                            });
        }

        @Test
        @DisplayName("존재하지 않는 세션을 완료하면 FileFlowNotFoundException이 발생한다")
        void shouldThrowNotFoundWhenSessionNotExists() {
            var request = new CompleteSingleUploadSessionRequest(1048576L, "\"abc123\"");

            assertThatThrownBy(() -> client.singleUploadSession().complete("non-existent", request))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("전체 플로우")
    class FullFlowTest {

        @Test
        @DisplayName("생성 -> 조회 -> 완료 -> 재조회 전체 플로우")
        void shouldCompleteFullFlow() {
            // Step 1: 세션 생성
            var createRequest =
                    new CreateSingleUploadSessionRequest(
                            "flow-test.jpg",
                            "image/jpeg",
                            "PUBLIC",
                            "PRODUCT_IMAGE",
                            "commerce-api");
            ApiResponse<SingleUploadSessionResponse> createResponse =
                    client.singleUploadSession().create(createRequest);

            String sessionId = createResponse.data().sessionId();
            assertThat(sessionId).isNotBlank();
            assertThat(createResponse.data().status()).isEqualTo("CREATED");

            // Step 2: 조회 - CREATED 상태
            ApiResponse<SingleUploadSessionResponse> getResponse =
                    client.singleUploadSession().get(sessionId);
            assertThat(getResponse.data().sessionId()).isEqualTo(sessionId);
            assertThat(getResponse.data().status()).isEqualTo("CREATED");

            // Step 3: 완료
            var completeRequest = new CompleteSingleUploadSessionRequest(1048576L, "\"etag\"");
            client.singleUploadSession().complete(sessionId, completeRequest);

            // Step 4: 재조회 - COMPLETED 상태
            ApiResponse<SingleUploadSessionResponse> afterComplete =
                    client.singleUploadSession().get(sessionId);
            assertThat(afterComplete.data().status()).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("완료 후 재완료 시 FileFlowConflictException이 발생한다")
        void shouldThrowConflictOnDoubleComplete() {
            // Step 1: 세션 생성
            var createRequest =
                    new CreateSingleUploadSessionRequest(
                            "double-complete.jpg",
                            "image/jpeg",
                            "PUBLIC",
                            "PRODUCT_IMAGE",
                            "commerce-api");
            String sessionId =
                    client.singleUploadSession().create(createRequest).data().sessionId();

            // Step 2: 첫 번째 완료 - 성공
            var completeRequest = new CompleteSingleUploadSessionRequest(1048576L, "\"etag\"");
            client.singleUploadSession().complete(sessionId, completeRequest);

            // Step 3: 두 번째 완료 - 409
            assertThatThrownBy(
                            () -> client.singleUploadSession().complete(sessionId, completeRequest))
                    .isInstanceOf(FileFlowConflictException.class);
        }
    }
}
