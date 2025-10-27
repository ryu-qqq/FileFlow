package com.ryuqq.fileflow.application.settings.assembler;

import com.ryuqq.fileflow.application.settings.dto.SettingResponse;
import com.ryuqq.fileflow.application.settings.port.in.CreateSettingUseCase;
import com.ryuqq.fileflow.application.settings.port.in.UpdateSettingUseCase;
import com.ryuqq.fileflow.domain.settings.Setting;

/**
 * Setting Assembler
 *
 * <p>Domain 객체(Setting)와 DTO(SettingResponse) 간 변환을 담당하는 Assembler입니다.</p>
 * <p>Assembler 패턴: Application Layer의 변환 로직을 캡슐화합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Assembler 패턴 - 변환 로직 캡슐화</li>
 *   <li>✅ Stateless - 상태를 가지지 않음</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public class SettingAssembler {

    /**
     * SettingAssembler 생성자.
     *
     * <p>Assembler는 stateless이므로 단순 생성자입니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingAssembler() {
        // Assembler - stateless
    }

    /**
     * Domain 객체(Setting)를 DTO(SettingResponse)로 변환합니다.
     *
     * <p>비밀 키는 getDisplayValue()를 통해 자동으로 마스킹됩니다.</p>
     *
     * @param setting Domain 객체
     * @return SettingResponse DTO
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingResponse toResponse(Setting setting) {
        if (setting == null) {
            return null;
        }

        return new SettingResponse(
            setting.getId(),
            setting.getKeyValue(),
            setting.getDisplayValue(), // 비밀 키 자동 마스킹
            setting.getValueType().name(),
            setting.getLevel().name(),
            setting.getContextId(),
            setting.isSecret(),
            setting.getCreatedAt(),
            setting.getUpdatedAt()
        );
    }

    /**
     * Domain 객체(Setting)를 UpdateSettingUseCase.Response로 변환합니다.
     *
     * <p>비밀 키는 getDisplayValue()를 통해 자동으로 마스킹됩니다.</p>
     *
     * @param setting Domain 객체
     * @return UpdateSettingUseCase.Response
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public UpdateSettingUseCase.Response toUpdateResponse(Setting setting) {
        if (setting == null) {
            return null;
        }

        return new UpdateSettingUseCase.Response(
            setting.getId(),
            setting.getKeyValue(),
            setting.getDisplayValue(), // 비밀 키 자동 마스킹
            setting.getValueType().name(),
            setting.getLevel().name(),
            setting.getContextId(),
            setting.isSecret(),
            setting.getCreatedAt(),
            setting.getUpdatedAt()
        );
    }

    /**
     * Domain 객체(Setting)를 CreateSettingUseCase.Response로 변환합니다.
     *
     * <p>비밀 키는 getDisplayValue()를 통해 자동으로 마스킹됩니다.</p>
     *
     * @param setting Domain 객체
     * @return CreateSettingUseCase.Response
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public CreateSettingUseCase.Response toCreateResponse(Setting setting) {
        if (setting == null) {
            return null;
        }

        return new CreateSettingUseCase.Response(
            setting.getId(),
            setting.getKeyValue(),
            setting.getDisplayValue(), // 비밀 키 자동 마스킹
            setting.getValueType().name(),
            setting.getLevel().name(),
            setting.getContextId(),
            setting.isSecret(),
            setting.getCreatedAt(),
            setting.getUpdatedAt()
        );
    }
}
