package com.ryuqq.fileflow.adapter.rest.controller;

import com.ryuqq.fileflow.adapter.rest.dto.response.UploadStatusApiResponse;
import com.ryuqq.fileflow.application.upload.dto.UploadStatusResponse;
import com.ryuqq.fileflow.application.upload.port.in.GetUploadStatusUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Upload Status REST Controller
 *
 * 업로드 진행률 조회 REST API를 제공합니다.
 * Hexagonal Architecture의 Inbound Adapter로서 동작합니다.
 *
 * 제약사항:
 * - NO Lombok
 * - UseCase만 의존
 * - NO Inner Class
 *
 * @author sangwon-ryu
 */
@RestController
@RequestMapping("/api/v1/upload/sessions")
public class UploadStatusController {

    private final GetUploadStatusUseCase getUploadStatusUseCase;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param getUploadStatusUseCase 업로드 진행률 조회 UseCase
     */
    public UploadStatusController(GetUploadStatusUseCase getUploadStatusUseCase) {
        this.getUploadStatusUseCase = Objects.requireNonNull(
                getUploadStatusUseCase,
                "GetUploadStatusUseCase must not be null"
        );
    }

    /**
     * GET /api/v1/upload/sessions/{sessionId}/status
     * 업로드 세션의 진행 상태를 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 200 OK with UploadStatusApiResponse
     * @throws IllegalArgumentException sessionId가 null이거나 빈 문자열인 경우
     * @throws com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     */
    @GetMapping("/{sessionId}/status")
    public ResponseEntity<UploadStatusApiResponse> getUploadStatus(@PathVariable String sessionId) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");

        UploadStatusResponse response = getUploadStatusUseCase.getUploadStatus(sessionId);

        return ResponseEntity.ok(UploadStatusApiResponse.from(response));
    }
}
