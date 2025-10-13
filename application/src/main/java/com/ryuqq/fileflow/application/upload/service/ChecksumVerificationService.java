package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.port.out.VerifyS3ObjectPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.exception.ChecksumMismatchException;
import com.ryuqq.fileflow.domain.upload.vo.CheckSum;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * 파일 무결성 검증 Service
 *
 * SHA-256 체크섬 기반으로 업로드된 파일의 무결성을 검증합니다.
 * S3 Object Metadata에 저장된 체크섬과 클라이언트가 제공한 체크섬을 비교합니다.
 *
 * 검증 프로세스:
 * 1. S3 Object User Metadata 조회 (VerifyS3ObjectPort)
 * 2. x-amz-meta-checksum-value 추출
 * 3. 클라이언트 체크섬과 비교
 * 4. 불일치 시 ChecksumMismatchException 발생
 *
 * Hexagonal Architecture:
 * - Application Layer의 Domain Service
 * - VerifyS3ObjectPort를 통해 S3와 통신 (Port/Adapter 패턴)
 *
 * @author sangwon-ryu
 */
@Service
public class ChecksumVerificationService {

    private static final String METADATA_KEY_CHECKSUM_VALUE = "checksum-value";
    private static final String METADATA_KEY_CHECKSUM_ALGORITHM = "checksum-algorithm";

    private final VerifyS3ObjectPort verifyS3ObjectPort;
    private final String s3BucketName;

    /**
     * ChecksumVerificationService 생성자
     *
     * @param verifyS3ObjectPort S3 객체 검증 Port
     * @param s3BucketName S3 버킷명
     * @throws IllegalArgumentException 파라미터가 null이거나 비어있는 경우
     */
    public ChecksumVerificationService(
            VerifyS3ObjectPort verifyS3ObjectPort,
            @org.springframework.beans.factory.annotation.Value("${aws.s3.bucket-name}") String s3BucketName
    ) {
        this.verifyS3ObjectPort = Objects.requireNonNull(
                verifyS3ObjectPort,
                "VerifyS3ObjectPort must not be null"
        );

        if (s3BucketName == null || s3BucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("s3BucketName must not be null or empty");
        }
        this.s3BucketName = s3BucketName;
    }

    /**
     * 업로드 세션의 파일 체크섬을 검증합니다.
     *
     * 검증 과정:
     * 1. 세션에서 체크섬 정보 추출
     * 2. 체크섬이 없으면 검증 스킵 (선택적 기능)
     * 3. S3 Object Metadata에서 저장된 체크섬 조회
     * 4. 클라이언트 체크섬과 비교
     * 5. 불일치 시 ChecksumMismatchException 발생
     *
     * @param session 업로드 세션
     * @param s3Key S3 객체 키 (업로드 경로)
     * @throws IllegalArgumentException session 또는 s3Key가 null이거나 유효하지 않은 경우
     * @throws ChecksumMismatchException 체크섬 불일치 시
     * @throws RuntimeException S3 API 호출 실패 시
     */
    public void verifyChecksum(UploadSession session, String s3Key) {
        validateSession(session);
        validateS3Key(s3Key);

        // 1. 세션에서 체크섬 정보 추출
        CheckSum clientChecksum = session.getUploadRequest().checksum();

        // 2. 체크섬이 없으면 검증 스킵 (선택적 기능)
        if (clientChecksum == null) {
            return; // 체크섬이 제공되지 않은 경우 검증 생략
        }

        // 3. S3 Object Metadata 조회
        Map<String, String> s3Metadata = getS3ObjectMetadata(s3Key);

        // 4. S3에 저장된 체크섬 추출
        String storedChecksumValue = s3Metadata.get(METADATA_KEY_CHECKSUM_VALUE);
        String storedChecksumAlgorithm = s3Metadata.get(METADATA_KEY_CHECKSUM_ALGORITHM);

        // 5. 메타데이터에 체크섬이 없으면 예외
        if (storedChecksumValue == null || storedChecksumValue.trim().isEmpty()) {
            throw new ChecksumMismatchException(
                    session.getSessionId(),
                    clientChecksum.normalizedValue(),
                    "METADATA_NOT_FOUND"
            );
        }

        // 6. 알고리즘 일치 확인
        if (storedChecksumAlgorithm != null && !storedChecksumAlgorithm.equals(clientChecksum.algorithm())) {
            throw new ChecksumMismatchException(
                    session.getSessionId(),
                    clientChecksum.normalizedValue(),
                    "ALGORITHM_MISMATCH: expected=" + clientChecksum.algorithm() + ", actual=" + storedChecksumAlgorithm
            );
        }

        // 7. 체크섬 값 비교 (대소문자 무시)
        String normalizedClientChecksum = clientChecksum.normalizedValue();
        String normalizedStoredChecksum = storedChecksumValue.toLowerCase().trim();

        if (!normalizedClientChecksum.equals(normalizedStoredChecksum)) {
            throw new ChecksumMismatchException(
                    session.getSessionId(),
                    normalizedClientChecksum,
                    normalizedStoredChecksum
            );
        }

        // 검증 성공 (예외 없음)
    }

    /**
     * S3 Object의 User Metadata를 조회합니다.
     *
     * @param s3Key S3 객체 키
     * @return S3 User Metadata 맵 (x-amz-meta-* 헤더들)
     * @throws RuntimeException S3 API 호출 실패 시
     */
    private Map<String, String> getS3ObjectMetadata(String s3Key) {
        return verifyS3ObjectPort.getUserMetadata(s3BucketName, s3Key);
    }

    // ========== Validation Methods ==========

    private static void validateSession(UploadSession session) {
        if (session == null) {
            throw new IllegalArgumentException("UploadSession must not be null");
        }
        if (session.getUploadRequest() == null) {
            throw new IllegalArgumentException("UploadRequest in session must not be null");
        }
    }

    private static void validateS3Key(String s3Key) {
        if (s3Key == null || s3Key.trim().isEmpty()) {
            throw new IllegalArgumentException("s3Key must not be null or empty");
        }
    }
}
