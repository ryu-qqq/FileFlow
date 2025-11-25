package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.MultipartUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.SingleUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.SessionQueryDslRepository;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.session.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 업로드 세션 조회 Query Adapter.
 *
 * <p>FindUploadSessionQueryPort를 구현하여 RDB에서 세션을 조회합니다.
 */
@Component
public class FindUploadSessionQueryAdapter implements FindUploadSessionQueryPort {

    private final SessionQueryDslRepository queryRepository;
    private final SingleUploadSessionJpaMapper singleMapper;
    private final MultipartUploadSessionJpaMapper multipartMapper;

    public FindUploadSessionQueryAdapter(
            SessionQueryDslRepository queryRepository,
            SingleUploadSessionJpaMapper singleMapper,
            MultipartUploadSessionJpaMapper multipartMapper) {
        this.queryRepository = queryRepository;
        this.singleMapper = singleMapper;
        this.multipartMapper = multipartMapper;
    }

    @Override
    public Optional<SingleUploadSession> findSingleUploadById(UploadSessionId sessionId) {
        String id = sessionId.value().toString();
        return queryRepository.findSingleUploadById(id).map(singleMapper::toDomain);
    }

    @Override
    public Optional<SingleUploadSession> findSingleUploadByIdempotencyKey(
            IdempotencyKey idempotencyKey) {
        String key = idempotencyKey.value().toString();
        return queryRepository.findSingleUploadByIdempotencyKey(key).map(singleMapper::toDomain);
    }

    @Override
    public Optional<MultipartUploadSession> findMultipartUploadById(UploadSessionId sessionId) {
        String id = sessionId.value().toString();
        return queryRepository.findMultipartUploadById(id).map(multipartMapper::toDomain);
    }

    @Override
    public Optional<UploadSession> findById(UploadSessionId sessionId) {
        // 1. Single 세션 먼저 조회
        Optional<SingleUploadSession> single = findSingleUploadById(sessionId);
        if (single.isPresent()) {
            return single.map(s -> s);
        }

        // 2. Multipart 세션 조회
        Optional<MultipartUploadSession> multipart = findMultipartUploadById(sessionId);
        return multipart.map(m -> m);
    }

    @Override
    public List<SingleUploadSession> findExpiredSingleUploads(Instant expiredBefore, int limit) {
        return queryRepository.findExpiredSingleUploads(expiredBefore, limit).stream()
                .map(singleMapper::toDomain)
                .toList();
    }

    @Override
    public List<MultipartUploadSession> findExpiredMultipartUploads(
            Instant expiredBefore, int limit) {
        return queryRepository.findExpiredMultipartUploads(expiredBefore, limit).stream()
                .map(multipartMapper::toDomain)
                .toList();
    }
}
