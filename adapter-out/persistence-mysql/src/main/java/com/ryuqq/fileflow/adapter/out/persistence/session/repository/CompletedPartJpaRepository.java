package com.ryuqq.fileflow.adapter.out.persistence.session.repository;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletedPartJpaRepository extends JpaRepository<CompletedPartJpaEntity, Long> {

    void deleteBySessionId(String sessionId);
}
