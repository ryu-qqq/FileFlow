package com.ryuqq.fileflow.adapter.in.rest.asset.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.fileflow.adapter.in.rest.asset.AssetApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.asset.mapper.AssetCommandApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.fileflow.application.asset.dto.command.DeleteAssetCommand;
import com.ryuqq.fileflow.application.asset.port.in.command.DeleteAssetUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * AssetCommandController REST Docs 테스트.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("AssetCommandController REST Docs 테스트")
@WebMvcTest(AssetCommandController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssetCommandControllerRestDocsTest extends RestDocsTestSupport {

    @MockBean private DeleteAssetUseCase deleteUseCase;

    @MockBean private AssetCommandApiMapper commandMapper;

    @Nested
    @DisplayName("Asset 삭제 API")
    class DeleteAssetTest {

        @Test
        @DisplayName("DELETE /api/v1/assets/{assetId} - Asset 삭제 성공")
        void deleteAsset_success() throws Exception {
            // given
            String assetId = AssetApiFixtures.ASSET_ID;
            String source = AssetApiFixtures.SOURCE;

            given(commandMapper.toDeleteCommand(any(String.class), any(String.class)))
                    .willReturn(new DeleteAssetCommand(assetId, source));
            willDoNothing().given(deleteUseCase).execute(any(DeleteAssetCommand.class));

            // when & then
            mockMvc.perform(delete("/api/v1/assets/{assetId}?source={source}", assetId, source))
                    .andExpect(status().isNoContent())
                    .andDo(
                            document.document(
                                    pathParameters(
                                            parameterWithName("assetId").description("Asset ID")),
                                    queryParameters(
                                            parameterWithName("source").description("요청 서비스명"))));
        }
    }
}
