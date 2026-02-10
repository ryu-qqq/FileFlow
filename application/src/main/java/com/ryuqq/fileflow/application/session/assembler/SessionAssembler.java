package com.ryuqq.fileflow.application.session.assembler;

import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.SingleUploadSessionResponse;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Session Assembler
 *
 * <p>Session Domain ↔ Response DTO 변환
 */
@Component
public class SessionAssembler {

    public SingleUploadSessionResponse toResponse(SingleUploadSession session) {
        return new SingleUploadSessionResponse(
                session.idValue(),
                session.presignedUrlValue(),
                session.s3Key(),
                session.bucket(),
                session.accessType(),
                session.fileName(),
                session.contentType(),
                session.status().name(),
                session.expiresAt(),
                session.createdAt());
    }

    public MultipartUploadSessionResponse toResponse(MultipartUploadSession session) {
        List<MultipartUploadSessionResponse.CompletedPartResponse> parts =
                session.completedParts().stream().map(this::toPartResponse).toList();

        return new MultipartUploadSessionResponse(
                session.idValue(),
                session.uploadId(),
                session.s3Key(),
                session.bucket(),
                session.accessType(),
                session.fileName(),
                session.contentType(),
                session.partSize(),
                session.status().name(),
                session.completedPartCount(),
                parts,
                session.expiresAt(),
                session.createdAt());
    }

    private MultipartUploadSessionResponse.CompletedPartResponse toPartResponse(
            CompletedPart part) {
        return new MultipartUploadSessionResponse.CompletedPartResponse(
                part.partNumber(), part.etag(), part.size());
    }
}
