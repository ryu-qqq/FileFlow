package com.ryuqq.fileflow.domain.session.vo;

/**
 * 파일 업로드 세션을 위한 사용자 Role 정의.
 */
public enum UserRole {

    ADMIN("connectly"),
    SELLER("setof"),
    DEFAULT("setof");

    private final String namespace;

    UserRole(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Role별 네임스페이스 반환.
     *
     * @return 네임스페이스 문자열
     */
    public String namespace() {
        return namespace;
    }

    public String getNamespace() {
        return namespace();
    }
}

