package com.ryuqq.fileflow.application.common.manager;

import com.ryuqq.fileflow.application.common.port.out.StorageBucketPort;
import org.springframework.stereotype.Component;

@Component
public class StorageBucketManager {

    private final StorageBucketPort storageBucketPort;

    public StorageBucketManager(StorageBucketPort storageBucketPort) {
        this.storageBucketPort = storageBucketPort;
    }

    public String getBucket() {
        return storageBucketPort.getBucket();
    }
}
