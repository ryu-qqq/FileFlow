package com.ryuqq.fileflow.application.settings.service.command;

import com.ryuqq.fileflow.application.settings.assembler.SettingAssembler;
import com.ryuqq.fileflow.application.settings.port.SchemaValidator;
import com.ryuqq.fileflow.application.settings.port.in.UpdateSettingUseCase;
import com.ryuqq.fileflow.application.settings.port.out.LoadSettingsPort;
import com.ryuqq.fileflow.application.settings.port.out.SaveSettingPort;
import com.ryuqq.fileflow.domain.settings.exception.InvalidSettingException;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.exception.SettingNotFoundException;
import com.ryuqq.fileflow.domain.settings.SettingType;
import com.ryuqq.fileflow.domain.settings.SettingValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Update Setting Service - Command Handler
 *
 * <p>설정 값을 업데이트하는 Command Handler 구현체입니다.</p>
 * <p>JSON 스키마 검증을 거쳐 설정 값을 업데이트합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ CQRS 패턴 - Command Handler (쓰기 작업)</li>
 *   <li>✅ Hexagonal Architecture - Port 사용 (LoadSettingsPort, SaveSettingPort)</li>
 *   <li>✅ Transaction 경계 - 쓰기 트랜잭션</li>
 *   <li>✅ Schema 검증 - SchemaValidator Port 활용</li>
 *   <li>✅ Domain 규칙 강제 - Setting Aggregate 메서드 사용</li>
 *   <li>✅ Assembler 사용 - Domain ↔ DTO 변환</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Service
@Transactional
public class UpdateSettingService implements UpdateSettingUseCase {

    private final LoadSettingsPort loadSettingsPort;
    private final SaveSettingPort saveSettingPort;
    private final SchemaValidator schemaValidator;
    private final SettingAssembler settingAssembler;

    /**
     * UpdateSettingService 생성자.
     *
     * <p>생성자 주입을 통한 의존성 주입입니다.</p>
     *
     * @param loadSettingsPort Load Settings Port (Query 전용 Outbound Port)
     * @param saveSettingPort Save Setting Port (Command 전용 Outbound Port)
     * @param schemaValidator Schema Validator Port
     * @param settingAssembler Setting Assembler
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public UpdateSettingService(
        LoadSettingsPort loadSettingsPort,
        SaveSettingPort saveSettingPort,
        SchemaValidator schemaValidator,
        SettingAssembler settingAssembler
    ) {
        this.loadSettingsPort = loadSettingsPort;
        this.saveSettingPort = saveSettingPort;
        this.schemaValidator = schemaValidator;
        this.settingAssembler = settingAssembler;
    }

    /**
     * 설정을 업데이트합니다.
     *
     * <p>업데이트 절차:</p>
     * <ol>
     *   <li>Command → Domain Value Object 변환</li>
     *   <li>기존 Setting 조회</li>
     *   <li>JSON 스키마 검증 (필요 시)</li>
     *   <li>Setting 업데이트 (Domain 메서드)</li>
     *   <li>Repository 저장</li>
     *   <li>Assembler를 통한 Response 변환</li>
     * </ol>
     *
     * <p>쓰기 트랜잭션으로 실행됩니다.</p>
     *
     * @param command 업데이트 Command
     * @return 업데이트된 Setting Response
     * @throws SettingNotFoundException Setting을 찾을 수 없는 경우
     * @throws InvalidSettingException 스키마 검증 실패 시
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public Response execute(Command command) {
        // 1. Command → Domain Value Object 변환
        SettingKey key = SettingKey.of(command.key());
        SettingLevel level = SettingLevel.valueOf(command.level().toUpperCase());
        Long contextId = command.contextId();

        // 2. 기존 Setting 조회 (Query Port 사용)
        Setting setting = loadSettingsPort.findByKeyAndLevel(key, level, contextId)
            .orElseThrow(() -> SettingNotFoundException.withKeyAndLevel(key, level, contextId));

        // 3. 새로운 값 생성 및 검증
        SettingType type = setting.getValueType();
        validateValue(command.value(), type);

        SettingValue newValue = setting.isSecret()
            ? SettingValue.secret(command.value(), type)
            : SettingValue.of(command.value(), type);

        // 4. Domain 메서드를 통한 업데이트 (비즈니스 규칙 강제)
        setting.updateValue(newValue);

        // 5. Repository 저장 (Command Port 사용)
        Setting savedSetting = saveSettingPort.save(setting);

        // 6. Assembler를 통한 Response 변환
        return settingAssembler.toUpdateResponse(savedSetting);
    }

    /**
     * 설정 값을 검증합니다.
     *
     * <p>JSON 타입의 경우 SchemaValidator를 통해 검증합니다.</p>
     *
     * @param value 설정 값
     * @param type 설정 타입
     * @throws InvalidSettingException 검증 실패 시
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private void validateValue(String value, SettingType type) {
        boolean isValid = schemaValidator.validate(value, type);
        if (!isValid) {
            throw new InvalidSettingException(
                "설정 값이 타입과 호환되지 않습니다. 타입: " + type + ", 값: " + value
            );
        }
    }
}
