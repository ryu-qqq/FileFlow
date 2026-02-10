package com.ryuqq.fileflow.adapter.in.rest.session;

import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.AddCompletedPartApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteMultipartUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteSingleUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CreateMultipartUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CreateSingleUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.MultipartUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.MultipartUploadSessionApiResponse.CompletedPartApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.PresignedPartUrlApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.SingleUploadSessionApiResponse;
import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse.CompletedPartResponse;
import com.ryuqq.fileflow.application.session.dto.response.PresignedPartUrlResponse;
import com.ryuqq.fileflow.application.session.dto.response.SingleUploadSessionResponse;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Instant;
import java.util.List;

/**
 * Session API 테스트 Fixtures.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public final class SessionApiFixtures {

    private SessionApiFixtures() {}

    // ===== 공통 상수 =====
    public static final String SESSION_ID = "sess_test_abc123";
    public static final String UPLOAD_ID = "upload-id-xyz789";
    public static final String S3_KEY = "public/2026/02/product-image.jpg";
    public static final String BUCKET = "fileflow-bucket";
    public static final String FILE_NAME = "product-image.jpg";
    public static final String CONTENT_TYPE = "image/jpeg";
    public static final String PRESIGNED_URL =
            "https://s3.amazonaws.com/fileflow-bucket/presigned?token=abc";
    public static final String ETAG = "\"d41d8cd98f00b204e9800998ecf8427e\"";
    public static final long FILE_SIZE = 1_048_576L;
    public static final long PART_SIZE = 5_242_880L;
    public static final String PURPOSE = "PRODUCT_IMAGE";
    public static final String SOURCE = "commerce-api";
    public static final String STATUS_INITIATED = "INITIATED";
    public static final Instant EXPIRES_AT = Instant.parse("2026-02-10T10:30:00Z");
    public static final Instant CREATED_AT = Instant.parse("2026-02-09T09:30:00Z");

    // ===== Request Fixtures =====

    public static CreateSingleUploadSessionApiRequest createSingleUploadSessionRequest() {
        return new CreateSingleUploadSessionApiRequest(
                FILE_NAME, CONTENT_TYPE, AccessType.PUBLIC, PURPOSE, SOURCE);
    }

    public static CompleteSingleUploadSessionApiRequest completeSingleUploadSessionRequest() {
        return new CompleteSingleUploadSessionApiRequest(FILE_SIZE, ETAG);
    }

    public static CreateMultipartUploadSessionApiRequest createMultipartUploadSessionRequest() {
        return new CreateMultipartUploadSessionApiRequest(
                "large-video.mp4",
                "video/mp4",
                AccessType.PUBLIC,
                PART_SIZE,
                "VIDEO_UPLOAD",
                SOURCE);
    }

    public static CompleteMultipartUploadSessionApiRequest completeMultipartUploadSessionRequest() {
        return new CompleteMultipartUploadSessionApiRequest(52_428_800L, ETAG);
    }

    public static AddCompletedPartApiRequest addCompletedPartRequest() {
        return new AddCompletedPartApiRequest(1, ETAG, PART_SIZE);
    }

    // ===== Application Response Fixtures =====

    public static SingleUploadSessionResponse singleUploadSessionResponse() {
        return new SingleUploadSessionResponse(
                SESSION_ID,
                PRESIGNED_URL,
                S3_KEY,
                BUCKET,
                AccessType.PUBLIC,
                FILE_NAME,
                CONTENT_TYPE,
                STATUS_INITIATED,
                EXPIRES_AT,
                CREATED_AT);
    }

    public static MultipartUploadSessionResponse multipartUploadSessionResponse() {
        return new MultipartUploadSessionResponse(
                SESSION_ID,
                UPLOAD_ID,
                S3_KEY,
                BUCKET,
                AccessType.PUBLIC,
                "large-video.mp4",
                "video/mp4",
                PART_SIZE,
                STATUS_INITIATED,
                2,
                List.of(
                        new CompletedPartResponse(1, ETAG, PART_SIZE),
                        new CompletedPartResponse(2, ETAG, PART_SIZE)),
                EXPIRES_AT,
                CREATED_AT);
    }

    public static PresignedPartUrlResponse presignedPartUrlResponse() {
        return new PresignedPartUrlResponse(PRESIGNED_URL, 1, 3600L);
    }

    // ===== API Response Fixtures =====

    public static SingleUploadSessionApiResponse singleUploadSessionApiResponse() {
        return new SingleUploadSessionApiResponse(
                SESSION_ID,
                PRESIGNED_URL,
                S3_KEY,
                BUCKET,
                "PUBLIC",
                FILE_NAME,
                CONTENT_TYPE,
                STATUS_INITIATED,
                "2026-02-10T19:30:00+09:00",
                "2026-02-09T18:30:00+09:00");
    }

    public static MultipartUploadSessionApiResponse multipartUploadSessionApiResponse() {
        return new MultipartUploadSessionApiResponse(
                SESSION_ID,
                UPLOAD_ID,
                S3_KEY,
                BUCKET,
                "PUBLIC",
                "large-video.mp4",
                "video/mp4",
                PART_SIZE,
                STATUS_INITIATED,
                2,
                List.of(
                        new CompletedPartApiResponse(1, ETAG, PART_SIZE),
                        new CompletedPartApiResponse(2, ETAG, PART_SIZE)),
                "2026-02-10T19:30:00+09:00",
                "2026-02-09T18:30:00+09:00");
    }

    public static PresignedPartUrlApiResponse presignedPartUrlApiResponse() {
        return new PresignedPartUrlApiResponse(PRESIGNED_URL, 1, 3600L);
    }
}
