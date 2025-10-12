package com.ryuqq.fileflow.domain.file;

import com.ryuqq.fileflow.domain.upload.vo.FileId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileMetadata 테스트")
class FileMetadataTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
        Instant.parse("2024-01-01T00:00:00Z"),
        ZoneId.systemDefault()
    );

    @Test
    @DisplayName("유효한 파라미터로 FileMetadata를 생성한다")
    void createFileMetadataWithValidParameters() {
        // given
        FileId fileId = FileId.generate();
        String key = "width";
        String value = "1920";
        MetadataType type = MetadataType.NUMBER;

        // when
        FileMetadata metadata = FileMetadata.create(fileId, key, value, type, FIXED_CLOCK);

        // then
        assertThat(metadata.getFileId()).isEqualTo(fileId);
        assertThat(metadata.getMetadataKey()).isEqualTo(key);
        assertThat(metadata.getMetadataValue()).isEqualTo(value);
        assertThat(metadata.getValueType()).isEqualTo(type);
        assertThat(metadata.getCreatedAt()).isNotNull();
        assertThat(metadata.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("null FileId로 생성 시 예외가 발생한다")
    void throwsExceptionWhenFileIdIsNull() {
        // when & then
        assertThatThrownBy(() -> FileMetadata.create(null, "key", "value", MetadataType.STRING))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("FileId cannot be null");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t", "\n"})
    @DisplayName("null 또는 빈 메타데이터 키로 생성 시 예외가 발생한다")
    void throwsExceptionWhenMetadataKeyIsNullOrEmpty(String invalidKey) {
        // given
        FileId fileId = FileId.generate();

        // when & then
        assertThatThrownBy(() -> FileMetadata.create(fileId, invalidKey, "value", MetadataType.STRING))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("MetadataKey cannot be null or empty");
    }

    @Test
    @DisplayName("100자를 초과하는 메타데이터 키로 생성 시 예외가 발생한다")
    void throwsExceptionWhenMetadataKeyExceeds100Characters() {
        // given
        FileId fileId = FileId.generate();
        String longKey = "a".repeat(101);

        // when & then
        assertThatThrownBy(() -> FileMetadata.create(fileId, longKey, "value", MetadataType.STRING))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("MetadataKey cannot exceed 100 characters");
    }

    @Test
    @DisplayName("null 메타데이터 값으로 생성 시 예외가 발생한다")
    void throwsExceptionWhenMetadataValueIsNull() {
        // given
        FileId fileId = FileId.generate();

        // when & then
        assertThatThrownBy(() -> FileMetadata.create(fileId, "key", null, MetadataType.STRING))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("MetadataValue cannot be null");
    }

    @Test
    @DisplayName("null 값 타입으로 생성 시 예외가 발생한다")
    void throwsExceptionWhenValueTypeIsNull() {
        // given
        FileId fileId = FileId.generate();

        // when & then
        assertThatThrownBy(() -> FileMetadata.create(fileId, "key", "value", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ValueType cannot be null");
    }

    @Test
    @DisplayName("타입과 호환되지 않는 값으로 생성 시 예외가 발생한다")
    void throwsExceptionWhenValueIsNotCompatibleWithType() {
        // given
        FileId fileId = FileId.generate();

        // when & then
        assertThatThrownBy(() -> FileMetadata.create(fileId, "key", "not-a-number", MetadataType.NUMBER))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not compatible with type");
    }

    @Test
    @DisplayName("STRING 타입의 메타데이터를 생성한다")
    void createStringTypeMetadata() {
        // given
        FileId fileId = FileId.generate();

        // when
        FileMetadata metadata = FileMetadata.create(fileId, "format", "JPEG", MetadataType.STRING);

        // then
        assertThat(metadata.getValueType()).isEqualTo(MetadataType.STRING);
        assertThat(metadata.asString()).isEqualTo("JPEG");
    }

    @Test
    @DisplayName("NUMBER 타입의 메타데이터를 생성한다")
    void createNumberTypeMetadata() {
        // given
        FileId fileId = FileId.generate();

        // when
        FileMetadata metadata = FileMetadata.create(fileId, "width", "1920", MetadataType.NUMBER);

        // then
        assertThat(metadata.getValueType()).isEqualTo(MetadataType.NUMBER);
        assertThat(metadata.asNumber()).isEqualTo(1920.0);
    }

    @Test
    @DisplayName("BOOLEAN 타입의 메타데이터를 생성한다")
    void createBooleanTypeMetadata() {
        // given
        FileId fileId = FileId.generate();

        // when
        FileMetadata metadata = FileMetadata.create(fileId, "has_alpha", "true", MetadataType.BOOLEAN);

        // then
        assertThat(metadata.getValueType()).isEqualTo(MetadataType.BOOLEAN);
        assertThat(metadata.asBoolean()).isTrue();
    }

    @Test
    @DisplayName("JSON 타입의 메타데이터를 생성한다")
    void createJsonTypeMetadata() {
        // given
        FileId fileId = FileId.generate();
        String jsonValue = "{\"key\":\"value\"}";

        // when
        FileMetadata metadata = FileMetadata.create(fileId, "exif_data", jsonValue, MetadataType.JSON);

        // then
        assertThat(metadata.getValueType()).isEqualTo(MetadataType.JSON);
        assertThat(metadata.asString()).isEqualTo(jsonValue);
    }

    @Test
    @DisplayName("메타데이터 값을 변경한 새 인스턴스를 생성한다")
    void createsNewInstanceWithUpdatedValue() {
        // given
        FileId fileId = FileId.generate();
        FileMetadata original = FileMetadata.create(fileId, "width", "1920", MetadataType.NUMBER, FIXED_CLOCK);

        Clock laterClock = Clock.fixed(
            Instant.parse("2024-01-02T00:00:00Z"),
            ZoneId.systemDefault()
        );

        // when
        FileMetadata updated = original.withValue("2560", laterClock);

        // then
        assertThat(updated.getMetadataValue()).isEqualTo("2560");
        assertThat(updated.getFileId()).isEqualTo(original.getFileId());
        assertThat(updated.getMetadataKey()).isEqualTo(original.getMetadataKey());
        assertThat(updated.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(updated.getUpdatedAt()).isAfter(original.getUpdatedAt());
    }

    @Test
    @DisplayName("타입과 호환되지 않는 값으로 변경 시 예외가 발생한다")
    void throwsExceptionWhenUpdatingWithIncompatibleValue() {
        // given
        FileId fileId = FileId.generate();
        FileMetadata metadata = FileMetadata.create(fileId, "width", "1920", MetadataType.NUMBER);

        // when & then
        assertThatThrownBy(() -> metadata.withValue("not-a-number"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not compatible with type");
    }

    @Test
    @DisplayName("asNumber를 NUMBER 타입이 아닌 메타데이터에 호출 시 예외가 발생한다")
    void throwsExceptionWhenCallingAsNumberOnNonNumberType() {
        // given
        FileId fileId = FileId.generate();
        FileMetadata metadata = FileMetadata.create(fileId, "format", "JPEG", MetadataType.STRING);

        // when & then
        assertThatThrownBy(metadata::asNumber)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot convert to number");
    }

    @Test
    @DisplayName("asBoolean을 BOOLEAN 타입이 아닌 메타데이터에 호출 시 예외가 발생한다")
    void throwsExceptionWhenCallingAsBooleanOnNonBooleanType() {
        // given
        FileId fileId = FileId.generate();
        FileMetadata metadata = FileMetadata.create(fileId, "format", "JPEG", MetadataType.STRING);

        // when & then
        assertThatThrownBy(metadata::asBoolean)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot convert to boolean");
    }

    @Test
    @DisplayName("hasKey가 올바르게 동작한다")
    void hasKeyWorksCorrectly() {
        // given
        FileId fileId = FileId.generate();
        FileMetadata metadata = FileMetadata.create(fileId, "width", "1920", MetadataType.NUMBER);

        // when & then
        assertThat(metadata.hasKey("width")).isTrue();
        assertThat(metadata.hasKey("height")).isFalse();
    }

    @Test
    @DisplayName("isType이 올바르게 동작한다")
    void isTypeWorksCorrectly() {
        // given
        FileId fileId = FileId.generate();
        FileMetadata metadata = FileMetadata.create(fileId, "width", "1920", MetadataType.NUMBER);

        // when & then
        assertThat(metadata.isType(MetadataType.NUMBER)).isTrue();
        assertThat(metadata.isType(MetadataType.STRING)).isFalse();
    }

    @Test
    @DisplayName("reconstitute로 기존 메타데이터를 재구성한다")
    void reconstitutesExistingMetadata() {
        // given
        FileId fileId = FileId.generate();
        String key = "width";
        String value = "1920";
        MetadataType type = MetadataType.NUMBER;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        // when
        FileMetadata metadata = FileMetadata.reconstitute(fileId, key, value, type, createdAt, updatedAt);

        // then
        assertThat(metadata.getFileId()).isEqualTo(fileId);
        assertThat(metadata.getMetadataKey()).isEqualTo(key);
        assertThat(metadata.getMetadataValue()).isEqualTo(value);
        assertThat(metadata.getValueType()).isEqualTo(type);
        assertThat(metadata.getCreatedAt()).isEqualTo(createdAt);
        assertThat(metadata.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("null createdAt으로 reconstitute 시 예외가 발생한다")
    void throwsExceptionWhenReconstituteWithNullCreatedAt() {
        // given
        FileId fileId = FileId.generate();

        // when & then
        assertThatThrownBy(() -> FileMetadata.reconstitute(
            fileId, "key", "value", MetadataType.STRING, null, LocalDateTime.now()
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CreatedAt cannot be null");
    }

    @Test
    @DisplayName("null updatedAt으로 reconstitute 시 예외가 발생한다")
    void throwsExceptionWhenReconstituteWithNullUpdatedAt() {
        // given
        FileId fileId = FileId.generate();

        // when & then
        assertThatThrownBy(() -> FileMetadata.reconstitute(
            fileId, "key", "value", MetadataType.STRING, LocalDateTime.now(), null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("UpdatedAt cannot be null");
    }

    @Test
    @DisplayName("동일한 fileId와 key를 가진 메타데이터는 같다")
    void equalMetadataAreEqual() {
        // given
        FileId fileId = FileId.generate();
        FileMetadata metadata1 = FileMetadata.create(fileId, "width", "1920", MetadataType.NUMBER);
        FileMetadata metadata2 = FileMetadata.create(fileId, "width", "2560", MetadataType.NUMBER);

        // when & then
        assertThat(metadata1).isEqualTo(metadata2);
        assertThat(metadata1.hashCode()).isEqualTo(metadata2.hashCode());
    }

    @Test
    @DisplayName("다른 fileId를 가진 메타데이터는 다르다")
    void metadataWithDifferentFileIdAreNotEqual() {
        // given
        FileId fileId1 = FileId.generate();
        FileId fileId2 = FileId.generate();
        FileMetadata metadata1 = FileMetadata.create(fileId1, "width", "1920", MetadataType.NUMBER);
        FileMetadata metadata2 = FileMetadata.create(fileId2, "width", "1920", MetadataType.NUMBER);

        // when & then
        assertThat(metadata1).isNotEqualTo(metadata2);
    }

    @Test
    @DisplayName("다른 key를 가진 메타데이터는 다르다")
    void metadataWithDifferentKeyAreNotEqual() {
        // given
        FileId fileId = FileId.generate();
        FileMetadata metadata1 = FileMetadata.create(fileId, "width", "1920", MetadataType.NUMBER);
        FileMetadata metadata2 = FileMetadata.create(fileId, "height", "1920", MetadataType.NUMBER);

        // when & then
        assertThat(metadata1).isNotEqualTo(metadata2);
    }

    @Test
    @DisplayName("toString이 모든 필드를 포함한다")
    void toStringContainsAllFields() {
        // given
        FileId fileId = FileId.generate();
        FileMetadata metadata = FileMetadata.create(fileId, "width", "1920", MetadataType.NUMBER);

        // when
        String result = metadata.toString();

        // then
        assertThat(result).contains("FileMetadata");
        assertThat(result).contains("width");
        assertThat(result).contains("1920");
        assertThat(result).contains("NUMBER");
    }

    @Test
    @DisplayName("소수점을 포함한 NUMBER 타입을 처리한다")
    void handlesDecimalNumbers() {
        // given
        FileId fileId = FileId.generate();

        // when
        FileMetadata metadata = FileMetadata.create(fileId, "aspect_ratio", "1.7777", MetadataType.NUMBER);

        // then
        assertThat(metadata.asNumber()).isEqualTo(1.7777);
    }

    @Test
    @DisplayName("음수 NUMBER 타입을 처리한다")
    void handlesNegativeNumbers() {
        // given
        FileId fileId = FileId.generate();

        // when
        FileMetadata metadata = FileMetadata.create(fileId, "temperature", "-10.5", MetadataType.NUMBER);

        // then
        assertThat(metadata.asNumber()).isEqualTo(-10.5);
    }

    @Test
    @DisplayName("빈 문자열 STRING 타입을 처리한다")
    void handlesEmptyStringValue() {
        // given
        FileId fileId = FileId.generate();

        // when
        FileMetadata metadata = FileMetadata.create(fileId, "description", "", MetadataType.STRING);

        // then
        assertThat(metadata.asString()).isEmpty();
    }

    @Test
    @DisplayName("false BOOLEAN 타입을 처리한다")
    void handlesFalseBooleanValue() {
        // given
        FileId fileId = FileId.generate();

        // when
        FileMetadata metadata = FileMetadata.create(fileId, "is_public", "false", MetadataType.BOOLEAN);

        // then
        assertThat(metadata.asBoolean()).isFalse();
    }

    @Test
    @DisplayName("대소문자 혼합 BOOLEAN 값을 처리한다")
    void handlesMixedCaseBooleanValue() {
        // given
        FileId fileId = FileId.generate();

        // when
        FileMetadata metadata1 = FileMetadata.create(fileId, "flag1", "True", MetadataType.BOOLEAN);
        FileMetadata metadata2 = FileMetadata.create(fileId, "flag2", "FALSE", MetadataType.BOOLEAN);

        // then
        assertThat(metadata1.asBoolean()).isTrue();
        assertThat(metadata2.asBoolean()).isFalse();
    }
}
