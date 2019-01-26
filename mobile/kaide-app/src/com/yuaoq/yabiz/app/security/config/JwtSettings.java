package com.yuaoq.yabiz.app.security.config;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Configuration;

public class JwtSettings {
    /**
     * {@link JwtToken} will expire after this time.
     */
    @Value("${app.security.jwt.tokenExpirationTime}")
    private Integer tokenExpirationTime;

    /**
     * Token issuer.
     */
    @Value("${app.security.jwt.tokenIssuer}")
    private String tokenIssuer;
    
    /**
     * Key is used to sign {@link JwtToken}.
     */
    @Value("${app.security.jwt.tokenSigningKey}")
    private String tokenSigningKey;
    
    /**
     * {@link JwtToken} can be refreshed during this timeframe.
     */
    @Value("${app.security.jwt.refreshTokenExpTime}")
    private Integer refreshTokenExpTime;
    
    public Integer getRefreshTokenExpTime() {
        return refreshTokenExpTime;
    }

    public void setRefreshTokenExpTime(Integer refreshTokenExpTime) {
        this.refreshTokenExpTime = refreshTokenExpTime;
    }

    public Integer getTokenExpirationTime() {
        return tokenExpirationTime;
    }
    
    public void setTokenExpirationTime(Integer tokenExpirationTime) {
        this.tokenExpirationTime = tokenExpirationTime;
    }
    
    public String getTokenIssuer() {
        return tokenIssuer;
    }
    public void setTokenIssuer(String tokenIssuer) {
        this.tokenIssuer = tokenIssuer;
    }
    
    public String getTokenSigningKey() {
        return tokenSigningKey;
    }
    
    public void setTokenSigningKey(String tokenSigningKey) {
        this.tokenSigningKey = tokenSigningKey;
    }
}
