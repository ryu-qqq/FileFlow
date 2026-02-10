package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.session.dto.command.GeneratePresignedPartUrlCommand;
import com.ryuqq.fileflow.application.session.dto.response.PresignedPartUrlResponse;
import com.ryuqq.fileflow.application.session.factory.command.MultipartSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.MultipartUploadManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.GeneratePresignedPartUrlUseCase;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.vo.PartPresignedUrlSpec;
import org.springframework.stereotype.Service;

@Service
public class GeneratePresignedPartUrlService implements GeneratePresignedPartUrlUseCase {

    private final SessionReadManager sessionReadManager;
    private final MultipartSessionCommandFactory multipartSessionCommandFactory;
    private final MultipartUploadManager multipartUploadManager;

    public GeneratePresignedPartUrlService(
            SessionReadManager sessionReadManager,
            MultipartSessionCommandFactory multipartSessionCommandFactory,
            MultipartUploadManager multipartUploadManager) {
        this.sessionReadManager = sessionReadManager;
        this.multipartSessionCommandFactory = multipartSessionCommandFactory;
        this.multipartUploadManager = multipartUploadManager;
    }

    @Override
    public PresignedPartUrlResponse execute(GeneratePresignedPartUrlCommand command) {
        MultipartUploadSession session = sessionReadManager.getMultipart(command.sessionId());

        PartPresignedUrlSpec spec =
                multipartSessionCommandFactory.createPartPresignedUrlSpec(
                        session, command.partNumber());

        session.validateUploadable(spec.createdAt());

        String presignedUrl = multipartUploadManager.generatePresignedPartUrl(spec);

        return new PresignedPartUrlResponse(presignedUrl, spec.partNumber(), spec.ttlSeconds());
    }
}
