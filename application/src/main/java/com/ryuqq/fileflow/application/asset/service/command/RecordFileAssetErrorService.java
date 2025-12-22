package com.ryuqq.fileflow.application.asset.service.command;

import com.ryuqq.fileflow.application.asset.manager.command.FileAssetTransactionManager;
import com.ryuqq.fileflow.application.asset.manager.query.FileAssetReadManager;
import com.ryuqq.fileflow.application.asset.port.in.command.RecordFileAssetErrorUseCase;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * FileAsset 에러 메시지 기록 서비스.
 *
 * <p>파일 처리 중 예외가 발생했을 때 실제 에러 메시지를 저장합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>FileAsset 조회
 *   <li>에러 메시지 기록 (도메인 메서드 호출)
 *   <li>영속화
 * </ul>
 *
 * <p><strong>주의사항</strong>: 상태 변경 없이 에러 메시지만 저장합니다.
 */
@Service
public class RecordFileAssetErrorService implements RecordFileAssetErrorUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecordFileAssetErrorService.class);

    private final FileAssetReadManager fileAssetReadManager;
    private final FileAssetTransactionManager fileAssetTransactionManager;

    public RecordFileAssetErrorService(
            FileAssetReadManager fileAssetReadManager,
            FileAssetTransactionManager fileAssetTransactionManager) {
        this.fileAssetReadManager = fileAssetReadManager;
        this.fileAssetTransactionManager = fileAssetTransactionManager;
    }

    @Override
    public void recordError(String fileAssetId, String errorMessage) {
        Optional<FileAsset> fileAssetOpt =
                fileAssetReadManager.findById(FileAssetId.of(fileAssetId));

        if (fileAssetOpt.isEmpty()) {
            log.warn("FileAsset not found for error recording: id={}", fileAssetId);
            return;
        }

        FileAsset fileAsset = fileAssetOpt.get();
        fileAsset.recordError(errorMessage);

        fileAssetTransactionManager.persist(fileAsset);

        log.debug(
                "FileAsset 에러 메시지 기록 완료: id={}, errorLength={}",
                fileAssetId,
                errorMessage != null ? errorMessage.length() : 0);
    }
}
