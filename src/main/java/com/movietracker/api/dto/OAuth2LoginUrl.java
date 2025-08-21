package com.movietracker.api.dto;

public class OAuth2LoginUrl {
    private OAuth2Provider provider;
    private String loginUrl;

    public OAuth2LoginUrl() {}

    public OAuth2LoginUrl(OAuth2Provider provider, String loginUrl) {
        this.provider = provider;
        this.loginUrl = loginUrl;
    }

    public OAuth2Provider getProvider() {
        return provider;
    }

    public void setProvider(OAuth2Provider provider) {
        this.provider = provider;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }
}