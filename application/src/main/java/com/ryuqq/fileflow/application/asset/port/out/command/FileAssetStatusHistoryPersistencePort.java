package com.ryuqq.fileflow.application.asset.port.out.command;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatusHistoryId;

/**
 * FileAssetStatusHistory 영속화 포트.
 *
 * <p>파일 자산 상태 변경 이력의 영속화를 담당한다.
 *
 * <p>이력은 불변(Append-Only)이므로 persist()만 제공한다.
 */
public interface FileAssetStatusHistoryPersistencePort {

    /**
     * FileAssetStatusHistory를 저장한다.
     *
     * <p>상태 변경 이력은 Append-Only로, 수정/삭제 없이 추가만 된다.
     *
     * @param history 저장할 FileAssetStatusHistory
     * @return 저장된 FileAssetStatusHistory ID
     */
    FileAssetStatusHistoryId persist(FileAssetStatusHistory history);
}
