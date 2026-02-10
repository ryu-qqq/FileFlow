package com.ryuqq.fileflow.adapter.out.client.s3.mapper;

import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MultipartUploadS3Mapper {

    public List<software.amazon.awssdk.services.s3.model.CompletedPart> toS3Parts(
            List<CompletedPart> domainParts) {
        return domainParts.stream().map(this::toS3Part).toList();
    }

    private software.amazon.awssdk.services.s3.model.CompletedPart toS3Part(
            CompletedPart domainPart) {
        return software.amazon.awssdk.services.s3.model.CompletedPart.builder()
                .partNumber(domainPart.partNumber())
                .eTag(domainPart.etag())
                .build();
    }
}
