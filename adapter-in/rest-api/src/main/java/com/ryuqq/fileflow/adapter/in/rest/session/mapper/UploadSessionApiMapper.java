package com.ryuqq.fileflow.adapter.in.rest.session.mapper;

import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteSingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.InitMultipartUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.InitSingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.MarkPartUploadedApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CancelUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CompleteMultipartUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CompleteSingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.InitMultipartUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.InitSingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.MarkPartUploadedApiResponse;
import com.ryuqq.fileflow.application.session.dto.command.CancelUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.InitMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.session.dto.response.CancelUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.MarkPartUploadedResponse;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * UploadSessionApiMapper - Upload Session REST API ↔ Application Layer 변환
 *
 * <p>REST API Layer와 Application Layer 간의 DTO 변환을 담당합니다.
 *
 * <p><strong>변환 방향:</strong>
 *
 * <ul>
 *   <li>API Request → Command (Controller → Application)
 *   <li>Application Response → API Response (Application → Controller)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UploadSessionApiMapper {

    // ========== API Request → Command 변환 ==========

    /**
     * InitSingleUploadApiRequest → InitSingleUploadCommand 변환
     *
     * @param request REST API 단일 업로드 초기화 요청
     * @return Application Layer 단일 업로드 초기화 명령
     */
    public InitSingleUploadCommand toInitSingleUploadCommand(InitSingleUploadApiRequest request) {
        return InitSingleUploadCommand.of(
                request.idempotencyKey(),
                request.fileName(),
                request.fileSize(),
                request.contentType(),
                request.tenantId(),
                request.organizationId(),
                request.userId(),
                request.userEmail());
    }

    /**
     * InitMultipartUploadApiRequest → InitMultipartUploadCommand 변환
     *
     * @param request REST API Multipart 업로드 초기화 요청
     * @return Application Layer Multipart 업로드 초기화 명령
     */
    public InitMultipartUploadCommand toInitMultipartUploadCommand(
            InitMultipartUploadApiRequest request) {
        return InitMultipartUploadCommand.of(
                request.fileName(),
                request.fileSize(),
                request.contentType(),
                request.partSize(),
                request.tenantId(),
                request.organizationId(),
                request.userId(),
                request.userEmail());
    }

    /**
     * CompleteSingleUploadApiRequest + sessionId → CompleteSingleUploadCommand 변환
     *
     * @param sessionId 세션 ID (PathVariable)
     * @param request REST API 단일 업로드 완료 요청
     * @return Application Layer 단일 업로드 완료 명령
     */
    public CompleteSingleUploadCommand toCompleteSingleUploadCommand(
            String sessionId, CompleteSingleUploadApiRequest request) {
        return CompleteSingleUploadCommand.of(sessionId, request.etag());
    }

    /**
     * sessionId → CompleteMultipartUploadCommand 변환
     *
     * @param sessionId 세션 ID (PathVariable)
     * @return Application Layer Multipart 업로드 완료 명령
     */
    public CompleteMultipartUploadCommand toCompleteMultipartUploadCommand(String sessionId) {
        return CompleteMultipartUploadCommand.of(sessionId);
    }

    /**
     * MarkPartUploadedApiRequest + sessionId → MarkPartUploadedCommand 변환
     *
     * @param sessionId 세션 ID (PathVariable)
     * @param request REST API Part 업로드 완료 요청
     * @return Application Layer Part 업로드 완료 명령
     */
    public MarkPartUploadedCommand toMarkPartUploadedCommand(
            String sessionId, MarkPartUploadedApiRequest request) {
        return MarkPartUploadedCommand.of(
                sessionId, request.partNumber(), request.etag(), request.size());
    }

    /**
     * sessionId → CancelUploadSessionCommand 변환
     *
     * @param sessionId 세션 ID (PathVariable)
     * @return Application Layer 세션 취소 명령
     */
    public CancelUploadSessionCommand toCancelUploadSessionCommand(String sessionId) {
        return CancelUploadSessionCommand.of(sessionId);
    }

    // ========== Application Response → API Response 변환 ==========

    /**
     * InitSingleUploadResponse → InitSingleUploadApiResponse 변환
     *
     * @param response Application Layer 단일 업로드 초기화 응답
     * @return REST API 단일 업로드 초기화 응답
     */
    public InitSingleUploadApiResponse toInitSingleUploadApiResponse(
            InitSingleUploadResponse response) {
        return InitSingleUploadApiResponse.of(
                response.sessionId(),
                response.presignedUrl(),
                response.expiresAt(),
                response.bucket(),
                response.key());
    }

    /**
     * InitMultipartUploadResponse → InitMultipartUploadApiResponse 변환
     *
     * @param response Application Layer Multipart 업로드 초기화 응답
     * @return REST API Multipart 업로드 초기화 응답
     */
    public InitMultipartUploadApiResponse toInitMultipartUploadApiResponse(
            InitMultipartUploadResponse response) {
        List<InitMultipartUploadApiResponse.PartInfoApiResponse> parts =
                response.parts().stream()
                        .map(
                                part ->
                                        InitMultipartUploadApiResponse.PartInfoApiResponse.of(
                                                part.partNumber(), part.presignedUrl()))
                        .toList();

        return InitMultipartUploadApiResponse.of(
                response.sessionId(),
                response.uploadId(),
                response.totalParts(),
                response.partSize(),
                response.expiresAt(),
                response.bucket(),
                response.key(),
                parts);
    }

    /**
     * CompleteSingleUploadResponse → CompleteSingleUploadApiResponse 변환
     *
     * @param response Application Layer 단일 업로드 완료 응답
     * @return REST API 단일 업로드 완료 응답
     */
    public CompleteSingleUploadApiResponse toCompleteSingleUploadApiResponse(
            CompleteSingleUploadResponse response) {
        return CompleteSingleUploadApiResponse.of(
                response.sessionId(),
                response.status(),
                response.bucket(),
                response.key(),
                response.etag(),
                response.completedAt());
    }

    /**
     * CompleteMultipartUploadResponse → CompleteMultipartUploadApiResponse 변환
     *
     * @param response Application Layer Multipart 업로드 완료 응답
     * @return REST API Multipart 업로드 완료 응답
     */
    public CompleteMultipartUploadApiResponse toCompleteMultipartUploadApiResponse(
            CompleteMultipartUploadResponse response) {
        List<CompleteMultipartUploadApiResponse.CompletedPartInfoApiResponse> completedParts =
                response.completedParts().stream()
                        .map(
                                part ->
                                        CompleteMultipartUploadApiResponse
                                                .CompletedPartInfoApiResponse.of(
                                                part.partNumber(),
                                                part.etag(),
                                                part.size(),
                                                part.uploadedAt()))
                        .toList();

        return CompleteMultipartUploadApiResponse.of(
                response.sessionId(),
                response.status(),
                response.bucket(),
                response.key(),
                response.uploadId(),
                response.totalParts(),
                completedParts,
                response.completedAt());
    }

    /**
     * MarkPartUploadedResponse → MarkPartUploadedApiResponse 변환
     *
     * @param response Application Layer Part 업로드 완료 응답
     * @return REST API Part 업로드 완료 응답
     */
    public MarkPartUploadedApiResponse toMarkPartUploadedApiResponse(
            MarkPartUploadedResponse response) {
        return MarkPartUploadedApiResponse.of(
                response.sessionId(),
                response.partNumber(),
                response.etag(),
                response.uploadedAt());
    }

    /**
     * CancelUploadSessionResponse → CancelUploadSessionApiResponse 변환
     *
     * @param response Application Layer 세션 취소 응답
     * @return REST API 세션 취소 응답
     */
    public CancelUploadSessionApiResponse toCancelUploadSessionApiResponse(
            CancelUploadSessionResponse response) {
        return CancelUploadSessionApiResponse.of(
                response.sessionId(), response.status(), response.bucket(), response.key());
    }
}
