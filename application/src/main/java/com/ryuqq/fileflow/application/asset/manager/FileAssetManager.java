package com.ryuqq.fileflow.application.asset.manager;

import com.ryuqq.fileflow.application.asset.assembler.FileAssetAssembler;
import com.ryuqq.fileflow.application.asset.port.out.command.FileAssetPersistencePort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadFileCreatedEvent;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * FileAsset 저장을 관리하는 Manager.
 *
 * <p>Transaction 경계를 담당합니다.
 */
@Component
public class FileAssetManager {

    private final FileAssetAssembler fileAssetAssembler;
    private final FileAssetPersistencePort fileAssetPersistencePort;

    public FileAssetManager(
            FileAssetAssembler fileAssetAssembler,
            FileAssetPersistencePort fileAssetPersistencePort) {
        this.fileAssetAssembler = fileAssetAssembler;
        this.fileAssetPersistencePort = fileAssetPersistencePort;
    }

    /**
     * 파일 업로드 완료 이벤트로부터 FileAsset을 생성하고 저장합니다.
     *
     * @param event 파일 업로드 완료 이벤트
     */
    @Transactional
    public void createAndPersist(FileUploadCompletedEvent event) {
        FileAsset fileAsset = fileAssetAssembler.toFileAsset(event);
        fileAssetPersistencePort.persist(fileAsset);
    }

    /**
     * 외부 다운로드 파일 생성 이벤트로부터 FileAsset을 생성하고 저장합니다.
     *
     * @param event 외부 다운로드 파일 생성 이벤트
     * @return 생성된 FileAssetId
     */
    @Transactional
    public FileAssetId createAndPersist(ExternalDownloadFileCreatedEvent event) {
        FileAsset fileAsset = fileAssetAssembler.toFileAsset(event);
        return fileAssetPersistencePort.persist(fileAsset);
    }
}
