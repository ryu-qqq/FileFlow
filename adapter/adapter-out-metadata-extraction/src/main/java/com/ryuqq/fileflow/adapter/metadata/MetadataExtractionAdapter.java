package com.ryuqq.fileflow.adapter.metadata;

import com.ryuqq.fileflow.application.file.MetadataExtractionException;
import com.ryuqq.fileflow.application.file.MetadataExtractionPort;
import com.ryuqq.fileflow.domain.file.FileMetadata;
import com.ryuqq.fileflow.domain.upload.vo.FileId;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * 파일 메타데이터 추출 Adapter 구현체
 *
 * 역할:
 * - Application Layer의 MetadataExtractionPort를 구현
 * - 파일 타입에 따라 적절한 Extractor에 위임
 * - 현재 이미지 파일만 지원 (ImageMetadataExtractor)
 * - 추후 비디오, 문서 Extractor 추가 예정
 *
 * @author sangwon-ryu
 */
@Component
public class MetadataExtractionAdapter implements MetadataExtractionPort {

    private final ImageMetadataExtractor imageMetadataExtractor;

    public MetadataExtractionAdapter(ImageMetadataExtractor imageMetadataExtractor) {
        this.imageMetadataExtractor = imageMetadataExtractor;
    }

    /**
     * 파일 스트림으로부터 메타데이터를 추출합니다.
     *
     * @param fileId 파일 ID
     * @param inputStream 파일 입력 스트림
     * @param contentType 파일의 Content-Type
     * @return 추출된 메타데이터 리스트
     * @throws MetadataExtractionException 메타데이터 추출 중 오류 발생 시
     */
    @Override
    public List<FileMetadata> extractMetadata(FileId fileId, InputStream inputStream, String contentType) {
        if (imageMetadataExtractor.supports(contentType)) {
            return imageMetadataExtractor.extract(fileId, inputStream, contentType);
        }

        // 지원하지 않는 타입은 빈 리스트 반환 (추후 비디오, 문서 Extractor 추가 시 확장)
        return Collections.emptyList();
    }

    /**
     * 특정 Content-Type의 메타데이터 추출을 지원하는지 확인합니다.
     *
     * @param contentType 파일의 Content-Type
     * @return 지원 여부
     */
    @Override
    public boolean supports(String contentType) {
        return imageMetadataExtractor.supports(contentType);
    }
}
