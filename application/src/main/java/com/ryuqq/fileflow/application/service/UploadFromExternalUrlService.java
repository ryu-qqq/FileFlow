package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.UploadFromExternalUrlCommand;
import com.ryuqq.fileflow.application.port.in.command.UploadFromExternalUrlPort;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 외부 URL 업로드 UseCase 구현
 * <p>
 * Zero-Tolerance 규칙:
 * - URL 검증 (HTTPS만 허용)
 * - @Transactional 내 외부 API 호출 절대 금지
 * - Transaction 경계: File 생성/상태 업데이트만 트랜잭션 내부
 * </p>
 */
@Service
public class UploadFromExternalUrlService implements UploadFromExternalUrlPort {

    @Override
    public void execute(UploadFromExternalUrlCommand command) {
        // 1. URL 검증 (HTTPS만 허용)
        validateUrl(command.externalUrl());
    }

    /**
     * URL 검증
     * <p>
     * HTTPS 프로토콜만 허용합니다.
     * HTTP는 보안상 허용하지 않습니다.
     * </p>
     */
    private void validateUrl(String externalUrl) {
        if (externalUrl == null || !externalUrl.startsWith("https://")) {
            throw new IllegalArgumentException("외부 URL은 HTTPS 프로토콜만 허용됩니다 (현재: " + externalUrl + ")");
        }
    }
}
