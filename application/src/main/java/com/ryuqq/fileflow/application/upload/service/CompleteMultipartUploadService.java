package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.file.manager.FileCommandManager;
import com.ryuqq.fileflow.application.iam.context.IamContext;
import com.ryuqq.fileflow.application.iam.context.IamContextFacade;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteMultipartCommand;
import com.ryuqq.fileflow.application.upload.dto.command.CompletedPartCommand;
import com.ryuqq.fileflow.application.upload.dto.response.CompleteMultipartResponse;
import com.ryuqq.fileflow.application.upload.dto.response.S3CompleteResultResponse;
import com.ryuqq.fileflow.application.upload.dto.response.S3HeadObjectResponse;
import com.ryuqq.fileflow.application.upload.dto.response.ValidationResultResponse;
import com.ryuqq.fileflow.application.upload.facade.S3MultipartFacade;
import com.ryuqq.fileflow.application.upload.manager.MultipartUploadManager;
import com.ryuqq.fileflow.application.upload.port.in.CompleteMultipartUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.out.S3StoragePort;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Multipart 업로드 완료 Service
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class CompleteMultipartUploadService implements CompleteMultipartUploadUseCase {

    private static final Logger log = LoggerFactory.getLogger(CompleteMultipartUploadService.class);

    private final UploadSessionPort uploadSessionPort;
    private final MultipartUploadManager multipartUploadManager;
    private final IamContextFacade iamContextFacade;
    private final S3MultipartFacade s3MultipartFacade;
    private final S3StoragePort s3StoragePort;
    private final FileCommandManager fileCommandManager;
    private final String s3Bucket;

    public CompleteMultipartUploadService(
        UploadSessionPort uploadSessionPort,
        MultipartUploadManager multipartUploadManager,
        IamContextFacade iamContextFacade,
        S3MultipartFacade s3MultipartFacade,
        S3StoragePort s3StoragePort,
        FileCommandManager fileCommandManager,
        @Value("${aws.s3.bucket}") String s3Bucket
    ) {
        this.uploadSessionPort = uploadSessionPort;
        this.multipartUploadManager = multipartUploadManager;
        this.iamContextFacade = iamContextFacade;
        this.s3MultipartFacade = s3MultipartFacade;
        this.s3StoragePort = s3StoragePort;
        this.fileCommandManager = fileCommandManager;
        this.s3Bucket = s3Bucket;
    }

    @Transactional(readOnly = true)
    @Override
    public CompleteMultipartResponse execute(CompleteMultipartCommand command) {
        // 1. 완료 가능 검증 (트랜잭션 내)
        ValidationResultResponse validationResultResponse = validateCanComplete(command.sessionKey());
        UploadSession session = validationResultResponse.session();
        MultipartUpload multipart = validationResultResponse.multipart();

        // 2. S3 Complete (트랜잭션 밖) ⭐
        S3CompleteResultResponse s3Result = completeS3Multipart(session, multipart);

        // 3. S3 파일 존재 및 메타데이터 검증 (트랜잭션 밖) ⭐ NEW
        S3HeadObjectResponse s3HeadResult = verifyS3Object(session);

        // 4. Domain 업데이트 (트랜잭션 내)
        completeUpload(session, multipart, s3Result, s3HeadResult);

        return buildResponse(session, s3Result);
    }

    /**
     * 완료 가능 검증
     *
     * <p>⭐ Read-only 트랜잭션</p>
     *
     * @param sessionKey 세션 키
     * @return 검증 결과
     */
    public ValidationResultResponse validateCanComplete(String sessionKey) {
        UploadSession session = uploadSessionPort
            .findBySessionKey(SessionKey.of(sessionKey))
            .orElseThrow(() ->
                new IllegalArgumentException("Upload session not found: " + sessionKey)
            );

        MultipartUpload multipart = multipartUploadManager
            .findByUploadSessionId(session.getId())
            .orElseThrow(() ->
                new IllegalStateException("Not a multipart upload")
            );

        if (!multipart.canComplete()) {
            throw new IllegalStateException(
                "Cannot complete multipart upload. " +
                "Uploaded parts: " + multipart.getUploadedParts().size() +
                ", Total parts: " + multipart.getTotalParts().value()
            );
        }

        return new ValidationResultResponse(session, multipart);
    }

    /**
     * S3 파일 존재 및 메타데이터 검증
     *
     * <p>⭐ 트랜잭션 밖에서 실행 (외부 API 호출)</p>
     *
     * <p>S3 Complete API 호출 후 파일이 실제로 존재하는지 HeadObject로 재확인합니다.</p>
     *
     * @param session UploadSession
     * @return S3 Object 메타데이터
     * @throws com.ryuqq.fileflow.adapter.out.aws.s3.exception.S3StorageException 파일이 존재하지 않거나 접근 실패 시
     */
    private S3HeadObjectResponse verifyS3Object(UploadSession session) {
        String key = session.getStorageKey().value();

        log.debug("Verifying S3 object after multipart complete: bucket={}, key={}", s3Bucket, key);

        S3HeadObjectResponse response = s3StoragePort.headObject(s3Bucket, key);

        log.info("S3 object verified after multipart complete: bucket={}, key={}, size={} bytes, etag={}",
            s3Bucket, key, response.contentLength(), response.etag());

        return response;
    }

    /**
     * S3 Multipart Complete API 호출
     *
     * <p>⭐ 트랜잭션 밖에서 실행 (외부 API 호출)</p>
     *
     * @param session UploadSession
     * @param multipart MultipartUpload
     * @return S3 완료 결과
     */
    private S3CompleteResultResponse completeS3Multipart(
        UploadSession session,
        MultipartUpload multipart
    ) {
        // 1. IAM Context 조회
        // UploadSession은 organizationId, userContextId를 저장하지 않음
        // IamContextFacade.loadContext는 이들을 Optional로 받음 (null 허용)
        IamContext iamContext = iamContextFacade.loadContext(
            session.getTenantId(),
            null, // organizationId는 UploadSession에 저장되지 않음
            null  // userContextId는 UploadSession에 저장되지 않음
        );

        // 2. CompletedPart 리스트 생성
        List<CompletedPartCommand> completedParts = multipart.getUploadedParts()
            .stream()
            .map(part -> CompletedPartCommand.of(
                part.getPartNumber().value(),
                part.getEtag().value()
            ))
            .collect(Collectors.toList());

        // 3. S3MultipartFacade 호출 (Facade가 Bucket 이름 결정 담당)
        return s3MultipartFacade.completeMultipart(
            iamContext,
            session.getStorageKey(),
            multipart.getProviderUploadIdValue(),
            completedParts,
            session.getFileSize().bytes()
        );
    }

    /**
     * Domain 상태 업데이트
     *
     * <p>⭐ 트랜잭션 내에서 실행</p>
     *
     * @param session UploadSession
     * @param multipart MultipartUpload
     * @param s3Result S3 완료 결과
     * @param s3HeadResult S3 HeadObject 검증 결과
     */
    public void completeUpload(
        UploadSession session,
        MultipartUpload multipart,
        S3CompleteResultResponse s3Result,
        S3HeadObjectResponse s3HeadResult
    ) {
        // 1. MultipartUpload 완료 (Manager 사용)
        multipartUploadManager.complete(multipart);

        // 2. FileAsset Aggregate 생성 (S3 검증 결과 활용)
        FileAsset fileAsset = FileAsset.forNew(
            session.getTenantId(),
            null, // organizationId는 나중에 추가 가능
            null, // ownerUserId는 나중에 추가 가능
            session.getFileName(),
            session.getFileSize(),
            null, // mimeType은 나중에 추가 가능 (또는 s3HeadResult.contentType() 사용)
            session.getStorageKey(),
            null, // checksum은 나중에 추가 가능 (또는 s3HeadResult.etag() 사용)
            session.getId()
        );
        FileAsset savedFileAsset = fileCommandManager.save(fileAsset);

        log.debug("FileAsset created from multipart upload: fileAssetId={}, sessionId={}, verifiedSize={} bytes",
            savedFileAsset.getIdValue(), session.getIdValue(), s3HeadResult.contentLength());

        // 3. UploadSession 완료 (FileAsset ID 전달)
        session.complete(savedFileAsset.getIdValue());

        // 4. 저장
        uploadSessionPort.save(session);

        log.info("Multipart upload completed: sessionId={}, fileAssetId={}, etag={}",
            session.getIdValue(), savedFileAsset.getIdValue(), s3Result.etag());
    }

    /**
     * Response 생성
     *
     * @param session UploadSession
     * @param s3Result S3 완료 결과
     * @return CompleteMultipartResponse
     */
    private CompleteMultipartResponse buildResponse(
        UploadSession session,
        S3CompleteResultResponse s3Result
    ) {
        return CompleteMultipartResponse.of(
            session.getIdValue(),
            s3Result.etag(),
            s3Result.location()
        );
    }
}
