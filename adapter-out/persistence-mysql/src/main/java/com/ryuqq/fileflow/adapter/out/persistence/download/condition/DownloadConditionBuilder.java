package com.ryuqq.fileflow.adapter.out.persistence.download.condition;

import static com.ryuqq.fileflow.adapter.out.persistence.download.entity.QDownloadTaskJpaEntity.downloadTaskJpaEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class DownloadConditionBuilder {

    public BooleanExpression idEq(String id) {
        if (id == null) return null;
        return downloadTaskJpaEntity.id.eq(id);
    }

    public BooleanExpression statusEq(DownloadTaskStatus status) {
        if (status == null) return null;
        return downloadTaskJpaEntity.status.eq(status);
    }

    public BooleanExpression createdBefore(Instant createdBefore) {
        if (createdBefore == null) return null;
        return downloadTaskJpaEntity.createdAt.before(createdBefore);
    }
}
