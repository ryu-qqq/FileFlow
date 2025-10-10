package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.dto.UploadStatusResponse;
import com.ryuqq.fileflow.application.upload.port.in.GetUploadStatusUseCase;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 업로드 진행률 조회 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * 업로드 세션의 진행 상태를 조회하는 비즈니스 로직을 처리합니다.
 *
 * @author sangwon-ryu
 */
@Service
public class GetUploadStatusService implements GetUploadStatusUseCase {

    private final UploadSessionPort uploadSessionPort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param uploadSessionPort 세션 저장소
     */
    public GetUploadStatusService(UploadSessionPort uploadSessionPort) {
        this.uploadSessionPort = Objects.requireNonNull(
                uploadSessionPort,
                "UploadSessionPort must not be null"
        );
    }

    /**
     * 세션 ID로 업로드 진행 상태를 조회합니다.
     *
     * 비즈니스 로직:
     * 1. 세션 ID 검증
     * 2. 세션 조회
     * 3. 진행 상태 Response 생성 (진행률은 DTO에서 자동 계산)
     *
     * @param sessionId 세션 ID
     * @return 업로드 진행 상태 정보
     * @throws IllegalArgumentException sessionId가 null이거나 빈 문자열인 경우
     * @throws UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     */
    @Override
    public UploadStatusResponse getUploadStatus(String sessionId) {
        // 1. 세션 ID 검증
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("SessionId must not be null or blank");
        }

        // 2. 세션 조회
        UploadSession session = uploadSessionPort.findById(sessionId)
                .orElseThrow(() -> new UploadSessionNotFoundException(sessionId));

        // 3. 진행 상태 Response 생성
        return UploadStatusResponse.from(session);
    }
}
