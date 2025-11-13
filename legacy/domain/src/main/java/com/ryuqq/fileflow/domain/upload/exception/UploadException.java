package com.ryuqq.fileflow.domain.upload.exception;

import com.ryuqq.fileflow.domain.common.DomainException;

import java.util.Map;

/**
 * Upload 도메인 예외 Base Class
 *
 * <p>Upload 바운디드 컨텍스트에서 발생하는 모든 도메인 예외의 Base Class입니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>DomainException 상속: 전체 도메인 예외 체계와 통합</li>
 *   <li>UploadErrorCode 활용: 타입 안전성 보장</li>
 *   <li>Immutable: 예외 객체 생성 후 변경 불가</li>
 *   <li>Context 정보 포함: args Map으로 상세 정보 전달</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * throw new UploadSessionNotFoundException(sessionKey);
 * throw new InvalidUploadSessionStateException(current, expected);
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadException extends DomainException {

    /**
     * UploadException 생성자
     *
     * <p>UploadErrorCode를 받아 DomainException으로 변환합니다.</p>
     *
     * @param errorCode Upload 에러 코드
     * @param args 에러 컨텍스트 정보 (null 가능)
     */
    public UploadException(UploadErrorCode errorCode, Map<String, Object> args) {
        super(errorCode.code(), errorCode.message(), args);
    }

    /**
     * UploadException 생성자 (args 없는 버전)
     *
     * @param errorCode Upload 에러 코드
     */
    public UploadException(UploadErrorCode errorCode) {
        super(errorCode.code(), errorCode.message(), null);
    }
}
