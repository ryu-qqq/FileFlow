package com.ryuqq.fileflow.adapter.in.rest.session.mapper;

import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteSingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.InitMultipartUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.InitSingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.MarkPartUploadedApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.query.UploadSessionSearchApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CancelUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CompleteMultipartUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CompleteSingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.InitMultipartUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.InitSingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.MarkPartUploadedApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.UploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.UploadSessionDetailApiResponse;
import com.ryuqq.fileflow.application.session.dto.command.CancelUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.InitMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.session.dto.query.GetUploadSessionQuery;
import com.ryuqq.fileflow.application.session.dto.query.ListUploadSessionsQuery;
import com.ryuqq.fileflow.application.session.dto.response.CancelUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.MarkPartUploadedResponse;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionDetailResponse;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionResponse;
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
                request.uploadCategory(),
                request.customPath());
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
                request.customPath());
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

    // ========== API Request → Query 변환 ==========

    /**
     * sessionId + tenantId → GetUploadSessionQuery 변환
     *
     * @param sessionId 세션 ID
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @return GetUploadSessionQuery
     */
    public GetUploadSessionQuery toGetUploadSessionQuery(String sessionId, String tenantId) {
        return GetUploadSessionQuery.of(sessionId, tenantId);
    }

    /**
     * UploadSessionSearchApiRequest → ListUploadSessionsQuery 변환
     *
     * @param request REST API 검색 요청
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @param organizationId 조직 ID (UUIDv7 문자열)
     * @return ListUploadSessionsQuery
     */
    public ListUploadSessionsQuery toListUploadSessionsQuery(
            UploadSessionSearchApiRequest request, String tenantId, String organizationId) {
        String uploadType = request.uploadType() != null ? request.uploadType().name() : null;
        String status = request.status() != null ? request.status().name() : null;

        return ListUploadSessionsQuery.of(
                tenantId, organizationId, status, uploadType, request.page(), request.size());
    }

    // ========== Application Response → Query API Response 변환 ==========

    /**
     * UploadSessionResponse → UploadSessionApiResponse 변환
     *
     * @param response Application Layer 세션 응답
     * @return REST API 세션 응답
     */
    public UploadSessionApiResponse toUploadSessionApiResponse(UploadSessionResponse response) {
        return UploadSessionApiResponse.of(
                response.sessionId(),
                response.fileName(),
                response.fileSize(),
                response.contentType(),
                response.uploadType(),
                response.status(),
                response.bucket(),
                response.key(),
                response.createdAt(),
                response.expiresAt());
    }

    /**
     * UploadSessionDetailResponse → UploadSessionDetailApiResponse 변환
     *
     * @param response Application Layer 세션 상세 응답
     * @return REST API 세션 상세 응답
     */
    public UploadSessionDetailApiResponse toUploadSessionDetailApiResponse(
            UploadSessionDetailResponse response) {
        if ("SINGLE".equals(response.uploadType())) {
            return UploadSessionDetailApiResponse.ofSingle(
                    response.sessionId(),
                    response.fileName(),
                    response.fileSize(),
                    response.contentType(),
                    response.status(),
                    response.bucket(),
                    response.key(),
                    response.etag(),
                    response.createdAt(),
                    response.expiresAt(),
                    response.completedAt());
        }

        List<UploadSessionDetailApiResponse.PartDetailApiResponse> parts = null;
        if (response.parts() != null) {
            parts =
                    response.parts().stream()
                            .map(
                                    part ->
                                            UploadSessionDetailApiResponse.PartDetailApiResponse.of(
                                                    part.partNumber(),
                                                    part.etag(),
                                                    part.size(),
                                                    part.uploadedAt()))
                            .toList();
        }

        return UploadSessionDetailApiResponse.ofMultipart(
                response.sessionId(),
                response.fileName(),
                response.fileSize(),
                response.contentType(),
                response.status(),
                response.bucket(),
                response.key(),
                response.uploadId(),
                response.totalParts() != null ? response.totalParts() : 0,
                response.uploadedParts() != null ? response.uploadedParts() : 0,
                parts,
                response.etag(),
                response.createdAt(),
                response.expiresAt(),
                response.completedAt());
    }
}
