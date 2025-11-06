package com.ryuqq.fileflow.domain.pipeline;

/**
 * Pipeline 처리 결과
 *
 * <p>PipelineWorker의 비동기 처리 결과를 나타냅니다.</p>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>PipelineWorker.startPipeline()의 반환 타입</li>
 *   <li>PipelineOutboxScheduler에서 처리 결과에 따른 상태 업데이트</li>
 * </ul>
 *
 * <p><strong>결과 타입:</strong></p>
 * <ul>
 *   <li>SUCCESS: Pipeline 처리 성공</li>
 *   <li>FAILURE: Pipeline 처리 실패 (재시도 가능)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record PipelineResult(
    /**
     * 처리 성공 여부
     */
    boolean success,

    /**
     * 에러 메시지 (실패 시)
     */
    String errorMessage
) {
    /**
     * 성공 결과 생성
     *
     * @return 성공 결과
     */
    public static PipelineResult success() {
        return new PipelineResult(true, null);
    }

    /**
     * 실패 결과 생성
     *
     * @param errorMessage 에러 메시지
     * @return 실패 결과
     */
    public static PipelineResult failure(String errorMessage) {
        return new PipelineResult(false, errorMessage);
    }

    /**
     * 실패 결과 생성 (예외 기반)
     *
     * @param exception 예외
     * @return 실패 결과
     */
    public static PipelineResult failure(Exception exception) {
        return new PipelineResult(false, exception.getMessage());
    }

    /**
     * 성공 여부 확인
     *
     * @return 성공이면 true
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 실패 여부 확인
     *
     * @return 실패이면 true
     */
    public boolean isFailure() {
        return !success;
    }
}

