package com.ryuqq.fileflow.application.file.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.application.file.manager.FileQueryManager;
import com.ryuqq.fileflow.application.file.port.out.MetadataPort;
import com.ryuqq.fileflow.application.file.port.out.SaveExtractedDataPort;
import com.ryuqq.fileflow.application.file.port.out.SaveFileVariantPort;
import com.ryuqq.fileflow.application.file.port.out.ThumbnailPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.asset.FileAssetId;
import com.ryuqq.fileflow.domain.file.extraction.ExtractedData;
import com.ryuqq.fileflow.domain.file.extraction.ExtractionMethod;
import com.ryuqq.fileflow.domain.file.extraction.ExtractionType;
import com.ryuqq.fileflow.domain.file.metadata.FileMetadata;
import com.ryuqq.fileflow.domain.file.thumbnail.ThumbnailInfo;
import com.ryuqq.fileflow.domain.file.variant.FileVariant;
import com.ryuqq.fileflow.domain.file.variant.VariantType;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Pipeline Worker
 *
 * <p>FileAsset에 대한 Pipeline 처리를 비동기로 실행하는 Worker 컴포넌트입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>FileAsset의 Pipeline 처리 실행</li>
 *   <li>비동기 처리 (@Async)</li>
 *   <li>Pipeline 작업 위임</li>
 * </ul>
 *
 * <p><strong>Pipeline 처리 흐름:</strong></p>
 * <ol>
 *   <li>FileAsset 조회</li>
 *   <li>썸네일 생성 (이미지만, ThumbnailPort)</li>
 *   <li>메타데이터 추출 (모든 파일, MetadataPort)</li>
 *   <li>결과 저장 (TODO: 별도 테이블에 저장)</li>
 * </ol>
 *
 * <p><strong>Phase 1 구현 (현재):</strong></p>
 * <ul>
 *   <li>✅ 썸네일 생성 (이미지 파일만)</li>
 *   <li>✅ 메타데이터 추출 (이미지, 비디오, 문서)</li>
 * </ul>
 *
 * <p><strong>Phase 2 확장 (미래):</strong></p>
 * <ul>
 *   <li>바이러스 스캔 (ClamAV)</li>
 *   <li>검색 인덱스 생성 (Elasticsearch)</li>
 *   <li>알림 발송 (SNS)</li>
 * </ul>
 *
 * <p><strong>트랜잭션 경계:</strong></p>
 * <ul>
 *   <li>✅ @Async로 비동기 실행 (호출자와 별도 스레드)</li>
 *   <li>✅ S3 작업은 트랜잭션 밖에서 (ThumbnailPort, MetadataPort)</li>
 *   <li>✅ 실패 시 Outbox Scheduler가 재시도 관리</li>
 * </ul>
 *
 * <p><strong>에러 처리 전략:</strong></p>
 * <ul>
 *   <li>썸네일 실패: 로깅만 (필수 아님)</li>
 *   <li>메타데이터 실패: 로깅만 (필수 아님)</li>
 *   <li>FileAsset 미존재: 로깅 후 종료</li>
 *   <li>치명적 오류: 로깅 후 Outbox는 Scheduler가 FAILED 처리</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class PipelineWorker {

    private static final Logger log = LoggerFactory.getLogger(PipelineWorker.class);

    private final FileQueryManager fileQueryManager;
    private final ThumbnailPort thumbnailPort;
    private final MetadataPort metadataPort;
    private final SaveFileVariantPort saveFileVariantPort;
    private final SaveExtractedDataPort saveExtractedDataPort;
    private final ObjectMapper objectMapper;

    /**
     * 생성자
     *
     * @param fileQueryManager      File Query Manager
     * @param thumbnailPort         Thumbnail Generation Port
     * @param metadataPort          Metadata Extraction Port
     * @param saveFileVariantPort   File Variant Save Port
     * @param saveExtractedDataPort Extracted Data Save Port
     * @param objectMapper          Object Mapper (JSON 변환)
     */
    public PipelineWorker(
        FileQueryManager fileQueryManager,
        ThumbnailPort thumbnailPort,
        MetadataPort metadataPort,
        SaveFileVariantPort saveFileVariantPort,
        SaveExtractedDataPort saveExtractedDataPort,
        ObjectMapper objectMapper
    ) {
        this.fileQueryManager = fileQueryManager;
        this.thumbnailPort = thumbnailPort;
        this.metadataPort = metadataPort;
        this.saveFileVariantPort = saveFileVariantPort;
        this.saveExtractedDataPort = saveExtractedDataPort;
        this.objectMapper = objectMapper;
    }

    /**
     * Pipeline 처리 시작 (비동기)
     *
     * <p><strong>실행 흐름:</strong></p>
     * <ol>
     *   <li>FileAsset 조회</li>
     *   <li>썸네일 생성 (이미지만)</li>
     *   <li>메타데이터 추출 (모든 파일)</li>
     *   <li>결과 저장 (TODO)</li>
     * </ol>
     *
     * <p><strong>비동기 실행:</strong></p>
     * <ul>
     *   <li>@Async로 별도 스레드에서 실행</li>
     *   <li>호출자는 즉시 반환</li>
     *   <li>스레드 풀: AsyncConfig에서 설정</li>
     * </ul>
     *
     * <p><strong>예외 처리:</strong></p>
     * <ul>
     *   <li>FileAsset 미존재: 로깅 후 종료</li>
     *   <li>썸네일 실패: 로깅만 (필수 아님, 계속 진행)</li>
     *   <li>메타데이터 실패: 로깅만 (필수 아님, 계속 진행)</li>
     *   <li>치명적 오류: 로깅 후 Scheduler가 FAILED 처리</li>
     * </ul>
     *
     * @param fileAssetId FileAsset ID
     */
    @Async
    public void startPipeline(Long fileAssetId) {
        log.info("Starting pipeline processing: fileAssetId={}", fileAssetId);

        try {
            // 1. FileAsset 조회
            FileAsset fileAsset = fileQueryManager.findById(fileAssetId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "FileAsset not found: " + fileAssetId
                ));

            log.debug("FileAsset found for pipeline: fileAssetId={}, fileName={}, contentType={}",
                fileAssetId, fileAsset.getFileName(), fileAsset.getContentType());

            // 2. 썸네일 생성 (이미지만)
            ThumbnailInfo thumbnailInfo = processThumbnail(fileAsset);

            // 3. 메타데이터 추출 (모든 파일)
            FileMetadata metadata = processMetadata(fileAsset);

            // 4. 결과 저장
            saveThumbnailAsFileVariant(fileAsset, thumbnailInfo);
            saveMetadataAsExtractedData(fileAsset, metadata);

            log.info("Pipeline processing completed successfully: fileAssetId={}, thumbnail={}, metadata={}",
                fileAssetId,
                thumbnailInfo != null ? "saved to file_variants" : "skipped",
                metadata.metadata().size() + " fields saved to extracted_data");

        } catch (IllegalArgumentException e) {
            // FileAsset 미존재 - 로깅만 (Outbox는 Scheduler가 관리)
            log.error("FileAsset not found for pipeline: fileAssetId={}", fileAssetId, e);

        } catch (Exception e) {
            // Pipeline 처리 실패 - 로깅 (Outbox는 Scheduler가 FAILED로 표시)
            log.error("Pipeline processing failed: fileAssetId={}", fileAssetId, e);
        }
    }

    /**
     * 썸네일 생성 (이미지만)
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>이미지 파일인지 확인</li>
     *   <li>이미지면 썸네일 생성 (ThumbnailPort)</li>
     *   <li>이미지 아니면 null 반환</li>
     * </ol>
     *
     * <p><strong>에러 처리:</strong></p>
     * <ul>
     *   <li>썸네일 생성 실패: 로깅 후 null 반환 (계속 진행)</li>
     * </ul>
     *
     * @param fileAsset FileAsset
     * @return ThumbnailInfo (이미지가 아니거나 실패 시 null)
     */
    private ThumbnailInfo processThumbnail(FileAsset fileAsset) {
        if (!fileAsset.isImage()) {
            log.debug("Skipping thumbnail generation (not an image): fileAssetId={}, contentType={}",
                fileAsset.getIdValue(), fileAsset.getContentType());
            return null;
        }

        try {
            log.debug("Generating thumbnail: fileAssetId={}", fileAsset.getIdValue());

            ThumbnailInfo thumbnailInfo = thumbnailPort.generateThumbnail(fileAsset);

            log.info("Thumbnail generated successfully: fileAssetId={}, thumbnailKey={}, size={}KB",
                fileAsset.getIdValue(),
                thumbnailInfo.storageKey().value(),
                thumbnailInfo.getSizeInKB());

            return thumbnailInfo;

        } catch (Exception e) {
            // 썸네일 생성 실패 - 로깅만 (계속 진행)
            log.warn("Failed to generate thumbnail (continuing pipeline): fileAssetId={}, error={}",
                fileAsset.getIdValue(), e.getMessage());

            return null;
        }
    }

    /**
     * 썸네일을 FileVariant로 저장
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>ThumbnailInfo가 null이면 스킵</li>
     *   <li>ThumbnailInfo → FileVariant 변환</li>
     *   <li>FileVariant 저장 (file_variants 테이블)</li>
     * </ol>
     *
     * <p><strong>에러 처리:</strong></p>
     * <ul>
     *   <li>저장 실패: 로깅만 (필수 아님, 계속 진행)</li>
     * </ul>
     *
     * @param fileAsset    FileAsset
     * @param thumbnailInfo ThumbnailInfo (null 가능)
     */
    private void saveThumbnailAsFileVariant(FileAsset fileAsset, ThumbnailInfo thumbnailInfo) {
        if (thumbnailInfo == null) {
            log.debug("No thumbnail to save: fileAssetId={}", fileAsset.getIdValue());
            return;
        }

        try {
            // ThumbnailInfo → FileVariant 변환
            FileVariant fileVariant = FileVariant.create(
                new FileAssetId(fileAsset.getIdValue()),
                VariantType.THUMBNAIL,
                thumbnailInfo.storageKey(),
                new FileSize(thumbnailInfo.size()),
                new MimeType(thumbnailInfo.contentType())
            );

            // FileVariant 저장
            FileVariant savedVariant = saveFileVariantPort.save(fileVariant);

            log.info("Thumbnail saved as FileVariant: fileAssetId={}, variantId={}, storageKey={}",
                fileAsset.getIdValue(),
                savedVariant.getIdValue(),
                thumbnailInfo.getStorageKeyValue());

        } catch (Exception e) {
            // 썸네일 저장 실패 - 로깅만 (필수 아님, 계속 진행)
            log.warn("Failed to save thumbnail as FileVariant (continuing pipeline): fileAssetId={}, error={}",
                fileAsset.getIdValue(), e.getMessage());
        }
    }

    /**
     * 메타데이터 추출 (모든 파일)
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>메타데이터 추출 (MetadataPort)</li>
     *   <li>추출된 메타데이터 반환</li>
     * </ol>
     *
     * <p><strong>에러 처리:</strong></p>
     * <ul>
     *   <li>메타데이터 추출 실패: 로깅 후 빈 메타데이터 반환 (계속 진행)</li>
     * </ul>
     *
     * @param fileAsset FileAsset
     * @return FileMetadata (실패 시 빈 메타데이터)
     */
    private FileMetadata processMetadata(FileAsset fileAsset) {
        try {
            log.debug("Extracting metadata: fileAssetId={}, contentType={}",
                fileAsset.getIdValue(), fileAsset.getContentType());

            FileMetadata metadata = metadataPort.extractMetadata(fileAsset);

            log.info("Metadata extracted successfully: fileAssetId={}, fields={}",
                fileAsset.getIdValue(), metadata.metadata().size());

            return metadata;

        } catch (Exception e) {
            // 메타데이터 추출 실패 - 로깅만 (계속 진행)
            log.warn("Failed to extract metadata (continuing pipeline): fileAssetId={}, error={}",
                fileAsset.getIdValue(), e.getMessage());

            // 빈 메타데이터 반환
            return new FileMetadata(
                fileAsset.getId(),
                java.util.Collections.emptyMap()
            );
        }
    }

    /**
     * 메타데이터를 ExtractedData로 저장
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>FileMetadata의 metadata Map이 비어있으면 스킵</li>
     *   <li>metadata Map → JSON 문자열 변환 (ObjectMapper)</li>
     *   <li>ExtractedData Domain 생성 (ExtractionType.METADATA, ExtractionMethod.TIKA)</li>
     *   <li>ExtractedData 저장 (extracted_data 테이블)</li>
     * </ol>
     *
     * <p><strong>에러 처리:</strong></p>
     * <ul>
     *   <li>JSON 변환 실패: 로깅만 (필수 아님, 계속 진행)</li>
     *   <li>저장 실패: 로깅만 (필수 아님, 계속 진행)</li>
     * </ul>
     *
     * <p><strong>Multi-tenant 처리:</strong></p>
     * <ul>
     *   <li>tenantId, organizationId는 FileAsset에서 추출해야 함 (현재는 1L 하드코딩)</li>
     *   <li>TODO: FileAsset에 tenantId, organizationId 필드 추가 필요</li>
     * </ul>
     *
     * @param fileAsset FileAsset
     * @param metadata  FileMetadata (null 또는 빈 Map 가능)
     */
    private void saveMetadataAsExtractedData(FileAsset fileAsset, FileMetadata metadata) {
        if (metadata == null || metadata.metadata().isEmpty()) {
            log.debug("No metadata to save: fileAssetId={}", fileAsset.getIdValue());
            return;
        }

        try {
            // 1. metadata Map → JSON 문자열 변환
            String structuredData = objectMapper.writeValueAsString(metadata.metadata());

            // 2. ExtractedData Domain 생성
            // TODO: tenantId, organizationId를 FileAsset에서 추출 (현재는 1L 하드코딩)
            ExtractedData extractedData = ExtractedData.create(
                new FileAssetId(fileAsset.getIdValue()),
                1L, // tenantId (TODO: FileAsset에서 추출)
                1L, // organizationId (TODO: FileAsset에서 추출)
                ExtractionType.METADATA,
                ExtractionMethod.TIKA,
                1, // version
                null, // traceId (선택사항)
                structuredData,
                1.0, // confidenceScore (Apache Tika는 신뢰도 높음)
                1.0  // qualityScore (Apache Tika는 품질 높음)
            );

            // 3. ExtractedData 저장
            ExtractedData savedData = saveExtractedDataPort.save(extractedData);

            log.info("Metadata saved as ExtractedData: fileAssetId={}, extractedDataId={}, fields={}",
                fileAsset.getIdValue(),
                savedData.getIdValue(),
                metadata.metadata().size());

        } catch (JsonProcessingException e) {
            // JSON 변환 실패 - 로깅만 (필수 아님, 계속 진행)
            log.warn("Failed to convert metadata to JSON (continuing pipeline): fileAssetId={}, error={}",
                fileAsset.getIdValue(), e.getMessage());

        } catch (Exception e) {
            // 메타데이터 저장 실패 - 로깅만 (필수 아님, 계속 진행)
            log.warn("Failed to save metadata as ExtractedData (continuing pipeline): fileAssetId={}, error={}",
                fileAsset.getIdValue(), e.getMessage());
        }
    }
}
