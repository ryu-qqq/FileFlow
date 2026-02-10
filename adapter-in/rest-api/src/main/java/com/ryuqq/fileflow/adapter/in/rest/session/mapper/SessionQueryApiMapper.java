package com.ryuqq.fileflow.adapter.in.rest.session.mapper;

import com.ryuqq.fileflow.adapter.in.rest.common.util.DateTimeFormatUtils;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.MultipartUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.MultipartUploadSessionApiResponse.CompletedPartApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.PresignedPartUrlApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.SingleUploadSessionApiResponse;
import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.PresignedPartUrlResponse;
import com.ryuqq.fileflow.application.session.dto.response.SingleUploadSessionResponse;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * SessionQueryApiMapper - 업로드 세션 Query API 변환 매퍼.
 *
 * <p>Application Response → API Response 변환을 담당합니다.
 *
 * <p>API-MAP-001: Mapper는 @Component로 등록.
 *
 * <p>API-MAP-003: 순수 변환 로직만.
 *
 * <p>API-DTO-005: 날짜 String 변환 필수.
 */
@Component
public class SessionQueryApiMapper {

    /**
     * SingleUploadSessionResponse → SingleUploadSessionApiResponse 변환.
     *
     * @param response Application 응답
     * @return SingleUploadSessionApiResponse
     */
    public SingleUploadSessionApiResponse toResponse(SingleUploadSessionResponse response) {
        return new SingleUploadSessionApiResponse(
                response.sessionId(),
                response.presignedUrl(),
                response.s3Key(),
                response.bucket(),
                response.accessType().name(),
                response.fileName(),
                response.contentType(),
                response.status(),
                DateTimeFormatUtils.formatIso8601(response.expiresAt()),
                DateTimeFormatUtils.formatIso8601(response.createdAt()));
    }

    /**
     * MultipartUploadSessionResponse → MultipartUploadSessionApiResponse 변환.
     *
     * @param response Application 응답
     * @return MultipartUploadSessionApiResponse
     */
    public MultipartUploadSessionApiResponse toResponse(MultipartUploadSessionResponse response) {
        List<CompletedPartApiResponse> completedParts =
                response.completedParts().stream()
                        .map(
                                part ->
                                        new CompletedPartApiResponse(
                                                part.partNumber(), part.etag(), part.size()))
                        .toList();

        return new MultipartUploadSessionApiResponse(
                response.sessionId(),
                response.uploadId(),
                response.s3Key(),
                response.bucket(),
                response.accessType().name(),
                response.fileName(),
                response.contentType(),
                response.partSize(),
                response.status(),
                response.completedPartCount(),
                completedParts,
                DateTimeFormatUtils.formatIso8601(response.expiresAt()),
                DateTimeFormatUtils.formatIso8601(response.createdAt()));
    }

    /**
     * PresignedPartUrlResponse → PresignedPartUrlApiResponse 변환.
     *
     * @param response Application 응답
     * @return PresignedPartUrlApiResponse
     */
    public PresignedPartUrlApiResponse toResponse(PresignedPartUrlResponse response) {
        return new PresignedPartUrlApiResponse(
                response.presignedUrl(), response.partNumber(), response.expiresInSeconds());
    }
}
