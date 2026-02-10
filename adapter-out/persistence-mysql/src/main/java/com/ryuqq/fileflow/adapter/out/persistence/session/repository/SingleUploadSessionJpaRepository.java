package com.ryuqq.fileflow.adapter.out.persistence.session.repository;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SingleUploadSessionJpaRepository
        extends JpaRepository<SingleUploadSessionJpaEntity, String> {}
