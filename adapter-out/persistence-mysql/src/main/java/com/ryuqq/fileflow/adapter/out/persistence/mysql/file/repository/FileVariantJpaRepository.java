package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileVariantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * FileVariant Spring Data JPA Repository
 *
 * <p><strong>역할</strong>: FileVariant Entity에 대한 기본 CRUD 제공 (Command Side)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/file/repository/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 인터페이스 (구현체 자동 생성)</li>
 *   <li>✅ Long FK 전략 (parentFileAssetId는 Long - FileAsset PK 타입과 일치)</li>
 *   <li>✅ CQRS Command Side - 기본 CRUD만 제공</li>
 *   <li>✅ 메서드 네이밍 규칙 준수 (Spring Data JPA Query Methods)</li>
 *   <li>❌ {@code @Query} 어노테이션 사용 금지 (Query Side는 FileVariantQueryAdapter 사용)</li>
 *   <li>❌ {@code @Repository} 어노테이션 불필요 (JpaRepository 상속 시 자동)</li>
 * </ul>
 *
 * <h3>Query Side</h3>
 * <p>조회 쿼리는 {@link com.ryuqq.fileflow.adapter.out.persistence.mysql.file.adapter.FileVariantQueryAdapter}에서
 * QueryDSL로 처리합니다.</p>
 *
 * @since 1.0.0
 */
public interface FileVariantJpaRepository extends JpaRepository<FileVariantJpaEntity, Long> {
    // 기본 CRUD 메서드만 사용 (save, findById, delete 등)
    // 모든 조회 쿼리는 FileVariantQueryAdapter에서 QueryDSL로 처리
}
