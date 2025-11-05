package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.upload.manager.MultipartUploadStateManager;
import com.ryuqq.fileflow.application.upload.port.in.MarkPartUploadedUseCase;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadMultipartUploadPort;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
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

    private final LoadUploadSessionPort loadUploadSessionPort;
    private final LoadMultipartUploadPort loadMultipartUploadPort;
    private final MultipartUploadStateManager multipartUploadStateManager;

    public MarkPartUploadedService(
        LoadUploadSessionPort loadUploadSessionPort,
        LoadMultipartUploadPort loadMultipartUploadPort,
        MultipartUploadStateManager multipartUploadStateManager
    ) {
        this.loadUploadSessionPort = loadUploadSessionPort;
        this.loadMultipartUploadPort = loadMultipartUploadPort;
        this.multipartUploadStateManager = multipartUploadStateManager;
    }

    @Override
    @Transactional
    public void execute(MarkPartUploadedCommand command) {
        // 1. 업로드 세션 조회 (Query Port)
        UploadSession session = loadUploadSessionPort
            .findBySessionKey(SessionKey.of(command.sessionKey()))
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Upload session not found: " + command.sessionKey()
                )
            );

        // 2. Multipart 정보 확인 (Query Port)
        MultipartUpload multipart = loadMultipartUploadPort
            .findByUploadSessionId(session.getIdValue())
            .orElseThrow(() ->
                new IllegalStateException("Not a multipart upload")
            );

        // 3. UploadPart Value Object 생성
        UploadPart part = UploadPart.of(
            PartNumber.of(command.partNumber()),
            ETag.of(command.etag()),
            FileSize.of(command.partSize())
        );

        // 4. 파트 추가 및 저장 (StateManager 사용)
        multipartUploadStateManager.addPart(multipart, part);
    }
}
