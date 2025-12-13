package com.ryuqq.fileflow.domain.asset.service;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.fixture.FileAssetFixture;
import com.ryuqq.fileflow.domain.asset.fixture.FileAssetStatusHistoryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAssetUpdateResult 단위 테스트")
class FileAssetUpdateResultTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드가 올바르게 설정된다")
        void constructor_ShouldSetAllFields() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            FileAssetStatusHistory statusHistory = FileAssetStatusHistoryFixture.aStatusHistory();

            // when
            FileAssetUpdateResult result = new FileAssetUpdateResult(fileAsset, statusHistory);

            // then
            assertThat(result.fileAsset()).isSameAs(fileAsset);
            assertThat(result.statusHistory()).isSameAs(statusHistory);
        }

        @Test
        @DisplayName("fileAssetId() 메서드는 FileAsset의 ID를 반환한다")
        void fileAssetId_ShouldReturnFileAssetId() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            FileAssetStatusHistory statusHistory = FileAssetStatusHistoryFixture.aStatusHistory();

            // when
            FileAssetUpdateResult result = new FileAssetUpdateResult(fileAsset, statusHistory);

            // then
            assertThat(result.fileAssetId()).isEqualTo(fileAsset.getId());
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("fileAsset이 null이면 예외가 발생한다")
        void constructor_WithNullFileAsset_ShouldThrowException() {
            // given
            FileAssetStatusHistory statusHistory = FileAssetStatusHistoryFixture.aStatusHistory();

            // when & then
            assertThatThrownBy(() -> new FileAssetUpdateResult(null, statusHistory))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FileAsset은 null일 수 없습니다");
        }

        @Test
        @DisplayName("statusHistory가 null이면 예외가 발생한다")
        void constructor_WithNullStatusHistory_ShouldThrowException() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();

            // when & then
            assertThatThrownBy(() -> new FileAssetUpdateResult(fileAsset, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("StatusHistory는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("레코드 동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 필드를 가진 두 Result는 동등하다")
        void equality_WithSameFields_ShouldBeEqual() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            FileAssetStatusHistory statusHistory = FileAssetStatusHistoryFixture.aStatusHistory();

            // when
            FileAssetUpdateResult result1 = new FileAssetUpdateResult(fileAsset, statusHistory);
            FileAssetUpdateResult result2 = new FileAssetUpdateResult(fileAsset, statusHistory);

            // then
            assertThat(result1).isEqualTo(result2);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("다른 FileAsset을 가진 두 Result는 동등하지 않다")
        void equality_WithDifferentFileAsset_ShouldNotBeEqual() {
            // given
            FileAsset fileAsset1 = FileAssetFixture.defaultFileAsset();
            FileAsset fileAsset2 = FileAssetFixture.defaultFileAsset();
            FileAssetStatusHistory statusHistory = FileAssetStatusHistoryFixture.aStatusHistory();

            // when
            FileAssetUpdateResult result1 = new FileAssetUpdateResult(fileAsset1, statusHistory);
            FileAssetUpdateResult result2 = new FileAssetUpdateResult(fileAsset2, statusHistory);

            // then
            assertThat(result1).isNotEqualTo(result2);
        }
    }
}
