package mutsa.api.service.user;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import mutsa.api.ApiApplication;
import mutsa.api.config.TestRedisConfiguration;
import mutsa.api.dto.user.SignUpOAuth2UserDto;
import mutsa.api.dto.user.SignUpUserDto;
import mutsa.common.domain.models.user.User;
import mutsa.common.domain.models.user.embedded.OAuth2Type;
import mutsa.common.exception.BusinessException;
import mutsa.common.repository.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static mutsa.common.exception.ErrorCode.DUPLICATION_USER;

@SpringBootTest(classes = {ApiApplication.class, TestRedisConfiguration.class})
@ActiveProfiles("test")
@Transactional
@Slf4j
class UserServiceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisTemplate<String, User> userRedisTemplate;
    @Autowired
    private UserService userService;
    private User testUser;

    @BeforeEach
    public void init() {
        testUser = User.of("user1", "password", "email1@", "oauthName1", null, "user1");
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    public void tearDown() {
        // Redis 데이터 삭제
        userRedisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("중복된 이름으로 회원가입 하는 경우-일반")
    void signUp() {
        //then
        Assertions.assertThatThrownBy(()
                        -> userService.signUp(new SignUpUserDto("user1", "pass", "pass", "nick", "email", "010-1234-5677", "street", "zipcode", "city")))
                .isInstanceOf(BusinessException.class).hasMessage(DUPLICATION_USER.getMessage());

    }


    @Test
    @DisplayName("중복된 이름으로 회원가입 하는 경우-oauth")
    void signUp2() {
        //then
        Assertions.assertThatThrownBy(()
                        -> userService.signUp(new SignUpOAuth2UserDto("user1", "pass", "nick", "email", "010-1234-5677", "street", "zipcode", OAuth2Type.GOOGLE)))
                .isInstanceOf(BusinessException.class).hasMessage(DUPLICATION_USER.getMessage());

    }


    @Test
    void isDuplicateEmail() {
        //when
        boolean duplicateEmail = userService.isDuplicateEmail(testUser.getEmail());

        //then
        Assertions.assertThat(duplicateEmail).isTrue();
    }

    @Test
    void isDuplicateEmail2() {
        //when
        boolean duplicateEmail = userService.isDuplicateEmail("new@email.com");

        //then
        Assertions.assertThat(duplicateEmail).isFalse();
    }

    @Test
    void isAvailableUser() {
        //when
        boolean availableUser = userService.isAvailableUser(testUser.getEmail());

        //then
        Assertions.assertThat(availableUser).isFalse();
    }

    @Test
    void isAvailableUser2() {
        //when
        boolean availableUser = userService.isAvailableUser("new@email.com");

        //then
        Assertions.assertThat(availableUser).isTrue();
    }

}