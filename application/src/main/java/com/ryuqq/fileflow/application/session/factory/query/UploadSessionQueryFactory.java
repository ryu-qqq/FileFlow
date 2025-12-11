package com.ryuqq.fileflow.application.session.factory.query;

import com.ryuqq.fileflow.application.session.dto.query.ListUploadSessionsQuery;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionSearchCriteria;
import org.springframework.stereotype.Component;

/**
 * UploadSession Query Factory.
 *
 * <p>Query DTO를 Domain Criteria VO로 변환합니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Query → Criteria 변환
 * </ul>
 *
 * <p><strong>규칙:</strong>
 *
 * <ul>
 *   <li>@Component 어노테이션 (Service 아님)
 *   <li>비즈니스 로직 금지 (순수 변환)
 *   <li>Port 호출 금지 (조회 없음)
 *   <li>@Transactional 금지
 * </ul>
 */
@Component
public class UploadSessionQueryFactory {

    /**
     * ListUploadSessionsQuery를 Domain 검색 조건으로 변환합니다.
     *
     * @param query 조회 Query
     * @return Domain 검색 조건
     */
    public UploadSessionSearchCriteria createCriteria(ListUploadSessionsQuery query) {
        SessionStatus status = query.status() != null
                ? SessionStatus.valueOf(query.status())
                : null;

        return UploadSessionSearchCriteria.of(
                query.tenantId(),
                query.organizationId(),
                status,
                query.uploadType(),
                query.offset(),
                query.size());
    }
}
