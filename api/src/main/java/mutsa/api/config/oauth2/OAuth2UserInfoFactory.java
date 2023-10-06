package mutsa.api.config.oauth2;

import lombok.extern.slf4j.Slf4j;
import mutsa.common.domain.models.user.embedded.OAuth2Type;
import mutsa.common.exception.BusinessException;
import mutsa.common.exception.ErrorCode;

import java.util.Map;

@Slf4j
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(OAuth2Type.GOOGLE.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(OAuth2Type.NAVER.toString())) {
            return new NaverOAuth2UserInfo(attributes);
        } else {
            log.info("Unsupported Login Type : {}",registrationId);
            throw new BusinessException(ErrorCode.UNKNOWN_OAUTH2_TYPE);
        }
    }

}