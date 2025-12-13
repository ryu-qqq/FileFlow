package com.ryuqq.fileflow.application.session.service.query;

import com.ryuqq.fileflow.application.common.dto.response.SliceResponse;
import com.ryuqq.fileflow.application.session.dto.query.ListUploadSessionsQuery;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionResponse;
import com.ryuqq.fileflow.application.session.factory.query.UploadSessionQueryFactory;
import com.ryuqq.fileflow.application.session.manager.query.UploadSessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.query.GetUploadSessionsUseCase;
import com.ryuqq.fileflow.application.session.service.assembler.UploadSessionQueryAssembler;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionSearchCriteria;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * UploadSession 목록 조회 Service.
 *
 * <p>GetUploadSessionsUseCase 구현체입니다.
 */
@Service
public class GetUploadSessionsService implements GetUploadSessionsUseCase {

    private final UploadSessionReadManager uploadSessionReadManager;
    private final UploadSessionQueryFactory queryFactory;
    private final UploadSessionQueryAssembler assembler;

    public GetUploadSessionsService(
            UploadSessionReadManager uploadSessionReadManager,
            UploadSessionQueryFactory queryFactory,
            UploadSessionQueryAssembler assembler) {
        this.uploadSessionReadManager = uploadSessionReadManager;
        this.queryFactory = queryFactory;
        this.assembler = assembler;
    }

    @Override
    public SliceResponse<UploadSessionResponse> execute(ListUploadSessionsQuery query) {
        UploadSessionSearchCriteria criteria = queryFactory.createCriteria(query);

        List<UploadSession> sessions = uploadSessionReadManager.findByCriteria(criteria);
        List<UploadSessionResponse> responses = assembler.toResponses(sessions);

        boolean hasNext = sessions.size() > query.size();
        if (hasNext) {
            responses = responses.subList(0, query.size());
        }

        return SliceResponse.of(responses, query.size(), hasNext);
    }
}
