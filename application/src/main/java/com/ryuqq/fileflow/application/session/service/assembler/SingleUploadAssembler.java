package com.ryuqq.fileflow.application.session.service.assembler;

import com.ryuqq.fileflow.application.session.dto.response.CancelUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.ExpireUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitSingleUploadResponse;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import org.springframework.stereotype.Component;

/**
 * 단일 업로드 세션 Assembler.
 *
 * <p>Domain 객체를 Response DTO로 변환합니다.
 *
 * <p><strong>규칙:</strong>
 *
 * <ul>
 *   <li>APP-AS-003: toResponse* 메서드만 허용
 *   <li>toDomain, toBundle, toCriteria 금지 → CommandFactory 사용
 * </ul>
 */
@Component
public class SingleUploadAssembler {

    /**
     * Domain → Response DTO 변환 (초기화).
     *
     * @param session 활성화된 세션
     * @return InitSingleUploadResponse
     */
    public InitSingleUploadResponse toResponseForInit(SingleUploadSession session) {
        return InitSingleUploadResponse.of(
                session.getIdValue(),
                session.getPresignedUrlValue(),
                session.getExpiresAt(),
                session.getBucketValue(),
                session.getS3KeyValue());
    }

    /**
     * Domain → Response DTO 변환 (완료).
     *
     * @param session 완료된 세션
     * @return CompleteSingleUploadResponse
     */
    public CompleteSingleUploadResponse toResponseForComplete(SingleUploadSession session) {
        return CompleteSingleUploadResponse.of(
                session.getIdValue(),
                session.getStatus().name(),
                session.getBucketValue(),
                session.getS3KeyValue(),
                session.getETagValue(),
                session.getCompletedAt());
    }

    /**
     * Domain → Response DTO 변환 (만료).
     *
     * @param session 만료된 세션
     * @return ExpireUploadSessionResponse
     */
    public ExpireUploadSessionResponse toResponseForExpire(SingleUploadSession session) {
        return ExpireUploadSessionResponse.of(
                session.getIdValue(),
                session.getStatus().name(),
                session.getBucketValue(),
                session.getS3KeyValue(),
                session.getExpiresAt());
    }

    /**
     * Domain → Response DTO 변환 (취소).
     *
     * @param session 취소된 세션 (FAILED 상태)
     * @return CancelUploadSessionResponse
     */
    public CancelUploadSessionResponse toResponseForCancel(SingleUploadSession session) {
        return CancelUploadSessionResponse.of(
                session.getIdValue(),
                session.getStatus().name(),
                session.getBucketValue(),
                session.getS3KeyValue());
    }
}
