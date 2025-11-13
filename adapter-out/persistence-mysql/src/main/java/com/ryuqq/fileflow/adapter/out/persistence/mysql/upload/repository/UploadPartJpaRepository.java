package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadPartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Upload Part Spring Data JPA Repository
 *
 * <p><strong>역할</strong>: Upload Part Entity에 대한 기본 CRUD 전용 (Command Side)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/upload/repository/</p>
 *
 * <h3>CQRS 설계 원칙</h3>
 * <ul>
 *   <li>✅ <strong>Command Side 전용</strong>: save, delete 등 CUD 작업만 담당</li>
 *   <li>✅ <strong>Query Side 분리</strong>: 조회 작업은 UploadPartQueryDslRepository로 위임</li>
 *   <li>✅ Spring Data JPA 인터페이스 (구현체 자동 생성)</li>
 *   <li>✅ Long FK 전략 (multipartUploadId는 Long - MultipartUpload PK 타입과 일치)</li>
 *   <li>❌ {@code @Repository} 어노테이션 불필요 (JpaRepository 상속 시 자동)</li>
 *   <li>❌ {@code @Query} 사용 금지 (복잡한 조회는 QueryAdapter로 위임)</li>
 *   <li>❌ <strong>findBy* 메서드 금지</strong> (Query Side에서만 허용)</li>
 * </ul>
 *
 * <h3>제공 메서드</h3>
 * <ul>
 *   <li>save(entity) - 엔티티 저장 (JpaRepository 기본 제공)</li>
 *   <li>delete(entity) - 엔티티 삭제 (JpaRepository 기본 제공)</li>
 *   <li>deleteByMultipartUploadId(id) - Multipart Upload ID로 일괄 삭제 (Command 작업)</li>
 * </ul>
 *
 * @since 1.0.0
 */
public interface UploadPartJpaRepository
    extends JpaRepository<UploadPartJpaEntity, Long> {

    /**
     * Multipart Upload ID로 Upload Part 삭제
     *
     * <p>특정 Multipart Upload에 속한 모든 Upload Part를 삭제합니다.</p>
     *
     * <p><strong>Spring Data JPA 메서드 네이밍 규칙</strong>:
     * {@code deleteBy*} 메서드는 자동으로 bulk delete 쿼리를 생성합니다.</p>
     *
     * <p><strong>Long FK 전략</strong>: MultipartUpload PK 타입(Long AUTO_INCREMENT)과 일치</p>
     *
     * @param multipartUploadId Multipart Upload ID (Long - MultipartUpload PK 타입과 일치)
     */
    void deleteByMultipartUploadId(Long multipartUploadId);
}
