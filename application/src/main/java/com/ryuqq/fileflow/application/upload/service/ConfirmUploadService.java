package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadResponse;
import com.ryuqq.fileflow.application.upload.port.in.ConfirmUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.application.upload.port.out.VerifyS3ObjectPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Service;
import com.ryuqq.fileflow.domain.upload.exception.ChecksumMismatchException;
import org.springframework.stereotype.Service;
import com.ryuqq.fileflow.domain.upload.exception.FileNotFoundInS3Exception;
import org.springframework.stereotype.Service;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import org.springframework.stereotype.Service;
import com.ryuqq.fileflow.domain.upload.vo.S3Location;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 업로드 완료 확인 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * 클라이언트의 업로드 완료 알림을 처리하고 S3에서 파일 존재를 검증합니다.
 *
 * 처리 흐름:
 * 1. 세션 조회
 * 2. 세션 상태 검증 (PENDING 상태여야 함)
 * 3. S3에 파일 존재 확인
 * 4. ETag 검증 (제공된 경우)
 * 5. 세션 상태를 COMPLETED로 업데이트
 *
 * @author sangwon-ryu
 */
@Service
public class ConfirmUploadService implements ConfirmUploadUseCase {

    private final UploadSessionPort uploadSessionPort;
    private final VerifyS3ObjectPort verifyS3ObjectPort;
    private final String s3BucketName;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param uploadSessionPort 세션 저장소
     * @param verifyS3ObjectPort S3 검증 Port
     * @param s3BucketName S3 버킷명 (외부 설정에서 주입)
     */
    public ConfirmUploadService(
            UploadSessionPort uploadSessionPort,
            VerifyS3ObjectPort verifyS3ObjectPort,
            String s3BucketName
    ) {
        this.uploadSessionPort = Objects.requireNonNull(
                uploadSessionPort,
                "UploadSessionPort must not be null"
        );
        this.verifyS3ObjectPort = Objects.requireNonNull(
                verifyS3ObjectPort,
                "VerifyS3ObjectPort must not be null"
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

        // 2. 세션 상태 검증 (confirmUpload() 내부에서 검증됨)
        // PENDING 상태가 아니면 IllegalStateException 발생

        // 3. S3 위치 추출 (PresignedUrlInfo는 UploadSession에 없으므로,
        //    실제 구현에서는 별도 저장소에서 조회하거나, UploadSession에 포함시켜야 함)
        //    임시로 uploadPath를 사용 (실제로는 리팩토링 필요)
        //
        // TODO: UploadSession에 S3Location 필드 추가 필요
        //       현재는 Presigned URL에서 추출한 경로를 사용한다고 가정

        // 임시 하드코딩 - 실제로는 세션에서 S3 정보를 가져와야 함
        String bucketName = extractBucketFromSession(session);
        String s3Key = extractS3KeyFromSession(session);

        S3Location s3Location = S3Location.of(bucketName, s3Key);

        // 4. S3에서 파일 존재 확인
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

        // 5. ETag 검증 (선택적)
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

        // 6. 세션 상태 업데이트
        UploadSession confirmedSession = session.confirmUpload();

        // 7. 변경된 세션 저장
        UploadSession savedSession = uploadSessionPort.save(confirmedSession);

        // 8. Response 생성
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
     * 세션에서 S3 버킷명을 추출합니다.
     *
     * TODO: 실제 구현에서는 UploadSession에 S3Location을 추가하거나,
     *       별도 저장소에서 조회해야 합니다.
     *       현재는 생성자에서 주입받은 버킷명을 사용합니다.
     *
     * @param session 업로드 세션
     * @return S3 버킷명
     */
    private String extractBucketFromSession(UploadSession session) {
        // 생성자에서 주입받은 버킷명 사용
        // 실제로는 UploadSession에 S3Location 필드가 있어야 함
        return this.s3BucketName;
    }

    /**
     * 세션에서 S3 객체 키를 추출합니다.
     *
     * TODO: 실제 구현에서는 UploadSession에 S3Location을 추가하거나,
     *       별도 저장소에서 조회해야 합니다.
     *
     * @param session 업로드 세션
     * @return S3 객체 키
     */
    private String extractS3KeyFromSession(UploadSession session) {
        // 임시 구현: 세션 ID와 파일명으로 키 생성
        // 실제로는 UploadSession에 S3Location 필드가 있어야 함
        return String.format(
                "%s/%s/%s",
                session.getPolicyKey().getTenantId(),
                session.getSessionId(),
                session.getUploadRequest().fileName()
        );
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
