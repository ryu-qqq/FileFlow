package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.ExtractedDataJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ExtractedData Spring Data JPA Repository
 *
 * <p><strong>역할</strong>: ExtractedData Entity에 대한 기본 CRUD 제공 (CQRS Command Side)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/file/repository/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 인터페이스 (구현체 자동 생성)</li>
 *   <li>✅ Long FK 전략 (fileId는 Long - FileAsset PK 타입과 일치)</li>
 *   <li>✅ <strong>CQRS Command Side</strong>: save, delete 등 기본 CRUD만 사용</li>
 *   <li>✅ <strong>Query Side 분리</strong>: 모든 조회 로직은 {@code ExtractedDataQueryAdapter} (QueryDSL) 사용</li>
 *   <li>❌ {@code @Repository} 어노테이션 불필요 (JpaRepository 상속 시 자동)</li>
 *   <li>❌ 커스텀 쿼리 메서드 불필요 (Query는 QueryDSL로 처리)</li>
 * </ul>
 *
 * <h3>사용 예시</h3>
 * <pre>
 * // Command (Write): Repository 사용
 * repository.save(entity);
 * repository.delete(entity);
 *
 * // Query (Read): QueryAdapter 사용
 * queryAdapter.findByFileId(fileId);
 * queryAdapter.findByExtractedUuid(uuid);
 * </pre>
 *
 * @since 1.0.0
 * @see com.ryuqq.fileflow.adapter.out.persistence.mysql.file.adapter.ExtractedDataQueryAdapter
 */
public interface ExtractedDataJpaRepository extends JpaRepository<ExtractedDataJpaEntity, Long> {
    // 기본 CRUD 메서드만 사용 (save, delete, findById 등)
    // 모든 조회 쿼리는 ExtractedDataQueryAdapter에서 QueryDSL로 처리
}
