package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.session.dto.command.InitMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.facade.UploadSessionFacade;
import com.ryuqq.fileflow.application.session.factory.command.UploadSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.query.CompletedPartReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.InitMultipartUploadUseCase;
import com.ryuqq.fileflow.application.session.service.assembler.MultiPartUploadAssembler;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.vo.S3UploadId;
import com.ryuqq.fileflow.domain.session.vo.S3UploadMetadata;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Multipart 파일 업로드 세션 초기화 Service.
 *
 * <p>대용량 파일 업로드를 위한 Multipart 세션을 생성하고 Part별 Presigned URL을 발급합니다.
 *
 * <p><strong>처리 플로우</strong>:
 *
 * <ol>
 *   <li>Command → S3UploadMetadata (CommandFactory)
 *   <li>S3 Multipart Upload 초기화 (Upload ID 발급)
 *   <li>Upload ID로 세션 생성 (CommandFactory)
 *   <li>세션 활성화 및 저장 (Facade: RDB 저장 → Part URLs 생성 → 활성화 → RDB 업데이트 → Redis)
 *   <li>Response 변환 (Assembler)
 * </ol>
 *
 * <p><strong>최적화 포인트</strong>:
 *
 * <ul>
 *   <li>불필요한 temp 세션 생성 제거 (S3UploadMetadata 사용)
 *   <li>세션은 한 번만 생성 (Upload ID 발급 후)
 *   <li>Part URLs는 Domain에 저장 (Facade가 관리)
 * </ul>
 */
@Service
public class InitMultipartUploadService implements InitMultipartUploadUseCase {

    private final UploadSessionCommandFactory commandFactory;
    private final MultiPartUploadAssembler assembler;
    private final UploadSessionFacade uploadSessionFacade;
    private final CompletedPartReadManager completedPartReadManager;

    public InitMultipartUploadService(
            UploadSessionCommandFactory commandFactory,
            MultiPartUploadAssembler assembler,
            UploadSessionFacade uploadSessionFacade,
            CompletedPartReadManager completedPartReadManager) {
        this.commandFactory = commandFactory;
        this.assembler = assembler;
        this.uploadSessionFacade = uploadSessionFacade;
        this.completedPartReadManager = completedPartReadManager;
    }

    @Override
    public InitMultipartUploadResponse execute(InitMultipartUploadCommand command) {
        // 1. Command → S3 업로드 메타데이터 (CommandFactory)
        S3UploadMetadata s3Metadata = commandFactory.createS3UploadMetadata(command);

        // 2. S3 Multipart Upload 초기화 (Upload ID 발급 - Facade 위임)
        S3UploadId s3UploadId = uploadSessionFacade.initiateMultipartUpload(s3Metadata);

        // 3. Upload ID로 세션 생성 (CommandFactory)
        MultipartUploadSession session =
                commandFactory.createMultipartUploadSession(command, s3UploadId);

        // 4. 세션 활성화 및 저장 (Facade: RDB 저장 → Part URLs 생성 → 활성화 → RDB 업데이트 → Redis)
        MultipartUploadSession activatedSession =
                uploadSessionFacade.createAndActivateMultipartUpload(session);

        // 5. Part 목록 조회
        List<CompletedPart> completedParts =
                completedPartReadManager.findAllBySessionId(activatedSession.getId());

        // 6. Response 변환 (Assembler)
        return assembler.toResponseForInit(activatedSession, completedParts);
    }
}
