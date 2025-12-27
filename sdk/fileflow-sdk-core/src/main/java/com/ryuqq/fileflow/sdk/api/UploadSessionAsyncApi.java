package com.ryuqq.fileflow.sdk.api;

import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadRequest;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadResponse;
import reactor.core.publisher.Mono;

/** Async API for upload session operations using reactive types. */
public interface UploadSessionAsyncApi {

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

    /**
     * Cancels an upload session.
     *
     * @param sessionId the upload session ID
     * @return Mono completing when done
     */
    Mono<Void> cancel(String sessionId);
}
