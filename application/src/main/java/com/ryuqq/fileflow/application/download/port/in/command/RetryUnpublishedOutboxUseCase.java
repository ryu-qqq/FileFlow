package com.ryuqq.fileflow.application.download.port.in.command;

/**
 * 미발행 Outbox 재시도 UseCase.
 *
 * <p>SQS 발행에 실패한 Outbox를 재시도합니다.
 *
 * <p><strong>사용처</strong>: ExternalDownloadOutBoxRetryScheduler
 *
 * @author Development Team
 * @since 1.0.0
 */
public interface RetryUnpublishedOutboxUseCase {

    /**
     * 미발행 Outbox 재시도를 실행합니다.
     *
     * @return 재시도 결과 (성공/실패 카운트 포함)
     */
    RetryResult execute();

    /**
     * 재시도 결과.
     *
     * @param totalRetried 총 재시도 건수
     * @param succeeded 성공 건수
     * @param failed 실패 건수
     * @param iterations 반복 횟수
     */
    record RetryResult(int totalRetried, int succeeded, int failed, int iterations) {

        /**
         * 빈 결과 생성.
         *
         * @return 모든 카운트가 0인 결과
         */
        public static RetryResult empty() {
            return new RetryResult(0, 0, 0, 0);
        }
    }
}
