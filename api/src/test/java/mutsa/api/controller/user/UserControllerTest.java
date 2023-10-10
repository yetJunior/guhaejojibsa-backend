package mutsa.api.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mutsa.api.dto.user.EmailChangeDto;
import mutsa.api.dto.user.Oauth2InfoUserDto;
import mutsa.api.dto.user.PasswordChangeDto;
import mutsa.api.dto.user.ProfileChangeDto;
import mutsa.api.service.user.UserService;
import mutsa.api.util.SecurityUtil;
import mutsa.common.domain.models.user.User;
import mutsa.common.domain.models.user.embedded.Address;
import mutsa.common.repository.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Slf4j
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private static MockedStatic<SecurityUtil> securityUtilMockedStatic;

    private User user;

    @BeforeAll
    public static void beforeAll() {
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
    }

    @AfterAll
    public static void afterAll() {
        securityUtilMockedStatic.close();
    }

    @BeforeEach
    public void init() {
        user = User.of("user1", bCryptPasswordEncoder.encode("password"), "email1@", "oauthName1", "/test.img", "user1");
        user = userRepository.save(user);
        user.addAddress(Address.of("zip", "city", "street"));

    }

    @AfterEach
    public void tearDown() {
        // Redis 데이터 삭제
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    void modifyPassword() throws Exception {
        //given
        when(SecurityUtil.getCurrentUsername()).thenReturn(user.getUsername());

        String newPassword = "newPass";
        String body = new ObjectMapper().writeValueAsString(new PasswordChangeDto("password", newPassword, newPassword));

        //when
        mockMvc.perform(patch("/api/user/password")
                        .content(body)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        //then
        Assertions.assertThat(bCryptPasswordEncoder.matches(newPassword, user.getPassword()));
    }

    @Test
    void modifyEmail() throws Exception {
        //given
        when(SecurityUtil.getCurrentUsername()).thenReturn(user.getUsername());

        String newEmail = "newEmail@naver.com";
        String body = new ObjectMapper().writeValueAsString(new EmailChangeDto(newEmail));

        //when
        mockMvc.perform(patch("/api/user/email")
                        .content(body)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        //then
        Assertions.assertThat(user.getEmail()).isEqualTo(newEmail);
    }

    @Test
    void modifyAddress() throws Exception {
        //given
        when(SecurityUtil.getCurrentUsername()).thenReturn(user.getUsername());

        Address address = Address.of("test", "test", "test");
        String body = new ObjectMapper().writeValueAsString(address);

        //when
        mockMvc.perform(patch("/api/user/address")
                        .content(body)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        //then
        Assertions.assertThat(user.getAddress()).isEqualTo(address);
    }


    @Test
    void modifyImageUrl() throws Exception {
        //given
        when(SecurityUtil.getCurrentUsername()).thenReturn(user.getUsername());

        ProfileChangeDto testUrl = new ProfileChangeDto("testUrl");
        String body = new ObjectMapper().writeValueAsString(testUrl);

        //when
        mockMvc.perform(patch("/api/user/image")
                        .content(body)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        //then
        Assertions.assertThat(user.getImageUrl()).isEqualTo(testUrl.getImageUrl());
    }


    @Test
    void readUserInfo() throws Exception {
        //given
        when(SecurityUtil.getCurrentUsername()).thenReturn(user.getUsername());

        //when, then
        mockMvc.perform(get("/api/user/info")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.apiId").value(user.getApiId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.nickname").value(user.getNickname()))
                .andExpect(jsonPath("$.image_url").value(user.getImageUrl()))
                .andExpect(jsonPath("$.role").exists())
                .andExpect(jsonPath("$.zipcode").value(user.getAddress().getZipcode()))
                .andExpect(jsonPath("$.city").value(user.getAddress().getCity()))
                .andExpect(jsonPath("$.street").value(user.getAddress().getStreet()));

    }


    @Test
    void oauthSignupTest() throws Exception {
        //given
        when(SecurityUtil.getCurrentUsername()).thenReturn(user.getUsername());
        Oauth2InfoUserDto oauth2InfoUserDto = new Oauth2InfoUserDto("010-1234-1234", "test", "test", "test");
        String body = new ObjectMapper().writeValueAsString(oauth2InfoUserDto);


        //when,
        mockMvc.perform(post("/api/user/oauth/signup")
                        .content(body)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        //then
        assertThat(user.getAddress().getZipcode()).isEqualTo("test");
        assertThat(user.getAddress().getStreet()).isEqualTo("test");
        assertThat(user.getAddress().getCity()).isEqualTo("test");
    }

}