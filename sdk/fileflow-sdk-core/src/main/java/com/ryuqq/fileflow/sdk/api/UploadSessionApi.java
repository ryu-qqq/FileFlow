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

/**
 * API for upload session operations.
 *
 * <p>Provides operations for managing file uploads including:
 *
 * <ul>
 *   <li>Single file upload with presigned URL
 *   <li>Multipart upload for large files
 *   <li>Upload session completion and cancellation
 *   <li>Session retrieval and listing
 * </ul>
 */
public interface UploadSessionApi {

    // ==================== Single Upload ====================

    /**
     * Initializes a single file upload session.
     *
     * <p>Returns a presigned PUT URL that can be used to upload the file directly to S3.
     *
     * @param request the upload initialization request
     * @return the upload session with presigned URL
     */
    InitSingleUploadResponse initSingle(InitSingleUploadRequest request);

    /**
     * Completes a single file upload session.
     *
     * <p>Call this after successfully uploading the file to the presigned URL.
     *
     * @param sessionId the upload session ID
     */
    void completeSingle(String sessionId);

    // ==================== Multipart Upload ====================

    /**
     * Initializes a multipart upload session.
     *
     * <p>Returns presigned URLs for each part that can be used to upload parts directly to S3.
     *
     * @param request the multipart upload initialization request
     * @return the upload session with presigned URLs for each part
     */
    InitMultipartUploadResponse initMultipart(InitMultipartUploadRequest request);

    /**
     * Marks a part as uploaded in a multipart upload session.
     *
     * <p>Call this after successfully uploading each part to S3. The ETag returned by S3 must be
     * provided.
     *
     * @param sessionId the upload session ID
     * @param request the part upload information
     * @return the part upload confirmation
     */
    MarkPartUploadedResponse markPartUploaded(String sessionId, MarkPartUploadedRequest request);

    /**
     * Completes a multipart upload session.
     *
     * <p>Call this after all parts have been uploaded and marked. This triggers S3 to merge all
     * parts into a single object.
     *
     * @param sessionId the upload session ID
     * @return the completed upload information
     */
    CompleteMultipartUploadResponse completeMultipart(String sessionId);

    // ==================== Session Management ====================

    /**
     * Retrieves an upload session by ID.
     *
     * @param sessionId the upload session ID
     * @return the session details
     */
    UploadSessionDetailResponse get(String sessionId);

    /**
     * Lists upload sessions with optional filtering.
     *
     * @param request the search criteria
     * @return paginated list of sessions
     */
    PageResponse<UploadSessionResponse> list(UploadSessionSearchRequest request);

    /**
     * Cancels an upload session.
     *
     * <p>For multipart uploads, this will abort the S3 multipart upload and clean up any uploaded
     * parts.
     *
     * @param sessionId the upload session ID
     * @return the cancelled session information
     */
    CancelUploadSessionResponse cancel(String sessionId);
}
