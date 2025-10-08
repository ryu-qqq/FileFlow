package com.ryuqq.fileflow.adapter.s3.adapter;

import com.ryuqq.fileflow.adapter.s3.config.S3Properties;
import com.ryuqq.fileflow.domain.upload.command.FileUploadCommand;
import com.ryuqq.fileflow.domain.upload.vo.CheckSum;
import com.ryuqq.fileflow.domain.upload.vo.MultipartUploadInfo;
import com.ryuqq.fileflow.domain.upload.vo.PartUploadInfo;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * AWS S3 멀티파트 업로드 Adapter
 * 대용량 파일(100MB 이상)을 여러 파트로 분할하여 업로드하는 기능을 제공합니다.
 *
 * Hexagonal Architecture:
 * - Adapter Layer의 구현체 (Outbound Adapter)
 * - S3PresignedUrlAdapter에서 위임받아 처리
 *
 * AWS S3 멀티파트 업로드 프로세스:
 * 1. CreateMultipartUpload - 업로드 시작 (uploadId 발급)
 * 2. UploadPart - 각 파트 업로드 (Presigned URL 사용)
 * 3. CompleteMultipartUpload - 모든 파트 병합 (클라이언트에서 호출)
 *
 * 제약사항:
 * - 파트 크기: 5MB (minimum) ~ 5GB (maximum)
 * - 파트 개수: 최대 10,000개
 * - 마지막 파트는 5MB 미만 가능
 * - 기본 파트 크기: 10MB (TARGET_PART_SIZE)
 *
 * NO Lombok:
 * - 명시적인 생성자와 메서드 사용
 */
@Component
public class S3MultipartAdapter {

    private static final long TARGET_PART_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MIN_PART_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_PARTS = 10_000;

    private static final String METADATA_UPLOADER_ID = "x-amz-meta-uploader-id";
    private static final String METADATA_ORIGINAL_FILENAME = "x-amz-meta-original-filename";
    private static final String METADATA_FILE_TYPE = "x-amz-meta-file-type";
    private static final String METADATA_CHECKSUM_ALGORITHM = "x-amz-meta-checksum-algorithm";
    private static final String METADATA_CHECKSUM_VALUE = "x-amz-meta-checksum-value";

    private final S3Presigner s3Presigner;
    private final software.amazon.awssdk.services.s3.S3Client s3Client;
    private final S3Properties s3Properties;

    /**
     * S3MultipartAdapter 생성자
     *
     * @param s3Presigner AWS S3 Presigner
     * @param s3Client AWS S3 Client
     * @param s3Properties S3 설정 프로퍼티
     * @throws IllegalArgumentException 파라미터가 null인 경우
     */
    public S3MultipartAdapter(
            S3Presigner s3Presigner,
            software.amazon.awssdk.services.s3.S3Client s3Client,
            S3Properties s3Properties
    ) {
        this.s3Presigner = Objects.requireNonNull(s3Presigner, "S3Presigner cannot be null");
        this.s3Client = Objects.requireNonNull(s3Client, "S3Client cannot be null");
        this.s3Properties = Objects.requireNonNull(s3Properties, "S3Properties cannot be null");
    }

    /**
     * 멀티파트 업로드를 시작하고 파트별 Presigned URL을 생성합니다.
     *
     * 프로세스:
     * 1. 업로드 경로 생성
     * 2. 멀티파트 업로드 시작 (CreateMultipartUpload)
     * 3. 파일을 파트로 분할 계산
     * 4. 각 파트에 대한 Presigned URL 생성
     * 5. MultipartUploadInfo 도메인 객체로 변환
     *
     * @param command 파일 업로드 명령
     * @return 멀티파트 업로드 정보 (uploadId와 파트별 Presigned URL 포함)
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws RuntimeException 멀티파트 업로드 시작 실패 시
     */
    public MultipartUploadInfo initiateMultipartUpload(FileUploadCommand command) {
        validateCommand(command);
        validateFileSizeForMultipart(command.fileSizeBytes());

        String uploadPath = buildUploadPath(command);
        String uploadId = createMultipartUpload(uploadPath, command);
        List<PartUploadInfo> parts = generatePartUploadInfos(
                uploadId,
                uploadPath,
                command.fileSizeBytes()
        );

        return MultipartUploadInfo.of(uploadId, uploadPath, parts);
    }

    // ========== Upload Path Building ==========

    // 형식: {pathPrefix}/{uploaderId}/{UUID}/{fileName}
    private String buildUploadPath(FileUploadCommand command) {
        String prefix = s3Properties.getPathPrefix();
        String uploaderId = command.uploaderId();
        String uniqueId = UUID.randomUUID().toString();
        String fileName = command.fileName();

        if (prefix.isEmpty()) {
            return String.format("%s/%s/%s", uploaderId, uniqueId, fileName);
        }

        return String.format("%s/%s/%s/%s", prefix, uploaderId, uniqueId, fileName);
    }

    // ========== Multipart Upload Creation ==========

    private String createMultipartUpload(String uploadPath, FileUploadCommand command) {
        java.util.Map<String, String> metadata = new java.util.HashMap<>();
        metadata.put(METADATA_UPLOADER_ID, command.uploaderId());
        metadata.put(METADATA_ORIGINAL_FILENAME, command.fileName());
        metadata.put(METADATA_FILE_TYPE, command.fileType().name());

        // CheckSum이 제공된 경우 메타데이터에 추가
        if (command.checkSum() != null) {
            metadata.put(METADATA_CHECKSUM_ALGORITHM, command.checkSum().algorithm());
            metadata.put(METADATA_CHECKSUM_VALUE, command.checkSum().normalizedValue());
        }

        CreateMultipartUploadRequest.Builder builder = CreateMultipartUploadRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(uploadPath)
                .contentType(command.contentType())
                .metadata(metadata);

        // CheckSum이 제공된 경우 checksum 알고리즘 지정
        if (command.checkSum() != null && CheckSum.ALGORITHM_SHA256.equals(command.checkSum().algorithm())) {
            builder.checksumAlgorithm(software.amazon.awssdk.services.s3.model.ChecksumAlgorithm.SHA256);
        }

        CreateMultipartUploadRequest request = builder.build();
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(request);

        return response.uploadId();
    }

    // ========== Part Info Generation ==========

    private List<PartUploadInfo> generatePartUploadInfos(
            String uploadId,
            String uploadPath,
            long totalFileSize
    ) {
        List<PartInfo> partInfos = calculateParts(totalFileSize);
        List<PartUploadInfo> partUploadInfos = new ArrayList<>(partInfos.size());
        Duration signatureDuration = Duration.ofMinutes(s3Properties.getPresignedUrlExpirationMinutes());

        for (PartInfo partInfo : partInfos) {
            String presignedUrl = generatePresignedUrlForPart(
                    uploadPath,
                    uploadId,
                    partInfo.partNumber(),
                    signatureDuration
            );

            LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC).plus(signatureDuration);

            PartUploadInfo partUploadInfo = PartUploadInfo.of(
                    partInfo.partNumber(),
                    presignedUrl,
                    partInfo.startByte(),
                    partInfo.endByte(),
                    expiresAt
            );

            partUploadInfos.add(partUploadInfo);
        }

        return partUploadInfos;
    }

    private List<PartInfo> calculateParts(long totalFileSize) {
        List<PartInfo> parts = new ArrayList<>();
        long remainingBytes = totalFileSize;
        long currentOffset = 0;
        int partNumber = 1;

        while (remainingBytes > 0) {
            long partSize = Math.min(TARGET_PART_SIZE, remainingBytes);

            // 마지막 파트가 MIN_PART_SIZE보다 작아지는 것을 방지하기 위해 파트 크기를 조정합니다.
            if (remainingBytes > partSize && remainingBytes - partSize < MIN_PART_SIZE) {
                // 남은 용량을 두 개의 파트로 균등하게 분할
                partSize = (long) Math.ceil((double) remainingBytes / 2.0);
            }

            long startByte = currentOffset;
            long endByte = currentOffset + partSize - 1;

            parts.add(new PartInfo(partNumber, startByte, endByte));

            currentOffset += partSize;
            remainingBytes -= partSize;
            partNumber++;
        }

        return parts;
    }

    private String generatePresignedUrlForPart(
            String uploadPath,
            String uploadId,
            int partNumber,
            Duration signatureDuration
    ) {
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(uploadPath)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();

        UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                .signatureDuration(signatureDuration)
                .uploadPartRequest(uploadPartRequest)
                .build();

        PresignedUploadPartRequest presignedRequest = s3Presigner.presignUploadPart(presignRequest);

        return presignedRequest.url().toString();
    }

    // ========== Validation Methods ==========

    private static void validateCommand(FileUploadCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("FileUploadCommand cannot be null");
        }
    }

    private static void validateFileSizeForMultipart(long fileSizeBytes) {
        // 최소 크기 검증 (5MB 이상)
        if (fileSizeBytes < MIN_PART_SIZE) {
            throw new IllegalArgumentException(
                    "File size (" + fileSizeBytes + " bytes) is too small for multipart upload. " +
                    "Minimum size: " + MIN_PART_SIZE + " bytes (5MB)"
            );
        }

        // 최대 파트 수 초과 검증
        long minPartsNeeded = (fileSizeBytes + TARGET_PART_SIZE - 1) / TARGET_PART_SIZE;
        if (minPartsNeeded > MAX_PARTS) {
            throw new IllegalArgumentException(
                    "File size (" + fileSizeBytes + " bytes) would require " + minPartsNeeded +
                    " parts, which exceeds the maximum allowed " + MAX_PARTS + " parts"
            );
        }
    }

    // ========== Internal Data Structures ==========

    /**
     * 파트 정보를 담는 내부 클래스
     * 멀티파트 업로드를 위한 파트 분할 정보를 표현합니다.
     */
    private record PartInfo(
            int partNumber,
            long startByte,
            long endByte
    ) {
        private PartInfo {
            if (partNumber < 1) {
                throw new IllegalArgumentException("Part number must be >= 1");
            }
            if (startByte < 0) {
                throw new IllegalArgumentException("Start byte must be >= 0");
            }
            if (endByte < startByte) {
                throw new IllegalArgumentException("End byte must be >= start byte");
            }
        }

        public long partSize() {
            return endByte - startByte + 1;
        }
    }
}
