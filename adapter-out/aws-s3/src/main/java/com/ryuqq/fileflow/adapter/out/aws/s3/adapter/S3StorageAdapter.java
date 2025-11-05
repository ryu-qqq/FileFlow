package com.ryuqq.fileflow.adapter.out.aws.s3.adapter;

import com.ryuqq.fileflow.adapter.out.aws.s3.exception.S3StorageException;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteMultipartUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteMultipartUploadResult;
import com.ryuqq.fileflow.application.upload.dto.command.InitiateMultipartUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.command.InitiateMultipartUploadResult;
import com.ryuqq.fileflow.application.upload.dto.command.UploadStreamResult;
import com.ryuqq.fileflow.application.upload.port.out.S3StoragePort;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * S3 Storage Adapter
 *
 * <p>Application Layer의 {@link S3StoragePort}를 구현하는 AWS S3 Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>S3 Multipart Upload API 호출</li>
 *   <li>Presigned URL 생성</li>
 *   <li>S3 Object 관리</li>
 *   <li>AWS SDK Exception을 Domain Exception으로 변환</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴 준수</li>
 *   <li>✅ AWS SDK v2 사용</li>
 *   <li>✅ Infrastructure 세부사항 격리</li>
 *   <li>✅ Exception 변환 (S3Exception → S3StorageException)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class S3StorageAdapter implements S3StoragePort {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    /**
     * 생성자
     *
     * @param s3Client S3 Client
     * @param s3Presigner S3 Presigner
     */
    public S3StorageAdapter(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /**
     * Multipart Upload 초기화
     *
     * <p>S3 Multipart Upload를 시작하고 Upload ID를 반환합니다.</p>
     *
     * @param command 초기화 Command
     * @return 초기화 결과
     * @throws S3StorageException S3 작업 실패 시
     */
    @Override
    public InitiateMultipartUploadResult initiateMultipartUpload(InitiateMultipartUploadCommand command) {
        try {
            CreateMultipartUploadRequest s3Request = CreateMultipartUploadRequest.builder()
                .bucket(command.bucket())
                .key(command.key())
                .contentType(command.contentType())
                .build();

            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(s3Request);

            return InitiateMultipartUploadResult.of(
                response.uploadId(),
                command.bucket(),
                command.key()
            );

        } catch (S3Exception e) {
            throw new S3StorageException(
                "Failed to initiate multipart upload: bucket=" + command.bucket() +
                ", key=" + command.key(),
                e
            );
        }
    }

    /**
     * 파트 업로드를 위한 Presigned URL 생성
     *
     * <p>클라이언트가 직접 S3에 파트를 업로드할 수 있는 Presigned URL을 생성합니다.</p>
     *
     * @param bucket S3 Bucket
     * @param key S3 Object Key
     * @param uploadId Upload ID
     * @param partNumber 파트 번호
     * @param duration 유효 시간
     * @return Presigned URL
     * @throws S3StorageException Presigned URL 생성 실패 시
     */
    @Override
    public String presignUploadPart(
        String bucket,
        String key,
        String uploadId,
        Integer partNumber,
        Duration duration
    ) {
        try {
            UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();

            UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                .signatureDuration(duration)
                .uploadPartRequest(uploadPartRequest)
                .build();

            PresignedUploadPartRequest presignedRequest = s3Presigner.presignUploadPart(presignRequest);

            return presignedRequest.url().toString();

        } catch (S3Exception e) {
            throw new S3StorageException(
                "Failed to presign upload part: bucket=" + bucket +
                ", key=" + key +
                ", uploadId=" + uploadId +
                ", partNumber=" + partNumber,
                e
            );
        }
    }

    /**
     * Multipart Upload 완료
     *
     * <p>업로드된 모든 파트를 조합하여 최종 파일을 생성합니다.</p>
     *
     * @param command 완료 Command
     * @return 완료 결과
     * @throws S3StorageException S3 작업 실패 시
     */
    @Override
    public CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadCommand command) {
        try {
            // CompletedPart 리스트 변환
            List<CompletedPart> completedParts = command.parts()
                .stream()
                .map(part -> CompletedPart.builder()
                    .partNumber(part.partNumber())
                    .eTag(part.etag())
                    .build())
                .collect(Collectors.toList());

            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(completedParts)
                .build();

            software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest s3Request =
                software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest.builder()
                .bucket(command.bucket())
                .key(command.key())
                .uploadId(command.uploadId())
                .multipartUpload(completedMultipartUpload)
                .build();

            CompleteMultipartUploadResponse response = s3Client.completeMultipartUpload(s3Request);

            return CompleteMultipartUploadResult.of(
                response.eTag(),
                response.location()
            );

        } catch (S3Exception e) {
            throw new S3StorageException(
                "Failed to complete multipart upload: bucket=" + command.bucket() +
                ", key=" + command.key() +
                ", uploadId=" + command.uploadId(),
                e
            );
        }
    }

    /**
     * Multipart Upload 중단
     *
     * <p>진행 중인 Multipart Upload를 중단하고 업로드된 파트를 삭제합니다.</p>
     *
     * @param bucket S3 Bucket
     * @param key S3 Object Key
     * @param uploadId Upload ID
     * @throws S3StorageException S3 작업 실패 시
     */
    @Override
    public void abortMultipartUpload(String bucket, String key, String uploadId) {
        try {
            AbortMultipartUploadRequest request = AbortMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .build();

            s3Client.abortMultipartUpload(request);

        } catch (S3Exception e) {
            throw new S3StorageException(
                "Failed to abort multipart upload: bucket=" + bucket +
                ", key=" + key +
                ", uploadId=" + uploadId,
                e
            );
        }
    }

    /**
     * S3 Object 삭제
     *
     * <p>S3에서 지정된 Object를 삭제합니다.</p>
     *
     * @param bucket S3 Bucket
     * @param key S3 Object Key
     * @throws S3StorageException S3 작업 실패 시
     */
    @Override
    public void deleteObject(String bucket, String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

            s3Client.deleteObject(request);

        } catch (S3Exception e) {
            throw new S3StorageException(
                "Failed to delete object: bucket=" + bucket + ", key=" + key,
                e
            );
        }
    }

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
     * @throws S3StorageException Presigned URL 생성 실패 시
     */
    @Override
    public String presignPutObject(
        String bucket,
        String key,
        String contentType,
        Duration duration
    ) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

            software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest presignRequest =
                software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .putObjectRequest(putObjectRequest)
                    .build();

            software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest presignedRequest =
                s3Presigner.presignPutObject(presignRequest);

            return presignedRequest.url().toString();

        } catch (S3Exception e) {
            throw new S3StorageException(
                "Failed to presign PUT object: bucket=" + bucket +
                ", key=" + key,
                e
            );
        }
    }

    /**
     * 스트림 업로드 (External Download용)
     *
     * <p>InputStream을 S3에 직접 업로드합니다. External Download 등에서 사용됩니다.</p>
     *
     * @param bucket S3 Bucket
     * @param key S3 Object Key
     * @param inputStream 입력 스트림
     * @param contentType Content Type
     * @return 업로드 결과
     * @throws S3StorageException S3 작업 실패 시
     */
    @Override
    public UploadStreamResult uploadStream(
        String bucket,
        String key,
        InputStream inputStream,
        String contentType
    ) {
        try {
            // InputStream을 읽어서 byte[]로 변환 (크기 측정)
            byte[] content = inputStream.readAllBytes();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength((long) content.length)
                .build();

            PutObjectResponse response = s3Client.putObject(
                putObjectRequest,
                RequestBody.fromBytes(content)
            );

            return UploadStreamResult.of(
                response.eTag(),
                (long) content.length
            );

        } catch (IOException e) {
            throw new S3StorageException(
                "Failed to read input stream: bucket=" + bucket + ", key=" + key,
                e
            );
        } catch (S3Exception e) {
            throw new S3StorageException(
                "Failed to upload stream: bucket=" + bucket + ", key=" + key,
                e
            );
        }
    }

    /**
     * S3 Object 메타데이터 조회 (HeadObject)
     *
     * <p>S3에 Object가 존재하는지 확인하고 메타데이터를 조회합니다.</p>
     * <p>Single Upload 완료 시 파일이 실제로 업로드되었는지 확인하는 용도로 사용됩니다.</p>
     *
     * @param bucket S3 Bucket
     * @param key S3 Object Key
     * @return S3 Object 메타데이터
     * @throws S3StorageException Object가 존재하지 않거나 접근 실패 시
     */
    @Override
    public com.ryuqq.fileflow.application.upload.dto.response.S3HeadObjectResponse headObject(
        String bucket,
        String key
    ) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

            HeadObjectResponse response = s3Client.headObject(request);

            return com.ryuqq.fileflow.application.upload.dto.response.S3HeadObjectResponse.of(
                response.contentLength(),
                response.eTag(),
                response.contentType()
            );

        } catch (NoSuchKeyException e) {
            throw new S3StorageException(
                "Object not found: bucket=" + bucket + ", key=" + key,
                e
            );
        } catch (S3Exception e) {
            throw new S3StorageException(
                "Failed to head object: bucket=" + bucket + ", key=" + key,
                e
            );
        }
    }
}
