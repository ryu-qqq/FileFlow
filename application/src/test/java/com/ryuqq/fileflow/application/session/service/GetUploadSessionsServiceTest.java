package com.ryuqq.fileflow.application.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.common.dto.response.SliceResponse;
import com.ryuqq.fileflow.application.session.assembler.UploadSessionQueryAssembler;
import com.ryuqq.fileflow.application.session.dto.query.ListUploadSessionsQuery;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionResponse;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionSearchCriteria;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("GetUploadSessionsService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class GetUploadSessionsServiceTest {

    private static final long TENANT_ID = 20L;
    private static final long ORGANIZATION_ID = 10L;
    private static final int PAGE = 0;
    private static final int SIZE = 10;

    @Mock private FindUploadSessionQueryPort findUploadSessionQueryPort;
    @Mock private UploadSessionQueryAssembler uploadSessionQueryAssembler;

    @InjectMocks private GetUploadSessionsService getUploadSessionsService;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("세션 목록을 조회하여 SliceResponse로 반환한다")
        void execute_ShouldReturnSliceResponse() {
            // given
            ListUploadSessionsQuery query =
                    ListUploadSessionsQuery.of(TENANT_ID, ORGANIZATION_ID, null, null, PAGE, SIZE);
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(
                            TENANT_ID, ORGANIZATION_ID, null, null, 0L, SIZE);

            List<UploadSession> sessions =
                    List.of(mock(UploadSession.class), mock(UploadSession.class));
            List<UploadSessionResponse> responses = createResponses(2);

            when(uploadSessionQueryAssembler.toCriteria(query)).thenReturn(criteria);
            when(findUploadSessionQueryPort.findByCriteria(criteria)).thenReturn(sessions);
            when(uploadSessionQueryAssembler.toResponses(sessions)).thenReturn(responses);

            // when
            SliceResponse<UploadSessionResponse> result = getUploadSessionsService.execute(query);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.size()).isEqualTo(SIZE);

            verify(uploadSessionQueryAssembler).toCriteria(query);
            verify(findUploadSessionQueryPort).findByCriteria(criteria);
            verify(uploadSessionQueryAssembler).toResponses(sessions);
        }

        @Test
        @DisplayName("다음 페이지가 있으면 hasNext가 true이다")
        void execute_HasMoreData_ShouldReturnHasNextTrue() {
            // given
            ListUploadSessionsQuery query =
                    ListUploadSessionsQuery.of(TENANT_ID, ORGANIZATION_ID, null, null, PAGE, SIZE);
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(
                            TENANT_ID, ORGANIZATION_ID, null, null, 0L, SIZE);

            List<UploadSession> sessions = new ArrayList<>();
            for (int i = 0; i < SIZE + 1; i++) {
                sessions.add(mock(UploadSession.class));
            }
            List<UploadSessionResponse> responses = createResponses(SIZE + 1);

            when(uploadSessionQueryAssembler.toCriteria(query)).thenReturn(criteria);
            when(findUploadSessionQueryPort.findByCriteria(criteria)).thenReturn(sessions);
            when(uploadSessionQueryAssembler.toResponses(sessions)).thenReturn(responses);

            // when
            SliceResponse<UploadSessionResponse> result = getUploadSessionsService.execute(query);

            // then
            assertThat(result.content()).hasSize(SIZE);
            assertThat(result.hasNext()).isTrue();
        }

        @Test
        @DisplayName("빈 결과를 반환할 수 있다")
        void execute_EmptyResult_ShouldReturnEmptySlice() {
            // given
            ListUploadSessionsQuery query =
                    ListUploadSessionsQuery.of(TENANT_ID, ORGANIZATION_ID, null, null, PAGE, SIZE);
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(
                            TENANT_ID, ORGANIZATION_ID, null, null, 0L, SIZE);

            when(uploadSessionQueryAssembler.toCriteria(query)).thenReturn(criteria);
            when(findUploadSessionQueryPort.findByCriteria(criteria)).thenReturn(List.of());
            when(uploadSessionQueryAssembler.toResponses(List.of())).thenReturn(List.of());

            // when
            SliceResponse<UploadSessionResponse> result = getUploadSessionsService.execute(query);

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("상태 필터를 적용하여 조회할 수 있다")
        void execute_WithStatusFilter_ShouldApplyFilter() {
            // given
            SessionStatus filterStatus = SessionStatus.COMPLETED;
            ListUploadSessionsQuery query =
                    ListUploadSessionsQuery.of(
                            TENANT_ID, ORGANIZATION_ID, filterStatus, null, PAGE, SIZE);
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(
                            TENANT_ID, ORGANIZATION_ID, filterStatus, null, 0L, SIZE);

            List<UploadSession> sessions = List.of(mock(UploadSession.class));
            List<UploadSessionResponse> responses = createResponses(1);

            when(uploadSessionQueryAssembler.toCriteria(query)).thenReturn(criteria);
            when(findUploadSessionQueryPort.findByCriteria(criteria)).thenReturn(sessions);
            when(uploadSessionQueryAssembler.toResponses(sessions)).thenReturn(responses);

            // when
            SliceResponse<UploadSessionResponse> result = getUploadSessionsService.execute(query);

            // then
            assertThat(result.content()).hasSize(1);
            verify(uploadSessionQueryAssembler).toCriteria(query);
        }

        @Test
        @DisplayName("업로드 타입 필터를 적용하여 조회할 수 있다")
        void execute_WithUploadTypeFilter_ShouldApplyFilter() {
            // given
            String uploadType = "MULTIPART";
            ListUploadSessionsQuery query =
                    ListUploadSessionsQuery.of(
                            TENANT_ID, ORGANIZATION_ID, null, uploadType, PAGE, SIZE);
            UploadSessionSearchCriteria criteria =
                    UploadSessionSearchCriteria.of(
                            TENANT_ID, ORGANIZATION_ID, null, uploadType, 0L, SIZE);

            List<UploadSession> sessions = List.of(mock(UploadSession.class));
            List<UploadSessionResponse> responses = createResponses(1);

            when(uploadSessionQueryAssembler.toCriteria(query)).thenReturn(criteria);
            when(findUploadSessionQueryPort.findByCriteria(criteria)).thenReturn(sessions);
            when(uploadSessionQueryAssembler.toResponses(sessions)).thenReturn(responses);

            // when
            SliceResponse<UploadSessionResponse> result = getUploadSessionsService.execute(query);

            // then
            assertThat(result.content()).hasSize(1);
            verify(uploadSessionQueryAssembler).toCriteria(query);
        }
    }

    private List<UploadSessionResponse> createResponses(int count) {
        List<UploadSessionResponse> responses = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            responses.add(
                    UploadSessionResponse.of(
                            UUID.randomUUID().toString(),
                            "file" + i + ".pdf",
                            1024L * (i + 1),
                            "application/pdf",
                            "SINGLE",
                            SessionStatus.COMPLETED,
                            "bucket",
                            "key" + i,
                            LocalDateTime.now().minusHours(i + 1),
                            LocalDateTime.now().plusHours(23)));
        }
        return responses;
    }
}
