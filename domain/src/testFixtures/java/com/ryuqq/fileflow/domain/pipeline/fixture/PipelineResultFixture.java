package com.ryuqq.fileflow.domain.pipeline.fixture;

import com.ryuqq.fileflow.domain.pipeline.PipelineResult;

/**
 * PipelineResult Test Fixture
 *
 * <p>테스트에서 PipelineResult 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class PipelineResultFixture {

    private static final String DEFAULT_ERROR_MESSAGE = "Pipeline processing failed";

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private PipelineResultFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 성공 결과 생성
     *
     * @return 성공 결과
     */
    public static PipelineResult success() {
        return PipelineResult.success();
    }

    /**
     * 실패 결과 생성 (기본 에러 메시지)
     *
     * @return 실패 결과
     */
    public static PipelineResult failure() {
        return PipelineResult.failure(DEFAULT_ERROR_MESSAGE);
    }

    /**
     * 실패 결과 생성 (커스텀 에러 메시지)
     *
     * @param errorMessage 에러 메시지
     * @return 실패 결과
     */
    public static PipelineResult failure(String errorMessage) {
        return PipelineResult.failure(errorMessage);
    }

    /**
     * 실패 결과 생성 (예외 기반)
     *
     * @param exception 예외
     * @return 실패 결과
     */
    public static PipelineResult failure(Exception exception) {
        return PipelineResult.failure(exception);
    }
}

