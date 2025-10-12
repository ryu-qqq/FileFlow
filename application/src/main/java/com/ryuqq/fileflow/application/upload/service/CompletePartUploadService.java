package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.port.in.CompletePartUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.out.MultipartProgressPort;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.MultipartUploadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 멀티파트 업로드 파트 완료 처리 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * 개별 파트 업로드 완료를 처리하고 Redis에 진행 상태를 기록합니다.
 *
 * 처리 흐름:
 * 1. 세션 조회 및 검증
 * 2. 멀티파트 업로드 여부 확인
 * 3. 파트 번호 유효성 검증
 * 4. Redis에 파트 완료 상태 기록
 *
 * 설계 원칙:
 * - Best Effort: Redis 저장 실패해도 업로드는 계속 진행
 * - 멀티파트 업로드가 아닌 경우 예외 발생
 * - 유효하지 않은 파트 번호인 경우 예외 발생
 *
 * @author sangwon-ryu
 */
public class CompletePartUploadService implements CompletePartUploadUseCase {

    private static final Logger log = LoggerFactory.getLogger(CompletePartUploadService.class);

    private final UploadSessionPort uploadSessionPort;
    private final MultipartProgressPort multipartProgressPort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param uploadSessionPort 세션 저장소
     * @param multipartProgressPort 멀티파트 진행률 추적 Port
     */
    public CompletePartUploadService(
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
     * 멀티파트 업로드의 개별 파트 완료를 처리합니다.
     *
     * @param command 파트 완료 Command
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     * @throws IllegalStateException 멀티파트 업로드가 아니거나 유효하지 않은 파트 번호인 경우
     */
    @Override
    public void completePart(CompletePartCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CompletePartCommand must not be null");
        }

        log.info("Processing part completion: sessionId={}, partNumber={}",
                command.sessionId(), command.partNumber());

        // 1. 세션 조회
        UploadSession session = findSessionById(command.sessionId());

        // 2. 멀티파트 업로드 여부 확인
        MultipartUploadInfo multipartInfo = session.getMultipartUploadInfo()
                .orElseThrow(() -> new IllegalStateException(
                        "Session " + command.sessionId() + " is not a multipart upload"
                ));

        // 3. 파트 번호 유효성 검증
        validatePartNumber(command.partNumber(), multipartInfo.totalParts());

        // 4. Redis에 파트 완료 상태 기록
        multipartProgressPort.markPartCompleted(command.sessionId(), command.partNumber());

        log.info("Successfully marked part {} as completed for session {}",
                command.partNumber(), command.sessionId());
    }

    // ========== Helper Methods ==========

    /**
     * 세션 ID를 검증하고 세션을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 조회된 UploadSession
     * @throws IllegalArgumentException sessionId가 null이거나 빈 문자열인 경우
     * @throws UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     */
    private UploadSession findSessionById(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId must not be null or empty");
        }
        return uploadSessionPort.findById(sessionId)
                .orElseThrow(() -> new UploadSessionNotFoundException(sessionId));
    }

    /**
     * 파트 번호 유효성을 검증합니다.
     *
     * @param partNumber 파트 번호
     * @param totalParts 전체 파트 수
     * @throws IllegalStateException 파트 번호가 유효 범위를 벗어난 경우
     */
    private void validatePartNumber(int partNumber, int totalParts) {
        if (partNumber < 1 || partNumber > totalParts) {
            throw new IllegalStateException(
                    String.format(
                            "Invalid part number: %d. Valid range: 1 to %d",
                            partNumber,
                            totalParts
                    )
            );
        }
    }
}
