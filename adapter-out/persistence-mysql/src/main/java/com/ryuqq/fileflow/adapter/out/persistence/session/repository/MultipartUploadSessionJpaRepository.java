package com.ryuqq.fileflow.adapter.out.persistence.session.repository;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * MultipartUploadSession JPA Repository.
 *
 * <p>Command 작업용 Spring Data JPA Repository입니다.
 */
public interface MultipartUploadSessionJpaRepository
        extends JpaRepository<MultipartUploadSessionJpaEntity, String> {}
