package mutsa.api.config.oauth2;

import mutsa.common.domain.models.user.embedded.OAuth2Type;

import java.util.Map;

public class NaverOAuth2UserInfo extends OAuth2UserInfo {

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        super((Map<String, Object>) attributes.get("response"));
    }


    @Override
    public String getProvider() {
        return OAuth2Type.NAVER.name();
    }

    @Override
    public String getId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("nickname");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("profile_image");
    }

}