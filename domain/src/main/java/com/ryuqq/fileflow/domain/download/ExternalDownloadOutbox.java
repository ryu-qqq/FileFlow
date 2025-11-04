package com.ryuqq.fileflow.domain.download;

import com.ryuqq.fileflow.domain.upload.UploadSessionId;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * External Download Outbox Aggregate Root
 *
 * <p>Transactional Outbox Pattern을 구현하는 도메인 객체입니다.</p>
 * <p>External Download 이벤트를 안전하게 발행하기 위해 트랜잭션 내에서 이벤트를 저장하고,
 * 별도 프로세스에서 비동기로 처리합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Law of Demeter - Getter 체이닝 방지</li>
 *   <li>✅ Tell, Don't Ask 패턴 적용</li>
 *   <li>✅ Value Object ID 래핑 (ExternalDownloadOutboxId, IdempotencyKey 등)</li>
 *   <li>✅ Aggregate 경계 내에서 일관성 보장</li>
 * </ul>
 *
 * <h3>Outbox 패턴</h3>
 * <ul>
 *   <li>멱등성 보장: {@link IdempotencyKey}로 중복 이벤트 발행 방지</li>
 *   <li>상태 전이: PENDING → PROCESSING → COMPLETED/FAILED</li>
 *   <li>재시도 메커니즘: 실패 시 재시도 카운터 증가</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class ExternalDownloadOutbox {

    private final ExternalDownloadOutboxId id;
    private final IdempotencyKey idempotencyKey;
    private final ExternalDownloadId downloadId;
    private final UploadSessionId uploadSessionId;
    private final Clock clock;
    private OutboxStatus status;
    private Integer retryCount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 신규 Outbox를 생성합니다 (Static Factory Method).
     *
     * <p><strong>ID 없이 신규 도메인 객체를 생성</strong>합니다 (DB 저장 전 상태).</p>
     * <p>ID = null로 초기화됩니다.</p>
     *
     * <p><strong>사용 시기</strong>: Application Layer에서 External Download 생성 시 Outbox 메시지 저장</p>
     * <p><strong>예시</strong>:</p>
     * <pre>{@code
     * // CreateExternalDownloadService.java (Application Layer)
     * ExternalDownloadOutbox outbox = ExternalDownloadOutbox.forNew(
     *     idempotencyKey,
     *     downloadId,
     *     uploadSessionId
     * );
     * // Persistence Layer에서 저장하면서 ID 자동 생성
     * }</pre>
     *
     * <p><strong>초기 상태</strong>:</p>
     * <ul>
     *   <li>status = PENDING</li>
     *   <li>retryCount = 0</li>
     *   <li>createdAt = 현재 시간</li>
     * </ul>
     *
     * @param idempotencyKey  멱등성 키 (중복 방지)
     * @param downloadId      External Download ID
     * @param uploadSessionId Upload Session ID
     * @return 생성된 ExternalDownloadOutbox (ID = null)
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static ExternalDownloadOutbox forNew(
        IdempotencyKey idempotencyKey,
        ExternalDownloadId downloadId,
        UploadSessionId uploadSessionId
    ) {
        return new ExternalDownloadOutbox(
            null,
            idempotencyKey,
            downloadId,
            uploadSessionId,
            Clock.systemDefaultZone()
        );
    }

    /**
     * Outbox를 생성합니다 (기존 ID 존재, Static Factory Method).
     *
     * <p><strong>ID가 이미 있는 도메인 객체를 생성</strong>합니다.</p>
     *
     * <p><strong>사용 시기</strong>: 테스트 또는 ID가 미리 정해진 특수한 경우</p>
     * <p><strong>주의</strong>: 일반적인 신규 생성에는 {@code forNew()} 사용 권장</p>
     *
     * @param id              Outbox 식별자 (필수)
     * @param idempotencyKey  멱등성 키
     * @param downloadId      External Download ID
     * @param uploadSessionId Upload Session ID
     * @return 생성된 ExternalDownloadOutbox (ID 포함)
     * @throws IllegalArgumentException id가 null이거나 필수 필드가 유효하지 않은 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static ExternalDownloadOutbox of(
        ExternalDownloadOutboxId id,
        IdempotencyKey idempotencyKey,
        ExternalDownloadId downloadId,
        UploadSessionId uploadSessionId
    ) {
        if (id == null) {
            throw new IllegalArgumentException("Outbox ID는 필수입니다");
        }
        return new ExternalDownloadOutbox(
            id,
            idempotencyKey,
            downloadId,
            uploadSessionId,
            Clock.systemDefaultZone()
        );
    }

    /**
     * Outbox 생성자 (package-private).
     *
     * @param id              Outbox 식별자 (null 가능 - 신규 생성 시)
     * @param idempotencyKey  멱등성 키
     * @param downloadId      External Download ID
     * @param uploadSessionId Upload Session ID
     * @param clock           시간 제공자
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    ExternalDownloadOutbox(
        ExternalDownloadOutboxId id,
        IdempotencyKey idempotencyKey,
        ExternalDownloadId downloadId,
        UploadSessionId uploadSessionId,
        Clock clock
    ) {
        validateRequiredFields(idempotencyKey, downloadId, uploadSessionId);

        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.downloadId = downloadId;
        this.uploadSessionId = uploadSessionId;
        this.clock = clock;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id              Outbox ID
     * @param idempotencyKey  멱등성 키
     * @param downloadId      External Download ID
     * @param uploadSessionId Upload Session ID
     * @param status          Outbox 상태
     * @param retryCount      재시도 횟수
     * @param clock           시간 제공자
     * @param createdAt       생성 일시
     * @param updatedAt       수정 일시
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private ExternalDownloadOutbox(
        ExternalDownloadOutboxId id,
        IdempotencyKey idempotencyKey,
        ExternalDownloadId downloadId,
        UploadSessionId uploadSessionId,
        OutboxStatus status,
        Integer retryCount,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.downloadId = downloadId;
        this.uploadSessionId = uploadSessionId;
        this.status = status;
        this.retryCount = retryCount;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * DB에서 조회한 데이터로 Outbox 재구성 (Static Factory Method)
     *
     * <p><strong>Persistence Layer → Domain Layer 변환 전용</strong></p>
     * <p>DB에서 조회한 데이터를 Domain 객체로 복원할 때 사용합니다.</p>
     *
     * <p><strong>사용 시기</strong>: Persistence Layer에서 JPA Entity → Domain 변환 시</p>
     *
     * @param id              Outbox ID (필수 - DB에서 조회된 ID)
     * @param idempotencyKey  멱등성 키
     * @param downloadId      External Download ID
     * @param uploadSessionId Upload Session ID
     * @param status          Outbox 상태
     * @param retryCount      재시도 횟수
     * @param createdAt       생성 일시
     * @param updatedAt       수정 일시
     * @return 재구성된 ExternalDownloadOutbox
     * @throws IllegalArgumentException id가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static ExternalDownloadOutbox reconstitute(
        ExternalDownloadOutboxId id,
        IdempotencyKey idempotencyKey,
        ExternalDownloadId downloadId,
        UploadSessionId uploadSessionId,
        OutboxStatus status,
        Integer retryCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new ExternalDownloadOutbox(
            id,
            idempotencyKey,
            downloadId,
            uploadSessionId,
            status,
            retryCount,
            Clock.systemDefaultZone(),
            createdAt,
            updatedAt
        );
    }

    /**
     * 필수 필드의 유효성을 검증합니다.
     *
     * @param idempotencyKey  멱등성 키
     * @param downloadId      External Download ID
     * @param uploadSessionId Upload Session ID
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private static void validateRequiredFields(
        IdempotencyKey idempotencyKey,
        ExternalDownloadId downloadId,
        UploadSessionId uploadSessionId
    ) {
        if (idempotencyKey == null) {
            throw new IllegalArgumentException("Idempotency Key는 필수입니다");
        }
        if (downloadId == null) {
            throw new IllegalArgumentException("Download ID는 필수입니다");
        }
        if (uploadSessionId == null) {
            throw new IllegalArgumentException("Upload Session ID는 필수입니다");
        }
    }

    /**
     * Outbox 처리를 시작합니다 (PENDING → PROCESSING).
     *
     * <p>Law of Demeter 준수: 상태 변경 로직을 캡슐화합니다.</p>
     * <p>Tell, Don't Ask: 외부에서 상태를 확인하지 않고 객체에 명령합니다.</p>
     *
     * @throws IllegalStateException PENDING 상태가 아닌 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void startProcessing() {
        if (this.status != OutboxStatus.PENDING) {
            throw new IllegalStateException(
                "PENDING 상태에서만 처리를 시작할 수 있습니다. 현재 상태: " + this.status
            );
        }
        this.status = OutboxStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Outbox 처리를 완료합니다 (PROCESSING → COMPLETED).
     *
     * <p>Law of Demeter 준수: 상태 변경 로직을 캡슐화합니다.</p>
     *
     * @throws IllegalStateException PROCESSING 상태가 아닌 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void complete() {
        if (this.status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException(
                "PROCESSING 상태에서만 완료할 수 있습니다. 현재 상태: " + this.status
            );
        }
        this.status = OutboxStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Outbox 처리를 실패 처리하고 재시도 카운터를 증가시킵니다 (PROCESSING → FAILED).
     *
     * <p>Law of Demeter 준수: 상태 변경 로직을 캡슐화합니다.</p>
     * <p>재시도 카운터를 자동으로 증가시킵니다.</p>
     *
     * @throws IllegalStateException PROCESSING 상태가 아닌 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void fail() {
        if (this.status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException(
                "PROCESSING 상태에서만 실패 처리할 수 있습니다. 현재 상태: " + this.status
            );
        }
        this.status = OutboxStatus.FAILED;
        this.retryCount++;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 재시도를 위해 상태를 PENDING으로 되돌립니다 (FAILED → PENDING).
     *
     * <p>Law of Demeter 준수: 상태 변경 로직을 캡슐화합니다.</p>
     *
     * @throws IllegalStateException FAILED 상태가 아닌 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void retryFromFailed() {
        if (this.status != OutboxStatus.FAILED) {
            throw new IllegalStateException(
                "FAILED 상태에서만 재시도할 수 있습니다. 현재 상태: " + this.status
            );
        }
        this.status = OutboxStatus.PENDING;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Outbox를 처리 중 상태로 변경합니다 (Manager 호환용).
     *
     * <p>내부적으로 startProcessing()을 호출합니다.</p>
     * <p>ExternalDownloadOutboxManager와의 호환성을 위해 제공됩니다.</p>
     *
     * @throws IllegalStateException PENDING 상태가 아닌 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void markProcessing() {
        startProcessing();
    }

    /**
     * Outbox를 처리 완료 상태로 변경합니다 (Manager 호환용).
     *
     * <p>내부적으로 complete()를 호출합니다.</p>
     * <p>ExternalDownloadOutboxManager와의 호환성을 위해 제공됩니다.</p>
     *
     * @throws IllegalStateException PROCESSING 상태가 아닌 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void markProcessed() {
        complete();
    }

    /**
     * Outbox를 실패 상태로 변경하고 에러 메시지를 저장합니다 (Manager 호환용).
     *
     * <p>내부적으로 fail()을 호출합니다.</p>
     * <p>현재 버전에서는 에러 메시지를 저장하지 않으나, 향후 확장을 위해 파라미터를 받습니다.</p>
     *
     * @param errorMessage 에러 메시지 (현재 사용하지 않음)
     * @throws IllegalStateException PROCESSING 상태가 아닌 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void markFailed(String errorMessage) {
        // TODO: 향후 에러 메시지 필드 추가 고려
        fail();
    }

    /**
     * Outbox를 영구 실패 상태로 변경합니다 (Manager 호환용).
     *
     * <p>최대 재시도 횟수를 초과한 경우 사용됩니다.</p>
     * <p>상태를 FAILED로 설정하고 추가 재시도를 방지합니다.</p>
     *
     * @param errorMessage 최종 에러 메시지 (현재 사용하지 않음)
     * @throws IllegalStateException PROCESSING 상태가 아닌 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void markPermanentlyFailed(String errorMessage) {
        // TODO: 향후 영구 실패 상태 추가 고려 (예: PERMANENTLY_FAILED)
        // 현재는 FAILED 상태로 처리하되, 재시도 로직에서 retryCount로 제어
        if (this.status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException(
                "PROCESSING 상태에서만 영구 실패 처리할 수 있습니다. 현재 상태: " + this.status
            );
        }
        this.status = OutboxStatus.FAILED;
        // 영구 실패를 나타내기 위해 retryCount를 크게 설정
        this.retryCount = Integer.MAX_VALUE;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 특정 상태인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 상태 확인 로직 캡슐화</p>
     * <p>❌ Bad: outbox.getStatus() == OutboxStatus.PENDING</p>
     * <p>✅ Good: outbox.isStatus(OutboxStatus.PENDING)</p>
     *
     * @param targetStatus 확인할 상태
     * @return 동일한 상태이면 true
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public boolean isStatus(OutboxStatus targetStatus) {
        return this.status == targetStatus;
    }

    /**
     * PENDING 상태인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 상태 확인 로직 캡슐화</p>
     *
     * @return PENDING 상태이면 true
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public boolean isPending() {
        return this.status == OutboxStatus.PENDING;
    }

    /**
     * PROCESSING 상태인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 상태 확인 로직 캡슐화</p>
     *
     * @return PROCESSING 상태이면 true
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public boolean isProcessing() {
        return this.status == OutboxStatus.PROCESSING;
    }

    /**
     * COMPLETED 상태인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 상태 확인 로직 캡슐화</p>
     *
     * @return COMPLETED 상태이면 true
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public boolean isCompleted() {
        return this.status == OutboxStatus.COMPLETED;
    }

    /**
     * FAILED 상태인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 상태 확인 로직 캡슐화</p>
     *
     * @return FAILED 상태이면 true
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public boolean isFailed() {
        return this.status == OutboxStatus.FAILED;
    }

    /**
     * 특정 Download ID를 가지는지 확인합니다.
     *
     * <p>Law of Demeter 준수: ID 비교 로직 캡슐화</p>
     * <p>❌ Bad: outbox.getDownloadId().equals(downloadId)</p>
     * <p>✅ Good: outbox.hasDownloadId(downloadId)</p>
     *
     * @param targetDownloadId 확인할 Download ID
     * @return 동일한 Download ID를 가지면 true
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public boolean hasDownloadId(ExternalDownloadId targetDownloadId) {
        if (targetDownloadId == null) {
            return false;
        }
        return this.downloadId.equals(targetDownloadId);
    }

    /**
     * 특정 Upload Session ID를 가지는지 확인합니다.
     *
     * <p>Law of Demeter 준수: ID 비교 로직 캡슐화</p>
     *
     * @param targetUploadSessionId 확인할 Upload Session ID
     * @return 동일한 Upload Session ID를 가지면 true
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public boolean hasUploadSessionId(UploadSessionId targetUploadSessionId) {
        if (targetUploadSessionId == null) {
            return false;
        }
        return this.uploadSessionId.equals(targetUploadSessionId);
    }

    /**
     * 특정 Idempotency Key를 가지는지 확인합니다.
     *
     * <p>Law of Demeter 준수: 키 비교 로직 캡슐화</p>
     *
     * @param targetKey 확인할 Idempotency Key
     * @return 동일한 키를 가지면 true
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public boolean hasIdempotencyKey(IdempotencyKey targetKey) {
        return this.idempotencyKey.isSameAs(targetKey);
    }

    /**
     * Outbox ID를 반환합니다.
     *
     * @return Outbox ID
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public ExternalDownloadOutboxId getId() {
        return id;
    }

    /**
     * Outbox ID 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: outbox.getId().value()</p>
     * <p>✅ Good: outbox.getIdValue()</p>
     *
     * @return Outbox ID 원시 값
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    /**
     * Idempotency Key를 반환합니다.
     *
     * @return Idempotency Key
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public IdempotencyKey getIdempotencyKey() {
        return idempotencyKey;
    }

    /**
     * Idempotency Key 문자열 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: outbox.getIdempotencyKey().value()</p>
     * <p>✅ Good: outbox.getIdempotencyKeyValue()</p>
     *
     * @return Idempotency Key 문자열 값
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public String getIdempotencyKeyValue() {
        return idempotencyKey.value();
    }

    /**
     * External Download ID를 반환합니다.
     *
     * @return External Download ID
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public ExternalDownloadId getDownloadId() {
        return downloadId;
    }

    /**
     * External Download ID 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: outbox.getDownloadId().value()</p>
     * <p>✅ Good: outbox.getDownloadIdValue()</p>
     *
     * @return External Download ID 원시 값
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Long getDownloadIdValue() {
        return downloadId.value();
    }

    /**
     * Upload Session ID를 반환합니다.
     *
     * @return Upload Session ID
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public UploadSessionId getUploadSessionId() {
        return uploadSessionId;
    }

    /**
     * Upload Session ID 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: outbox.getUploadSessionId().value()</p>
     * <p>✅ Good: outbox.getUploadSessionIdValue()</p>
     *
     * @return Upload Session ID 원시 값
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Long getUploadSessionIdValue() {
        return uploadSessionId.value();
    }

    /**
     * Outbox 상태를 반환합니다.
     *
     * @return Outbox 상태
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public OutboxStatus getStatus() {
        return status;
    }

    /**
     * 재시도 횟수를 반환합니다.
     *
     * @return 재시도 횟수
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * 생성 시각을 반환합니다.
     *
     * @return 생성 시각
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 최종 수정 시각을 반환합니다.
     *
     * @return 최종 수정 시각
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 동등성을 비교합니다.
     *
     * <p>동일성은 ID로만 판단합니다 (Aggregate 식별자 기반).</p>
     *
     * @param o 비교 대상 객체
     * @return 동등 여부
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExternalDownloadOutbox that = (ExternalDownloadOutbox) o;
        return Objects.equals(id, that.id);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * <p>해시코드는 ID로만 계산합니다.</p>
     *
     * @return 해시코드
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return ExternalDownloadOutbox 정보 문자열
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return "ExternalDownloadOutbox{" +
            "id=" + id +
            ", idempotencyKey=" + idempotencyKey +
            ", downloadId=" + downloadId +
            ", uploadSessionId=" + uploadSessionId +
            ", status=" + status +
            ", retryCount=" + retryCount +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
