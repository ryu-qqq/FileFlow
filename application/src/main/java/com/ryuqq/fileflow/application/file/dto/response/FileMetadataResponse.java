package com.ryuqq.fileflow.application.file.dto.response;

import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.asset.FileStatus;
import com.ryuqq.fileflow.domain.file.asset.Visibility;
import com.ryuqq.fileflow.domain.file.extraction.ExtractedData;
import com.ryuqq.fileflow.domain.file.variant.FileVariant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 파일 메타데이터 응답 DTO
 *
 * <p>CQRS Query Side - 파일 상세 정보 응답</p>
 *
 * <p><strong>포함 정보:</strong></p>
 * <ul>
 *   <li>파일 식별 정보 (ID, 이름, 크기, MIME 타입)</li>
 *   <li>저장소 정보 (Storage Key)</li>
 *   <li>상태 정보 (Status, Visibility)</li>
 *   <li>타임스탬프 (업로드 시간, 처리 완료 시간, 만료 시간)</li>
 * </ul>
 *
 * @param fileId 파일 ID
 * @param tenantId 테넌트 ID
 * @param organizationId 조직 ID
 * @param ownerUserId 소유자 사용자 ID
 * @param fileName 파일명
 * @param fileSize 파일 크기 (bytes)
 * @param mimeType MIME 타입
 * @param storageKey S3 저장 키
 * @param checksum SHA-256 체크섬
 * @param status 파일 상태
 * @param visibility 가시성
 * @param uploadedAt 업로드 시간
 * @param processedAt 처리 완료 시간
 * @param expiresAt 만료 시간
 * @param retentionDays 보존 기간 (일)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record FileMetadataResponse(
    // FileAsset 기본 정보
    Long fileId,
    String tenantId,
    Long organizationId,
    Long ownerUserId,
    String fileName,
    Long fileSize,
    String mimeType,
    String storageKey,
    String checksum,
    FileStatus status,
    Visibility visibility,
    LocalDateTime uploadedAt,
    LocalDateTime processedAt,
    LocalDateTime expiresAt,
    Integer retentionDays,

    // FileVariant 목록
    List<FileVariantInfo> variants,

    // ExtractedData 메타데이터
    Map<String, String> metadata
) {

    /**
     * FileAsset Domain 객체로부터 Response 생성 (기본 정보만)
     *
     * <p>하위 호환성 유지: variants와 metadata는 빈 리스트/맵</p>
     *
     * @param fileAsset FileAsset Aggregate
     * @return FileMetadataResponse
     */
    public static FileMetadataResponse from(FileAsset fileAsset) {
        return new FileMetadataResponse(
            fileAsset.getIdValue(),
            String.valueOf(fileAsset.getTenantId().value()),
            fileAsset.getOrganizationId(),
            fileAsset.getOwnerUserId(),
            fileAsset.getFileName() != null ? fileAsset.getFileName().value() : null,
            fileAsset.getFileSize() != null ? fileAsset.getFileSize().bytes() : null,
            fileAsset.getMimeType() != null ? fileAsset.getMimeType().value() : null,
            fileAsset.getStorageKey().value(),
            fileAsset.getChecksum() != null ? fileAsset.getChecksum().value() : null,
            fileAsset.getStatus(),
            fileAsset.getVisibility(),
            fileAsset.getUploadedAt(),
            fileAsset.getProcessedAt(),
            fileAsset.getExpiresAt(),
            fileAsset.getRetentionDays(),
            List.of(),  // 빈 variants
            Map.of()    // 빈 metadata
        );
    }

    /**
     * FileAsset + FileVariant + ExtractedData로부터 Response 생성 (전체 정보)
     *
     * <p>파일 상세 조회 시 사용: Variants와 메타데이터 포함</p>
     *
     * @param fileAsset FileAsset Aggregate
     * @param variants FileVariant 목록
     * @param extractedDataList ExtractedData 목록
     * @return FileMetadataResponse
     */
    public static FileMetadataResponse of(
        FileAsset fileAsset,
        List<FileVariant> variants,
        List<ExtractedData> extractedDataList
    ) {
        // FileVariant → FileVariantInfo 변환
        List<FileVariantInfo> variantInfos = variants.stream()
            .map(FileVariantInfo::from)
            .toList();

        // ExtractedData → Map<String, String> 변환
        Map<String, String> metadataMap = extractedDataList.stream()
            .collect(Collectors.toMap(
                data -> data.getExtractionType().name(),  // Key: EXIF, OCR, FACE_DETECTION 등
                data -> data.getStructuredData()          // Value: JSON 문자열
            ));

        return new FileMetadataResponse(
            fileAsset.getIdValue(),
            String.valueOf(fileAsset.getTenantId().value()),
            fileAsset.getOrganizationId(),
            fileAsset.getOwnerUserId(),
            fileAsset.getFileName() != null ? fileAsset.getFileName().value() : null,
            fileAsset.getFileSize() != null ? fileAsset.getFileSize().bytes() : null,
            fileAsset.getMimeType() != null ? fileAsset.getMimeType().value() : null,
            fileAsset.getStorageKey().value(),
            fileAsset.getChecksum() != null ? fileAsset.getChecksum().value() : null,
            fileAsset.getStatus(),
            fileAsset.getVisibility(),
            fileAsset.getUploadedAt(),
            fileAsset.getProcessedAt(),
            fileAsset.getExpiresAt(),
            fileAsset.getRetentionDays(),
            variantInfos,
            metadataMap
        );
    }

    /**
     * FileVariant 정보 DTO
     *
     * <p>API 응답에서 사용할 Variant 간략 정보</p>
     */
    public record FileVariantInfo(
        String variantType,
        String storageKey,
        Long fileSize
    ) {
        public static FileVariantInfo from(FileVariant variant) {
            return new FileVariantInfo(
                variant.getVariantType().name(),
                variant.getStorageKey().value(),
                variant.getFileSize() != null ? variant.getFileSize().bytes() : null
            );
        }
    }
}
