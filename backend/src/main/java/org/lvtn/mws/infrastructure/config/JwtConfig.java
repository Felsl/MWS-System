package org.lvtn.mws.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;
    private long expirationMs;
    private long refreshExpirationDays;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getExpirationMs() { return expirationMs; }
    public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
    public long getRefreshExpirationDays() { return refreshExpirationDays; }
    public void setRefreshExpirationDays(long refreshExpirationDays) { this.refreshExpirationDays = refreshExpirationDays; }
}
