package com.ryuqq.fileflow.application.upload.facade;

import com.ryuqq.fileflow.application.upload.dto.command.UploadStreamResult;
import com.ryuqq.fileflow.application.upload.dto.response.S3HeadObjectResponse;
import com.ryuqq.fileflow.application.upload.port.out.S3StoragePort;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Storage Upload Facade
 * 스토리지 직접 업로드를 위한 Facade
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>외부 다운로드 파일의 S3 직접 업로드</li>
 *   <li>스토리지 작업 추상화</li>
 *   <li>에러 처리 및 로깅</li>
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>ExternalDownloadWorker: 외부 URL에서 다운로드한 파일을 S3에 업로드</li>
 *   <li>DirectUploadService: 서버 사이드 직접 업로드</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class StorageUploadFacade {

    private static final Logger log = LoggerFactory.getLogger(StorageUploadFacade.class);

    private final S3StoragePort storagePort;
    private final String s3Bucket;

    public StorageUploadFacade(
        S3StoragePort storagePort,
        @Value("${aws.s3.bucket-name}") String s3Bucket
    ) {
        this.storagePort = storagePort;
        this.s3Bucket = s3Bucket;
    }

    /**
     * 파일을 스토리지에 직접 업로드
     *
     * <p>외부 URL에서 다운로드한 스트림을 S3에 직접 저장합니다.</p>
     *
     * @param storageKey S3 키
     * @param inputStream 입력 스트림
     * @param fileSize 파일 크기
     * @param contentType Content-Type
     * @return 업로드 성공 여부
     */
    public boolean uploadFile(
        StorageKey storageKey,
        InputStream inputStream,
        long fileSize,
        String contentType
    ) {
        try {
            log.info("Uploading file to storage: key={}, size={}, contentType={}",
                storageKey.value(), fileSize, contentType);

            // S3StoragePort.uploadStream() 사용
            UploadStreamResult result = storagePort.uploadStream(
                s3Bucket,
                storageKey.value(),
                inputStream,
                contentType
            );

            boolean success = result != null;

            if (success) {
                log.info("File uploaded successfully: key={}", storageKey.value());
            } else {
                log.error("Failed to upload file: key={}", storageKey.value());
            }

            return success;
        } catch (Exception e) {
            log.error("Error uploading file to storage: key={}", storageKey.value(), e);
            return false;
        }
    }

    /**
     * 스토리지에서 파일 존재 여부 확인
     *
     * @param storageKey S3 키
     * @return 파일 존재 여부
     */
    public boolean fileExists(StorageKey storageKey) {
        try {
            // TODO: S3StoragePort에 fileExists 메서드 추가 필요
            log.warn("fileExists not implemented yet: key={}", storageKey.value());
            return false;
        } catch (Exception e) {
            log.error("Error checking file existence: key={}", storageKey.value(), e);
            return false;
        }
    }

    /**
     * 스토리지에서 파일 삭제
     *
     * @param storageKey S3 키
     * @return 삭제 성공 여부
     */
    public boolean deleteFile(StorageKey storageKey) {
        try {
            log.info("Deleting file from storage: key={}", storageKey.value());

            // S3StoragePort.deleteObject() 사용
            storagePort.deleteObject(s3Bucket, storageKey.value());

            log.info("File deleted successfully: key={}", storageKey.value());
            return true;
        } catch (Exception e) {
            log.error("Error deleting file from storage: key={}", storageKey.value(), e);
            return false;
        }
    }

    /**
     * 파일의 체크섬 계산
     *
     * <p>S3 HeadObject를 사용하여 ETag를 조회합니다.</p>
     *
     * @param storageKey S3 키
     * @return 체크섬 (ETag)
     */
    public String calculateChecksum(StorageKey storageKey) {
        try {
            S3HeadObjectResponse headResult = storagePort.headObject(s3Bucket, storageKey.value());
            String etag = headResult.etag();
            log.debug("Checksum calculated from S3 ETag: key={}, etag={}", storageKey.value(), etag);
            return etag;
        } catch (Exception e) {
            log.error("Error calculating checksum: key={}", storageKey.value(), e);
            return null;
        }
    }

    /**
     * S3 Object 메타데이터 조회
     *
     * <p>S3 HeadObject를 사용하여 메타데이터를 조회합니다.</p>
     *
     * @param storageKey S3 키
     * @return S3 메타데이터
     */
    public S3HeadObjectResponse getS3Metadata(StorageKey storageKey) {
        try {
            return storagePort.headObject(s3Bucket, storageKey.value());
        } catch (Exception e) {
            log.error("Error getting S3 metadata: key={}", storageKey.value(), e);
            throw new RuntimeException("Failed to get S3 metadata: key=" + storageKey.value(), e);
        }
    }
}