package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.ExternalDownloadOutboxJpaEntity;
import com.ryuqq.fileflow.domain.download.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.ExternalDownloadOutboxId;
import com.ryuqq.fileflow.domain.download.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;

/**
 * External Download Outbox Entity Mapper
 *
 * <p>Domain {@link ExternalDownloadOutbox} ↔ JPA {@link ExternalDownloadOutboxJpaEntity} 변환을 담당하는 Stateless Utility 클래스입니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>Stateless: 상태를 저장하지 않는 순수 변환 로직</li>
 *   <li>Pure Function: 동일한 입력에 항상 동일한 출력</li>
 *   <li>Law of Demeter 준수: {@code outbox.getIdValue()}, {@code outbox.getIdempotencyKeyValue()} 사용</li>
 *   <li>Final 클래스, Private 생성자: 인스턴스화 방지</li>
 * </ul>
 *
 * <h3>Outbox 패턴 매핑</h3>
 * <ul>
 *   <li>Domain → JPA: Value Object → 원시 타입 (Long, String, Enum)</li>
 *   <li>JPA → Domain: 원시 타입 → Value Object (reconstitute)</li>
 *   <li>멱등성 키: IdempotencyKey ↔ String</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class ExternalDownloadOutboxEntityMapper {

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @throws AssertionError 인스턴스 생성 시도 시
     */
    private ExternalDownloadOutboxEntityMapper() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * JPA Entity → Domain 변환
     *
     * <p><strong>변환 과정:</strong></p>
     * <ol>
     *   <li>Entity getter로 원시 타입 추출 (Long, String, Enum)</li>
     *   <li>Value Object Static Factory Method 호출</li>
     *   <li>Domain reconstitute() 호출 (기존 데이터 복원)</li>
     * </ol>
     *
     * <p><strong>Law of Demeter 준수:</strong></p>
     * <ul>
     *   <li>ExternalDownloadOutboxId.of(Long) - ID Value Object 생성</li>
     *   <li>IdempotencyKey(String) - Record 생성자</li>
     *   <li>ExternalDownloadId.of(Long) - Download ID Value Object 생성</li>
     *   <li>UploadSessionId.of(Long) - Upload Session ID Value Object 생성</li>
     * </ul>
     *
     * @param entity JPA Entity ({@link ExternalDownloadOutboxJpaEntity})
     * @return Domain Aggregate ({@link ExternalDownloadOutbox})
     * @throws IllegalArgumentException entity가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static ExternalDownloadOutbox toDomain(ExternalDownloadOutboxJpaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ExternalDownloadOutboxJpaEntity must not be null");
        }

        // 1. Value Object 생성 (Static Factory Method)
        ExternalDownloadOutboxId outboxId = new ExternalDownloadOutboxId(entity.getId());
        IdempotencyKey idempotencyKey = new IdempotencyKey(entity.getIdempotencyKey());
        ExternalDownloadId downloadId = new ExternalDownloadId(entity.getDownloadId());
        UploadSessionId uploadSessionId = UploadSessionId.of(entity.getUploadSessionId());

        // 2. Domain reconstitute (DB 데이터 복원)
        return ExternalDownloadOutbox.reconstitute(
            outboxId,
            idempotencyKey,
            downloadId,
            uploadSessionId,
            entity.getStatus(),
            entity.getRetryCount(),
            entity.getCreatedAt(),
            entity.getCreatedAt()  // Entity에 updatedAt 필드 없음, createdAt 사용
        );
    }

    /**
     * Domain → JPA Entity 변환
     *
     * <p><strong>변환 과정:</strong></p>
     * <ol>
     *   <li>Domain getter로 Value Object 추출</li>
     *   <li>Law of Demeter 준수 메서드로 원시 타입 추출</li>
     *   <li>Entity Static Factory Method 호출</li>
     * </ol>
     *
     * <p><strong>Law of Demeter 준수:</strong></p>
     * <ul>
     *   <li>❌ Bad: {@code outbox.getId().value()}</li>
     *   <li>✅ Good: {@code outbox.getIdValue()}</li>
     *   <li>❌ Bad: {@code outbox.getIdempotencyKey().value()}</li>
     *   <li>✅ Good: {@code outbox.getIdempotencyKeyValue()}</li>
     *   <li>❌ Bad: {@code outbox.getDownloadId().value()}</li>
     *   <li>✅ Good: {@code outbox.getDownloadIdValue()}</li>
     *   <li>❌ Bad: {@code outbox.getUploadSessionId().value()}</li>
     *   <li>✅ Good: {@code outbox.getUploadSessionIdValue()}</li>
     * </ul>
     *
     * <p><strong>신규 생성 vs 기존 데이터 구분:</strong></p>
     * <ul>
     *   <li>신규 생성 (ID == null): {@code ExternalDownloadOutboxJpaEntity.create()}</li>
     *   <li>기존 데이터 (ID != null): {@code ExternalDownloadOutboxJpaEntity.reconstitute()}</li>
     * </ul>
     *
     * @param outbox Domain Aggregate ({@link ExternalDownloadOutbox})
     * @return JPA Entity ({@link ExternalDownloadOutboxJpaEntity})
     * @throws IllegalArgumentException outbox가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static ExternalDownloadOutboxJpaEntity toEntity(ExternalDownloadOutbox outbox) {
        if (outbox == null) {
            throw new IllegalArgumentException("ExternalDownloadOutbox must not be null");
        }

        // 신규 생성 vs 기존 데이터 업데이트 구분
        if (outbox.getIdValue() == null) {
            // 신규 생성 (Law of Demeter 준수)
            return ExternalDownloadOutboxJpaEntity.create(
                outbox.getIdempotencyKeyValue(),  // String
                outbox.getDownloadIdValue(),      // Long
                outbox.getUploadSessionIdValue()  // Long
            );
        } else {
            // 기존 데이터 reconstitute (Law of Demeter 준수)
            return ExternalDownloadOutboxJpaEntity.reconstitute(
                outbox.getIdValue(),              // Long
                outbox.getIdempotencyKeyValue(),  // String
                outbox.getDownloadIdValue(),      // Long
                outbox.getUploadSessionIdValue(), // Long
                outbox.getStatus(),               // OutboxStatus (Enum)
                outbox.getRetryCount(),           // Integer
                outbox.getCreatedAt()             // LocalDateTime (Entity는 updatedAt 파라미터 없음)
            );
        }
    }
}
