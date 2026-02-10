package com.ryuqq.fileflow.application.asset.port.out.command;

import com.ryuqq.fileflow.domain.asset.aggregate.Asset;

public interface AssetPersistencePort {

    void persist(Asset asset);
}
