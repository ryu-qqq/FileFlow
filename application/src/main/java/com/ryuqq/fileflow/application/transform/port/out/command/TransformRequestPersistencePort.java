package com.ryuqq.fileflow.application.transform.port.out.command;

import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;

public interface TransformRequestPersistencePort {

    void persist(TransformRequest transformRequest);
}
