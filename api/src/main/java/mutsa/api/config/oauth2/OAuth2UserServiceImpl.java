package mutsa.api.config.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                userRequest.getClientRegistration().getRegistrationId(),
                super.loadUser(userRequest).getAttributes()
        );

        // 사용할 데이터를 다시 정리하는 목적의 Map
        Map<String, Object> attributes = new HashMap<>();

        //전달 객체로 정리해서 반환
        attributes.put("provider", oAuth2UserInfo.getProvider());
        attributes.put("id", oAuth2UserInfo.getId());
        attributes.put("email", oAuth2UserInfo.getEmail());
        attributes.put("nickname", oAuth2UserInfo.getName());
        attributes.put("picture", oAuth2UserInfo.getImageUrl());

        log.info(attributes.toString());

        return new DefaultOAuth2User(
                null,
                attributes,
                "email"
        );
    }
}
