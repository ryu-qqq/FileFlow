package com.ryuqq.fileflow.application.file.service;

import com.ryuqq.fileflow.application.file.dto.query.FileMetadataQuery;
import com.ryuqq.fileflow.application.file.dto.response.FileMetadataResponse;
import com.ryuqq.fileflow.application.file.port.in.GetFileMetadataUseCase;
import com.ryuqq.fileflow.application.file.port.out.ExtractedDataQueryPort;
import com.ryuqq.fileflow.application.file.port.out.FileQueryPort;
import com.ryuqq.fileflow.application.file.port.out.FileVariantQueryPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.extraction.ExtractedData;
import com.ryuqq.fileflow.domain.file.variant.FileVariant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 파일 메타데이터 조회 Service
 *
 * <p>CQRS Query Side - 파일 상세 정보 조회 구현</p>
 *
 * <p><strong>트랜잭션 전략:</strong></p>
 * <ul>
 *   <li>@Transactional(readOnly = true) - 읽기 전용 최적화</li>
 *   <li>Dirty Checking 비활성화</li>
 *   <li>FlushMode = MANUAL</li>
 * </ul>
 *
 * <p><strong>실행 흐름:</strong></p>
 * <ol>
 *   <li>FileQueryPort를 통해 파일 조회</li>
 *   <li>파일 존재 여부 검증</li>
 *   <li>Domain 객체 → Response DTO 변환</li>
 * </ol>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class GetFileMetadataService implements GetFileMetadataUseCase {

    private final FileQueryPort fileQueryPort;
    private final FileVariantQueryPort fileVariantQueryPort;
    private final ExtractedDataQueryPort extractedDataQueryPort;

    /**
     * 생성자
     *
     * @param fileQueryPort 파일 조회 Port
     * @param fileVariantQueryPort FileVariant 조회 Port
     * @param extractedDataQueryPort ExtractedData 조회 Port
     */
    public GetFileMetadataService(
        FileQueryPort fileQueryPort,
        FileVariantQueryPort fileVariantQueryPort,
        ExtractedDataQueryPort extractedDataQueryPort
    ) {
        this.fileQueryPort = fileQueryPort;
        this.fileVariantQueryPort = fileVariantQueryPort;
        this.extractedDataQueryPort = extractedDataQueryPort;
    }

    /**
     * 파일 메타데이터 조회 (Variants + ExtractedData 포함)
     *
     * <p><strong>조회 항목:</strong></p>
     * <ul>
     *   <li>FileAsset 기본 정보</li>
     *   <li>FileVariant 목록 (THUMBNAIL, OPTIMIZED 등)</li>
     *   <li>ExtractedData 메타데이터 (EXIF, OCR, FACE_DETECTION 등)</li>
     * </ul>
     *
     * @param query 파일 메타데이터 조회 Query
     * @return 파일 메타데이터 응답 (variants + metadata 포함)
     * @throws IllegalArgumentException 파일이 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    @Override
    public FileMetadataResponse execute(FileMetadataQuery query) {
        // 1. FileAsset 조회
        FileAsset fileAsset = fileQueryPort.findByQuery(query)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format(
                    "File not found: fileId=%s, tenantId=%s",
                    query.fileId().value(),
                    query.tenantId().value()
                )
            ));

        // 2. FileVariant 목록 조회 (fileId 기준)
        List<FileVariant> variants = fileVariantQueryPort.findAllByFileId(fileAsset.getIdValue());

        // 3. ExtractedData 목록 조회 (fileId 기준)
        List<ExtractedData> extractedDataList = extractedDataQueryPort.findAllByFileId(fileAsset.getIdValue());

        // 4. Response 변환 (FileAsset + Variants + ExtractedData)
        return FileMetadataResponse.of(fileAsset, variants, extractedDataList);
    }
}
