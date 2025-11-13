package com.ryuqq.fileflow.application.upload.port.out;

import com.ryuqq.fileflow.application.upload.dto.command.CompleteMultipartUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteMultipartUploadResult;
import com.ryuqq.fileflow.application.upload.dto.command.InitiateMultipartUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.command.InitiateMultipartUploadResult;
import com.ryuqq.fileflow.application.upload.dto.command.UploadStreamResult;
import com.ryuqq.fileflow.application.upload.dto.response.S3HeadObjectResponse;

import java.io.InputStream;
import java.time.Duration;

/**
 * S3 Storage Port (Out)
 *
 * <p>Application Layer에서 S3 Storage Adapter로 나가는 Port 인터페이스입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>S3 Multipart Upload API 호출 인터페이스 정의</li>
 *   <li>Presigned URL 생성 인터페이스 정의</li>
 *   <li>S3 리소스 정리 인터페이스 정의</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴의 Port 역할</li>
 *   <li>✅ Infrastructure 독립적 (AWS SDK 타입 노출 최소화)</li>
 *   <li>✅ 비즈니스 의미 있는 메서드명</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface S3StoragePort {

    /**
     * Multipart Upload 초기화
     *
     * @param command 초기화 Command
     * @return 초기화 결과
     */
    InitiateMultipartUploadResult initiateMultipartUpload(InitiateMultipartUploadCommand command);

    /**
     * 파트 업로드를 위한 Presigned URL 생성
     *
     * @param bucket S3 Bucket
     * @param key S3 Object Key
     * @param uploadId Upload ID
     * @param partNumber 파트 번호
     * @param duration 유효 시간
     * @return Presigned URL
     */
    String presignUploadPart(
        String bucket,
        String key,
        String uploadId,
        Integer partNumber,
        Duration duration
    );

    /**
     * Multipart Upload 완료
     *
     * @param command 완료 Command
     * @return 완료 결과
     */
    CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadCommand command);

    /**
     * Multipart Upload 중단
     *
     * @param bucket S3 Bucket
     * @param key S3 Object Key
     * @param uploadId Upload ID
     */
    void abortMultipartUpload(String bucket, String key, String uploadId);

    /**
     * S3 Object 삭제
     *
     * @param bucket S3 Bucket
     * @param key S3 Object Key
     */
    void deleteObject(String bucket, String key);

    /**
     * 단일 업로드를 위한 Presigned PUT URL 생성
     *
     * <p>100MB 미만 파일의 단일 HTTP PUT 업로드를 위한 Presigned URL을 생성합니다.</p>
     *
     * @param bucket S3 Bucket
     * @param key S3 Object Key
     * @param contentType Content Type
     * @param duration 유효 시간
     * @return Presigned PUT URL
     */
    String presignPutObject(
        String bucket,
        String key,
        String contentType,
        Duration duration
    );

    /**
     * 스트림 업로드 (External Download용)
     *
     * @param bucket S3 Bucket
     * @param key S3 Object Key
     * @param inputStream 입력 스트림
     * @param contentType Content Type
     * @return 업로드 결과
     */
    UploadStreamResult uploadStream(String bucket, String key, InputStream inputStream, String contentType);

    /**
     * S3 Object 메타데이터 조회 (HeadObject)
     *
     * <p>S3에 Object가 존재하는지 확인하고 메타데이터를 조회합니다.</p>
     * <p>Single Upload 완료 확인 시 사용됩니다.</p>
     *
     * @param bucket S3 Bucket
     * @param key S3 Object Key
     * @return S3 Object 메타데이터
     * @throws com.ryuqq.fileflow.adapter.out.aws.s3.exception.S3StorageException Object가 존재하지 않거나 접근 실패 시
     */
    S3HeadObjectResponse headObject(String bucket, String key);
}
