package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadResponse;
import com.ryuqq.fileflow.application.upload.port.in.ConfirmUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.application.upload.port.out.VerifyS3ObjectPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.exception.ChecksumMismatchException;
import com.ryuqq.fileflow.domain.upload.exception.FileNotFoundInS3Exception;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.S3Location;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 업로드 완료 확인 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * 클라이언트의 업로드 완료 알림을 처리하고 S3에서 파일 존재 및 무결성을 검증합니다.
 *
 * 처리 흐름:
 * 1. 세션 조회
 * 2. 세션 상태 검증 (PENDING 상태여야 함)
 * 3. S3에 파일 존재 확인
 * 4. SHA-256 체크섬 검증 (ChecksumVerificationService 사용)
 * 5. ETag 검증 (선택적, 하위 호환성)
 * 6. 세션 상태를 COMPLETED로 업데이트
 *
 * @author sangwon-ryu
 */
@Service
public class ConfirmUploadService implements ConfirmUploadUseCase {

    private final UploadSessionPort uploadSessionPort;
    private final VerifyS3ObjectPort verifyS3ObjectPort;
    private final ChecksumVerificationService checksumVerificationService;
    private final String s3BucketName;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param uploadSessionPort 세션 저장소
     * @param verifyS3ObjectPort S3 검증 Port
     * @param checksumVerificationService 체크섬 검증 서비스
     * @param s3BucketName S3 버킷명 (외부 설정에서 주입)
     */
    public ConfirmUploadService(
            UploadSessionPort uploadSessionPort,
            VerifyS3ObjectPort verifyS3ObjectPort,
            ChecksumVerificationService checksumVerificationService,
            @org.springframework.beans.factory.annotation.Value("${aws.s3.bucket-name}") String s3BucketName
    ) {
        this.uploadSessionPort = Objects.requireNonNull(
                uploadSessionPort,
                "UploadSessionPort must not be null"
        );
        this.verifyS3ObjectPort = Objects.requireNonNull(
                verifyS3ObjectPort,
                "VerifyS3ObjectPort must not be null"
        );
        this.checksumVerificationService = Objects.requireNonNull(
                checksumVerificationService,
                "ChecksumVerificationService must not be null"
        );
        this.s3BucketName = Objects.requireNonNull(
                s3BucketName,
                "s3BucketName must not be null"
        );
    }

    /**
     * 클라이언트의 업로드 완료를 확인하고 세션을 완료 처리합니다.
     *
     * @param command 업로드 완료 확인 Command
     * @return 확인 결과 응답
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws UploadSessionNotFoundException 세션을 찾을 수 없는 경우
     * @throws IllegalStateException 이미 처리된 세션이거나 유효하지 않은 상태인 경우
     * @throws FileNotFoundInS3Exception S3에 파일이 없는 경우
     * @throws ChecksumMismatchException ETag 불일치 시
     */
    @Override
    public ConfirmUploadResponse confirm(ConfirmUploadCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("ConfirmUploadCommand must not be null");
        }

        // 1. 세션 조회
        UploadSession session = findSessionById(command.sessionId());

        // 2. 멱등성 처리: 이미 완료된 세션은 성공 응답 반환
        if (session.getStatus() == UploadStatus.COMPLETED) {
            return ConfirmUploadResponse.success(
                    session.getSessionId(),
                    session.getStatus()
            );
        }

        // 3. 세션 상태 검증 (confirmUpload() 내부에서 검증됨)
        // PENDING 상태가 아니면 IllegalStateException 발생

        // 4. S3 위치 생성 (클라이언트가 제공한 uploadPath 사용)
        // uploadPath는 Presigned URL 응답에 포함된 경로와 동일해야 함
        S3Location s3Location = S3Location.of(s3BucketName, command.uploadPath());

        // 5. S3에서 파일 존재 확인
        boolean exists = verifyS3ObjectPort.doesObjectExist(
                s3Location.bucket(),
                s3Location.key()
        );

        if (!exists) {
            throw new FileNotFoundInS3Exception(
                    session.getSessionId(),
                    s3Location.bucket(),
                    s3Location.key()
            );
        }

        // 6. SHA-256 체크섬 검증 (ChecksumVerificationService 사용)
        // 세션에 체크섬이 포함되어 있으면 검증 수행
        checksumVerificationService.verifyChecksum(session, s3Location.key());

        // 7. ETag 검증 (선택적, 하위 호환성 유지)
        if (command.hasEtag()) {
            String actualEtag = verifyS3ObjectPort.getObjectETag(
                    s3Location.bucket(),
                    s3Location.key()
            );

            if (actualEtag != null && !normalizeEtag(actualEtag).equals(normalizeEtag(command.etag()))) {
                throw new ChecksumMismatchException(
                        session.getSessionId(),
                        command.etag(),
                        actualEtag
                );
            }
        }

        // 8. 세션 상태 업데이트
        UploadSession confirmedSession = session.confirmUpload();

        // 9. 변경된 세션 저장
        UploadSession savedSession = uploadSessionPort.save(confirmedSession);

        // 10. Response 생성
        return ConfirmUploadResponse.success(
                savedSession.getSessionId(),
                savedSession.getStatus()
        );
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
     * ETag를 정규화합니다.
     * S3 ETag는 큰따옴표로 감싸져 반환되므로 (예: "md5hash"),
     * 비교를 위해 큰따옴표를 제거합니다.
     *
     * @param etag 원본 ETag
     * @return 정규화된 ETag (큰따옴표 제거)
     */
    private String normalizeEtag(String etag) {
        return etag != null ? etag.replace("\"", "") : null;
    }
}
