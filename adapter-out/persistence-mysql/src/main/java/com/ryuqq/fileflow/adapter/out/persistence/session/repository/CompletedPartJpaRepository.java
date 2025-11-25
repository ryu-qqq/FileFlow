package com.ryuqq.fileflow.adapter.out.persistence.session.repository;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CompletedPart JPA Repository.
 *
 * <p>Command 작업용 Spring Data JPA Repository입니다.
 */
public interface CompletedPartJpaRepository extends JpaRepository<CompletedPartJpaEntity, Long> {}
