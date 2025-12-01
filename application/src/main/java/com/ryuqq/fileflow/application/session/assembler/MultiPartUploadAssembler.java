package com.ryuqq.fileflow.application.session.assembler;

import com.ryuqq.fileflow.application.session.dto.command.InitMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse.CompletedPartInfo;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse.PartInfo;
import com.ryuqq.fileflow.application.session.dto.response.MarkPartUploadedResponse;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ExpirationTime;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.PartNumber;
import com.ryuqq.fileflow.domain.session.vo.PartSize;
import com.ryuqq.fileflow.domain.session.vo.PresignedUrl;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.S3UploadId;
import com.ryuqq.fileflow.domain.session.vo.S3UploadMetadata;
import com.ryuqq.fileflow.domain.session.vo.TotalParts;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * Multipart 업로드 세션 Assembler.
 *
 * <p>Application DTO를 Domain Aggregate로 변환합니다.
 */
@Component
public class MultiPartUploadAssembler {

    private static final Duration MULTIPART_UPLOAD_EXPIRATION = Duration.ofHours(24);

    private final ClockHolder clockHolder;
    private final Supplier<UserContext> userContextSupplier;

    public MultiPartUploadAssembler(
            ClockHolder clockHolder, Supplier<UserContext> userContextSupplier) {
        this.clockHolder = clockHolder;
        this.userContextSupplier = userContextSupplier;
    }

    /**
     * Command에서 S3 업로드 메타데이터를 추출합니다.
     *
     * <p>S3 Multipart Upload 초기화 (Upload ID 발급)에 필요한 최소 정보만 생성합니다.
     *
     * <p><strong>목적</strong>:
     *
     * <ul>
     *   <li>불필요한 전체 세션 생성 방지
     *   <li>S3 Upload ID 발급 전 필요한 최소 정보만 제공
     * </ul>
     *
     * @param command 초기화 명령
     * @return S3 업로드 메타데이터 (Bucket, S3Key, ContentType)
     */
    public S3UploadMetadata toS3Metadata(InitMultipartUploadCommand command) {
        // ThreadLocal에서 UserContext 조회
        UserContext userContext = userContextSupplier.get();

        // S3 경로 생성 (UserContext 기반)
        S3Bucket bucket = userContext.getS3Bucket();
        S3Key s3Key = userContext.generateS3KeyToday(null, command.fileName());

        // Content-Type
        ContentType contentType = ContentType.of(command.contentType());

        return S3UploadMetadata.of(bucket, s3Key, contentType);
    }

    /**
     * InitMultipartUploadCommand를 MultipartUploadSession으로 변환합니다.
     *
     * @param command 초기화 명령
     * @param s3UploadId S3 Multipart Upload ID
     * @return 신규 MultipartUploadSession (status: PREPARING)
     */
    public MultipartUploadSession toDomain(
            InitMultipartUploadCommand command, S3UploadId s3UploadId) {
        Clock clock = clockHolder.getClock();

        // ThreadLocal에서 UserContext 조회
        UserContext userContext = userContextSupplier.get();

        FileName fileName = FileName.of(command.fileName());
        FileSize fileSize = FileSize.of(command.fileSize());
        ContentType contentType = ContentType.of(command.contentType());

        // S3 경로 생성 (UserContext 기반)
        S3Bucket bucket = userContext.getS3Bucket();
        S3Key s3Key =
                userContext.generateS3KeyToday(null, command.fileName()); // Customer는 category null

        // Part 크기 및 개수 계산
        PartSize partSize = PartSize.of(command.partSize());
        TotalParts totalParts = TotalParts.calculate(fileSize.size(), partSize.bytes());

        // 만료 시각 계산 (24시간 후)
        LocalDateTime expiresAt = LocalDateTime.now(clock).plus(MULTIPART_UPLOAD_EXPIRATION);
        ExpirationTime expirationTime = ExpirationTime.of(expiresAt);

        return MultipartUploadSession.forNew(
                userContext,
                fileName,
                fileSize,
                contentType,
                bucket,
                s3Key,
                s3UploadId,
                totalParts,
                partSize,
                expirationTime,
                clock);
    }

    /**
     * Domain → Response DTO 변환 (초기화).
     *
     * <p>activatedSession과 Part 목록을 받아 Response DTO로 변환합니다.
     *
     * @param session 활성화된 세션
     * @param completedParts Part 목록 (초기화된 상태)
     * @return InitMultipartUploadResponse
     */
    public InitMultipartUploadResponse toResponse(
            MultipartUploadSession session, List<CompletedPart> completedParts) {
        // Domain VO → Response DTO 변환
        List<PartInfo> partInfosDto =
                completedParts.stream()
                        .map(
                                part ->
                                        PartInfo.of(
                                                part.getPartNumberValue(),
                                                part.getPresignedUrlValue()))
                        .toList();

        return InitMultipartUploadResponse.of(
                session.getId().value().toString(),
                session.getS3UploadIdValue(),
                session.getTotalPartsValue(),
                session.getPartSizeValue(),
                session.getExpiresAt(),
                session.getBucketValue(),
                session.getS3KeyValue(),
                partInfosDto);
    }

    /**
     * Domain → Response DTO 변환 (Multipart 업로드 완료).
     *
     * @param session 완료된 세션
     * @param completedParts 완료된 Part 목록
     * @return CompleteMultipartUploadResponse
     */
    public CompleteMultipartUploadResponse toCompleteResponse(
            MultipartUploadSession session, List<CompletedPart> completedParts) {
        // CompletedPart 목록을 CompletedPartInfo DTO로 변환
        List<CompletedPartInfo> completedPartInfos =
                completedParts.stream()
                        .map(
                                part ->
                                        CompletedPartInfo.of(
                                                part.getPartNumberValue(),
                                                part.getETagValue(),
                                                part.getSize(),
                                                part.getUploadedAt()))
                        .toList();

        return CompleteMultipartUploadResponse.of(
                session.getId().value().toString(),
                session.getStatus().name(),
                session.getBucketValue(),
                session.getS3KeyValue(),
                session.getS3UploadIdValue(),
                session.getTotalPartsValue(),
                completedPartInfos,
                session.getCompletedAt());
    }

    public MarkPartUploadedResponse toCompleteMarkPartResponse(CompletedPart completedPart) {
        return MarkPartUploadedResponse.of(
                completedPart.getSessionIdValue(),
                completedPart.getPartNumberValue(),
                completedPart.getETagValue(),
                completedPart.getUploadedAt());
    }

    /**
     * Part별 Presigned URL을 생성하고 초기 CompletedPart 목록을 반환합니다.
     *
     * <p>각 Part에 대해 S3 Presigned URL을 발급하고 초기화된 CompletedPart를 생성합니다.
     *
     * @param session Multipart 세션 (S3 Upload ID 포함)
     * @param presignedUrlGenerator Part별 Presigned URL 생성 함수
     * @return 초기화된 CompletedPart 목록 (presignedUrl 포함)
     */
    public List<CompletedPart> toInitialCompletedParts(
            MultipartUploadSession session, PartPresignedUrlGenerator presignedUrlGenerator) {
        int totalParts = session.getTotalPartsValue();
        List<CompletedPart> parts = new ArrayList<>(totalParts);
        Clock clock = clockHolder.getClock();

        for (int partNumber = 1; partNumber <= totalParts; partNumber++) {
            String presignedUrlString =
                    presignedUrlGenerator.generate(
                            session.getBucket(),
                            session.getS3Key(),
                            session.getS3UploadIdValue(),
                            partNumber);

            PartNumber partNumberVo = PartNumber.of(partNumber);
            PresignedUrl presignedUrl = PresignedUrl.of(presignedUrlString);
            parts.add(CompletedPart.forNew(session.getId(), partNumberVo, presignedUrl, clock));
        }

        return parts;
    }

    /** Part Presigned URL 생성 함수형 인터페이스. */
    @FunctionalInterface
    public interface PartPresignedUrlGenerator {
        String generate(S3Bucket bucket, S3Key s3Key, String uploadId, int partNumber);
    }
}
