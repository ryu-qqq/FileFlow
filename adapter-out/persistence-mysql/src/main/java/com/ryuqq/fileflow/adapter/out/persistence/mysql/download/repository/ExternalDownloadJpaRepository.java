package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.ExternalDownloadJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * External Download Spring Data JPA Repository
 *
 * <p><strong>역할</strong>: External Download Entity에 대한 기본 CRUD 및 Command 메서드 제공</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/download/repository/</p>
 * <p><strong>CQRS</strong>: Command Side (생성, 수정, 삭제) - Query는 {@link com.ryuqq.fileflow.adapter.out.persistence.mysql.download.adapter.ExternalDownloadQueryAdapter} 사용</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 인터페이스 (구현체 자동 생성)</li>
 *   <li>✅ Long FK 전략 (uploadSessionId는 Long - UploadSession PK 타입과 일치)</li>
 *   <li>✅ 메서드 네이밍 규칙 준수 (Spring Data JPA Query Methods)</li>
 *   <li>✅ CQRS Command Side (조회는 QueryAdapter + QueryDSL 사용)</li>
 *   <li>❌ {@code @Repository} 어노테이션 불필요 (JpaRepository 상속 시 자동)</li>
 *   <li>❌ {@code @Query} 어노테이션 사용 금지 (복잡한 조회는 QueryDSL 사용)</li>
 * </ul>
 *
 * @since 1.0.0
 */
public interface ExternalDownloadJpaRepository
    extends JpaRepository<ExternalDownloadJpaEntity, Long> {

    /**
     * Upload Session ID로 조회
     *
     * <p>특정 Upload Session에 연관된 External Download를 조회합니다.</p>
     *
     * <p><strong>Long FK 전략</strong>: UploadSession PK 타입(Long AUTO_INCREMENT)과 일치</p>
     *
     * @param uploadSessionId Upload Session ID (Long - UploadSession PK 타입과 일치)
     * @return External Download Entity (존재하지 않으면 {@code Optional.empty()})
     */
    Optional<ExternalDownloadJpaEntity> findByUploadSessionId(Long uploadSessionId);

}
