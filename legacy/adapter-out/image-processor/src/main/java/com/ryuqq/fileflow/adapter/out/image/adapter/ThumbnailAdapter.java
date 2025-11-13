package com.ryuqq.fileflow.adapter.out.image.adapter;

import com.ryuqq.fileflow.adapter.out.aws.s3.config.S3ClientConfiguration;
import com.ryuqq.fileflow.adapter.out.image.processor.ThumbnailatorImageProcessor;
import com.ryuqq.fileflow.application.file.port.out.ThumbnailPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.thumbnail.ImageHeight;
import com.ryuqq.fileflow.domain.file.thumbnail.ImageWidth;
import com.ryuqq.fileflow.domain.file.thumbnail.ThumbnailInfo;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;

/**
 * Thumbnail Adapter
 *
 * <p>Application Layer의 {@link ThumbnailPort}를 구현하는 Image Processing Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>S3에서 원본 이미지 다운로드</li>
 *   <li>썸네일 생성 (리사이징 + 압축)</li>
 *   <li>S3에 썸네일 업로드</li>
 *   <li>ThumbnailInfo 생성 및 반환</li>
 * </ul>
 *
 * <p><strong>처리 흐름:</strong></p>
 * <ol>
 *   <li>파일 타입 검증 (이미지만 처리)</li>
 *   <li>S3에서 원본 이미지 다운로드 (InputStream)</li>
 *   <li>ThumbnailatorImageProcessor로 리사이징 + 압축</li>
 *   <li>S3에 썸네일 업로드 (thumbnails/ 경로)</li>
 *   <li>ThumbnailInfo 생성 및 반환</li>
 * </ol>
 *
 * <p><strong>썸네일 설정:</strong></p>
 * <ul>
 *   <li>크기: 300x300 (정사각형)</li>
 *   <li>포맷: JPEG</li>
 *   <li>품질: 85%</li>
 *   <li>저장 경로: thumbnails/{year}/{month}/{filename}_300x300.jpg</li>
 * </ul>
 *
 * <p><strong>에러 처리:</strong></p>
 * <ul>
 *   <li>이미지가 아닌 파일: IllegalArgumentException</li>
 *   <li>S3 다운로드 실패: RuntimeException (재시도 가능)</li>
 *   <li>리사이징 실패: RuntimeException (재시도 가능)</li>
 *   <li>S3 업로드 실패: RuntimeException (재시도 가능)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class ThumbnailAdapter implements ThumbnailPort {

    private static final Logger log = LoggerFactory.getLogger(ThumbnailAdapter.class);

    private static final int THUMBNAIL_SIZE = 300;
    private static final String THUMBNAIL_PREFIX = "thumbnails/";
    private static final String THUMBNAIL_SUFFIX = "_300x300.jpg";
    private static final String THUMBNAIL_CONTENT_TYPE = "image/jpeg";

    private final S3Client s3Client;
    private final String bucket;
    private final ThumbnailatorImageProcessor imageProcessor;

    /**
     * 생성자
     *
     * @param s3Client       S3 Client
     * @param s3Properties   S3 Properties
     * @param imageProcessor 이미지 처리기
     */
    public ThumbnailAdapter(
        S3Client s3Client,
        S3ClientConfiguration.S3Properties s3Properties,
        ThumbnailatorImageProcessor imageProcessor
    ) {
        this.s3Client = s3Client;
        this.bucket = s3Properties.getBucket();
        this.imageProcessor = imageProcessor;
    }

    /**
     * 썸네일 생성 및 S3 업로드
     *
     * <p><strong>처리 단계:</strong></p>
     * <ol>
     *   <li>이미지 파일 검증</li>
     *   <li>S3에서 원본 이미지 다운로드</li>
     *   <li>썸네일 생성 (300x300, JPEG 85%)</li>
     *   <li>S3에 썸네일 업로드</li>
     *   <li>ThumbnailInfo 반환</li>
     * </ol>
     *
     * @param fileAsset 원본 파일
     * @return 생성된 썸네일 정보
     * @throws IllegalArgumentException 이미지가 아닌 경우
     * @throws RuntimeException         S3 또는 이미지 처리 실패
     */
    @Override
    public ThumbnailInfo generateThumbnail(FileAsset fileAsset) {
        log.info("Starting thumbnail generation: fileId={}, fileName={}",
            fileAsset.getIdValue(), fileAsset.getFileName());

        // 1. 이미지 파일 검증
        if (!fileAsset.isImage()) {
            throw new IllegalArgumentException(
                "Thumbnail generation is only supported for images: " +
                "fileId=" + fileAsset.getIdValue() +
                ", contentType=" + fileAsset.getContentType()
            );
        }

        String originalKey = fileAsset.getStorageKeyValue();

        try {
            // 2. S3에서 원본 이미지 다운로드
            log.debug("Downloading original image from S3: bucket={}, key={}",
                bucket, originalKey);

            InputStream originalStream = downloadFromS3(originalKey);

            // 3. 썸네일 생성 (리사이징 + 압축)
            log.debug("Generating thumbnail: size={}x{}", THUMBNAIL_SIZE, THUMBNAIL_SIZE);

            byte[] thumbnailBytes = imageProcessor.createThumbnail(
                originalStream,
                THUMBNAIL_SIZE,
                THUMBNAIL_SIZE
            );

            // 4. 썸네일 S3 Key 생성
            String thumbnailKey = generateThumbnailKey(originalKey);

            log.debug("Uploading thumbnail to S3: bucket={}, key={}, size={}",
                bucket, thumbnailKey, thumbnailBytes.length);

            // 5. S3에 썸네일 업로드
            uploadToS3(thumbnailKey, thumbnailBytes);

            // 6. ThumbnailInfo 생성 및 반환
            ThumbnailInfo thumbnailInfo = new ThumbnailInfo(
                StorageKey.of(thumbnailKey),
                ImageWidth.of(THUMBNAIL_SIZE),
                ImageHeight.of(THUMBNAIL_SIZE),
                FileSize.of((long) thumbnailBytes.length),
                MimeType.of(THUMBNAIL_CONTENT_TYPE)
            );

            log.info("Thumbnail generation completed: fileId={}, thumbnailKey={}, size={}KB",
                fileAsset.getIdValue(), thumbnailKey, thumbnailBytes.length / 1024);

            return thumbnailInfo;

        } catch (IllegalArgumentException e) {
            // 검증 실패는 재시도 불필요
            throw e;

        } catch (Exception e) {
            // S3 또는 이미지 처리 실패 - 재시도 가능
            log.error("Failed to generate thumbnail: fileId={}, key={}",
                fileAsset.getIdValue(), originalKey, e);

            throw new RuntimeException(
                "Thumbnail generation failed: fileId=" + fileAsset.getIdValue(),
                e
            );
        }
    }

    /**
     * S3에서 파일 다운로드
     *
     * @param key S3 Key
     * @return 파일 InputStream
     * @throws S3Exception S3 다운로드 실패
     */
    private InputStream downloadFromS3(String key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

        return s3Client.getObject(getRequest);
    }

    /**
     * S3에 파일 업로드
     *
     * @param key   S3 Key
     * @param bytes 파일 데이터
     * @throws S3Exception S3 업로드 실패
     */
    private void uploadToS3(String key, byte[] bytes) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(THUMBNAIL_CONTENT_TYPE)
            .contentLength((long) bytes.length)
            .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));
    }

    /**
     * 썸네일 S3 Key 생성
     *
     * <p><strong>변환 예시:</strong></p>
     * <pre>
     * 원본: uploads/2025/01/abc123.jpg
     * 썸네일: thumbnails/2025/01/abc123_300x300.jpg
     * </pre>
     *
     * @param originalKey 원본 S3 Key
     * @return 썸네일 S3 Key
     */
    private String generateThumbnailKey(String originalKey) {
        // uploads/2025/01/abc123.jpg → abc123.jpg
        int lastSlashIndex = originalKey.lastIndexOf('/');
        String fileName = lastSlashIndex >= 0 ?
            originalKey.substring(lastSlashIndex + 1) :
            originalKey;

        // abc123.jpg → abc123
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex >= 0 ?
            fileName.substring(0, dotIndex) :
            fileName;

        // uploads/2025/01/ → 2025/01/
        String pathPrefix = lastSlashIndex >= 0 ?
            originalKey.substring(0, lastSlashIndex + 1) :
            "";

        // uploads/ → (제거)
        String dateBasedPath = pathPrefix.replaceFirst("^uploads/", "");

        // thumbnails/2025/01/abc123_300x300.jpg
        return THUMBNAIL_PREFIX + dateBasedPath + baseName + THUMBNAIL_SUFFIX;
    }
}
