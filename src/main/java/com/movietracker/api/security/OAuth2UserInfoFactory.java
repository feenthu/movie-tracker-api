package com.movietracker.api.security;

import com.movietracker.api.exception.AuthenticationException;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google")) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("facebook")) {
            return new FacebookOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("apple")) {
            return new AppleOAuth2UserInfo(attributes);
        } else {
            throw new AuthenticationException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}