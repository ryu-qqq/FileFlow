package com.ryuqq.fileflow.adapter.out.metadata.adapter;

import com.ryuqq.fileflow.adapter.out.metadata.extractor.TikaMetadataExtractor;
import com.ryuqq.fileflow.application.file.port.out.MetadataPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.metadata.FileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Metadata Extraction Adapter
 *
 * <p>Application Layer의 {@link MetadataPort}를 구현하는 Metadata Extraction Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>S3에서 파일 다운로드 (전체 또는 부분)</li>
 *   <li>Apache Tika를 이용한 메타데이터 추출</li>
 *   <li>메타데이터 정규화 (공통 포맷)</li>
 *   <li>FileMetadata 생성 및 반환</li>
 * </ul>
 *
 * <p><strong>처리 흐름:</strong></p>
 * <ol>
 *   <li>파일 타입 확인 (Content-Type)</li>
 *   <li>S3에서 파일 다운로드 (부분 다운로드 최적화)</li>
 *   <li>TikaMetadataExtractor로 메타데이터 추출</li>
 *   <li>메타데이터 정규화 및 변환</li>
 *   <li>FileMetadata 생성 및 반환</li>
 * </ol>
 *
 * <p><strong>지원 파일 타입:</strong></p>
 * <ul>
 *   <li>이미지: JPEG (EXIF), PNG, GIF, BMP, TIFF, WebP</li>
 *   <li>비디오: MP4, AVI, MOV, MKV, WebM</li>
 *   <li>문서: PDF, DOCX, XLSX, PPTX</li>
 *   <li>기타: TXT, CSV (기본 정보만)</li>
 * </ul>
 *
 * <p><strong>에러 처리:</strong></p>
 * <ul>
 *   <li>지원하지 않는 파일: 빈 메타데이터 반환 (예외 없음)</li>
 *   <li>S3 다운로드 실패: RuntimeException</li>
 *   <li>메타데이터 추출 실패: 빈 메타데이터 반환 (예외 없음)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class MetadataAdapter implements MetadataPort {

    private static final Logger log = LoggerFactory.getLogger(MetadataAdapter.class);

    private static final long MAX_DOWNLOAD_SIZE = 10 * 1024 * 1024; // 10MB (메타데이터는 파일 헤더에만 있음)

    private final S3Client s3Client;
    private final String bucket;
    private final TikaMetadataExtractor metadataExtractor;

    /**
     * 생성자
     *
     * @param s3Client          S3 Client
     * @param bucket            S3 Bucket Name
     * @param metadataExtractor 메타데이터 추출기
     */
    public MetadataAdapter(
        S3Client s3Client,
        String bucket,
        TikaMetadataExtractor metadataExtractor
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.metadataExtractor = metadataExtractor;
    }

    /**
     * 파일 메타데이터 추출
     *
     * <p><strong>처리 단계:</strong></p>
     * <ol>
     *   <li>파일 타입 확인</li>
     *   <li>S3에서 파일 다운로드 (부분 다운로드 최적화)</li>
     *   <li>Apache Tika로 메타데이터 추출</li>
     *   <li>메타데이터 정규화</li>
     *   <li>FileMetadata 반환</li>
     * </ol>
     *
     * @param fileAsset 원본 파일
     * @return 추출된 메타데이터 (없으면 빈 Map)
     * @throws RuntimeException S3 다운로드 실패
     */
    @Override
    public FileMetadata extractMetadata(FileAsset fileAsset) {
        log.info("Starting metadata extraction: fileId={}, fileName={}, contentType={}",
            fileAsset.getIdValue(), fileAsset.getFileName(), fileAsset.getContentType());

        String storageKey = fileAsset.getStorageKeyValue();

        try {
            // 1. S3에서 파일 다운로드 (부분 다운로드 최적화)
            log.debug("Downloading file from S3: bucket={}, key={}", bucket, storageKey);

            InputStream fileStream = downloadFromS3(storageKey);

            // 2. 메타데이터 추출
            log.debug("Extracting metadata: contentType={}", fileAsset.getContentType());

            Map<String, Object> extractedMetadata = metadataExtractor.extract(
                fileStream,
                fileAsset.getContentType()
            );

            // 3. FileMetadata 생성
            FileMetadata metadata = new FileMetadata(
                fileAsset.getId(),
                extractedMetadata
            );

            log.info("Metadata extraction completed: fileId={}, metadataCount={}",
                fileAsset.getIdValue(), extractedMetadata.size());

            return metadata;

        } catch (S3Exception e) {
            // S3 다운로드 실패 - 재시도 가능
            log.error("Failed to download file from S3: fileId={}, key={}",
                fileAsset.getIdValue(), storageKey, e);

            throw new RuntimeException(
                "File download failed: fileId=" + fileAsset.getIdValue(),
                e
            );

        } catch (Exception e) {
            // 메타데이터 추출 실패 - 빈 메타데이터 반환 (예외 없음)
            log.warn("Failed to extract metadata (returning empty): fileId={}, error={}",
                fileAsset.getIdValue(), e.getMessage());

            return new FileMetadata(
                fileAsset.getId(),
                new HashMap<>()
            );
        }
    }

    /**
     * S3에서 파일 다운로드 (부분 다운로드 최적화)
     *
     * <p><strong>최적화:</strong></p>
     * <ul>
     *   <li>메타데이터는 파일 헤더에만 있음 (대부분 첫 10MB)</li>
     *   <li>대용량 파일도 부분 다운로드로 빠른 처리</li>
     * </ul>
     *
     * @param key S3 Key
     * @return 파일 InputStream
     * @throws S3Exception S3 다운로드 실패
     */
    private InputStream downloadFromS3(String key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            // 메타데이터는 파일 헤더에만 있으므로 부분 다운로드
            // (Tika는 헤더만 읽고 처리)
            .range("bytes=0-" + (MAX_DOWNLOAD_SIZE - 1))
            .build();

        ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getRequest);

        return response;
    }
}
