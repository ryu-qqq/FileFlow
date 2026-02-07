package com.ryuqq.fileflow.domain.common.exception;

/**
 * ErrorCode 인터페이스.
 * 도메인별 ErrorCode enum이 이 인터페이스를 구현합니다.
 *
 * <p>getCode() 형식: "{DOMAIN}-{NUMBER}" (예: "SESSION-001", "ASSET-001")
 * <p>getHttpStatus()는 int 반환 (Spring HttpStatus 사용 금지 - DOM-ERR-001)
 */
public interface ErrorCode {

    String getCode();

    int getHttpStatus();

    String getMessage();
}
