package mutsa.api.config.oauth2;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import mutsa.api.ApiApplication;
import mutsa.api.config.TestRedisConfiguration;
import mutsa.common.domain.models.user.embedded.OAuth2Type;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {ApiApplication.class, TestRedisConfiguration.class})
@ActiveProfiles("test")
@Transactional
@Slf4j
class OAuth2UserInfoFactoryTest {

    @Test
    public void test_google() {
        //when
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("google", getGoogleUserInfo());

        //then
        log.info("{}",userInfo);
        assertThat(userInfo.getProvider()).isEqualTo(OAuth2Type.GOOGLE.toString());
        assertThat(userInfo.getEmail()).isEqualTo("test@example.com");
        assertThat(userInfo.getImageUrl()).isEqualTo("test-img-picture");
    }


    @Test
    public void test_naver() {
        //when
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("naver", getNaverUserInfo());

        //then
        log.info("{}",userInfo);
        assertThat(userInfo.getProvider()).isEqualTo(OAuth2Type.NAVER.toString());
        assertThat(userInfo.getEmail()).isEqualTo("test@example.com");
        assertThat(userInfo.getImageUrl()).isEqualTo("test-img-picture");
    }

    private HashMap<String, Object> getGoogleUserInfo() {
        HashMap<String, Object> attribute = new HashMap<>();
        attribute.put("sub", "12345");
        attribute.put("email", "test@example.com");
        attribute.put("name", "Test User");
        attribute.put("picture", "test-img-picture");
        return attribute;
    }

    private HashMap<String, Object> getNaverUserInfo() {
        HashMap<String, Object> attribute = new HashMap<>();
        attribute.put("id", "12345");
        attribute.put("email", "test@example.com");
        attribute.put("nickname", "Test User");
        attribute.put("profile_image", "test-img-picture");

        HashMap<String, Object> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("response", attribute);
        return stringStringHashMap;
    }
}