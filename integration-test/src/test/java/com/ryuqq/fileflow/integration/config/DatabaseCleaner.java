package com.ryuqq.fileflow.integration.config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 테스트 격리를 위한 데이터베이스 정리 컴포넌트.
 *
 * TestRestTemplate은 별도의 스레드에서 실행되므로 @Transactional 롤백이 작동하지 않습니다.
 * 따라서 각 테스트 전에 TRUNCATE를 사용하여 모든 테이블을 정리합니다.
 *
 * 사용법:
 * @BeforeEach
 * void setUp() {
 *     databaseCleaner.clean();
 * }
 */
@Component
public class DatabaseCleaner {

    @PersistenceContext
    private EntityManager entityManager;

    private List<String> tableNames;

    @PostConstruct
    public void init() {
        Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
        tableNames = new ArrayList<>();

        for (EntityType<?> entity : entities) {
            Class<?> javaType = entity.getJavaType();
            jakarta.persistence.Table tableAnnotation = javaType.getAnnotation(jakarta.persistence.Table.class);

            if (tableAnnotation != null && !tableAnnotation.name().isBlank()) {
                tableNames.add(tableAnnotation.name());
            } else {
                tableNames.add(convertToSnakeCase(entity.getName()));
            }
        }
    }

    @Transactional
    public void clean() {
        entityManager.flush();

        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        for (String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }

        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    private String convertToSnakeCase(String camelCase) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
