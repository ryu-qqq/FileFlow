package com.ryuqq.fileflow.adapter.rest.upload.mapper;

import com.ryuqq.fileflow.adapter.rest.upload.dto.request.InitMultipartApiRequest;
import com.ryuqq.fileflow.adapter.rest.upload.dto.request.MarkPartUploadedApiRequest;
import com.ryuqq.fileflow.adapter.rest.upload.dto.request.SingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.rest.upload.dto.response.CompleteMultipartApiResponse;
import com.ryuqq.fileflow.adapter.rest.upload.dto.response.CompleteSingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.rest.upload.dto.response.InitMultipartApiResponse;
import com.ryuqq.fileflow.adapter.rest.upload.dto.response.PartPresignedUrlApiResponse;
import com.ryuqq.fileflow.adapter.rest.upload.dto.response.SingleUploadApiResponse;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteMultipartCommand;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteSingleUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.command.GeneratePartUrlCommand;
import com.ryuqq.fileflow.application.upload.dto.command.InitMultipartCommand;
import com.ryuqq.fileflow.application.upload.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.upload.dto.response.CompleteMultipartResponse;
import com.ryuqq.fileflow.application.upload.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.upload.dto.response.InitMultipartResponse;
import com.ryuqq.fileflow.application.upload.dto.response.PartPresignedUrlResponse;
import com.ryuqq.fileflow.application.upload.dto.response.SingleUploadResponse;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

/**
 * Upload API Mapper
 *
 * <p>REST API Request/Response와 Application Layer Command/Response 간 변환을 담당합니다.</p>
 *
 * <p><strong>변환 규칙:</strong></p>
 * <ul>
 *   <li>API Request → Application Command</li>
 *   <li>Application Response → API Response</li>
 *   <li>헤더 정보(tenantId) 추가</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class UploadApiMapper {

    private UploadApiMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * InitMultipartApiRequest → InitMultipartCommand 변환
     *
     * @param request API Request
     * @param tenantId Tenant ID
     * @return InitMultipartCommand
     */
    public static InitMultipartCommand toCommand(InitMultipartApiRequest request, Long tenantId) {
        // 기본 팩토리 메서드 사용 (organizationId, userContextId 없이)
        return InitMultipartCommand.of(
            new TenantId(tenantId),
            request.getFileName(),
            request.getFileSize(),
            request.getContentType()
        );
    }

    /**
     * InitMultipartResponse → InitMultipartApiResponse 변환
     *
     * @param response Application Response
     * @return InitMultipartApiResponse
     */
    public static InitMultipartApiResponse toApiResponse(InitMultipartResponse response) {
        return InitMultipartApiResponse.of(
            response.sessionKey(),
            response.uploadId(),
            response.totalParts(),
            response.storageKey()
        );
    }

    /**
     * GeneratePartUrlCommand 생성
     *
     * @param sessionKey 세션 키
     * @param partNumber 파트 번호
     * @return GeneratePartUrlCommand
     */
    public static GeneratePartUrlCommand toCommand(String sessionKey, Integer partNumber) {
        return GeneratePartUrlCommand.of(sessionKey, partNumber);
    }

    /**
     * PartPresignedUrlResponse → PartPresignedUrlApiResponse 변환
     *
     * @param response Application Response
     * @return PartPresignedUrlApiResponse
     */
    public static PartPresignedUrlApiResponse toApiResponse(PartPresignedUrlResponse response) {
        return PartPresignedUrlApiResponse.of(
            response.partNumber(),
            response.presignedUrl(),
            response.expiresIn().toSeconds()
        );
    }

    /**
     * MarkPartUploadedApiRequest → MarkPartUploadedCommand 변환
     *
     * @param sessionKey 세션 키
     * @param partNumber 파트 번호
     * @param request API Request
     * @return MarkPartUploadedCommand
     */
    public static MarkPartUploadedCommand toCommand(
        String sessionKey,
        Integer partNumber,
        MarkPartUploadedApiRequest request
    ) {
        return MarkPartUploadedCommand.of(
            sessionKey,
            partNumber,
            request.getEtag(),
            request.getPartSize()
        );
    }

    /**
     * CompleteMultipartCommand 생성
     *
     * @param sessionKey 세션 키
     * @return CompleteMultipartCommand
     */
    public static CompleteMultipartCommand toCommand(String sessionKey) {
        return CompleteMultipartCommand.of(sessionKey);
    }

    /**
     * CompleteMultipartResponse → CompleteMultipartApiResponse 변환
     *
     * @param response Application Response
     * @return CompleteMultipartApiResponse
     */
    public static CompleteMultipartApiResponse toApiResponse(CompleteMultipartResponse response) {
        return CompleteMultipartApiResponse.of(
            response.fileId(),
            response.etag(),
            response.location()
        );
    }

    /**
     * SingleUploadApiRequest → InitSingleUploadCommand 변환
     *
     * @param request API Request
     * @param tenantId Tenant ID
     * @return InitSingleUploadCommand
     */
    public static InitSingleUploadCommand toCommand(SingleUploadApiRequest request, Long tenantId) {
        // 기본 팩토리 메서드 사용 (organizationId, userContextId 없이)
        return InitSingleUploadCommand.of(
            TenantId.of(tenantId),
            request.getFileName(),
            request.getFileSize(),
            request.getContentType()
        );
    }

    /**
     * SingleUploadResponse → SingleUploadApiResponse 변환
     *
     * @param response Application Response
     * @return SingleUploadApiResponse
     */
    public static SingleUploadApiResponse toApiResponse(SingleUploadResponse response) {
        return SingleUploadApiResponse.of(
            response.sessionKey(),
            response.uploadUrl(),
            response.storageKey()
        );
    }

    /**
     * CompleteSingleUploadCommand 생성
     *
     * @param sessionKey 세션 키
     * @return CompleteSingleUploadCommand
     */
    public static CompleteSingleUploadCommand toCompleteSingleCommand(String sessionKey) {
        return CompleteSingleUploadCommand.of(sessionKey);
    }

    /**
     * CompleteSingleUploadResponse → CompleteSingleUploadApiResponse 변환
     *
     * @param response Application Response
     * @return CompleteSingleUploadApiResponse
     */
    public static CompleteSingleUploadApiResponse toApiResponse(CompleteSingleUploadResponse response) {
        return new CompleteSingleUploadApiResponse(
            response.fileId(),
            response.etag(),
            response.fileSize()
        );
    }
}
