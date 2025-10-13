package com.ryuqq.fileflow.adapter.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAPI Configuration Properties
 *
 * OpenAPI 설정을 위한 Properties 클래스
 * application.yml에서 api.* 프로퍼티를 바인딩합니다.
 *
 * 제약사항:
 * - NO Lombok
 * - NO Inner Class (Contact, Server는 별도 클래스)
 *
 * @author sangwon-ryu
 */
@ConfigurationProperties(prefix = "api")
public class OpenApiProperties {

    private Contact contact = new Contact();
    private Server server = new Server();

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * Contact 정보 설정
     */
    public static class Contact {
        private String email = "fileflow@example.com";
        private String url = "https://github.com/your-org/fileflow";

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    /**
     * Server URL 설정
     */
    public static class Server {
        private String devUrl = "";
        private String prodUrl = "";

        public String getDevUrl() {
            return devUrl;
        }

        public void setDevUrl(String devUrl) {
            this.devUrl = devUrl;
        }

        public String getProdUrl() {
            return prodUrl;
        }

        public void setProdUrl(String prodUrl) {
            this.prodUrl = prodUrl;
        }
    }
}
