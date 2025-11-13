package com.ryuqq.fileflow.domain.download.fixture;

import com.ryuqq.fileflow.domain.common.OutboxStatus;
import com.ryuqq.fileflow.domain.download.*;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

/**
 * ExternalDownloadOutbox Test Fixture
 *
 * <p>테스트에서 ExternalDownloadOutbox 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 신규 Outbox (ID 없음)
 * ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.createNew();
 *
 * // ID가 있는 Outbox
 * ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.createWithId(1L);
 *
 * // 처리 중인 Outbox
 * ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.createProcessing();
 *
 * // 완료된 Outbox
 * ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.createCompleted();
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 2025-11-02
 */
public class ExternalDownloadOutboxFixture {

    private static final IdempotencyKey DEFAULT_IDEMPOTENCY_KEY = IdempotencyKeyFixture.createSequential(1);
    private static final ExternalDownloadId DEFAULT_DOWNLOAD_ID = ExternalDownloadIdFixture.create(1L);
    private static final UploadSessionId DEFAULT_UPLOAD_SESSION_ID = UploadSessionId.of(1L);

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    private ExternalDownloadOutboxFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 신규 ExternalDownloadOutbox를 생성합니다 (ID 없음).
     *
     * <p>기본값:</p>
     * <ul>
     *   <li>status: PENDING</li>
     *   <li>retryCount: 0</li>
     * </ul>
     *
     * @return ExternalDownloadOutbox 인스턴스 (ID = null)
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ExternalDownloadOutbox createNew() {
        return ExternalDownloadOutbox.forNew(
            DEFAULT_IDEMPOTENCY_KEY,
            DEFAULT_DOWNLOAD_ID,
            DEFAULT_UPLOAD_SESSION_ID
        );
    }

    /**
     * 신규 ExternalDownloadOutbox를 생성합니다 (커스텀 값).
     *
     * @param idempotencyKey 멱등성 키
     * @param downloadId External Download ID
     * @param uploadSessionId Upload Session ID
     * @return ExternalDownloadOutbox 인스턴스 (ID = null)
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ExternalDownloadOutbox createNew(
        IdempotencyKey idempotencyKey,
        ExternalDownloadId downloadId,
        UploadSessionId uploadSessionId
    ) {
        return ExternalDownloadOutbox.forNew(idempotencyKey, downloadId, uploadSessionId);
    }

    /**
     * ID가 있는 ExternalDownloadOutbox를 생성합니다.
     *
     * @param id Outbox ID
     * @return ExternalDownloadOutbox 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ExternalDownloadOutbox createWithId(Long id) {
        return ExternalDownloadOutbox.of(
            new ExternalDownloadOutboxId(id),
            DEFAULT_IDEMPOTENCY_KEY,
            DEFAULT_DOWNLOAD_ID,
            DEFAULT_UPLOAD_SESSION_ID
        );
    }

    /**
     * ID가 있는 ExternalDownloadOutbox를 생성합니다 (커스텀 값).
     *
     * @param id Outbox ID
     * @param idempotencyKey 멱등성 키
     * @param downloadId External Download ID
     * @param uploadSessionId Upload Session ID
     * @return ExternalDownloadOutbox 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ExternalDownloadOutbox createWithId(
        Long id,
        IdempotencyKey idempotencyKey,
        ExternalDownloadId downloadId,
        UploadSessionId uploadSessionId
    ) {
        return ExternalDownloadOutbox.of(
            new ExternalDownloadOutboxId(id),
            idempotencyKey,
            downloadId,
            uploadSessionId
        );
    }

    /**
     * 처리 중인 ExternalDownloadOutbox를 생성합니다 (PROCESSING 상태).
     *
     * @return ExternalDownloadOutbox 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ExternalDownloadOutbox createProcessing() {
        ExternalDownloadOutbox outbox = createNew();
        outbox.startProcessing();
        return outbox;
    }

    /**
     * 완료된 ExternalDownloadOutbox를 생성합니다 (COMPLETED 상태).
     *
     * @return ExternalDownloadOutbox 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ExternalDownloadOutbox createCompleted() {
        ExternalDownloadOutbox outbox = createNew();
        outbox.startProcessing();
        outbox.complete();
        return outbox;
    }

    /**
     * 실패한 ExternalDownloadOutbox를 생성합니다 (FAILED 상태).
     *
     * @return ExternalDownloadOutbox 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ExternalDownloadOutbox createFailed() {
        ExternalDownloadOutbox outbox = createNew();
        outbox.startProcessing();
        outbox.fail();
        return outbox;
    }

    /**
     * 재시도 횟수가 있는 실패한 ExternalDownloadOutbox를 생성합니다.
     *
     * @param retryCount 재시도 횟수
     * @return ExternalDownloadOutbox 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ExternalDownloadOutbox createFailedWithRetry(int retryCount) {
        ExternalDownloadOutbox outbox = createNew();
        for (int i = 0; i < retryCount; i++) {
            outbox.startProcessing();
            outbox.fail();
            if (i < retryCount - 1) {
                outbox.retryFromFailed();
            }
        }
        return outbox;
    }

    /**
     * DB에서 조회된 ExternalDownloadOutbox를 재구성합니다 (reconstitute).
     *
     * @param id Outbox ID
     * @param idempotencyKey 멱등성 키
     * @param downloadId External Download ID
     * @param uploadSessionId Upload Session ID
     * @param status Outbox 상태
     * @param retryCount 재시도 횟수
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @return 재구성된 ExternalDownloadOutbox
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ExternalDownloadOutbox reconstitute(
        Long id,
        IdempotencyKey idempotencyKey,
        ExternalDownloadId downloadId,
        UploadSessionId uploadSessionId,
        OutboxStatus status,
        Integer retryCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return ExternalDownloadOutbox.reconstitute(
            new ExternalDownloadOutboxId(id),
            idempotencyKey,
            downloadId,
            uploadSessionId,
            status,
            retryCount,
            createdAt,
            updatedAt
        );
    }

    /**
     * 기본값으로 재구성된 ExternalDownloadOutbox를 생성합니다.
     *
     * @param id Outbox ID
     * @return ExternalDownloadOutbox 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ExternalDownloadOutbox reconstituteDefault(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return ExternalDownloadOutbox.reconstitute(
            new ExternalDownloadOutboxId(id),
            DEFAULT_IDEMPOTENCY_KEY,
            DEFAULT_DOWNLOAD_ID,
            DEFAULT_UPLOAD_SESSION_ID,
            OutboxStatus.PENDING,
            0,
            now,
            now
        );
    }

    /**
     * 여러 개의 ExternalDownloadOutbox를 생성합니다.
     *
     * <p>ID는 1부터 시작하는 연속된 값을 사용합니다.</p>
     *
     * @param count 생성할 Outbox 개수
     * @return ExternalDownloadOutbox 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static List<ExternalDownloadOutbox> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return IntStream.rangeClosed(1, count)
            .mapToObj(i -> createWithId(
                (long) i,
                IdempotencyKeyFixture.createSequential(i),
                new ExternalDownloadId((long) i),
                UploadSessionId.of((long) i)
            ))
            .toList();
    }

    /**
     * Builder 패턴으로 ExternalDownloadOutbox 생성
     *
     * @return ExternalDownloadOutboxBuilder 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ExternalDownloadOutboxBuilder builder() {
        return new ExternalDownloadOutboxBuilder();
    }

    /**
     * ExternalDownloadOutbox Builder
     *
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static class ExternalDownloadOutboxBuilder {
        private Long id;
        private IdempotencyKey idempotencyKey = DEFAULT_IDEMPOTENCY_KEY;
        private ExternalDownloadId downloadId = DEFAULT_DOWNLOAD_ID;
        private UploadSessionId uploadSessionId = DEFAULT_UPLOAD_SESSION_ID;
        private boolean shouldStartProcessing = false;
        private boolean shouldComplete = false;
        private boolean shouldFail = false;
        private int retryCount = 0;

        public ExternalDownloadOutboxBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ExternalDownloadOutboxBuilder idempotencyKey(IdempotencyKey idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public ExternalDownloadOutboxBuilder downloadId(ExternalDownloadId downloadId) {
            this.downloadId = downloadId;
            return this;
        }

        public ExternalDownloadOutboxBuilder uploadSessionId(UploadSessionId uploadSessionId) {
            this.uploadSessionId = uploadSessionId;
            return this;
        }

        public ExternalDownloadOutboxBuilder startProcessing() {
            this.shouldStartProcessing = true;
            return this;
        }

        public ExternalDownloadOutboxBuilder complete() {
            this.shouldComplete = true;
            return this;
        }

        public ExternalDownloadOutboxBuilder fail() {
            this.shouldFail = true;
            return this;
        }

        public ExternalDownloadOutboxBuilder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public ExternalDownloadOutbox build() {
            ExternalDownloadOutbox outbox;
            
            if (id == null) {
                outbox = ExternalDownloadOutbox.forNew(idempotencyKey, downloadId, uploadSessionId);
            } else {
                outbox = ExternalDownloadOutbox.of(
                    new ExternalDownloadOutboxId(id),
                    idempotencyKey,
                    downloadId,
                    uploadSessionId
                );
            }

            if (shouldStartProcessing) {
                outbox.startProcessing();
            }

            if (shouldComplete) {
                outbox.complete();
            } else if (shouldFail) {
                for (int i = 0; i < retryCount; i++) {
                    outbox.fail();
                    if (i < retryCount - 1) {
                        outbox.retryFromFailed();
                        outbox.startProcessing();
                    }
                }
                if (retryCount == 0) {
                    outbox.fail();
                }
            }

            return outbox;
        }
    }
}
