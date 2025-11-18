package com.ryuqq.fileflow.application.file.fixture;

import com.ryuqq.fileflow.application.session.dto.response.FileResponse;

import java.time.LocalDateTime;

/**
 * FileResponse TestFixture (Object Mother 패턴)
 * <p>
 * MVP Scope: Single Presigned URL Upload
 * </p>
 */
public class FileResponseFixture {

    public static FileResponse aResponse() {
        return new FileResponse(
                "01JD8000-1234-5678-9abc-def012345678",
                "01JD8001-1234-5678-9abc-def012345678",
                "example.jpg",
                1048576L,
                "image/jpeg",
                "COMPLETED",
                "uploads/1/admin/connectly/banner/01JD8001_example.jpg",
                "fileflow-uploads-1",
                LocalDateTime.now()
        );
    }

    public static FileResponse create() {
        return aResponse();
    }

    public static FileResponse withStatus(String status) {
        return new FileResponse(
                "01JD8000-1234-5678-9abc-def012345678",
                "01JD8001-1234-5678-9abc-def012345678",
                "example.jpg",
                1048576L,
                "image/jpeg",
                status,
                "uploads/1/admin/connectly/banner/01JD8001_example.jpg",
                "fileflow-uploads-1",
                LocalDateTime.now()
        );
    }

    public static FileResponse withFileId(String fileId) {
        return new FileResponse(
                "01JD8000-1234-5678-9abc-def012345678",
                fileId,
                "example.jpg",
                1048576L,
                "image/jpeg",
                "COMPLETED",
                "uploads/1/admin/connectly/banner/01JD8001_example.jpg",
                "fileflow-uploads-1",
                LocalDateTime.now()
        );
    }

    public static FileResponse completed() {
        return withStatus("COMPLETED");
    }

    public static FileResponse processing() {
        return withStatus("PROCESSING");
    }

    public static FileResponse failed() {
        return withStatus("FAILED");
    }
}
