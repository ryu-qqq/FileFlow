package com.ryuqq.fileflow.application.session.factory.command;

import com.ryuqq.fileflow.application.session.dto.command.InitMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.common.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
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
import com.ryuqq.fileflow.domain.session.vo.UploadCategory;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * UploadSession Command Factory.
 *
 * <p>UploadSession 관련 Domain 객체 생성 및 상태 변경을 담당합니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Clock 제공 (Domain 상태 변경 시 필요)
 *   <li>SingleUploadSession 생성
 *   <li>MultipartUploadSession 생성
 *   <li>S3UploadMetadata 생성
 *   <li>CompletedPart 목록 초기화
 *   <li>세션 활성화 (activate)
 *   <li>세션 완료 (complete)
 *   <li>세션 실패 (fail)
 *   <li>Part 완료 (complete)
 * </ul>
 *
 * <p><strong>규칙:</strong>
 *
 * <ul>
 *   <li>@Component 어노테이션 (Service 아님)
 *   <li>비즈니스 로직 금지 (순수 변환)
 *   <li>Port 호출 금지 (조회 없음)
 *   <li>@Transactional 금지
 * </ul>
 */
@Component
public class UploadSessionCommandFactory {

    private static final Duration SINGLE_UPLOAD_EXPIRATION = Duration.ofMinutes(15);
    private static final Duration MULTIPART_UPLOAD_EXPIRATION = Duration.ofHours(24);

    private final ClockHolder clockHolder;
    private final Supplier<UserContext> userContextSupplier;

    public UploadSessionCommandFactory(
            ClockHolder clockHolder, Supplier<UserContext> userContextSupplier) {
        this.clockHolder = clockHolder;
        this.userContextSupplier = userContextSupplier;
    }

    /**
     * 현재 Clock을 반환합니다.
     *
     * @return Clock 인스턴스
     */
    public Clock getClock() {
        return clockHolder.getClock();
    }

    /**
     * SingleUploadSession을 활성화합니다.
     *
     * @param session 대상 세션
     * @param presignedUrl Presigned URL
     */
    public void activateSingleUpload(SingleUploadSession session, PresignedUrl presignedUrl) {
        session.activate(presignedUrl, clockHolder.getClock());
    }

    /**
     * SingleUploadSession을 완료 처리합니다.
     *
     * @param session 대상 세션
     * @param clientETag 클라이언트 제공 ETag
     * @param s3ETag S3에서 조회한 ETag
     */
    public void completeSingleUpload(SingleUploadSession session, ETag clientETag, ETag s3ETag) {
        session.complete(clientETag, s3ETag, clockHolder.getClock());
    }

    /**
     * SingleUploadSession을 실패 처리합니다.
     *
     * @param session 대상 세션
     */
    public void failSingleUpload(SingleUploadSession session) {
        session.fail(clockHolder.getClock());
    }

    /**
     * MultipartUploadSession을 완료 처리합니다.
     *
     * @param session 대상 세션
     * @param mergedETag 병합 후 ETag
     * @param completedParts 완료된 Part 목록
     */
    public void completeMultipartUpload(
            MultipartUploadSession session, ETag mergedETag, List<CompletedPart> completedParts) {
        session.complete(mergedETag, completedParts, clockHolder.getClock());
    }

    /**
     * CompletedPart를 완료 처리합니다.
     *
     * @param part 대상 Part
     * @param etag ETag
     * @param size 파일 크기
     */
    public void completePart(CompletedPart part, ETag etag, long size) {
        part.complete(etag, size, clockHolder.getClock());
    }

    // ==================== Domain 객체 생성 ====================

    /**
     * InitSingleUploadCommand를 SingleUploadSession으로 변환합니다.
     *
     * @param command 초기화 명령
     * @return 신규 SingleUploadSession (status: PREPARING)
     * @throws IllegalArgumentException customPath와 uploadCategory를 동시에 사용한 경우
     */
    public SingleUploadSession createSingleUploadSession(InitSingleUploadCommand command) {
        Clock clock = clockHolder.getClock();

        // 멱등성 키 변환
        IdempotencyKey idempotencyKey = IdempotencyKey.fromString(command.idempotencyKey());

        // ThreadLocal에서 UserContext 조회
        UserContext userContext = userContextSupplier.get();

        FileName fileName = FileName.of(command.fileName());
        FileSize fileSize = FileSize.of(command.fileSize());
        ContentType contentType = ContentType.of(command.contentType());

        // customPath와 uploadCategory 동시 사용 검증
        validateCustomPathExclusivity(command.customPath(), command.uploadCategory());

        // S3 경로 생성 (UserContext 기반, customPath 우선)
        S3Bucket bucket = userContext.getS3Bucket();
        S3Key s3Key = resolveS3KeyForSingleUpload(userContext, command);

        // 만료 시각 계산 (15분 후)
        ExpirationTime expirationTime =
                ExpirationTime.fromNow(clock, SINGLE_UPLOAD_EXPIRATION.toMinutes());

        return SingleUploadSession.forNew(
                idempotencyKey,
                userContext,
                fileName,
                fileSize,
                contentType,
                bucket,
                s3Key,
                expirationTime,
                clock);
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
     * @throws IllegalArgumentException customPath와 uploadCategory를 동시에 사용한 경우
     */
    public S3UploadMetadata createS3UploadMetadata(InitMultipartUploadCommand command) {
        // customPath와 uploadCategory 동시 사용 검증
        validateCustomPathExclusivity(command.customPath(), command.uploadCategory());

        // ThreadLocal에서 UserContext 조회
        UserContext userContext = userContextSupplier.get();

        // S3 경로 생성 (UserContext 기반, customPath 우선)
        S3Bucket bucket = userContext.getS3Bucket();
        S3Key s3Key = resolveS3KeyForMultipartUpload(userContext, command);

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
    public MultipartUploadSession createMultipartUploadSession(
            InitMultipartUploadCommand command, S3UploadId s3UploadId) {
        Clock clock = clockHolder.getClock();

        // ThreadLocal에서 UserContext 조회
        UserContext userContext = userContextSupplier.get();

        FileName fileName = FileName.of(command.fileName());
        FileSize fileSize = FileSize.of(command.fileSize());
        ContentType contentType = ContentType.of(command.contentType());

        // S3 경로 생성 (UserContext 기반, customPath 우선)
        S3Bucket bucket = userContext.getS3Bucket();
        S3Key s3Key = resolveS3KeyForMultipartUpload(userContext, command);

        // Part 크기 및 개수 계산
        PartSize partSize = PartSize.of(command.partSize());
        TotalParts totalParts = TotalParts.calculate(fileSize.size(), partSize.bytes());

        // 만료 시각 계산 (24시간 후)
        ExpirationTime expirationTime =
                ExpirationTime.fromNow(clock, MULTIPART_UPLOAD_EXPIRATION.toMinutes());

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
     * Part별 Presigned URL을 생성하고 초기 CompletedPart 목록을 반환합니다.
     *
     * <p>각 Part에 대해 S3 Presigned URL을 발급하고 초기화된 CompletedPart를 생성합니다.
     *
     * @param session Multipart 세션 (S3 Upload ID 포함)
     * @param presignedUrlGenerator Part별 Presigned URL 생성 함수
     * @return 초기화된 CompletedPart 목록 (presignedUrl 포함)
     */
    public List<CompletedPart> createInitialCompletedParts(
            MultipartUploadSession session, PartPresignedUrlGenerator presignedUrlGenerator) {
        int totalParts = session.getTotalPartsValue();
        List<CompletedPart> parts = new ArrayList<>(totalParts);

        for (int partNumber = 1; partNumber <= totalParts; partNumber++) {
            String presignedUrlString =
                    presignedUrlGenerator.generate(
                            session.getBucket(),
                            session.getS3Key(),
                            session.getS3UploadIdValue(),
                            partNumber);

            PartNumber partNumberVo = PartNumber.of(partNumber);
            PresignedUrl presignedUrl = PresignedUrl.of(presignedUrlString);
            parts.add(CompletedPart.forNew(session.getId(), partNumberVo, presignedUrl));
        }

        return parts;
    }

    /** Part Presigned URL 생성 함수형 인터페이스. */
    @FunctionalInterface
    public interface PartPresignedUrlGenerator {
        String generate(S3Bucket bucket, S3Key s3Key, String uploadId, int partNumber);
    }

    // ==================== Private Helper Methods ====================

    /**
     * customPath와 uploadCategory 동시 사용 여부를 검증합니다.
     *
     * @param customPath 커스텀 경로
     * @param uploadCategory 업로드 카테고리
     * @throws IllegalArgumentException 둘 다 값이 있는 경우
     */
    private void validateCustomPathExclusivity(String customPath, String uploadCategory) {
        if (hasValue(customPath) && hasValue(uploadCategory)) {
            throw new IllegalArgumentException("customPath와 uploadCategory는 동시에 사용할 수 없습니다.");
        }
    }

    /**
     * SingleUploadCommand에서 S3Key를 결정합니다.
     *
     * <p>customPath가 있으면 SYSTEM 전용 경로, 없으면 uploadCategory 기반 경로를 사용합니다.
     */
    private S3Key resolveS3KeyForSingleUpload(
            UserContext userContext, InitSingleUploadCommand command) {
        if (hasValue(command.customPath())) {
            return userContext.generateS3KeyWithCustomPath(
                    command.customPath(), command.fileName());
        }

        UploadCategory uploadCategory = null;
        if (hasValue(command.uploadCategory())) {
            uploadCategory = UploadCategory.fromPath(command.uploadCategory());
        }
        return userContext.generateS3KeyToday(uploadCategory, command.fileName());
    }

    /**
     * MultipartUploadCommand에서 S3Key를 결정합니다.
     *
     * <p>customPath가 있으면 SYSTEM 전용 경로, 없으면 uploadCategory 기반 경로를 사용합니다.
     */
    private S3Key resolveS3KeyForMultipartUpload(
            UserContext userContext, InitMultipartUploadCommand command) {
        if (hasValue(command.customPath())) {
            return userContext.generateS3KeyWithCustomPath(
                    command.customPath(), command.fileName());
        }

        UploadCategory uploadCategory = null;
        if (hasValue(command.uploadCategory())) {
            uploadCategory = UploadCategory.fromPath(command.uploadCategory());
        }
        return userContext.generateS3KeyToday(uploadCategory, command.fileName());
    }

    /**
     * 문자열이 유효한 값인지 확인합니다.
     *
     * @param value 확인할 문자열
     * @return null이 아니고 비어있지 않으면 true
     */
    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }
}
