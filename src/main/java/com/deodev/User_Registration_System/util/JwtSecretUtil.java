package com.deodev.User_Registration_System.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtSecretUtil {

    private String secret;
    private Expiration expiration;

    @Data
    public static class Expiration {
        private int access;
        private int refresh;
    }
}
