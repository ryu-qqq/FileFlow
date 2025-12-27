package com.ryuqq.fileflow.sdk.api;

import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadRequest;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadResponse;

/**
 * API for upload session operations.
 *
 * <p>Provides operations for managing file uploads including:
 *
 * <ul>
 *   <li>Single file upload with presigned URL
 *   <li>Multipart upload for large files
 *   <li>Upload session completion and cancellation
 * </ul>
 */
public interface UploadSessionApi {

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

    /**
     * Cancels an upload session.
     *
     * @param sessionId the upload session ID
     */
    void cancel(String sessionId);
}
