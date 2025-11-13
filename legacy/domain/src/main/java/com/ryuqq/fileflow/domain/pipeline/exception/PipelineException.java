package com.ryuqq.fileflow.domain.pipeline.exception;

import com.ryuqq.fileflow.domain.common.DomainException;

/**
 * Pipeline Exception
 *
 * <p>Pipeline 바운더리에서 발생하는 모든 예외의 기본 클래스입니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class PipelineException extends DomainException {

    /**
     * ErrorCode 기본 메시지 사용 생성자
     *
     * @param errorCode 에러 코드
     */
    protected PipelineException(PipelineErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * ErrorCode 커스텀 메시지 사용 생성자
     *
     * @param errorCode 에러 코드
     * @param message   커스텀 에러 메시지
     */
    protected PipelineException(PipelineErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * ErrorCode Cause 포함 생성자
     *
     * @param errorCode 에러 코드
     * @param message   커스텀 에러 메시지
     * @param cause     원인 예외
     */
    protected PipelineException(PipelineErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}

