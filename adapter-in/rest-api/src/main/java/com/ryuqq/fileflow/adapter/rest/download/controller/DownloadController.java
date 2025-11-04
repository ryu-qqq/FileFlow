package com.ryuqq.fileflow.adapter.rest.download.controller;

import com.ryuqq.fileflow.adapter.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.rest.download.dto.request.StartDownloadApiRequest;
import com.ryuqq.fileflow.adapter.rest.download.dto.response.DownloadStatusApiResponse;
import com.ryuqq.fileflow.adapter.rest.download.dto.response.StartDownloadApiResponse;
import com.ryuqq.fileflow.adapter.rest.download.mapper.DownloadApiMapper;
import com.ryuqq.fileflow.application.download.dto.command.StartExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.application.download.port.in.GetDownloadStatusUseCase;
import com.ryuqq.fileflow.application.download.port.in.StartExternalDownloadUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Download Controller
 *
 * <p>External Download REST API 엔드포인트를 제공합니다.</p>
 *
 * <p><strong>제공 기능:</strong></p>
 * <ul>
 *   <li>외부 URL 다운로드 시작 (비동기)</li>
 *   <li>다운로드 상태 조회</li>
 * </ul>
 *
 * <p><strong>비동기 처리:</strong></p>
 * <ul>
 *   <li>다운로드는 백그라운드에서 진행됩니다</li>
 *   <li>즉시 202 Accepted 응답을 반환합니다</li>
 *   <li>진행 상태는 status API로 확인할 수 있습니다</li>
 *   <li>실패 시 아웃박스 패턴으로 재시도 가능</li>
 * </ul>
 *
 * <p><strong>공통 헤더:</strong></p>
 * <ul>
 *   <li>{@code X-Tenant-Id}: 테넌트 ID (필수)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.download.base}")
public class DownloadController {

    private final StartExternalDownloadUseCase startExternalDownloadUseCase;
    private final GetDownloadStatusUseCase getDownloadStatusUseCase;
    // DownloadApiMapper는 유틸리티 클래스 (static 메서드)

    /**
     * 생성자
     *
     * @param startExternalDownloadUseCase 외부 다운로드 시작 UseCase
     * @param getDownloadStatusUseCase 다운로드 상태 조회 UseCase
     * @param mapper API Mapper
     */
    public DownloadController(
        StartExternalDownloadUseCase startExternalDownloadUseCase,
        GetDownloadStatusUseCase getDownloadStatusUseCase
    ) {
        this.startExternalDownloadUseCase = startExternalDownloadUseCase;
        this.getDownloadStatusUseCase = getDownloadStatusUseCase;
    }

    /**
     * 외부 URL 다운로드 시작
     *
     * <p><strong>엔드포인트:</strong> {@code POST /api/v1/downloads/external}</p>
     *
     * <p><strong>비동기 처리:</strong></p>
     * <ul>
     *   <li>다운로드 세션을 생성하고 즉시 응답을 반환합니다</li>
     *   <li>실제 다운로드는 백그라운드에서 진행됩니다</li>
     *   <li>진행 상태는 {@code GET /api/v1/downloads/external/{downloadId}/status}로 확인</li>
     * </ul>
     *
     * <p><strong>Request Body 예시:</strong></p>
     * <pre>{@code
     * {
     *   "sourceUrl": "https://example.com/files/document.pdf",
     *   "fileName": "document.pdf"
     * }
     * }</pre>
     *
     * <p><strong>Response 예시:</strong></p>
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "downloadId": 67890,
     *     "uploadSessionId": 12345,
     *     "status": "PENDING"
     *   }
     * }
     * }</pre>
     *
     * <p><strong>아웃박스 패턴:</strong></p>
     * <ul>
     *   <li>다운로드 실패 시 아웃박스 테이블에 기록됩니다</li>
     *   <li>백그라운드 작업자가 주기적으로 재시도합니다</li>
     *   <li>최대 재시도 횟수 초과 시 FAILED 상태로 변경됩니다</li>
     * </ul>
     *
     * @param request 다운로드 시작 요청
     * @param tenantId 테넌트 ID (헤더)
     * @return 다운로드 시작 응답 (202 Accepted)
     */
    @PostMapping("/external")
    public ResponseEntity<ApiResponse<StartDownloadApiResponse>> startExternalDownload(
        @Valid @RequestBody StartDownloadApiRequest request,
        @RequestHeader("X-Tenant-Id") Long tenantId,
        @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        // idempotencyKey가 없으면 UUID로 생성
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            idempotencyKey = java.util.UUID.randomUUID().toString();
        }

        StartExternalDownloadCommand command = DownloadApiMapper.toCommand(request, tenantId, idempotencyKey);
        ExternalDownloadResponse response = startExternalDownloadUseCase.execute(command);
        StartDownloadApiResponse apiResponse = DownloadApiMapper.toApiResponse(response);

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 다운로드 상태 조회
     *
     * <p><strong>엔드포인트:</strong> {@code GET /api/v1/downloads/external/{downloadId}/status}</p>
     *
     * <p><strong>Response 예시:</strong></p>
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "downloadId": 67890,
     *     "status": "DOWNLOADING",
     *     "sourceUrl": "https://example.com/files/document.pdf",
     *     "uploadSessionId": 12345
     *   }
     * }
     * }</pre>
     *
     * <p><strong>상태 값:</strong></p>
     * <ul>
     *   <li>{@code PENDING}: 다운로드 대기 중</li>
     *   <li>{@code DOWNLOADING}: 다운로드 진행 중</li>
     *   <li>{@code COMPLETED}: 다운로드 완료</li>
     *   <li>{@code FAILED}: 다운로드 실패 (아웃박스에서 재시도 예정)</li>
     * </ul>
     *
     * @param downloadId 다운로드 ID
     * @return 다운로드 상태 응답
     */
    @GetMapping("/external/{downloadId}/status")
    public ResponseEntity<ApiResponse<DownloadStatusApiResponse>> getDownloadStatus(
        @PathVariable Long downloadId
    ) {
        ExternalDownloadResponse response = getDownloadStatusUseCase.execute(downloadId);
        DownloadStatusApiResponse apiResponse = DownloadApiMapper.toStatusApiResponse(response);

        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

}
