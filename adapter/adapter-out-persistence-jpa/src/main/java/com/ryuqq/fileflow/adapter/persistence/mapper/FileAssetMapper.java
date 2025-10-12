package com.ryuqq.fileflow.adapter.persistence.mapper;

import com.ryuqq.fileflow.adapter.persistence.entity.FileAssetEntity;
import com.ryuqq.fileflow.domain.upload.vo.*;
import org.springframework.stereotype.Component;

/**
 * FileAsset Entity ↔ Domain 양방향 Mapper
 *
 * 변환 규칙:
 * - FileId: Domain VO ↔ Entity String
 * - TenantId: Domain VO ↔ Entity String
 * - S3Location: Domain VO (bucket, key) ↔ Entity fields (s3Bucket, s3Key)
 * - CheckSum: Domain VO ↔ Entity String
 * - FileSize: Domain VO ↔ Entity Long
 * - ContentType: Domain VO ↔ Entity String
 *
 * @author sangwon-ryu
 */
@Component
public class FileAssetMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain FileAsset 도메인 객체
     * @return FileAssetEntity
     */
    public FileAssetEntity toEntity(FileAsset domain) {
        if (domain == null) {
            return null;
        }

        // S3 Key에서 파일 이름 추출
        String s3Key = domain.getS3Location().key();
        String fileName = extractFileNameFromS3Key(s3Key);
        String fileExtension = extractFileExtension(fileName);

        // CheckSum 포맷: "algorithm:value" (예: "SHA-256:abc123...")
        String checksumValue = domain.getChecksum().algorithm() + ":" + domain.getChecksum().value();

        return FileAssetEntity.of(
                domain.getFileId().value(),
                domain.getSessionId(),
                domain.getTenantId().value(),
                fileName,                              // originalFileName
                fileName,                              // storedFileName (동일하게 설정)
                domain.getS3Location().bucket(),
                s3Key,
                "ap-northeast-2",                      // s3Region (기본값)
                null,                                  // cdnUrl (없음)
                domain.getFileSize().bytes(),
                domain.getContentType().value(),
                fileExtension,
                checksumValue,
                false                                  // isPublic (기본값)
        );
    }

    /**
     * S3 Key에서 파일 이름 추출
     */
    private String extractFileNameFromS3Key(String s3Key) {
        if (s3Key == null || s3Key.isEmpty()) {
            return "unknown";
        }
        int lastSlashIndex = s3Key.lastIndexOf('/');
        return (lastSlashIndex >= 0) ? s3Key.substring(lastSlashIndex + 1) : s3Key;
    }

    /**
     * 파일 이름에서 확장자 추출
     */
    private String extractFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex >= 0) ? fileName.substring(lastDotIndex + 1) : null;
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity FileAssetEntity
     * @return FileAsset 도메인 객체
     */
    public FileAsset toDomain(FileAssetEntity entity) {
        if (entity == null) {
            return null;
        }

        FileId fileId = FileId.of(entity.getFileId());
        TenantId tenantId = TenantId.of(entity.getTenantId());
        S3Location s3Location = S3Location.of(entity.getS3Bucket(), entity.getS3Key());

        // CheckSum 파싱: "algorithm:value" 포맷 (예: "SHA-256:abc123...")
        CheckSum checkSum = parseCheckSum(entity.getChecksum());

        FileSize fileSize = FileSize.ofBytes(entity.getFileSize());
        ContentType contentType = ContentType.of(entity.getContentType());

        return FileAsset.reconstitute(
                fileId,
                entity.getSessionId(),
                tenantId,
                s3Location,
                checkSum,
                fileSize,
                contentType,
                entity.getCreatedAt()
        );
    }

    /**
     * CheckSum 문자열 파싱
     *
     * @param checksumString "algorithm:value" 포맷
     * @return CheckSum 객체
     */
    private CheckSum parseCheckSum(String checksumString) {
        if (checksumString == null || checksumString.isEmpty()) {
            throw new IllegalArgumentException("Checksum string cannot be null or empty");
        }

        String[] parts = checksumString.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid checksum format. Expected 'algorithm:value', got: " + checksumString
            );
        }

        String algorithm = parts[0];
        String value = parts[1];

        return switch (algorithm) {
            case "SHA-256" -> CheckSum.sha256(value);
            case "SHA-512" -> CheckSum.sha512(value);
            case "MD5" -> CheckSum.md5(value);
            default -> throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        };
    }
}
