package com.ryuqq.fileflow.application.session;

import com.ryuqq.fileflow.application.session.dto.command.AbortMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.AddCompletedPartCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CreateMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CreateSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.GeneratePresignedPartUrlCommand;
import com.ryuqq.fileflow.domain.common.vo.AccessType;

/**
 * Session Application Command 테스트 Fixtures.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public final class SessionCommandFixtures {

    private SessionCommandFixtures() {}

    // ===== Single Session Command Fixtures =====

    public static CreateSingleUploadSessionCommand createSingleCommand() {
        return new CreateSingleUploadSessionCommand(
                "product-image.jpg",
                "image/jpeg",
                AccessType.PUBLIC,
                "product-image",
                "commerce-service");
    }

    public static CreateSingleUploadSessionCommand createSingleCommand(
            String fileName, String contentType, AccessType accessType) {
        return new CreateSingleUploadSessionCommand(
                fileName, contentType, accessType, "product-image", "commerce-service");
    }

    public static CompleteSingleUploadSessionCommand completeSingleCommand() {
        return completeSingleCommand("single-session-001");
    }

    public static CompleteSingleUploadSessionCommand completeSingleCommand(String sessionId) {
        return new CompleteSingleUploadSessionCommand(sessionId, 1024L, "etag-123");
    }

    // ===== Multipart Session Command Fixtures =====

    public static CreateMultipartUploadSessionCommand createMultipartCommand() {
        return new CreateMultipartUploadSessionCommand(
                "large-file.jpg",
                "image/jpeg",
                AccessType.PUBLIC,
                5_242_880L,
                "product-image",
                "commerce-service");
    }

    public static CreateMultipartUploadSessionCommand createMultipartCommand(
            String fileName, String contentType, AccessType accessType, long partSize) {
        return new CreateMultipartUploadSessionCommand(
                fileName, contentType, accessType, partSize, "product-image", "commerce-service");
    }

    public static CompleteMultipartUploadSessionCommand completeMultipartCommand() {
        return completeMultipartCommand("multipart-session-001");
    }

    public static CompleteMultipartUploadSessionCommand completeMultipartCommand(String sessionId) {
        return new CompleteMultipartUploadSessionCommand(sessionId, 10_485_760L, "etag-final");
    }

    public static AbortMultipartUploadSessionCommand abortMultipartCommand() {
        return abortMultipartCommand("multipart-session-001");
    }

    public static AbortMultipartUploadSessionCommand abortMultipartCommand(String sessionId) {
        return new AbortMultipartUploadSessionCommand(sessionId);
    }

    public static AddCompletedPartCommand addCompletedPartCommand() {
        return addCompletedPartCommand("multipart-session-001", 1);
    }

    public static AddCompletedPartCommand addCompletedPartCommand(
            String sessionId, int partNumber) {
        return new AddCompletedPartCommand(
                sessionId, partNumber, "etag-part-" + partNumber, 5_242_880L);
    }

    // ===== Generate Presigned Part URL Command Fixtures =====

    public static GeneratePresignedPartUrlCommand generatePresignedPartUrlCommand() {
        return generatePresignedPartUrlCommand("multipart-session-001", 1);
    }

    public static GeneratePresignedPartUrlCommand generatePresignedPartUrlCommand(
            String sessionId, int partNumber) {
        return new GeneratePresignedPartUrlCommand(sessionId, partNumber);
    }
}
