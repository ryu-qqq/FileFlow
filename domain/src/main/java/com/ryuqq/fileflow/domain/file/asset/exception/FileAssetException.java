package com.ryuqq.fileflow.domain.file.asset.exception;

import com.ryuqq.fileflow.domain.common.DomainException;

import java.util.Map;

/**
 * FileAsset Domain Exception (Base Class)
 *
 * <p>모든 FileAsset 도메인 예외의 부모 클래스입니다.</p>
 *
 * <p><strong>특징:</strong></p>
 * <ul>
 *   <li>DomainException 상속</li>
 *   <li>FileErrorCode와 연동</li>
 *   <li>다국어 메시지 지원 (args Map)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public abstract class FileAssetException extends DomainException {

    private final FileErrorCode errorCode;

    /**
     * FileAssetException 생성자
     *
     * @param errorCode File 에러 코드
     * @param args 에러 컨텍스트 정보
     */
    protected FileAssetException(FileErrorCode errorCode, Map<String, Object> args) {
        super(errorCode.getCode(), errorCode.getDefaultMessage(), args);
        this.errorCode = errorCode;
    }

    /**
     * FileAssetException 생성자 (Cause 포함)
     *
     * @param errorCode File 에러 코드
     * @param args 에러 컨텍스트 정보
     * @param cause 원인 예외
     */
    protected FileAssetException(FileErrorCode errorCode, Map<String, Object> args, Throwable cause) {
        super(errorCode.getCode(), errorCode.getDefaultMessage() + (cause != null ? ": " + cause.getMessage() : ""), args);
        this.errorCode = errorCode;
        initCause(cause);
    }

    /**
     * 에러 코드 반환
     *
     * @return FileErrorCode
     */
    public FileErrorCode getErrorCode() {
        return errorCode;
    }
}

