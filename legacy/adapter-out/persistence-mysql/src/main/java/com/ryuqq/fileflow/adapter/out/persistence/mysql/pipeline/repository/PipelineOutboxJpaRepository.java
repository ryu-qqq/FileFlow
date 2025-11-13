package com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.entity.PipelineOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Pipeline Outbox Spring Data JPA Repository
 *
 * <p><strong>역할</strong>: PipelineOutbox Entity에 대한 기본 CRUD 전용 (Command Side)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/pipeline/repository/</p>
 *
 * <h3>CQRS 설계 원칙</h3>
 * <ul>
 *   <li>✅ <strong>Command Side 전용</strong>: save, delete 등 CUD 작업만 담당</li>
 *   <li>✅ <strong>Query Side 분리</strong>: 조회 작업은 PipelineOutboxQueryDslRepository로 위임</li>
 *   <li>✅ Spring Data JPA 인터페이스 (구현체 자동 생성)</li>
 *   <li>✅ Long FK 전략 (fileId는 Long - FileAsset PK 타입과 일치)</li>
 *   <li>❌ {@code @Repository} 어노테이션 불필요 (JpaRepository 상속 시 자동)</li>
 *   <li>❌ {@code @Query} 어노테이션 금지 (Query Side는 별도 Query Adapter에서 처리)</li>
 *   <li>❌ <strong>findBy*, countBy* 메서드 금지</strong> (Query Side에서만 허용)</li>
 * </ul>
 *
 * <h3>제공 메서드</h3>
 * <ul>
 *   <li>save(entity) - 엔티티 저장 (JpaRepository 기본 제공)</li>
 *   <li>delete(entity) - 엔티티 삭제 (JpaRepository 기본 제공)</li>
 *   <li>findById(id) - ID로 단건 조회 (JpaRepository 기본 제공, Command 검증용)</li>
 * </ul>
 *
 * @since 1.0.0
 */
public interface PipelineOutboxJpaRepository extends JpaRepository<PipelineOutboxJpaEntity, Long> {
    // Command Side - CUD 작업만 담당
    // 조회 메서드는 PipelineOutboxQueryDslRepository에서 제공
}
