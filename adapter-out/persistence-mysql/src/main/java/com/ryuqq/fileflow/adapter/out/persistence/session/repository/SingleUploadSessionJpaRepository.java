package com.ryuqq.fileflow.adapter.out.persistence.session.repository;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SingleUploadSession JPA Repository.
 *
 * <p>Command 작업용 Spring Data JPA Repository입니다.
 */
public interface SingleUploadSessionJpaRepository
        extends JpaRepository<SingleUploadSessionJpaEntity, String> {}
