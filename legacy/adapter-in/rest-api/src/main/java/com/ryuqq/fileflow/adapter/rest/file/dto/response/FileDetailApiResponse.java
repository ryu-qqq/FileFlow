package com.ryuqq.fileflow.adapter.rest.file.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * File Detail API Response
 *
 * <p>파일 상세 조회 전용 응답 DTO (Variant 정보 포함)</p>
 *
 * <p><strong>응답 예시:</strong></p>
 * <pre>{@code
 * {
 *   "fileId": 123,
 *   "fileName": "profile.jpg",
 *   "fileSize": 5242880,
 *   "contentType": "image/jpeg",
 *   "status": "AVAILABLE",
 *   "visibility": "PUBLIC",
 *   
 *   "ownerUserId": 100,
 *   "tenantId": "1",
 *   "organizationId": 2,
 *   
 *   "uploadSessionKey": "spu_abc123",
 *   "checksum": "d41d8cd98f00b204e9800998ecf8427e",
 *   
 *   "original": {
 *     "type": "ORIGINAL",
 *     "storageKey": "tenant-1/org-2/original/profile.jpg",
 *     "url": "https://cdn.example.com/original/profile.jpg",
 *     "width": 2000,
 *     "height": 1500,
 *     "fileSize": 5242880
 *   },
 *   
 *   "variants": [
 *     {
 *       "type": "THUMBNAIL",
 *       "storageKey": "tenant-1/org-2/thumbnail/profile.jpg",
 *       "url": "https://cdn.example.com/thumbnail/profile.jpg",
 *       "width": 200,
 *       "height": 150,
 *       "fileSize": 51200
 *     }
 *   ],
 *   
 *   "metadata": {
 *     "camera": "iPhone 14 Pro"
 *   },
 *   
 *   "uploadedAt": "2024-10-31T12:00:00",
 *   "processedAt": "2024-10-31T12:01:00",
 *   "expiresAt": "2024-11-30T12:00:00",
 *   "retentionDays": 30
 * }
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FileDetailApiResponse {
    
    // 기본 정보
    private Long fileId;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String status;
    private String visibility;
    
    // 소유권 정보
    private Long ownerUserId;
    private String tenantId;
    private Long organizationId;
    
    // 업로드 정보
    private String uploadSessionKey;
    private String checksum;
    
    // 원본 정보
    private FileVariantInfo original;
    
    // 변형본 목록
    private List<FileVariantInfo> variants;
    
    // 메타데이터
    private Map<String, String> metadata;
    
    // 타임스탬프
    private LocalDateTime uploadedAt;
    private LocalDateTime processedAt;
    private LocalDateTime expiresAt;
    private Integer retentionDays;

    /**
     * Private 생성자
     */
    private FileDetailApiResponse() {
    }

    /**
     * Static Factory Method
     *
     * @param fileId File ID
     * @param fileName 파일명
     * @param fileSize 파일 크기 (bytes)
     * @param contentType Content Type
     * @param status 파일 상태
     * @param visibility 파일 가시성
     * @param ownerUserId 소유자 사용자 ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param uploadSessionKey 업로드 세션 키
     * @param checksum 체크섬
     * @param original 원본 정보
     * @param variants 변형본 목록
     * @param metadata 메타데이터
     * @param uploadedAt 업로드 시간
     * @param processedAt 처리 완료 시간
     * @param expiresAt 만료 시간
     * @param retentionDays 보존 기간 (일)
     * @return FileDetailApiResponse
     */
    public static FileDetailApiResponse of(
        Long fileId,
        String fileName,
        Long fileSize,
        String contentType,
        String status,
        String visibility,
        Long ownerUserId,
        String tenantId,
        Long organizationId,
        String uploadSessionKey,
        String checksum,
        FileVariantInfo original,
        List<FileVariantInfo> variants,
        Map<String, String> metadata,
        LocalDateTime uploadedAt,
        LocalDateTime processedAt,
        LocalDateTime expiresAt,
        Integer retentionDays
    ) {
        FileDetailApiResponse response = new FileDetailApiResponse();
        response.fileId = fileId;
        response.fileName = fileName;
        response.fileSize = fileSize;
        response.contentType = contentType;
        response.status = status;
        response.visibility = visibility;
        response.ownerUserId = ownerUserId;
        response.tenantId = tenantId;
        response.organizationId = organizationId;
        response.uploadSessionKey = uploadSessionKey;
        response.checksum = checksum;
        response.original = original;
        response.variants = variants != null ? List.copyOf(variants) : List.of();
        response.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
        response.uploadedAt = uploadedAt;
        response.processedAt = processedAt;
        response.expiresAt = expiresAt;
        response.retentionDays = retentionDays;
        return response;
    }

    /**
     * File ID Getter
     *
     * @return File ID
     */
    public Long getFileId() {
        return fileId;
    }

    /**
     * File Name Getter
     *
     * @return 파일명
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * File Size Getter
     *
     * @return 파일 크기 (bytes)
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * Content Type Getter
     *
     * @return Content Type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Status Getter
     *
     * @return 파일 상태
     */
    public String getStatus() {
        return status;
    }

    /**
     * Visibility Getter
     *
     * @return 파일 가시성
     */
    public String getVisibility() {
        return visibility;
    }

    /**
     * Owner User ID Getter
     *
     * @return 소유자 사용자 ID
     */
    public Long getOwnerUserId() {
        return ownerUserId;
    }

    /**
     * Tenant ID Getter
     *
     * @return 테넌트 ID
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Organization ID Getter
     *
     * @return 조직 ID
     */
    public Long getOrganizationId() {
        return organizationId;
    }

    /**
     * Upload Session Key Getter
     *
     * @return 업로드 세션 키
     */
    public String getUploadSessionKey() {
        return uploadSessionKey;
    }

    /**
     * Checksum Getter
     *
     * @return 체크섬
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Original Getter
     *
     * @return 원본 정보
     */
    public FileVariantInfo getOriginal() {
        return original;
    }

    /**
     * Variants Getter
     *
     * @return 변형본 목록
     */
    public List<FileVariantInfo> getVariants() {
        return variants;
    }

    /**
     * Metadata Getter
     *
     * @return 메타데이터
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Uploaded At Getter
     *
     * @return 업로드 시간
     */
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    /**
     * Processed At Getter
     *
     * @return 처리 완료 시간
     */
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    /**
     * Expires At Getter
     *
     * @return 만료 시간
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Retention Days Getter
     *
     * @return 보존 기간 (일)
     */
    public Integer getRetentionDays() {
        return retentionDays;
    }
}
