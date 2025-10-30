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
     * 설정을 업데이트합니다 (Upsert: 없으면 생성, 있으면 수정).
     *
     * <p><strong>Upsert 동작:</strong></p>
     * <ul>
     *   <li>기존 설정이 있으면 → 기존 값을 업데이트</li>
     *   <li>기존 설정이 없으면 → 새로운 설정 생성 (secret=false, type=STRING 기본값)</li>
     * </ul>
     *
     * <p><strong>실행 절차:</strong></p>
     * <ol>
     *   <li>Command → Domain Value Object 변환</li>
     *   <li>기존 Setting 조회</li>
     *   <li>있으면 → 기존 설정 수정 (기존 type과 secret 유지)</li>
     *   <li>없으면 → 새 설정 생성 (secret=false, type=STRING)</li>
     *   <li>JSON 스키마 검증 (필요 시)</li>
     *   <li>Repository 저장</li>
     *   <li>Assembler를 통한 Response 변환</li>
     * </ol>
     *
     * <p>쓰기 트랜잭션으로 실행됩니다.</p>
     *
     * @param command 업데이트 Command
     * @return 업데이트된 Setting Response (생성 또는 수정)
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
        java.util.Optional<Setting> existingSetting = loadSettingsPort.findByKeyAndLevel(key, level, contextId);

        Setting savedSetting;

        if (existingSetting.isPresent()) {
            // 3-A. 기존 설정이 있으면 수정
            Setting setting = existingSetting.get();
            SettingType type = setting.getValueType();
            validateValue(command.value(), type);

            SettingValue newValue = setting.isSecret()
                ? SettingValue.secret(command.value(), type)
                : SettingValue.of(command.value(), type);

            setting.updateValue(newValue);
            savedSetting = saveSettingPort.save(setting);
        } else {
            // 3-B. 기존 설정이 없으면 생성 (type은 STRING 기본값, secret은 키 패턴으로 자동 판단)
            SettingType type = SettingType.STRING;
            validateValue(command.value(), type);

            // 키 패턴에 따라 자동으로 secret 여부 판단
            SettingValue value = key.isSecretKey()
                ? SettingValue.secret(command.value(), type)
                : SettingValue.of(command.value(), type);
            Setting newSetting = Setting.forNew(key, value, level, contextId);
            savedSetting = saveSettingPort.save(newSetting);
        }

        // 4. Assembler를 통한 Response 변환
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
