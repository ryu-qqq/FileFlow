package com.ryuqq.fileflow.application.session.service;

import com.ryuqq.fileflow.application.common.dto.response.SliceResponse;
import com.ryuqq.fileflow.application.session.assembler.UploadSessionQueryAssembler;
import com.ryuqq.fileflow.application.session.dto.query.ListUploadSessionsQuery;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionResponse;
import com.ryuqq.fileflow.application.session.port.in.query.GetUploadSessionsUseCase;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionSearchCriteria;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UploadSession 목록 조회 Service.
 *
 * <p>GetUploadSessionsUseCase 구현체입니다.
 */
@Service
public class GetUploadSessionsService implements GetUploadSessionsUseCase {

    private final FindUploadSessionQueryPort findUploadSessionQueryPort;
    private final UploadSessionQueryAssembler uploadSessionQueryAssembler;

    public GetUploadSessionsService(
            FindUploadSessionQueryPort findUploadSessionQueryPort,
            UploadSessionQueryAssembler uploadSessionQueryAssembler) {
        this.findUploadSessionQueryPort = findUploadSessionQueryPort;
        this.uploadSessionQueryAssembler = uploadSessionQueryAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public SliceResponse<UploadSessionResponse> execute(ListUploadSessionsQuery query) {
        UploadSessionSearchCriteria criteria = uploadSessionQueryAssembler.toCriteria(query);

        List<UploadSession> sessions = findUploadSessionQueryPort.findByCriteria(criteria);
        List<UploadSessionResponse> responses = uploadSessionQueryAssembler.toResponses(sessions);

        boolean hasNext = sessions.size() > query.size();
        if (hasNext) {
            responses = responses.subList(0, query.size());
        }

        return SliceResponse.of(responses, query.size(), hasNext);
    }
}
