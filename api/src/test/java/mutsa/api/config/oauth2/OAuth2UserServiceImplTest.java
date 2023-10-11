package mutsa.api.config.oauth2;

import lombok.extern.slf4j.Slf4j;
import mutsa.api.ApiApplication;
import mutsa.api.config.TestBootstrapConfig;
import mutsa.api.config.TestRedisConfiguration;
import mutsa.api.config.security.OAuth2SuccessHandler;
import mutsa.common.domain.models.user.User;
import mutsa.common.domain.models.user.embedded.OAuth2Type;
import mutsa.common.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ApiApplication.class, TestRedisConfiguration.class, TestBootstrapConfig.class})
@Transactional
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Slf4j
class OAuth2UserServiceImplTest {

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;
    @Mock
    private OAuth2User oAuth2User;
    @Mock
    private Authentication authentication;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void testOnAuthenticationSuccess() throws Exception {
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("provider")).thenReturn("Google");
        when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
        when(oAuth2User.getAttribute("nickname")).thenReturn("TestUser");
        when(oAuth2User.getAttribute("picture")).thenReturn("profile-picture-url");

        MockHttpServletResponse response = new MockHttpServletResponse();
        oAuth2SuccessHandler.onAuthenticationSuccess(new MockHttpServletRequest("get", "/test"), response, authentication);

        log.info("response : {}", response.getRedirectedUrl());
        log.info("SIZE : {}", userRepository.findAll().size());
        Optional<User> users = userRepository.findByUsername("test");

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(users).isNotEmpty();
        User expect = users.get();
        assertThat(expect.getEmail()).isEqualTo("test@example.com");
        assertThat(expect.getIsOAuth2()).isEqualTo(true);
        assertThat(expect.getIsAvailable()).isEqualTo(false);
        assertThat(expect.getOAuth2Type()).isEqualTo(OAuth2Type.GOOGLE);
        assertThat(expect.getImageUrl()).isEqualTo("profile-picture-url");
        assertThat(expect.getNickname()).isEqualTo("TestUser");
    }
}