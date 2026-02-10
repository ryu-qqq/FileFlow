package com.ryuqq.fileflow.adapter.out.persistence.session.repository;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MultipartUploadSessionJpaRepository
        extends JpaRepository<MultipartUploadSessionJpaEntity, String> {}
