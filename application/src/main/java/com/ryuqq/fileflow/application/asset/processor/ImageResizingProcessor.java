package com.ryuqq.fileflow.application.asset.processor;

import com.ryuqq.fileflow.application.asset.component.ImageUploader;
import com.ryuqq.fileflow.application.asset.dto.processor.ResizingTask;
import com.ryuqq.fileflow.application.asset.dto.processor.UploadedImage;
import com.ryuqq.fileflow.application.asset.dto.response.ImageProcessingResultResponse;
import com.ryuqq.fileflow.application.asset.port.out.client.ImageProcessingPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageResizingSpec;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * 이미지 리사이징 프로세서.
 *
 * <p>이미지 다운로드, 리사이징, S3 업로드를 멀티스레드로 병렬 처리합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>원본 이미지 다운로드 (ImageDownloader)
 *   <li>리사이징 작업 생성 (ImageResizingSpec 기반)
 *   <li>parallelStream을 사용한 병렬 처리
 *   <li>결과 수집
 * </ul>
 *
 * <p><strong>스레드 풀</strong>:
 *
 * <ul>
 *   <li>CPU 바운드 작업이므로 커스텀 ForkJoinPool 사용
 *   <li>Virtual Thread 사용 금지
 *   <li>스레드 풀 크기는 bootstrap 모듈에서 설정
 * </ul>
 */
@Component
@ConditionalOnBean(ImageProcessingPort.class)
public class ImageResizingProcessor {

    private static final Logger log = LoggerFactory.getLogger(ImageResizingProcessor.class);

    private static final List<ImageVariant> TARGET_VARIANTS =
            List.of(
                    ImageVariant.ORIGINAL,
                    ImageVariant.LARGE,
                    ImageVariant.MEDIUM,
                    ImageVariant.THUMBNAIL);

    private static final List<ImageFormat> TARGET_FORMATS =
            List.of(ImageFormat.WEBP, ImageFormat.JPEG);

    private final ImageUploader imageUploader;
    private final ImageProcessingPort imageProcessingPort;
    private final ForkJoinPool resizingForkJoinPool;

    public ImageResizingProcessor(
            ImageUploader imageUploader,
            ImageProcessingPort imageProcessingPort,
            ForkJoinPool resizingForkJoinPool) {
        this.imageUploader = imageUploader;
        this.imageProcessingPort = imageProcessingPort;
        this.resizingForkJoinPool = resizingForkJoinPool;
    }

    /**
     * 원본 이미지를 리사이징한 후 S3에 업로드합니다.
     *
     * <p>모든 변형 × 포맷 조합을 병렬로 처리합니다.
     *
     * @param originalAsset 원본 FileAsset
     * @param originalImageData 이미 다운로드된 원본 이미지 데이터
     * @return 업로드된 이미지 결과 목록
     */
    public List<UploadedImage> processAndUpload(FileAsset originalAsset, byte[] originalImageData) {
        log.info("이미지 리사이징 시작: fileAssetId={}", originalAsset.getIdValue());

        // 1. 리사이징 작업 목록 생성 (스트림)
        List<ResizingTask> tasks = createResizingTasks();
        log.debug("리사이징 작업 수: {}", tasks.size());

        // 2. 커스텀 ForkJoinPool에서 parallelStream 실행
        List<UploadedImage> results =
                resizingForkJoinPool
                        .submit(
                                () ->
                                        tasks.parallelStream()
                                                .map(
                                                        task ->
                                                                processTask(
                                                                        task,
                                                                        originalAsset,
                                                                        originalImageData))
                                                .toList())
                        .join();

        log.info(
                "이미지 리사이징 완료: fileAssetId={}, processedCount={}",
                originalAsset.getIdValue(),
                results.size());

        return results;
    }

    /**
     * 모든 변형 × 포맷 조합의 리사이징 작업을 생성합니다.
     *
     * @return 리사이징 작업 목록
     */
    private List<ResizingTask> createResizingTasks() {
        return TARGET_VARIANTS.stream()
                .flatMap(
                        variant ->
                                TARGET_FORMATS.stream()
                                        .map(format -> ResizingTask.of(variant, format)))
                .toList();
    }

    /**
     * 단일 리사이징 작업을 처리합니다.
     *
     * @param task 리사이징 작업
     * @param originalAsset 원본 FileAsset
     * @param originalImageData 원본 이미지 데이터
     * @return 업로드된 이미지 결과
     */
    private UploadedImage processTask(
            ResizingTask task, FileAsset originalAsset, byte[] originalImageData) {
        log.debug("리사이징 작업 시작: {}", task.taskId());

        // 1. 이미지 리사이징
        ImageProcessingResultResponse result =
                imageProcessingPort.resize(originalImageData, task.variant(), task.format());

        // 2. S3 키 생성
        S3Key processedS3Key = generateProcessedS3Key(originalAsset.getS3KeyValue(), task.spec());

        // 3. S3 업로드
        ETag etag =
                imageUploader.upload(
                        originalAsset.getBucket(),
                        processedS3Key,
                        task.spec().mimeType(),
                        result.data());

        log.debug(
                "리사이징 작업 완료: {}, s3Key={}, etag={}",
                task.taskId(),
                processedS3Key.key(),
                etag.value());

        // 4. UploadedImage 생성
        return UploadedImage.fromResult(
                task.spec(), result, originalAsset.getBucket(), processedS3Key, etag);
    }

    /**
     * 처리된 이미지의 S3 키를 생성합니다.
     *
     * <p>예: uploads/2024/01/product.jpg → uploads/2024/01/processed/product_thumb.webp
     */
    private S3Key generateProcessedS3Key(String originalS3Key, ImageResizingSpec spec) {
        int lastSlashIndex = originalS3Key.lastIndexOf('/');
        String directory = (lastSlashIndex > 0) ? originalS3Key.substring(0, lastSlashIndex) : "";

        String fileName =
                (lastSlashIndex > 0) ? originalS3Key.substring(lastSlashIndex + 1) : originalS3Key;

        int lastDotIndex = fileName.lastIndexOf('.');
        String baseName = (lastDotIndex > 0) ? fileName.substring(0, lastDotIndex) : fileName;

        String processedFileName = baseName + spec.suffix() + "." + spec.extension();

        String processedKey =
                directory.isEmpty()
                        ? "processed/" + processedFileName
                        : directory + "/processed/" + processedFileName;

        return S3Key.of(processedKey);
    }
}
