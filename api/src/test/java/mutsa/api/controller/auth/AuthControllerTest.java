package mutsa.api.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mutsa.api.ApiApplication;
import mutsa.api.config.TestBootstrapConfig;
import mutsa.api.config.TestRedisConfiguration;
import mutsa.api.config.security.OAuth2SuccessHandler;
import mutsa.api.dto.auth.LoginRequest;
import mutsa.api.dto.user.SignUpUserDto;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {ApiApplication.class, TestRedisConfiguration.class, TestBootstrapConfig.class})
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Slf4j
@Transactional
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;
    @Mock
    private OAuth2User oAuth2User;
    @Mock
    private Authentication authentication;
    @Autowired
    private UserRepository userRepository;

    @Test
    void loginTest() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin1234");
        String body = new ObjectMapper().writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/auth/login", body)
                        .contentType("application/json")
                        .content(body)
                )
                .andDo(print())
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void loginTest_fail() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin11234234");
        String body = new ObjectMapper().writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/auth/login", body)
                        .contentType("application/json")
                        .content(body)
                )
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }


    @Test
    void signupTest() throws Exception {
        SignUpUserDto testDto = SignUpUserDto.builder()
                .username("testuser1")
                .password("testUser123!@#")
                .checkPassword("testUser123!@#")
                .nickname("nickname")
                .phoneNumber("010-1234-5678")
                .email("testUse122r@gmail.com")
                .build();

        String body = new ObjectMapper().writeValueAsString(testDto);

        mockMvc.perform(post("/api/auth/signup")
                        .content(body)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }


    @Test
    void signupGoogleTest() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/{registrationId}", "google"))
                .andExpect(status().is3xxRedirection()) // Redirects to OAuth provider login page
                .andExpect(header().string("Location", containsString("accounts.google.com")));
    }

    @Test
    void signupNaverTest() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/{registrationId}", "naver"))
                .andExpect(status().is3xxRedirection()) // Redirects to OAuth provider login page
                .andExpect(header().string("Location", containsString("nid.naver.com")));
    }

    @Test
    public void testOnSaveUser() throws Exception {
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