package com.ryuqq.fileflow.domain.upload.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileId 테스트")
class FileIdTest {

    @Test
    @DisplayName("새로운 FileId를 생성한다")
    void generateNewFileId() {
        // when
        FileId fileId = FileId.generate();

        // then
        assertThat(fileId.value()).isNotNull();
        assertThat(fileId.value()).isNotEmpty();
    }

    @Test
    @DisplayName("generate로 생성된 FileId는 UUID 형식이다")
    void generatedFileIdIsUUIDFormat() {
        // when
        FileId fileId = FileId.generate();

        // then
        assertThat(fileId.value()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    @DisplayName("generate로 생성된 각 FileId는 고유하다")
    void eachGeneratedFileIdIsUnique() {
        // when
        FileId fileId1 = FileId.generate();
        FileId fileId2 = FileId.generate();
        FileId fileId3 = FileId.generate();

        // then
        assertThat(fileId1).isNotEqualTo(fileId2);
        assertThat(fileId2).isNotEqualTo(fileId3);
        assertThat(fileId1).isNotEqualTo(fileId3);
    }

    @Test
    @DisplayName("유효한 UUID 문자열로 FileId를 생성한다")
    void createFileIdWithValidUUID() {
        // given
        String uuid = "550e8400-e29b-41d4-a716-446655440000";

        // when
        FileId fileId = FileId.of(uuid);

        // then
        assertThat(fileId.value()).isEqualTo(uuid);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("null 또는 빈 문자열로 생성 시 예외가 발생한다")
    void throwsExceptionWhenValueIsNullOrEmpty(String invalidValue) {
        // when & then
        assertThatThrownBy(() -> FileId.of(invalidValue))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("FileId cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid-uuid",
        "not-a-uuid",
        "12345",
        "abcdefgh-1234-5678-90ab-cdefghijklmn",
        "550e8400-e29b-41d4-a716",
        "550e8400e29b41d4a716446655440000",
        "550e8400-e29b-41d4-a716-446655440000-extra"
    })
    @DisplayName("유효하지 않은 UUID 형식으로 생성 시 예외가 발생한다")
    void throwsExceptionWhenValueIsNotValidUUID(String invalidUuid) {
        // when & then
        assertThatThrownBy(() -> FileId.of(invalidUuid))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("FileId must be a valid UUID format");
    }

    @Test
    @DisplayName("동일한 UUID 값의 FileId는 같다")
    void equalFileIdsAreEqual() {
        // given
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        FileId fileId1 = FileId.of(uuid);
        FileId fileId2 = FileId.of(uuid);

        // when & then
        assertThat(fileId1).isEqualTo(fileId2);
        assertThat(fileId1.hashCode()).isEqualTo(fileId2.hashCode());
    }

    @Test
    @DisplayName("다른 UUID 값의 FileId는 다르다")
    void differentFileIdsAreNotEqual() {
        // given
        FileId fileId1 = FileId.of("550e8400-e29b-41d4-a716-446655440000");
        FileId fileId2 = FileId.of("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

        // when & then
        assertThat(fileId1).isNotEqualTo(fileId2);
    }

    @Test
    @DisplayName("대문자 UUID로 FileId를 생성한다")
    void createFileIdWithUppercaseUUID() {
        // given
        String uuid = "550E8400-E29B-41D4-A716-446655440000";

        // when
        FileId fileId = FileId.of(uuid);

        // then
        assertThat(fileId.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("소문자 UUID로 FileId를 생성한다")
    void createFileIdWithLowercaseUUID() {
        // given
        String uuid = "550e8400-e29b-41d4-a716-446655440000";

        // when
        FileId fileId = FileId.of(uuid);

        // then
        assertThat(fileId.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("대소문자 혼합 UUID로 FileId를 생성한다")
    void createFileIdWithMixedCaseUUID() {
        // given
        String uuid = "550E8400-e29b-41D4-a716-446655440000";

        // when
        FileId fileId = FileId.of(uuid);

        // then
        assertThat(fileId.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("UUID v1 형식의 FileId를 생성한다")
    void createFileIdWithUUIDv1Format() {
        // given
        String uuid = "6ba7b810-9dad-11d1-80b4-00c04fd430c8";

        // when
        FileId fileId = FileId.of(uuid);

        // then
        assertThat(fileId.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("UUID v4 형식의 FileId를 생성한다")
    void createFileIdWithUUIDv4Format() {
        // given
        String uuid = UUID.randomUUID().toString();

        // when
        FileId fileId = FileId.of(uuid);

        // then
        assertThat(fileId.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("nil UUID로 FileId를 생성한다")
    void createFileIdWithNilUUID() {
        // given
        String nilUuid = "00000000-0000-0000-0000-000000000000";

        // when
        FileId fileId = FileId.of(nilUuid);

        // then
        assertThat(fileId.value()).isEqualTo(nilUuid);
    }

    @Test
    @DisplayName("toString이 UUID 값을 포함한다")
    void toStringContainsUUID() {
        // given
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        FileId fileId = FileId.of(uuid);

        // when
        String result = fileId.toString();

        // then
        assertThat(result).contains(uuid);
    }

    @Test
    @DisplayName("generate로 생성한 FileId는 of로 재생성할 수 있다")
    void generatedFileIdCanBeRecreatedWithOf() {
        // given
        FileId original = FileId.generate();

        // when
        FileId recreated = FileId.of(original.value());

        // then
        assertThat(recreated).isEqualTo(original);
    }

    @Test
    @DisplayName("연속으로 생성한 FileId들은 모두 고유하다")
    void consecutivelyGeneratedFileIdsAreAllUnique() {
        // given
        int count = 100;

        // when
        FileId[] fileIds = new FileId[count];
        for (int i = 0; i < count; i++) {
            fileIds[i] = FileId.generate();
        }

        // then
        for (int i = 0; i < count; i++) {
            for (int j = i + 1; j < count; j++) {
                assertThat(fileIds[i]).isNotEqualTo(fileIds[j]);
            }
        }
    }

    @Test
    @DisplayName("유효한 UUID 형식이지만 잘못된 버전도 허용한다")
    void acceptsValidUUIDFormatRegardlessOfVersion() {
        // given - UUID v5 형식
        String uuid = "886313e1-3b8a-5372-9b90-0c9aee199e5d";

        // when
        FileId fileId = FileId.of(uuid);

        // then
        assertThat(fileId.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("공백이 포함된 UUID로 생성 시 예외가 발생한다")
    void throwsExceptionWhenUUIDContainsWhitespace() {
        // given
        String invalidUuid = "550e8400-e29b-41d4-a716-446655440000 ";

        // when & then
        assertThatThrownBy(() -> FileId.of(invalidUuid))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
