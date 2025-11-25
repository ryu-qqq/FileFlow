package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.MultipartUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.CompletedPartQueryDslRepository;
import com.ryuqq.fileflow.application.session.port.out.query.FindCompletedPartQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CompletedPart 조회 Query Adapter.
 *
 * <p>MySQL에서 CompletedPart를 조회합니다.
 */
@Component
public class FindCompletedPartQueryAdapter implements FindCompletedPartQueryPort {

    private final CompletedPartQueryDslRepository repository;
    private final MultipartUploadSessionJpaMapper mapper;

    public FindCompletedPartQueryAdapter(
            CompletedPartQueryDslRepository repository, MultipartUploadSessionJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<CompletedPart> findBySessionIdAndPartNumber(
            UploadSessionId sessionId, int partNumber) {
        String sessionIdValue = sessionId.value().toString();
        return repository
                .findBySessionIdAndPartNumber(sessionIdValue, partNumber)
                .map(mapper::toCompletedPart);
    }

    @Override
    public List<CompletedPart> findAllBySessionId(UploadSessionId sessionId) {
        String sessionIdValue = sessionId.value().toString();
        return repository.findAllBySessionId(sessionIdValue).stream()
                .map(mapper::toCompletedPart)
                .toList();
    }
}
