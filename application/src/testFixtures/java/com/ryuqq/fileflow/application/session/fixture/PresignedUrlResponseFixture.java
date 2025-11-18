package com.ryuqq.fileflow.application.session.fixture;

import com.ryuqq.fileflow.application.session.dto.response.PresignedUrlResponse;

/**
 * PresignedUrlResponse TestFixture (Object Mother 패턴)
 * <p>
 * MVP Scope: Single Presigned URL Upload
 * </p>
 */
public class PresignedUrlResponseFixture {

    public static PresignedUrlResponse aResponse() {
        return new PresignedUrlResponse(
                "01JD8000-1234-5678-9abc-def012345678",
                "01JD8001-1234-5678-9abc-def012345678",
                "https://fileflow-uploads-1.s3.amazonaws.com/uploads/1/admin/connectly/banner/01JD8001_example.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256",
                300,
                "SINGLE"
        );
    }

    public static PresignedUrlResponse create() {
        return aResponse();
    }

    public static PresignedUrlResponse withSessionId(String sessionId) {
        return new PresignedUrlResponse(
                sessionId,
                "01JD8001-1234-5678-9abc-def012345678",
                "https://fileflow-uploads-1.s3.amazonaws.com/uploads/1/admin/connectly/banner/01JD8001_example.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256",
                300,
                "SINGLE"
        );
    }

    public static PresignedUrlResponse withFileId(String fileId) {
        return new PresignedUrlResponse(
                "01JD8000-1234-5678-9abc-def012345678",
                fileId,
                "https://fileflow-uploads-1.s3.amazonaws.com/uploads/1/admin/connectly/banner/01JD8001_example.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256",
                300,
                "SINGLE"
        );
    }

    public static PresignedUrlResponse withPresignedUrl(String presignedUrl) {
        return new PresignedUrlResponse(
                "01JD8000-1234-5678-9abc-def012345678",
                "01JD8001-1234-5678-9abc-def012345678",
                presignedUrl,
                300,
                "SINGLE"
        );
    }
}
