package com.ryuqq.adapter.out.persistence.{domain_lower}.repository;

import com.ryuqq.adapter.out.persistence.{domain_lower}.entity.{Domain}OperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * {Domain} Operation Repository
 *
 * @author {author_name}
 * @since {version}
 */
public interface {Domain}OperationRepository extends JpaRepository<{Domain}OperationEntity, String> {

    /**
     * IdemKey로 Operation 조회 (중복 체크용)
     */
    Optional<{Domain}OperationEntity> findByIdemKey(String idemKey);

    /**
     * State별 Operation 조회
     */
    List<{Domain}OperationEntity> findByState(OperationState state);

    /**
     * Retry 대상 조회 (State = PENDING AND nextRetryAt <= now)
     */
    @Query("SELECT o FROM {Domain}OperationEntity o WHERE o.state = :state AND o.nextRetryAt <= :now")
    List<{Domain}OperationEntity> findRetryableOperations(
        @Param("state") OperationState state,
        @Param("now") LocalDateTime now
    );

    /**
     * Timeout 대상 조회 (attemptCount >= maxAttempts AND state != COMPLETED/FAILED)
     */
    @Query("SELECT o FROM {Domain}OperationEntity o WHERE o.attemptCount >= o.maxAttempts " +
           "AND o.state NOT IN ('COMPLETED', 'FAILED', 'TIMEOUT')")
    List<{Domain}OperationEntity> findTimeoutOperations();

    /**
     * 상태 일괄 업데이트
     */
    @Modifying
    @Query("UPDATE {Domain}OperationEntity o SET o.state = :newState, o.updatedAt = :now WHERE o.opId IN :opIds")
    void updateStateByOpIds(
        @Param("opIds") List<String> opIds,
        @Param("newState") OperationState newState,
        @Param("now") LocalDateTime now
    );
}
