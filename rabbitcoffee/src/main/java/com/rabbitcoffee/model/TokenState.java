package com.rabbitcoffee.model;

public class TokenState {
    private String accessToken;
    private Long expiresIn;

    public TokenState() {
        this.accessToken = null;
        this.expiresIn = null;
    }

    public TokenState(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expires_in) {
        this.expiresIn = expires_in;
    }
}