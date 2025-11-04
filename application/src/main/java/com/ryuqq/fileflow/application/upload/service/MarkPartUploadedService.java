package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.upload.manager.MultipartUploadManager;
import com.ryuqq.fileflow.application.upload.port.in.MarkPartUploadedUseCase;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.upload.ETag;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.PartNumber;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.UploadPart;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 파트 업로드 완료 처리 Service
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class MarkPartUploadedService implements MarkPartUploadedUseCase {

    private final UploadSessionPort uploadSessionPort;
    private final MultipartUploadManager multipartUploadManager;

    public MarkPartUploadedService(
        UploadSessionPort uploadSessionPort,
        MultipartUploadManager multipartUploadManager
    ) {
        this.uploadSessionPort = uploadSessionPort;
        this.multipartUploadManager = multipartUploadManager;
    }

    @Override
    @Transactional
    public void execute(MarkPartUploadedCommand command) {
        // 1. 업로드 세션 조회
        UploadSession session = uploadSessionPort
            .findBySessionKey(SessionKey.of(command.sessionKey()))
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Upload session not found: " + command.sessionKey()
                )
            );

        // 2. Multipart 정보 확인
        MultipartUpload multipart = multipartUploadManager
            .findByUploadSessionId(session.getId())
            .orElseThrow(() ->
                new IllegalStateException("Not a multipart upload")
            );

        // 3. UploadPart Value Object 생성
        UploadPart part = UploadPart.of(
            PartNumber.of(command.partNumber()),
            ETag.of(command.etag()),
            FileSize.of(command.partSize())
        );

        // 4. 파트 추가 및 저장 (Manager 사용)
        multipartUploadManager.addPart(multipart, part);
    }
}
