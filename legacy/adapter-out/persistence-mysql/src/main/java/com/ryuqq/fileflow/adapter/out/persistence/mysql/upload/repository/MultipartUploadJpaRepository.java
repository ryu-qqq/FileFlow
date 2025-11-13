package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.MultipartUploadJpaEntity;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Multipart Upload Spring Data JPA Repository (CQRS Command Side)
 *
 * <p><strong>역할</strong>: Multipart Upload Entity에 대한 기본 CRUD 및 쿼리 메서드 제공</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/upload/repository/</p>
 *
 * <h3>CQRS 분리 원칙</h3>
 * <ul>
 *   <li>✅ <strong>Command Side</strong>: 이 Repository는 CUD(Create/Update/Delete) 전용</li>
 *   <li>✅ <strong>Query Side</strong>: 복잡한 조회는 QueryAdapter로 분리 (QueryDSL 사용)</li>
 * </ul>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 인터페이스 (구현체 자동 생성)</li>
 *   <li>✅ 메서드 네이밍 규칙 준수 (Spring Data JPA Query Methods)</li>
 *   <li>✅ Long FK 전략 (uploadSessionId는 Long)</li>
 *   <li>❌ {@code @Query} 어노테이션 사용 금지 (복잡한 쿼리는 QueryAdapter로)</li>
 *   <li>❌ {@code @Repository} 어노테이션 불필요 (JpaRepository 상속 시 자동)</li>
 * </ul>
 *
 * @since 1.0.0
 */
public interface MultipartUploadJpaRepository
    extends JpaRepository<MultipartUploadJpaEntity, Long> {

    /**
     * 업로드 세션 ID로 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return MultipartUploadJpaEntity (Optional)
     */
    Optional<MultipartUploadJpaEntity> findByUploadSessionId(Long uploadSessionId);

    /**
     * 상태별 조회
     *
     * @param status Multipart 상태
     * @return MultipartUploadJpaEntity 목록
     */
    List<MultipartUploadJpaEntity> findByStatus(MultipartUpload.MultipartStatus status);

    /**
     * 세션 ID 목록으로 조회
     *
     * <p>여러 Upload Session에 속한 Multipart Upload를 한 번에 조회합니다.</p>
     *
     * @param uploadSessionIds Upload Session ID 목록
     * @return MultipartUploadJpaEntity 목록 (빈 리스트 가능)
     */
    List<MultipartUploadJpaEntity> findByUploadSessionIdIn(List<Long> uploadSessionIds);

    /**
     * Provider Upload ID로 조회
     *
     * @param providerUploadId Provider Upload ID
     * @return MultipartUploadJpaEntity (Optional)
     */
    Optional<MultipartUploadJpaEntity> findByProviderUploadId(String providerUploadId);

    /**
     * 상태별 개수 조회
     *
     * @param status Multipart 상태
     * @return 개수
     */
    long countByStatus(MultipartUpload.MultipartStatus status);
}
