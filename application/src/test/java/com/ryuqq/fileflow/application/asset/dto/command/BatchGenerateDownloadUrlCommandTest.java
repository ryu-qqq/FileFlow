package com.ryuqq.fileflow.application.asset.dto.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("BatchGenerateDownloadUrlCommand 단위 테스트")
class BatchGenerateDownloadUrlCommandTest {

    private static final List<String> FILE_ASSET_IDS = List.of("id-1", "id-2", "id-3");
    private static final Long TENANT_ID = 10L;
    private static final Long ORGANIZATION_ID = 20L;
    private static final int EXPIRATION_MINUTES = 60;

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("of() 팩토리 메서드로 Command를 생성할 수 있다")
        void of_ShouldCreateCommand() {
            // given & when
            BatchGenerateDownloadUrlCommand command =
                    BatchGenerateDownloadUrlCommand.of(
                            FILE_ASSET_IDS, TENANT_ID, ORGANIZATION_ID, EXPIRATION_MINUTES);

            // then
            assertThat(command.fileAssetIds()).isEqualTo(FILE_ASSET_IDS);
            assertThat(command.tenantId()).isEqualTo(TENANT_ID);
            assertThat(command.organizationId()).isEqualTo(ORGANIZATION_ID);
            assertThat(command.expirationMinutes()).isEqualTo(EXPIRATION_MINUTES);
        }

        @Test
        @DisplayName("빈 파일 ID 목록으로 Command를 생성할 수 있다")
        void of_EmptyList_ShouldCreateCommand() {
            // given & when
            BatchGenerateDownloadUrlCommand command =
                    BatchGenerateDownloadUrlCommand.of(
                            List.of(), TENANT_ID, ORGANIZATION_ID, EXPIRATION_MINUTES);

            // then
            assertThat(command.fileAssetIds()).isEmpty();
        }

        @Test
        @DisplayName("단일 파일 ID로 Command를 생성할 수 있다")
        void of_SingleId_ShouldCreateCommand() {
            // given
            List<String> singleId = List.of("id-1");

            // when
            BatchGenerateDownloadUrlCommand command =
                    BatchGenerateDownloadUrlCommand.of(
                            singleId, TENANT_ID, ORGANIZATION_ID, EXPIRATION_MINUTES);

            // then
            assertThat(command.fileAssetIds()).hasSize(1);
            assertThat(command.fileAssetIds().get(0)).isEqualTo("id-1");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("동일한 값으로 생성된 두 Command는 동등해야 한다")
        void equals_SameValues_ShouldBeEqual() {
            // given
            BatchGenerateDownloadUrlCommand command1 =
                    BatchGenerateDownloadUrlCommand.of(
                            FILE_ASSET_IDS, TENANT_ID, ORGANIZATION_ID, EXPIRATION_MINUTES);
            BatchGenerateDownloadUrlCommand command2 =
                    BatchGenerateDownloadUrlCommand.of(
                            FILE_ASSET_IDS, TENANT_ID, ORGANIZATION_ID, EXPIRATION_MINUTES);

            // then
            assertThat(command1).isEqualTo(command2);
            assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
        }

        @Test
        @DisplayName("다른 유효 기간을 가진 두 Command는 동등하지 않아야 한다")
        void equals_DifferentExpiration_ShouldNotBeEqual() {
            // given
            BatchGenerateDownloadUrlCommand command1 =
                    BatchGenerateDownloadUrlCommand.of(
                            FILE_ASSET_IDS, TENANT_ID, ORGANIZATION_ID, 60);
            BatchGenerateDownloadUrlCommand command2 =
                    BatchGenerateDownloadUrlCommand.of(
                            FILE_ASSET_IDS, TENANT_ID, ORGANIZATION_ID, 120);

            // then
            assertThat(command1).isNotEqualTo(command2);
        }
    }
}
