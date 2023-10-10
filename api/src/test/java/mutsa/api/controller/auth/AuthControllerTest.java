package mutsa.api.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import mutsa.api.ApiApplication;
import mutsa.api.config.TestBootstrapConfig;
import mutsa.api.config.TestRedisConfiguration;
import mutsa.api.dto.auth.LoginRequest;
import mutsa.api.dto.user.SignUpUserDto;
import mutsa.api.util.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
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
@Transactional
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

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
}