package com.ryuqq.fileflow.application.upload.port.out;

import com.ryuqq.fileflow.domain.upload.MultipartUpload;

import java.util.List;
import java.util.Optional;

/**
 * Multipart Upload Port (Out)
 *
 * <p>Application Layer에서 Persistence Layer로 나가는 Port 인터페이스입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Multipart Upload Aggregate의 영속화 인터페이스 정의</li>
 *   <li>Adapter 구현체와 Application Layer 간 계약</li>
 *   <li>도메인 용어 사용 (JPA/DB 용어 금지)</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴의 Port 역할</li>
 *   <li>✅ Domain 객체만 사용 (Entity, DTO 금지)</li>
 *   <li>✅ 비즈니스 의미 있는 메서드명</li>
 *   <li>✅ Infrastructure 독립적</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface MultipartUploadPort {

    /**
     * Multipart Upload 저장
     *
     * <p>신규 생성 또는 기존 데이터 업데이트를 수행합니다.</p>
     *
     * @param multipart Multipart Upload Domain Aggregate
     * @return 저장된 Multipart Upload (ID 포함)
     */
    MultipartUpload save(MultipartUpload multipart);

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

    /**
     * Multipart Upload 삭제
     *
     * @param id Multipart Upload ID
     */
    void delete(Long id);
}
