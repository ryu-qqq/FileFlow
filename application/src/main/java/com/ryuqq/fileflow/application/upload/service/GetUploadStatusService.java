package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.dto.UploadStatusResponse;
import com.ryuqq.fileflow.application.upload.port.in.GetUploadStatusUseCase;
import com.ryuqq.fileflow.application.upload.port.out.MultipartProgressPort;
import com.ryuqq.fileflow.application.upload.port.out.MultipartProgressPort.MultipartProgress;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 업로드 진행률 조회 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * 업로드 세션의 진행 상태를 조회하는 비즈니스 로직을 처리합니다.
 *
 * 진행률 계산 로직:
 * - 단일 파일 업로드: 상태 기반 (PENDING=0%, UPLOADING=50%, COMPLETED=100%)
 * - 멀티파트 업로드: Redis 기반 실시간 진행률 (완료된 파트 수 / 전체 파트 수)
 *
 * @author sangwon-ryu
 */
@Service
public class GetUploadStatusService implements GetUploadStatusUseCase {

    private final UploadSessionPort uploadSessionPort;
    private final MultipartProgressPort multipartProgressPort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param uploadSessionPort 세션 저장소
     * @param multipartProgressPort 멀티파트 진행률 추적 Port
     */
    public GetUploadStatusService(
            UploadSessionPort uploadSessionPort,
            MultipartProgressPort multipartProgressPort
    ) {
        this.uploadSessionPort = Objects.requireNonNull(
                uploadSessionPort,
                "UploadSessionPort must not be null"
        );
        this.multipartProgressPort = Objects.requireNonNull(
                multipartProgressPort,
                "MultipartProgressPort must not be null"
        );
    }

    /**
     * 세션 ID로 업로드 진행 상태를 조회합니다.
     *
     * 비즈니스 로직:
     * 1. 세션 ID 검증
     * 2. 세션 조회
     * 3. 진행률 계산
     *    - 멀티파트 업로드: Redis에서 실제 완료된 파트 수 조회
     *    - 단일 파일 업로드: 상태 기반 진행률 사용
     * 4. 진행 상태 Response 생성
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

        // 3. 진행률 계산
        int actualProgress = calculateProgress(session);

        // 4. 진행 상태 Response 생성
        return UploadStatusResponse.fromWithProgress(session, actualProgress);
    }

    /**
     * 업로드 세션의 실제 진행률을 계산합니다.
     *
     * 진행률 계산 전략:
     * - 멀티파트 업로드: Redis에서 완료된 파트 수 기반으로 계산
     * - 단일 파일 업로드: 상태 기반 진행률 (PENDING=0%, UPLOADING=50%, COMPLETED=100%)
     *
     * @param session 업로드 세션
     * @return 진행률 (0-100)
     */
    private int calculateProgress(UploadSession session) {
        // 멀티파트 업로드인 경우 Redis에서 실제 진행률 조회
        if (session.isMultipartUpload()) {
            return calculateMultipartProgress(session);
        }

        // 단일 파일 업로드는 상태 기반 진행률 사용
        return calculateStatusBasedProgress(session.getStatus());
    }

    /**
     * 멀티파트 업로드의 실제 진행률을 계산합니다.
     *
     * Redis에서 완료된 파트 수를 조회하여 실제 진행률을 계산합니다.
     * Redis 조회 실패 시 상태 기반 진행률로 폴백합니다.
     *
     * @param session 멀티파트 업로드 세션
     * @return 진행률 (0-100)
     */
    private int calculateMultipartProgress(UploadSession session) {
        try {
            MultipartProgress progress = multipartProgressPort.getProgress(session.getSessionId());

            // Redis에 진행 상태가 있으면 실제 진행률 반환
            if (progress.totalParts() > 0) {
                return progress.getProgressPercentage();
            }

            // Redis에 진행 상태가 없으면 상태 기반 진행률로 폴백
            return calculateStatusBasedProgress(session.getStatus());
        } catch (Exception e) {
            // Best Effort: Redis 조회 실패 시 상태 기반 진행률로 폴백
            return calculateStatusBasedProgress(session.getStatus());
        }
    }

    /**
     * 업로드 상태 기반 진행률을 계산합니다.
     *
     * 단일 파일 업로드 또는 멀티파트 진행률 조회 실패 시 사용됩니다.
     *
     * @param status 업로드 상태
     * @return 진행률 (0-100)
     */
    private int calculateStatusBasedProgress(UploadStatus status) {
        return switch (status) {
            case PENDING -> 0;
            case UPLOADING -> 50;
            case COMPLETED -> 100;
            case FAILED, CANCELLED -> 0;
        };
    }
}
