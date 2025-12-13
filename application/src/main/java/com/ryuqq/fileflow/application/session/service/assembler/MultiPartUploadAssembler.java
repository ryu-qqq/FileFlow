package com.ryuqq.fileflow.application.session.service.assembler;

import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse.CompletedPartInfo;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse.PartInfo;
import com.ryuqq.fileflow.application.session.dto.response.MarkPartUploadedResponse;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Multipart 업로드 세션 Assembler.
 *
 * <p>Domain 객체를 Response DTO로 변환합니다.
 *
 * <p><strong>규칙:</strong>
 *
 * <ul>
 *   <li>APP-AS-003: toResponse* 메서드만 허용
 *   <li>toDomain, toS3Metadata, toInitialCompletedParts 금지 → CommandFactory 사용
 * </ul>
 */
@Component
public class MultiPartUploadAssembler {

    /**
     * Domain → Response DTO 변환 (초기화).
     *
     * <p>activatedSession과 Part 목록을 받아 Response DTO로 변환합니다.
     *
     * @param session 활성화된 세션
     * @param completedParts Part 목록 (초기화된 상태)
     * @return InitMultipartUploadResponse
     */
    public InitMultipartUploadResponse toResponseForInit(
            MultipartUploadSession session, List<CompletedPart> completedParts) {
        // Domain VO → Response DTO 변환
        List<PartInfo> partInfosDto =
                completedParts.stream()
                        .map(
                                part ->
                                        PartInfo.of(
                                                part.getPartNumberValue(),
                                                part.getPresignedUrlValue()))
                        .toList();

        return InitMultipartUploadResponse.of(
                session.getId().value().toString(),
                session.getS3UploadIdValue(),
                session.getTotalPartsValue(),
                session.getPartSizeValue(),
                session.getExpiresAt(),
                session.getBucketValue(),
                session.getS3KeyValue(),
                partInfosDto);
    }

    /**
     * Domain → Response DTO 변환 (Multipart 업로드 완료).
     *
     * @param session 완료된 세션
     * @param completedParts 완료된 Part 목록
     * @return CompleteMultipartUploadResponse
     */
    public CompleteMultipartUploadResponse toResponseForComplete(
            MultipartUploadSession session, List<CompletedPart> completedParts) {
        // CompletedPart 목록을 CompletedPartInfo DTO로 변환
        List<CompletedPartInfo> completedPartInfos =
                completedParts.stream()
                        .map(
                                part ->
                                        CompletedPartInfo.of(
                                                part.getPartNumberValue(),
                                                part.getETagValue(),
                                                part.getSize(),
                                                part.getUploadedAt()))
                        .toList();

        return CompleteMultipartUploadResponse.of(
                session.getId().value().toString(),
                session.getStatus().name(),
                session.getBucketValue(),
                session.getS3KeyValue(),
                session.getS3UploadIdValue(),
                session.getTotalPartsValue(),
                completedPartInfos,
                session.getCompletedAt());
    }

    /**
     * Domain → Response DTO 변환 (Part 업로드 완료 표시).
     *
     * @param completedPart 완료된 Part
     * @return MarkPartUploadedResponse
     */
    public MarkPartUploadedResponse toResponseForMarkPart(CompletedPart completedPart) {
        return MarkPartUploadedResponse.of(
                completedPart.getSessionIdValue(),
                completedPart.getPartNumberValue(),
                completedPart.getETagValue(),
                completedPart.getUploadedAt());
    }
}
