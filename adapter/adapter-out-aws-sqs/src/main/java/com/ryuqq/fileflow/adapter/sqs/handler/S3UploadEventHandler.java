package com.ryuqq.fileflow.adapter.sqs.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.sqs.dto.S3EventNotification;
import com.ryuqq.fileflow.adapter.sqs.exception.S3EventParsingException;
import com.ryuqq.fileflow.adapter.sqs.exception.SessionMatchingException;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.application.upload.service.ChecksumVerificationService;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.exception.ChecksumMismatchException;
import com.ryuqq.fileflow.domain.upload.vo.S3Location;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * S3 업로드 이벤트 핸들러
 *
 * S3 이벤트를 파싱하고 업로드 세션을 업데이트합니다.
 * S3 key에서 세션 ID를 추출하여 매칭하고, 체크섬 검증을 수행합니다.
 *
 * @author sangwon-ryu
 */
@Component
public class S3UploadEventHandler {

    private static final Logger log = LoggerFactory.getLogger(S3UploadEventHandler.class);

    /**
     * S3 key 패턴: uploads/{sessionId}/{filename}
     */
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("uploads/([^/]+)/.*");

    private final ObjectMapper objectMapper;
    private final UploadSessionPort uploadSessionPort;
    private final ChecksumVerificationService checksumVerificationService;
    private final RetryTemplate retryTemplate;
    private final CircuitBreaker circuitBreaker;

    public S3UploadEventHandler(
            ObjectMapper objectMapper,
            UploadSessionPort uploadSessionPort,
            ChecksumVerificationService checksumVerificationService,
            RetryTemplate retryTemplate,
            CircuitBreaker circuitBreaker
    ) {
        this.objectMapper = objectMapper;
        this.uploadSessionPort = uploadSessionPort;
        this.checksumVerificationService = checksumVerificationService;
        this.retryTemplate = retryTemplate;
        this.circuitBreaker = circuitBreaker;
    }

    /**
     * S3 이벤트를 처리합니다.
     *
     * @param messageBody SQS 메시지 본문
     * @throws S3EventParsingException 이벤트 파싱 실패 시
     * @throws SessionMatchingException 세션 매칭 실패 시
     */
    public void handleS3Event(String messageBody) {
        // 1. 이벤트 파싱
        S3EventNotification event = parseS3Event(messageBody);

        // 2. 레코드별 처리
        event.getRecords().forEach(this::processS3EventRecord);
    }

    /**
     * S3 이벤트 메시지를 파싱합니다.
     *
     * @param messageBody 메시지 본문
     * @return 파싱된 S3 이벤트
     * @throws S3EventParsingException 파싱 실패 시
     */
    private S3EventNotification parseS3Event(String messageBody) {
        try {
            return objectMapper.readValue(messageBody, S3EventNotification.class);
        } catch (Exception e) {
            log.error("Failed to parse S3 event message: {}", messageBody, e);
            throw new S3EventParsingException("Failed to parse S3 event message", e);
        }
    }

    /**
     * 개별 S3 이벤트 레코드를 처리합니다.
     *
     * @param record S3 이벤트 레코드
     */
    private void processS3EventRecord(S3EventNotification.S3EventRecord record) {
        try {
            // 1. S3 위치 정보 추출
            S3Location s3Location = extractS3Location(record);
            log.info("Processing S3 event for object: {}", s3Location.toUri());

            // 2. 세션 ID 추출
            String sessionId = extractSessionId(s3Location.key());
            log.info("Extracted session ID: {} from key: {}", sessionId, s3Location.key());

            // 3. 업로드 세션 조회 및 업데이트
            updateUploadSession(sessionId, s3Location, record);

        } catch (Exception e) {
            log.error("Failed to process S3 event record: {}", record.getEventName(), e);
            throw e;
        }
    }

    /**
     * S3 이벤트 레코드에서 S3 위치 정보를 추출합니다.
     *
     * @param record S3 이벤트 레코드
     * @return S3 위치 정보
     */
    private S3Location extractS3Location(S3EventNotification.S3EventRecord record) {
        String bucket = record.getS3().getBucket().getName();
        String key = record.getS3().getObject().getKey();

        return S3Location.of(bucket, key);
    }

    /**
     * S3 key에서 세션 ID를 추출합니다.
     *
     * S3 key 형식: uploads/{sessionId}/{filename}
     *
     * @param key S3 객체 키
     * @return 세션 ID
     * @throws SessionMatchingException 세션 ID 추출 실패 시
     */
    private String extractSessionId(String key) {
        Matcher matcher = SESSION_ID_PATTERN.matcher(key);

        if (!matcher.matches()) {
            String errorMsg = String.format(
                    "Failed to extract session ID from S3 key: %s. " +
                    "Expected format: uploads/{sessionId}/{filename}",
                    key
            );
            log.error(errorMsg);
            throw new SessionMatchingException(errorMsg);
        }

        return matcher.group(1);
    }

    /**
     * 업로드 세션을 조회하고 완료 상태로 업데이트합니다.
     *
     * @param sessionId 세션 ID
     * @param s3Location S3 위치 정보
     * @param record S3 이벤트 레코드
     * @throws SessionMatchingException 세션 조회 실패 시
     */
    private void updateUploadSession(
            String sessionId,
            S3Location s3Location,
            S3EventNotification.S3EventRecord record
    ) {
        // 1. 세션 조회
        UploadSession session = uploadSessionPort.findById(sessionId)
                .orElseThrow(() -> {
                    String errorMsg = String.format(
                            "Upload session not found for ID: %s. S3 object: %s",
                            sessionId, s3Location.toUri()
                    );
                    log.error(errorMsg);
                    return new SessionMatchingException(errorMsg);
                });

        // 2. 세션 상태 검증
        if (!session.isActive()) {
            log.warn("Session {} is not active. Current status: {}. Skipping update.",
                    sessionId, session.getStatus());
            return;
        }

        // 3. 체크섬 검증 (SHA-256)
        try {
            checksumVerificationService.verifyChecksum(session, s3Location.key());
            log.info("Checksum verification passed for session: {}", sessionId);
        } catch (ChecksumMismatchException e) {
            log.error("Checksum verification failed for session: {}. " +
                            "Expected: {}, Actual: {}. S3 object: {}",
                    sessionId, e.getExpectedEtag(), e.getActualEtag(), s3Location.toUri());
            // 체크섬 불일치 시 처리 중단 (DLQ로 전송됨)
            throw e;
        }

        // 4. 세션 완료 처리
        try {
            UploadSession completedSession = session.complete();

            // Circuit Breaker와 Retry를 적용하여 세션 저장
            circuitBreaker.executeSupplier(() ->
                    retryTemplate.execute(context -> {
                        uploadSessionPort.save(completedSession);
                        return completedSession;
                    })
            );

            log.info("Successfully updated upload session: {} to COMPLETED. " +
                    "S3 object: {}, Size: {} bytes, ETag: {}",
                    sessionId,
                    s3Location.toUri(),
                    record.getS3().getObject().getSize(),
                    record.getS3().getObject().geteTag()
            );

        } catch (IllegalStateException e) {
            log.error("Failed to complete upload session: {}. Error: {}",
                    sessionId, e.getMessage(), e);
            throw new SessionMatchingException(
                    "Failed to complete upload session: " + sessionId, e
            );
        }
    }
}
