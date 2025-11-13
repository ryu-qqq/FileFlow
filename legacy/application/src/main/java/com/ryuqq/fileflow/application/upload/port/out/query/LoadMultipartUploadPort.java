package com.ryuqq.fileflow.application.upload.port.out.query;

import com.ryuqq.fileflow.domain.upload.MultipartUpload;

import java.util.List;
import java.util.Optional;

/**
 * Multipart Upload 조회 Port (Query)
 *
 * <p>Application Layer에서 Persistence Layer로 나가는 Query Port입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Multipart Upload Aggregate 조회</li>
 *   <li>CQRS Query 패턴 구현</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Query Port (Read 전담)</li>
 *   <li>✅ Domain 객체만 사용 (Entity, DTO 금지)</li>
 *   <li>✅ Infrastructure 독립적</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface LoadMultipartUploadPort {

    /**
     * ID로 Multipart Upload 조회
     *
     * @param id Multipart Upload ID
     * @return Multipart Upload (Optional)
     */
    Optional<MultipartUpload> findById(Long id);

    /**
     * Upload Session ID로 Multipart Upload 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return Multipart Upload (Optional)
     */
    Optional<MultipartUpload> findByUploadSessionId(Long uploadSessionId);

    /**
     * 상태별 Multipart Upload 목록 조회
     *
     * @param status Multipart 상태
     * @return Multipart Upload 목록
     */
    List<MultipartUpload> findByStatus(MultipartUpload.MultipartStatus status);
}

