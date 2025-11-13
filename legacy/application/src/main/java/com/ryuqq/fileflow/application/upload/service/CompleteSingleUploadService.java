package com.ryuqq.fileflow.application.upload.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ryuqq.fileflow.application.file.manager.FileCommandManager;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteSingleUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.upload.dto.response.S3HeadObjectResponse;
import com.ryuqq.fileflow.application.upload.port.in.CompleteSingleUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.out.S3StoragePort;
import com.ryuqq.fileflow.application.upload.port.out.command.SaveUploadSessionPort;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.UploadType;

/**
 * Single Upload 완료 Service
 *
 * <p>Client가 S3 Presigned URL로 직접 업로드를 완료한 후,
 * FileAsset을 생성하는 UseCase 구현체입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>UploadSession 조회 및 검증 (PENDING, SINGLE_UPLOAD 타입)</li>
 *   <li>S3 HeadObject로 파일 존재 및 메타데이터 확인</li>
 *   <li>FileAsset Aggregate 생성</li>
 *   <li>UploadSession 완료 처리</li>
 * </ul>
 *
 * <p><strong>트랜잭션 경계:</strong></p>
 * <ul>
 *   <li>S3 HeadObject는 트랜잭션 밖에서 실행 (외부 API)</li>
 *   <li>Domain 업데이트는 트랜잭션 내에서 실행</li>
 * </ul>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Transaction 경계: 외부 API는 트랜잭션 밖</li>
 *   <li>✅ Manager Pattern: Domain 로직은 Manager 위임</li>
 *   <li>✅ Tell, Don't Ask: session.complete() 메서드 호출</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class CompleteSingleUploadService implements CompleteSingleUploadUseCase {

    private static final Logger log = LoggerFactory.getLogger(CompleteSingleUploadService.class);

    private final LoadUploadSessionPort loadUploadSessionPort;
    private final SaveUploadSessionPort saveUploadSessionPort;
    private final S3StoragePort s3StoragePort;
    private final FileCommandManager fileCommandManager;
    private final String s3Bucket;

    /**
     * 생성자
     *
     * @param loadUploadSessionPort Load UploadSession Port (Query)
     * @param saveUploadSessionPort Save UploadSession Port (Command)
     * @param s3StoragePort S3 Storage Port
     * @param fileCommandManager File Command Manager
     * @param s3Bucket S3 Bucket 이름
     */
    public CompleteSingleUploadService(
        LoadUploadSessionPort loadUploadSessionPort,
        SaveUploadSessionPort saveUploadSessionPort,
        S3StoragePort s3StoragePort,
        FileCommandManager fileCommandManager,
        @Value("${aws.s3.bucket}") String s3Bucket
    ) {
        this.loadUploadSessionPort = loadUploadSessionPort;
        this.saveUploadSessionPort = saveUploadSessionPort;
        this.s3StoragePort = s3StoragePort;
        this.fileCommandManager = fileCommandManager;
        this.s3Bucket = s3Bucket;
    }

    /**
     * Single Upload 완료 처리
     *
     * <p><strong>실행 순서:</strong></p>
     * <ol>
     *   <li>UploadSession 조회 및 검증 (트랜잭션 내)</li>
     *   <li>S3 HeadObject로 파일 존재 확인 (트랜잭션 밖) ⭐</li>
     *   <li>FileAsset 생성 및 UploadSession 완료 (트랜잭션 내)</li>
     * </ol>
     *
     * @param command Complete Command
     * @return 생성된 FileAsset 정보
     * @throws IllegalStateException UploadSession이 완료 가능한 상태가 아닌 경우
     * @throws com.ryuqq.fileflow.adapter.out.aws.s3.exception.S3StorageException S3에 파일이 존재하지 않는 경우
     */
    @Transactional
    @Override
    public CompleteSingleUploadResponse execute(CompleteSingleUploadCommand command) {
        // 1. UploadSession 조회 및 검증 (트랜잭션 내)
        UploadSession session = validateSession(command.sessionKey());

        // 2. S3 HeadObject로 파일 존재 확인 (트랜잭션 밖) ⭐
        S3HeadObjectResponse s3HeadResult = verifyS3Object(session);

        // 3. FileAsset 생성 및 UploadSession 완료 (트랜잭션 내)
        Long fileAssetId = completeUpload(session, s3HeadResult);

        log.info("Single upload completed: sessionKey={}, fileAssetId={}",
            command.sessionKey(), fileAssetId);

        return CompleteSingleUploadResponse.of(
            fileAssetId,
            s3HeadResult.etag(),
            s3HeadResult.contentLength()
        );
    }

    /**
     * UploadSession 조회 및 검증
     *
     * <p><strong>검증 규칙:</strong></p>
     * <ul>
     *   <li>UploadSession이 존재해야 함</li>
     *   <li>타입이 SINGLE_UPLOAD이어야 함</li>
     *   <li>상태가 PENDING이어야 함 (이미 완료된 세션 재처리 방지)</li>
     * </ul>
     *
     * @param sessionKey 세션 키
     * @return 검증된 UploadSession
     * @throws IllegalStateException 검증 실패 시
     */
    public UploadSession validateSession(String sessionKey) {
        SessionKey key = SessionKey.of(sessionKey);
        UploadSession session = loadUploadSessionPort.findBySessionKey(key)
            .orElseThrow(() -> new IllegalStateException(
                "UploadSession not found: " + key
            ));

        // 타입 검증: SINGLE만 허용
        if (session.getUploadType() != UploadType.SINGLE) {
            throw new IllegalStateException(
                "Invalid session type. Expected SINGLE but got: " + session.getUploadType()
            );
        }

        // 상태 검증: PENDING만 허용 (중복 완료 방지)
        if (session.getStatus() != SessionStatus.PENDING) {
            throw new IllegalStateException(
                "Session is not in PENDING status: " + session.getStatus()
            );
        }

        log.debug("UploadSession validated: sessionKey={}, type={}, status={}",
            sessionKey, session.getUploadType(), session.getStatus());

        return session;
    }

    /**
     * S3에 파일이 실제로 존재하는지 확인
     *
     * <p>⭐ <strong>트랜잭션 밖에서 실행</strong> (외부 API 호출)</p>
     *
     * <p>S3 HeadObject API를 호출하여:</p>
     * <ul>
     *   <li>파일 존재 여부 확인</li>
     *   <li>파일 크기 (Content-Length) 확인</li>
     *   <li>ETag 확인 (파일 무결성)</li>
     * </ul>
     *
     * @param session UploadSession
     * @return S3 Object 메타데이터
     * @throws com.ryuqq.fileflow.adapter.out.aws.s3.exception.S3StorageException 파일이 존재하지 않거나 접근 실패 시
     */
    private S3HeadObjectResponse verifyS3Object(UploadSession session) {
        String key = session.getStorageKey().value();

        log.debug("Verifying S3 object: bucket={}, key={}", s3Bucket, key);

        S3HeadObjectResponse response = s3StoragePort.headObject(s3Bucket, key);

        log.info("S3 object verified: bucket={}, key={}, size={} bytes, etag={}",
            s3Bucket, key, response.contentLength(), response.etag());

        return response;
    }

    /**
     * FileAsset 생성 및 UploadSession 완료 처리
     *
     * <p><strong>트랜잭션 내에서 실행</strong></p>
     *
     * <p><strong>실행 순서:</strong></p>
     * <ol>
     *   <li>FileAsset Aggregate 생성 (forNew)</li>
     *   <li>FileAsset 저장 (FileManager)</li>
     *   <li>UploadSession 완료 상태로 업데이트 (Tell, Don't Ask)</li>
     *   <li>UploadSession 저장</li>
     * </ol>
     *
     * @param session UploadSession
     * @param s3HeadResult S3 HeadObject 결과
     * @return 생성된 FileAsset ID
     */
    @Transactional
    public Long completeUpload(UploadSession session, S3HeadObjectResponse s3HeadResult) {
        // 1. UploadSession 시작 (PENDING → IN_PROGRESS)
        session.start();

        // 2. Application DTO → Domain VO 변환
        com.ryuqq.fileflow.domain.file.asset.S3UploadMetadata s3Metadata = s3HeadResult.toDomain();

        // 3. FileAsset Aggregate 생성 (Domain VO 사용)
        FileAsset fileAsset = FileAsset.fromS3Upload(session, s3Metadata);

        // 4. FileAsset 저장
        FileAsset savedFileAsset = fileCommandManager.save(fileAsset);

        log.debug("FileAsset created: fileAssetId={}, sessionId={}",
            savedFileAsset.getIdValue(), session.getIdValue());

        // 5. UploadSession 완료 (Tell, Don't Ask 패턴)
        session.complete(savedFileAsset.getIdValue());

        // 5. UploadSession 저장 (Command Port 사용)
        saveUploadSessionPort.save(session);

        log.info("UploadSession completed: sessionId={}, fileAssetId={}",
            session.getIdValue(), savedFileAsset.getIdValue());

        return savedFileAsset.getIdValue();
    }
}
