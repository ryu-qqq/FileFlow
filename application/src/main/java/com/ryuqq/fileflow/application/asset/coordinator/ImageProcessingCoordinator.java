package com.ryuqq.fileflow.application.asset.coordinator;

import com.ryuqq.fileflow.application.asset.component.ImageDownloader;
import com.ryuqq.fileflow.application.asset.component.ImageMetadataExtractor;
import com.ryuqq.fileflow.application.asset.dto.processor.UploadedImage;
import com.ryuqq.fileflow.application.asset.port.out.client.ImageProcessingPort;
import com.ryuqq.fileflow.application.asset.processor.ImageResizingProcessor;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * 이미지 처리 Coordinator.
 *
 * <p>이미지 처리의 전체 흐름을 조율합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>S3에서 원본 이미지 다운로드
 *   <li>메타데이터 추출 및 dimension 업데이트
 *   <li>리사이징 + S3 업로드 (병렬 처리)
 * </ol>
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>이미지 처리 단계 조율
 *   <li>처리 컴포넌트 간 데이터 전달
 * </ul>
 *
 * <p><strong>트랜잭션</strong>:
 *
 * <ul>
 *   <li>모든 작업은 트랜잭션 외부에서 수행 (외부 I/O)
 *   <li>DB 저장은 호출자(Service)가 담당
 * </ul>
 */
@Component
@ConditionalOnBean(ImageProcessingPort.class)
public class ImageProcessingCoordinator {

    private static final Logger log = LoggerFactory.getLogger(ImageProcessingCoordinator.class);

    private final ImageDownloader imageDownloader;
    private final ImageMetadataExtractor metadataExtractor;
    private final ImageResizingProcessor resizingProcessor;

    public ImageProcessingCoordinator(
            ImageDownloader imageDownloader,
            ImageMetadataExtractor metadataExtractor,
            ImageResizingProcessor resizingProcessor) {
        this.imageDownloader = imageDownloader;
        this.metadataExtractor = metadataExtractor;
        this.resizingProcessor = resizingProcessor;
    }

    /**
     * 파일 에셋의 이미지 처리를 조율한다.
     *
     * <p>다운로드 → 메타데이터 추출 → 리사이징의 전체 흐름을 수행합니다.
     *
     * @param fileAsset 처리할 FileAsset (dimension이 업데이트됨)
     * @return 업로드된 이미지 결과 목록
     */
    public List<UploadedImage> process(FileAsset fileAsset) {
        log.info("이미지 처리 시작: fileAssetId={}", fileAsset.getIdValue());

        // 1. 원본 이미지 다운로드
        byte[] imageData = imageDownloader.download(fileAsset);
        log.debug("원본 이미지 다운로드 완료: size={} bytes", imageData.length);

        // 2. 메타데이터 추출 및 dimension 업데이트
        metadataExtractor.extractAndUpdateDimension(fileAsset, imageData);

        // 3. 리사이징 + S3 업로드 (병렬 처리)
        List<UploadedImage> results = resizingProcessor.processAndUpload(fileAsset, imageData);
        log.info(
                "이미지 처리 완료: fileAssetId={}, processedCount={}",
                fileAsset.getIdValue(),
                results.size());

        return results;
    }
}
