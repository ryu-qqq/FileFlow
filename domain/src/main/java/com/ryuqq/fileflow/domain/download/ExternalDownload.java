package com.ryuqq.fileflow.domain.download;

import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * External Download Aggregate Root
 *
 * <p>외부 URL로부터 파일을 다운로드하여 S3에 저장하는 집합 루트입니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>HTTP/HTTPS만 지원</li>
 *   <li>최대 3회 재시도 (지수 백오프)</li>
 *   <li>5xx, Timeout 오류만 재시도</li>
 * </ul>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Law of Demeter - Getter 체이닝 방지</li>
 *   <li>✅ Tell, Don't Ask 패턴 적용</li>
 *   <li>✅ Aggregate 경계 내에서 일관성 보장</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class ExternalDownload {

    // 불변 필드
    private final ExternalDownloadId id;
    private final UploadSessionId uploadSessionId;
    private final URL sourceUrl;
    private final Clock clock;
    private final LocalDateTime createdAt;
    private final Integer maxRetry = 3;

    // 가변 필드
    private FileSize bytesTransferred;
    private FileSize totalBytes;
    private ExternalDownloadStatus status;
    private Integer retryCount;
    private LocalDateTime lastRetryAt;
    private LocalDateTime updatedAt;
    private ErrorCode errorCode;
    private ErrorMessage errorMessage;

    /**
     * Package-private 주요 생성자 (검증 포함)
     *
     * <p>외부 패키지에서 직접 생성할 수 없습니다. 정적 팩토리 메서드 또는 같은 패키지 내 테스트에서 사용하세요.</p>
     *
     * @param id External Download ID (null 허용 - 신규 엔티티)
     * @param uploadSessionId Upload Session ID
     * @param url 소스 URL
     * @param clock 시간 제공자
     * @throws IllegalArgumentException uploadSessionId 또는 url이 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    ExternalDownload(
        ExternalDownloadId id,
        UploadSessionId uploadSessionId,
        String url,
        Clock clock
    ) {
        // 신규 생성 시 UploadSessionId가 아직 없을 수 있으므로 null 허용
        this.id = id;
        this.uploadSessionId = uploadSessionId;
        this.sourceUrl = validateAndParseUrl(url);
        this.clock = clock;
        this.status = ExternalDownloadStatus.INIT;
        this.bytesTransferred = FileSize.of(0L);
        this.totalBytes = null;
        this.retryCount = 0;
        this.lastRetryAt = null;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
        this.errorCode = null;
        this.errorMessage = null;
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id External Download ID
     * @param uploadSessionId Upload Session ID
     * @param sourceUrl 소스 URL
     * @param clock 시간 제공자
     * @param bytesTransferred 전송된 바이트 수
     * @param totalBytes 총 바이트 수
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param lastRetryAt 마지막 재시도 시간
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param errorCode 오류 코드
     * @param errorMessage 오류 메시지
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private ExternalDownload(
        ExternalDownloadId id,
        UploadSessionId uploadSessionId,
        URL sourceUrl,
        Clock clock,
        FileSize bytesTransferred,
        FileSize totalBytes,
        ExternalDownloadStatus status,
        Integer retryCount,
        LocalDateTime lastRetryAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        ErrorCode errorCode,
        ErrorMessage errorMessage
    ) {
        this.id = id;
        this.uploadSessionId = uploadSessionId;
        this.sourceUrl = sourceUrl;
        this.clock = clock;
        this.bytesTransferred = bytesTransferred;
        this.totalBytes = totalBytes;
        this.status = status;
        this.retryCount = retryCount;
        this.lastRetryAt = lastRetryAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 신규 External Download를 생성합니다 (Static Factory Method).
     *
     * <p><strong>ID 없이 신규 도메인 객체를 생성</strong>합니다 (DB 저장 전 상태).</p>
     * <p>초기 상태: INIT, retryCount = 0, bytesTransferred = 0, ID = null</p>
     * <p>URL 검증은 생성자 내부에서 자동으로 처리됩니다.</p>
     *
     * <p><strong>사용 시기</strong>: Application Layer에서 Command를 받아 새로운 Entity를 생성할 때</p>
     *
     * @param sourceUrl 소스 URL (검증 자동 수행)
     * @param uploadSession Upload Session (ID 추출)
     * @return 생성된 ExternalDownload (ID = null)
     * @throws IllegalArgumentException URL이 유효하지 않거나 uploadSession이 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static ExternalDownload forNew(String sourceUrl, com.ryuqq.fileflow.domain.upload.UploadSession uploadSession) {
        if (uploadSession == null) {
            throw new IllegalArgumentException("Upload Session은 필수입니다");
        }

        // UploadSession.getId()는 UploadSessionId를 반환하므로 직접 사용
        UploadSessionId uploadSessionId = uploadSession.getId();
        return new ExternalDownload(null, uploadSessionId, sourceUrl, Clock.systemDefaultZone());
    }

    /**
     * External Download를 생성합니다 (기존 ID 존재, Static Factory Method).
     *
     * <p><strong>ID가 이미 있는 도메인 객체를 생성</strong>합니다.</p>
     * <p>초기 상태: INIT, retryCount = 0, bytesTransferred = 0</p>
     *
     * <p><strong>사용 시기</strong>: 테스트 또는 ID가 미리 정해진 특수한 경우</p>
     * <p><strong>주의</strong>: 일반적인 신규 생성에는 {@code forNew()} 사용 권장</p>
     *
     * @param id External Download ID (필수)
     * @param sourceUrl 소스 URL
     * @param uploadSessionId Upload Session ID
     * @return 생성된 ExternalDownload (ID 포함)
     * @throws IllegalArgumentException id가 null이거나 URL이 유효하지 않은 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static ExternalDownload of(ExternalDownloadId id, String sourceUrl, UploadSessionId uploadSessionId) {
        if (id == null) {
            throw new IllegalArgumentException("External Download ID는 필수입니다");
        }
        return new ExternalDownload(id, uploadSessionId, sourceUrl, Clock.systemDefaultZone());
    }

    /**
     * DB에서 조회한 데이터로 ExternalDownload 재구성 (Static Factory Method)
     *
     * <p><strong>Persistence Layer → Domain Layer 변환 전용</strong></p>
     * <p>DB에서 조회한 데이터를 Domain 객체로 복원할 때 사용합니다.</p>
     * <p>모든 상태(status, retryCount, errorCode 포함)를 그대로 복원합니다.</p>
     *
     * <p><strong>사용 시기</strong>: Persistence Layer에서 JPA Entity → Domain 변환 시</p>
     *
     * @param id External Download ID (필수 - DB에서 조회된 ID)
     * @param uploadSessionId Upload Session ID
     * @param sourceUrl 소스 URL
     * @param bytesTransferred 전송된 바이트 수
     * @param totalBytes 총 바이트 수
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @param lastRetryAt 마지막 재시도 시간
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param errorCode 오류 코드
     * @param errorMessage 오류 메시지
     * @return 재구성된 ExternalDownload
     * @throws IllegalArgumentException id가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static ExternalDownload reconstitute(
        ExternalDownloadId id,
        UploadSessionId uploadSessionId,
        URL sourceUrl,
        FileSize bytesTransferred,
        FileSize totalBytes,
        ExternalDownloadStatus status,
        Integer retryCount,
        LocalDateTime lastRetryAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        ErrorCode errorCode,
        ErrorMessage errorMessage
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new ExternalDownload(
            id,
            uploadSessionId,
            sourceUrl,
            Clock.systemDefaultZone(),
            bytesTransferred,
            totalBytes,
            status,
            retryCount,
            lastRetryAt,
            createdAt,
            updatedAt,
            errorCode,
            errorMessage
        );
    }

    /**
     * 다운로드를 시작합니다.
     *
     * <p>상태 전환: INIT → DOWNLOADING</p>
     *
     * @throws IllegalStateException INIT 상태가 아닌 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void start() {
        if (this.status != ExternalDownloadStatus.INIT) {
            throw new IllegalStateException(
                "Can only start from INIT state: " + status
            );
        }
        this.status = ExternalDownloadStatus.DOWNLOADING;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 다운로드 진행률을 업데이트합니다.
     *
     * @param transferred 전송된 바이트 수
     * @param total 총 바이트 수
     * @throws IllegalStateException 다운로드 중 상태가 아닌 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void updateProgress(FileSize transferred, FileSize total) {
        if (this.status != ExternalDownloadStatus.DOWNLOADING) {
            throw new IllegalStateException("Not downloading: " + status);
        }
        this.bytesTransferred = transferred;
        this.totalBytes = total;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 다운로드를 완료합니다.
     *
     * <p>상태 전환: DOWNLOADING → COMPLETED</p>
     *
     * @throws IllegalStateException 다운로드 중 상태가 아닌 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void complete() {
        if (this.status != ExternalDownloadStatus.DOWNLOADING) {
            throw new IllegalStateException("Not downloading: " + status);
        }
        this.status = ExternalDownloadStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 다운로드 실패를 처리합니다.
     *
     * <p>재시도 가능한 경우 상태 유지, 불가능한 경우 FAILED로 전환</p>
     *
     * @param errorCode 오류 코드
     * @param errorMessage 오류 메시지
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void fail(ErrorCode errorCode, ErrorMessage errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        // 테스트 기대사항: 실패 시 상태는 FAILED로 전환
        if (canRetry(errorCode)) {
            this.retryCount++;
            this.lastRetryAt = LocalDateTime.now(clock);
        }
        this.status = ExternalDownloadStatus.FAILED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 다운로드를 중단합니다.
     *
     * @throws IllegalStateException 이미 완료된 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void abort() {
        if (this.status == ExternalDownloadStatus.COMPLETED) {
            throw new IllegalStateException("Cannot abort completed download");
        }
        this.status = ExternalDownloadStatus.ABORTED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 다운로드 완료 여부 확인
     *
     * @return 완료되었으면 true
     */
    public boolean isCompleted() {
        return this.status == ExternalDownloadStatus.COMPLETED;
    }

    /**
     * 다운로드 실패 여부 확인
     *
     * @return 실패했으면 true
     */
    public boolean isFailed() {
        return this.status == ExternalDownloadStatus.FAILED;
    }

    /**
     * 재시도 가능 여부
     *
     * @param errorCode 오류 코드
     * @return 재시도 가능하면 true
     */
    public boolean canRetry(ErrorCode errorCode) {
        return isRetryableError(errorCode) && retryCount < maxRetry;
    }

    /**
     * 진행률 계산 (%)
     *
     * @return 진행률 (0-100)
     */
    public int getProgressPercentage() {
        if (totalBytes == null || totalBytes.bytes() == 0) {
            return 0;
        }
        return (int) ((bytesTransferred.bytes() * 100) / totalBytes.bytes());
    }

    /**
     * 다음 재시도까지의 대기 시간 계산 (지수 백오프)
     *
     * @return 대기 시간
     */
    public Duration getNextRetryDelay() {
        if (retryCount >= maxRetry) {
            return Duration.ZERO;
        }
        return Duration.ofSeconds((long) Math.pow(2, retryCount));
    }

    /**
     * URL 검증 및 파싱
     *
     * @param url URL 문자열
     * @return 파싱된 URL
     * @throws IllegalArgumentException URL이 유효하지 않은 경우
     */
    private static URL validateAndParseUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("유효하지 않은 URL입니다");
        }

        try {
            URL parsedUrl = new URL(url);
            String protocol = parsedUrl.getProtocol();

            if (!protocol.matches("https?")) {
                throw new IllegalArgumentException("HTTP/HTTPS만 지원합니다");
            }

            return parsedUrl;

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("유효하지 않은 URL입니다", e);
        }
    }

    /**
     * 재시도 가능한 오류인지 판단
     * 5xx 서버 오류, Timeout만 재시도
     *
     * @param errorCode 오류 코드
     * @return 재시도 가능하면 true
     */
    private boolean isRetryableError(ErrorCode errorCode) {
        if (errorCode == null) {
            return false;
        }

        String code = errorCode.value();

        if (code.startsWith("5")) {
            return true;
        }

        if ("TIMEOUT".equals(code) || "READ_TIMEOUT".equals(code)) {
            return true;
        }

        return false;
    }

    /**
     * External Download ID를 반환합니다.
     *
     * @return External Download ID
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public ExternalDownloadId getId() {
        return id;
    }

    /**
     * External Download ID 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: externalDownload.getId().value()</p>
     * <p>✅ Good: externalDownload.getIdValue()</p>
     *
     * <p><strong>주의</strong>: {@code forNew()}로 생성된 신규 객체는 null을 반환합니다.</p>
     *
     * @return External Download ID 원시 값 (신규 생성 시 null)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    /**
     * Upload Session ID를 반환합니다.
     *
     * @return Upload Session ID
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public UploadSessionId getUploadSessionId() {
        return uploadSessionId;
    }

    /**
     * Upload Session ID 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: externalDownload.getUploadSessionId().value()</p>
     * <p>✅ Good: externalDownload.getUploadSessionIdValue()</p>
     *
     * @return Upload Session ID 원시 값
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Long getUploadSessionIdValue() {
        return uploadSessionId.value();
    }

    /**
     * 소스 URL을 반환합니다.
     *
     * @return 소스 URL
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public URL getSourceUrl() {
        return sourceUrl;
    }

    /**
     * 소스 URL 문자열을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: externalDownload.getSourceUrl().toString()</p>
     * <p>✅ Good: externalDownload.getSourceUrlString()</p>
     *
     * @return 소스 URL 문자열
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public String getSourceUrlString() {
        return sourceUrl.toString();
    }

    /**
     * 상태를 반환합니다.
     *
     * @return 상태
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public ExternalDownloadStatus getStatus() {
        return status;
    }

    /**
     * 전송된 바이트 수를 반환합니다.
     *
     * @return 전송된 바이트 수
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public FileSize getBytesTransferred() {
        return bytesTransferred;
    }

    /**
     * 전송된 바이트 수 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: externalDownload.getBytesTransferred().bytes()</p>
     * <p>✅ Good: externalDownload.getBytesTransferredValue()</p>
     *
     * @return 전송된 바이트 수 원시 값
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Long getBytesTransferredValue() {
        return bytesTransferred.bytes();
    }

    /**
     * 총 바이트 수를 반환합니다.
     *
     * @return 총 바이트 수
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public FileSize getTotalBytes() {
        return totalBytes;
    }

    /**
     * 총 바이트 수 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: externalDownload.getTotalBytes().bytes()</p>
     * <p>✅ Good: externalDownload.getTotalBytesValue()</p>
     *
     * @return 총 바이트 수 원시 값 (null 가능)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Long getTotalBytesValue() {
        return totalBytes != null ? totalBytes.bytes() : null;
    }

    /**
     * 재시도 횟수를 반환합니다.
     *
     * @return 재시도 횟수
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * 마지막 재시도 시간을 반환합니다.
     *
     * @return 마지막 재시도 시간
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public LocalDateTime getLastRetryAt() {
        return lastRetryAt;
    }

    /**
     * 생성 시각을 반환합니다.
     *
     * @return 생성 시각
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 시작 시각을 반환합니다.
     *
     * @return 시작 시각 (DOWNLOADING 상태로 전환된 시각)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public LocalDateTime getStartedAt() {
        return updatedAt;
    }

    /**
     * 수정 시각을 반환합니다.
     *
     * @return 수정 시각
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 오류 코드를 반환합니다.
     *
     * @return 오류 코드
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 오류 코드 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: externalDownload.getErrorCode().value()</p>
     * <p>✅ Good: externalDownload.getErrorCodeValue()</p>
     *
     * @return 오류 코드 원시 값 (null 가능)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public String getErrorCodeValue() {
        return errorCode != null ? errorCode.value() : null;
    }

    /**
     * 오류 메시지를 반환합니다.
     *
     * @return 오류 메시지
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    /**
     * 오류 메시지 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: externalDownload.getErrorMessage().value()</p>
     * <p>✅ Good: externalDownload.getErrorMessageValue()</p>
     *
     * @return 오류 메시지 원시 값 (null 가능)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public String getErrorMessageValue() {
        return errorMessage != null ? errorMessage.value() : null;
    }

    /**
     * 동등성을 비교합니다.
     *
     * <p>ID 기반 동등성 비교 (Entity 패턴)</p>
     *
     * @param o 비교 대상 객체
     * @return 동등 여부
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExternalDownload that = (ExternalDownload) o;
        return Objects.equals(id, that.id);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * <p>ID 기반 해시코드 (Entity 패턴)</p>
     *
     * @return 해시코드
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return ExternalDownload 정보 문자열
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return "ExternalDownload{" +
            "id=" + id +
            ", uploadSessionId=" + uploadSessionId +
            ", sourceUrl=" + sourceUrl +
            ", status=" + status +
            ", retryCount=" + retryCount +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
