package com.ryuqq.fileflow.application.session.assembler;

import com.ryuqq.fileflow.application.session.dto.query.ListUploadSessionsQuery;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionDetailResponse;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionDetailResponse.PartDetailResponse;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionResponse;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionSearchCriteria;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * UploadSession Query Assembler.
 *
 * <p>Query 요청을 Domain 검색 조건으로 변환하고, Domain 객체를 Response DTO로 변환합니다.
 */
@Component
public class UploadSessionQueryAssembler {

    /**
     * ListUploadSessionsQuery를 Domain 검색 조건으로 변환합니다.
     *
     * @param query 조회 Query
     * @return Domain 검색 조건
     */
    public UploadSessionSearchCriteria toCriteria(ListUploadSessionsQuery query) {
        return UploadSessionSearchCriteria.of(
                query.tenantId(),
                query.organizationId(),
                query.status(),
                query.uploadType(),
                query.offset(),
                query.size());
    }

    /**
     * UploadSession을 UploadSessionResponse로 변환합니다.
     *
     * @param session 업로드 세션
     * @return UploadSessionResponse
     */
    public UploadSessionResponse toResponse(UploadSession session) {
        if (session instanceof SingleUploadSession singleSession) {
            return toSingleResponse(singleSession);
        } else if (session instanceof MultipartUploadSession multipartSession) {
            return toMultipartResponse(multipartSession);
        }
        throw new IllegalArgumentException("Unknown UploadSession type: " + session.getClass());
    }

    /**
     * UploadSession 목록을 UploadSessionResponse 목록으로 변환합니다.
     *
     * @param sessions 업로드 세션 목록
     * @return UploadSessionResponse 목록
     */
    public List<UploadSessionResponse> toResponses(List<UploadSession> sessions) {
        return sessions.stream().map(this::toResponse).toList();
    }

    /**
     * UploadSession을 UploadSessionDetailResponse로 변환합니다.
     *
     * @param session 업로드 세션
     * @param completedParts 완료된 Part 목록 (Multipart 전용, Single은 null)
     * @return UploadSessionDetailResponse
     */
    public UploadSessionDetailResponse toDetailResponse(
            UploadSession session, List<CompletedPart> completedParts) {
        if (session instanceof SingleUploadSession singleSession) {
            return toSingleDetailResponse(singleSession);
        } else if (session instanceof MultipartUploadSession multipartSession) {
            return toMultipartDetailResponse(multipartSession, completedParts);
        }
        throw new IllegalArgumentException("Unknown UploadSession type: " + session.getClass());
    }

    private UploadSessionResponse toSingleResponse(SingleUploadSession session) {
        return UploadSessionResponse.of(
                session.getIdValue(),
                session.getFileNameValue(),
                session.getFileSizeValue(),
                session.getContentTypeValue(),
                "SINGLE",
                session.getStatus(),
                session.getBucketValue(),
                session.getS3KeyValue(),
                session.getCreatedAt(),
                session.getExpiresAt());
    }

    private UploadSessionResponse toMultipartResponse(MultipartUploadSession session) {
        return UploadSessionResponse.of(
                session.getId().getValue(),
                session.getFileNameValue(),
                session.getFileSizeValue(),
                session.getContentTypeValue(),
                "MULTIPART",
                session.getStatus(),
                session.getBucketValue(),
                session.getS3KeyValue(),
                session.getCreatedAt(),
                session.getExpiresAt());
    }

    private UploadSessionDetailResponse toSingleDetailResponse(SingleUploadSession session) {
        return UploadSessionDetailResponse.ofSingle(
                session.getIdValue(),
                session.getFileNameValue(),
                session.getFileSizeValue(),
                session.getContentTypeValue(),
                session.getStatus(),
                session.getBucketValue(),
                session.getS3KeyValue(),
                session.getETagValue(),
                session.getCreatedAt(),
                session.getExpiresAt(),
                session.getCompletedAt());
    }

    private UploadSessionDetailResponse toMultipartDetailResponse(
            MultipartUploadSession session, List<CompletedPart> completedParts) {
        List<PartDetailResponse> parts =
                completedParts == null
                        ? List.of()
                        : completedParts.stream().map(this::toPartDetailResponse).toList();

        String mergedETagValue =
                session.getMergedETag() != null ? session.getMergedETag().value() : null;

        return UploadSessionDetailResponse.ofMultipart(
                session.getId().getValue(),
                session.getFileNameValue(),
                session.getFileSizeValue(),
                session.getContentTypeValue(),
                session.getStatus(),
                session.getBucketValue(),
                session.getS3KeyValue(),
                session.getS3UploadIdValue(),
                session.getTotalPartsValue(),
                parts.size(),
                parts,
                mergedETagValue,
                session.getCreatedAt(),
                session.getExpiresAt(),
                session.getCompletedAt());
    }

    private PartDetailResponse toPartDetailResponse(CompletedPart part) {
        return PartDetailResponse.of(
                part.getPartNumberValue(),
                part.getETagValue(),
                part.getSize(),
                part.getUploadedAt());
    }
}
