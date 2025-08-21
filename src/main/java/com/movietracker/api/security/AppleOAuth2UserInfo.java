package com.movietracker.api.security;

import java.util.Map;

public class AppleOAuth2UserInfo extends OAuth2UserInfo {

    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getFirstName() {
        // Apple provides name in a nested structure
        Object nameObj = attributes.get("name");
        if (nameObj instanceof Map) {
            Map<String, Object> name = (Map<String, Object>) nameObj;
            return (String) name.get("firstName");
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getLastName() {
        // Apple provides name in a nested structure
        Object nameObj = attributes.get("name");
        if (nameObj instanceof Map) {
            Map<String, Object> name = (Map<String, Object>) nameObj;
            return (String) name.get("lastName");
        }
        return null;
    }
}