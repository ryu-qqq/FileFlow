package com.ryuqq.fileflow.integration.test.e2e.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.adapter.out.persistence.session.MultipartUploadSessionJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.CompletedPartJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.MultipartUploadSessionJpaRepository;
import com.ryuqq.fileflow.domain.session.vo.MultipartSessionStatus;
import com.ryuqq.fileflow.sdk.exception.FileFlowConflictException;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.session.AddCompletedPartRequest;
import com.ryuqq.fileflow.sdk.model.session.CompleteMultipartUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.CreateMultipartUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.sdk.model.session.PresignedPartUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * SDK를 통한 멀티파트 업로드 세션 통합 테스트.
 *
 * <p>FileFlowClient SDK를 사용하여 실제 서버에 요청을 보내고 SDK 모델의 직렬화/역직렬화, 에러 핸들링을 검증합니다.
 */
@DisplayName("SDK - Multipart Upload Session 통합 테스트")
class MultipartUploadSessionSdkTest extends SdkTestBase {

    @Autowired private MultipartUploadSessionJpaRepository multipartUploadSessionJpaRepository;

    @Autowired private CompletedPartJpaRepository completedPartJpaRepository;

    @BeforeEach
    void setUp() {
        completedPartJpaRepository.deleteAllInBatch();
        multipartUploadSessionJpaRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("세션 생성")
    class CreateTest {

        @Test
        @DisplayName("유효한 요청으로 세션을 생성하면 세션 정보가 반환된다")
        void shouldCreateSession() {
            // given
            var request =
                    new CreateMultipartUploadSessionRequest(
                            "large-video.mp4",
                            "video/mp4",
                            "PUBLIC",
                            5242880L,
                            "VIDEO_UPLOAD",
                            "commerce-api");

            // when
            ApiResponse<MultipartUploadSessionResponse> response =
                    client.multipartUploadSession().create(request);

            // then
            MultipartUploadSessionResponse session = response.data();
            assertThat(session.sessionId()).isNotBlank();
            assertThat(session.uploadId()).isNotBlank();
            assertThat(session.s3Key()).isNotBlank();
            assertThat(session.bucket()).isNotBlank();
            assertThat(session.accessType()).isEqualTo("PUBLIC");
            assertThat(session.fileName()).isEqualTo("large-video.mp4");
            assertThat(session.contentType()).isEqualTo("video/mp4");
            assertThat(session.partSize()).isEqualTo(5242880L);
            assertThat(session.status()).isEqualTo("INITIATED");
            assertThat(session.completedPartCount()).isZero();
            assertThat(session.completedParts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("세션 조회")
    class GetTest {

        @Test
        @DisplayName("존재하는 세션을 조회하면 세션 정보가 반환된다")
        void shouldGetSession() {
            // given
            MultipartUploadSessionJpaEntity entity =
                    multipartUploadSessionJpaRepository.save(
                            MultipartUploadSessionJpaEntityFixture.anInitiatedEntity());

            // when
            ApiResponse<MultipartUploadSessionResponse> response =
                    client.multipartUploadSession().get(entity.getId());

            // then
            MultipartUploadSessionResponse session = response.data();
            assertThat(session.sessionId()).isEqualTo(entity.getId());
            assertThat(session.uploadId()).isEqualTo(entity.getUploadId());
            assertThat(session.status()).isEqualTo("INITIATED");
            assertThat(session.completedPartCount()).isZero();
            assertThat(session.completedParts()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 세션을 조회하면 FileFlowNotFoundException이 발생한다")
        void shouldThrowNotFoundWhenSessionNotExists() {
            assertThatThrownBy(() -> client.multipartUploadSession().get("non-existent-id"))
                    .isInstanceOf(FileFlowNotFoundException.class)
                    .satisfies(
                            ex -> {
                                FileFlowNotFoundException e = (FileFlowNotFoundException) ex;
                                assertThat(e.getErrorCode()).isEqualTo("SESSION-001");
                            });
        }
    }

    @Nested
    @DisplayName("Presigned URL 발급")
    class PresignedUrlTest {

        @Test
        @DisplayName("INITIATED 상태 세션의 파트 URL을 발급하면 Presigned URL이 반환된다")
        void shouldReturnPresignedUrl() {
            // given - API로 세션 생성 (LocalStack S3에 실제 업로드 세션 존재)
            var createRequest =
                    new CreateMultipartUploadSessionRequest(
                            "test.mp4",
                            "video/mp4",
                            "PUBLIC",
                            5242880L,
                            "VIDEO_UPLOAD",
                            "commerce-api");
            String sessionId =
                    client.multipartUploadSession().create(createRequest).data().sessionId();

            // when
            ApiResponse<PresignedPartUrlResponse> response =
                    client.multipartUploadSession().getPresignedPartUrl(sessionId, 1);

            // then
            PresignedPartUrlResponse partUrl = response.data();
            assertThat(partUrl.presignedUrl()).isNotBlank();
            assertThat(partUrl.partNumber()).isEqualTo(1);
            assertThat(partUrl.expiresInSeconds()).isPositive();
        }

        @Test
        @DisplayName("존재하지 않는 세션의 파트 URL 발급 시 FileFlowNotFoundException이 발생한다")
        void shouldThrowNotFoundWhenSessionNotExists() {
            assertThatThrownBy(
                            () ->
                                    client.multipartUploadSession()
                                            .getPresignedPartUrl("non-existent", 1))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("파트 추가")
    class AddPartTest {

        @Test
        @DisplayName("INITIATED 세션에 파트를 추가하면 정상 처리된다")
        void shouldAddPart() {
            // given
            var createRequest =
                    new CreateMultipartUploadSessionRequest(
                            "test.mp4",
                            "video/mp4",
                            "PUBLIC",
                            5242880L,
                            "VIDEO_UPLOAD",
                            "commerce-api");
            String sessionId =
                    client.multipartUploadSession().create(createRequest).data().sessionId();

            // when
            var partRequest = new AddCompletedPartRequest(1, "\"part1etag\"", 5242880L);
            client.multipartUploadSession().addCompletedPart(sessionId, partRequest);

            // then - DB 검증
            var updated = multipartUploadSessionJpaRepository.findById(sessionId).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(MultipartSessionStatus.UPLOADING);
            assertThat(completedPartJpaRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("중복 파트번호 추가 시 FileFlowConflictException이 발생한다")
        void shouldThrowConflictOnDuplicatePart() {
            // given
            var createRequest =
                    new CreateMultipartUploadSessionRequest(
                            "test.mp4",
                            "video/mp4",
                            "PUBLIC",
                            5242880L,
                            "VIDEO_UPLOAD",
                            "commerce-api");
            String sessionId =
                    client.multipartUploadSession().create(createRequest).data().sessionId();

            client.multipartUploadSession()
                    .addCompletedPart(
                            sessionId, new AddCompletedPartRequest(1, "\"part1etag\"", 5242880L));

            // when & then
            assertThatThrownBy(
                            () ->
                                    client.multipartUploadSession()
                                            .addCompletedPart(
                                                    sessionId,
                                                    new AddCompletedPartRequest(
                                                            1, "\"duplicate\"", 5242880L)))
                    .isInstanceOf(FileFlowConflictException.class)
                    .satisfies(
                            ex -> {
                                FileFlowConflictException e = (FileFlowConflictException) ex;
                                assertThat(e.getErrorCode()).isEqualTo("SESSION-006");
                            });
        }
    }

    @Nested
    @DisplayName("세션 완료")
    class CompleteTest {

        @Test
        @DisplayName("파트가 있는 세션을 완료하면 정상 처리된다")
        void shouldCompleteSession() {
            // given
            String sessionId = createSessionAndAddParts(3);

            // when
            var completeRequest =
                    new CompleteMultipartUploadSessionRequest(15728640L, "\"combined-etag\"");
            client.multipartUploadSession().complete(sessionId, completeRequest);

            // then - DB 검증
            var updated = multipartUploadSessionJpaRepository.findById(sessionId).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(MultipartSessionStatus.COMPLETED);
        }

        @Test
        @DisplayName("이미 완료된 세션을 재완료하면 FileFlowConflictException이 발생한다")
        void shouldThrowConflictWhenAlreadyCompleted() {
            // given
            MultipartUploadSessionJpaEntity entity =
                    multipartUploadSessionJpaRepository.save(
                            MultipartUploadSessionJpaEntityFixture.aCompletedEntity());

            var completeRequest = new CompleteMultipartUploadSessionRequest(15728640L, "\"etag\"");

            // when & then
            assertThatThrownBy(
                            () ->
                                    client.multipartUploadSession()
                                            .complete(entity.getId(), completeRequest))
                    .isInstanceOf(FileFlowConflictException.class)
                    .satisfies(
                            ex -> {
                                FileFlowConflictException e = (FileFlowConflictException) ex;
                                assertThat(e.getErrorCode()).isEqualTo("SESSION-002");
                            });
        }
    }

    @Nested
    @DisplayName("세션 중단")
    class AbortTest {

        @Test
        @DisplayName("INITIATED 세션을 중단하면 정상 처리된다")
        void shouldAbortSession() {
            // given
            var createRequest =
                    new CreateMultipartUploadSessionRequest(
                            "abort-test.mp4",
                            "video/mp4",
                            "PUBLIC",
                            5242880L,
                            "VIDEO_UPLOAD",
                            "commerce-api");
            String sessionId =
                    client.multipartUploadSession().create(createRequest).data().sessionId();

            // when
            client.multipartUploadSession().abort(sessionId);

            // then - DB 검증
            var updated = multipartUploadSessionJpaRepository.findById(sessionId).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(MultipartSessionStatus.ABORTED);
        }

        @Test
        @DisplayName("존재하지 않는 세션을 중단하면 FileFlowNotFoundException이 발생한다")
        void shouldThrowNotFoundWhenSessionNotExists() {
            assertThatThrownBy(() -> client.multipartUploadSession().abort("non-existent"))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("전체 플로우")
    class FullFlowTest {

        @Test
        @DisplayName("생성 -> Presigned URL -> 파트 추가 -> 조회 -> 완료 -> 재조회 전체 플로우")
        void shouldCompleteFullMultipartFlow() {
            // Step 1: 세션 생성
            var createRequest =
                    new CreateMultipartUploadSessionRequest(
                            "full-flow.mp4",
                            "video/mp4",
                            "PUBLIC",
                            5242880L,
                            "VIDEO_UPLOAD",
                            "commerce-api");
            ApiResponse<MultipartUploadSessionResponse> createResponse =
                    client.multipartUploadSession().create(createRequest);
            String sessionId = createResponse.data().sessionId();
            assertThat(createResponse.data().status()).isEqualTo("INITIATED");

            // Step 2: Presigned URL 발급
            ApiResponse<PresignedPartUrlResponse> presignedResponse =
                    client.multipartUploadSession().getPresignedPartUrl(sessionId, 1);
            assertThat(presignedResponse.data().presignedUrl()).isNotBlank();

            // Step 3: 파트 1 추가
            client.multipartUploadSession()
                    .addCompletedPart(
                            sessionId, new AddCompletedPartRequest(1, "\"part1etag\"", 5242880L));

            // Step 4: 파트 2 추가
            client.multipartUploadSession()
                    .addCompletedPart(
                            sessionId, new AddCompletedPartRequest(2, "\"part2etag\"", 5242880L));

            // Step 5: 조회 - UPLOADING, 파트 2건
            ApiResponse<MultipartUploadSessionResponse> getResponse =
                    client.multipartUploadSession().get(sessionId);
            assertThat(getResponse.data().status()).isEqualTo("UPLOADING");
            assertThat(getResponse.data().completedPartCount()).isEqualTo(2);
            assertThat(getResponse.data().completedParts()).hasSize(2);

            // Step 6: 세션 완료
            client.multipartUploadSession()
                    .complete(
                            sessionId,
                            new CompleteMultipartUploadSessionRequest(10485760L, "\"combined\""));

            // Step 7: 재조회 - COMPLETED
            ApiResponse<MultipartUploadSessionResponse> afterComplete =
                    client.multipartUploadSession().get(sessionId);
            assertThat(afterComplete.data().status()).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("생성 -> 파트 추가 -> 중단 -> 파트 추가 시도 실패 플로우")
        void shouldAbortFlowThenFailOnAddPart() {
            // Step 1: 세션 생성
            var createRequest =
                    new CreateMultipartUploadSessionRequest(
                            "abort-flow.mp4",
                            "video/mp4",
                            "PUBLIC",
                            5242880L,
                            "VIDEO_UPLOAD",
                            "commerce-api");
            String sessionId =
                    client.multipartUploadSession().create(createRequest).data().sessionId();

            // Step 2: 파트 1 추가
            client.multipartUploadSession()
                    .addCompletedPart(
                            sessionId, new AddCompletedPartRequest(1, "\"part1etag\"", 5242880L));

            // Step 3: 세션 중단
            client.multipartUploadSession().abort(sessionId);

            // Step 4: 재조회 - ABORTED
            assertThat(client.multipartUploadSession().get(sessionId).data().status())
                    .isEqualTo("ABORTED");

            // Step 5: 중단 후 파트 추가 시도 - 409
            assertThatThrownBy(
                            () ->
                                    client.multipartUploadSession()
                                            .addCompletedPart(
                                                    sessionId,
                                                    new AddCompletedPartRequest(
                                                            2, "\"part2etag\"", 5242880L)))
                    .isInstanceOf(FileFlowConflictException.class)
                    .satisfies(
                            ex -> {
                                FileFlowConflictException e = (FileFlowConflictException) ex;
                                assertThat(e.getErrorCode()).isEqualTo("SESSION-004");
                            });
        }
    }

    // ========================================
    // Helper 메서드
    // ========================================

    private String createSessionAndAddParts(int partCount) {
        var createRequest =
                new CreateMultipartUploadSessionRequest(
                        "parts-test.mp4",
                        "video/mp4",
                        "PUBLIC",
                        5242880L,
                        "VIDEO_UPLOAD",
                        "commerce-api");
        String sessionId = client.multipartUploadSession().create(createRequest).data().sessionId();

        for (int i = 1; i <= partCount; i++) {
            client.multipartUploadSession()
                    .addCompletedPart(
                            sessionId,
                            new AddCompletedPartRequest(i, "\"part" + i + "etag\"", 5242880L));
        }

        return sessionId;
    }
}
