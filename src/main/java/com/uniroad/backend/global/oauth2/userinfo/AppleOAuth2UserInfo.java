package com.uniroad.backend.global.oauth2.userinfo;

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
    public String getName() {
        // Apple only provides name on the first login via user info.
        // We will fallback to email or null if not provided.
        if (attributes.containsKey("name")) {
            return (String) attributes.get("name");
        }
        return "Apple User";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getProfileImageUrl() {
        return null; // Apple does not provide profile image
    }
}
