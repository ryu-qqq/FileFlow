package com.ryuqq.fileflow.adapter.rest.controller;

import com.ryuqq.fileflow.adapter.rest.dto.request.CreateUploadSessionRequest;
import com.ryuqq.fileflow.adapter.rest.dto.response.CreateUploadSessionApiResponse;
import com.ryuqq.fileflow.application.upload.dto.CreateUploadSessionCommand;
import com.ryuqq.fileflow.application.upload.port.in.CreateUploadSessionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Upload Session REST Controller
 *
 * 업로드 세션 생성 및 Presigned URL 발급 REST API를 제공합니다.
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
public class UploadSessionController {

    private final CreateUploadSessionUseCase createUploadSessionUseCase;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param createUploadSessionUseCase 업로드 세션 생성 UseCase
     */
    public UploadSessionController(CreateUploadSessionUseCase createUploadSessionUseCase) {
        this.createUploadSessionUseCase = Objects.requireNonNull(
                createUploadSessionUseCase,
                "CreateUploadSessionUseCase must not be null"
        );
    }

    /**
     * POST /api/v1/upload/sessions
     * 새로운 업로드 세션을 생성하고 Presigned URL을 발급합니다.
     *
     * 비즈니스 플로우:
     * 1. 정책 검증 (Epic 1)
     * 2. 업로드 세션 생성
     * 3. S3 Presigned URL 발급
     * 4. 세션 정보 저장
     *
     * @param request 세션 생성 요청
     * @return 201 CREATED with CreateUploadSessionApiResponse
     * @throws IllegalArgumentException request가 null이거나 유효하지 않은 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException 정책을 찾을 수 없는 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException 정책 검증 실패 시
     */
    @PostMapping
    public ResponseEntity<CreateUploadSessionApiResponse> createUploadSession(
            @Valid @RequestBody CreateUploadSessionRequest request
    ) {
        Objects.requireNonNull(request, "CreateUploadSessionRequest must not be null");

        CreateUploadSessionCommand command = request.toCommand();
        CreateUploadSessionUseCase.UploadSessionWithUrlResponse response =
                createUploadSessionUseCase.createSession(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateUploadSessionApiResponse.from(response));
    }
}
