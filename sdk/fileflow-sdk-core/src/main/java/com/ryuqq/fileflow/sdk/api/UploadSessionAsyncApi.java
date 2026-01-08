package com.ryuqq.fileflow.sdk.api;

import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import com.ryuqq.fileflow.sdk.model.session.CancelUploadSessionResponse;
import com.ryuqq.fileflow.sdk.model.session.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.sdk.model.session.InitMultipartUploadRequest;
import com.ryuqq.fileflow.sdk.model.session.InitMultipartUploadResponse;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadRequest;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadResponse;
import com.ryuqq.fileflow.sdk.model.session.MarkPartUploadedRequest;
import com.ryuqq.fileflow.sdk.model.session.MarkPartUploadedResponse;
import com.ryuqq.fileflow.sdk.model.session.UploadSessionDetailResponse;
import com.ryuqq.fileflow.sdk.model.session.UploadSessionResponse;
import com.ryuqq.fileflow.sdk.model.session.UploadSessionSearchRequest;
import reactor.core.publisher.Mono;

/**
 * Async API for upload session operations using reactive types.
 *
 * <p>Provides non-blocking operations for managing file uploads including:
 *
 * <ul>
 *   <li>Single file upload with presigned URL
 *   <li>Multipart upload for large files
 *   <li>Upload session completion and cancellation
 *   <li>Session retrieval and listing
 * </ul>
 */
public interface UploadSessionAsyncApi {

    // ==================== Single Upload ====================

    /**
     * Initializes a single file upload session.
     *
     * @param request the upload initialization request
     * @return Mono emitting the upload session with presigned URL
     */
    Mono<InitSingleUploadResponse> initSingle(InitSingleUploadRequest request);

    /**
     * Completes a single file upload session.
     *
     * @param sessionId the upload session ID
     * @return Mono completing when done
     */
    Mono<Void> completeSingle(String sessionId);

    // ==================== Multipart Upload ====================

    /**
     * Initializes a multipart upload session.
     *
     * @param request the multipart upload initialization request
     * @return Mono emitting the upload session with presigned URLs for each part
     */
    Mono<InitMultipartUploadResponse> initMultipart(InitMultipartUploadRequest request);

    /**
     * Marks a part as uploaded in a multipart upload session.
     *
     * @param sessionId the upload session ID
     * @param request the part upload information
     * @return Mono emitting the part upload confirmation
     */
    Mono<MarkPartUploadedResponse> markPartUploaded(String sessionId, MarkPartUploadedRequest request);

    /**
     * Completes a multipart upload session.
     *
     * @param sessionId the upload session ID
     * @return Mono emitting the completed upload information
     */
    Mono<CompleteMultipartUploadResponse> completeMultipart(String sessionId);

    // ==================== Session Management ====================

    /**
     * Retrieves an upload session by ID.
     *
     * @param sessionId the upload session ID
     * @return Mono emitting the session details
     */
    Mono<UploadSessionDetailResponse> get(String sessionId);

    /**
     * Lists upload sessions with optional filtering.
     *
     * @param request the search criteria
     * @return Mono emitting paginated list of sessions
     */
    Mono<PageResponse<UploadSessionResponse>> list(UploadSessionSearchRequest request);

    /**
     * Cancels an upload session.
     *
     * @param sessionId the upload session ID
     * @return Mono emitting the cancelled session information
     */
    Mono<CancelUploadSessionResponse> cancel(String sessionId);
}
