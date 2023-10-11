package mutsa.api.service.user;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import mutsa.api.ApiApplication;
import mutsa.api.config.TestRedisConfiguration;
import mutsa.common.domain.models.user.User;
import mutsa.common.exception.BusinessException;
import mutsa.common.exception.ErrorCode;
import mutsa.common.repository.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

@SpringBootTest(classes = {ApiApplication.class, TestRedisConfiguration.class})
@ActiveProfiles("test")
@Transactional
@Slf4j
class UserModuleServiceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisTemplate<String, User> userRedisTemplate;
    @Autowired
    private UserModuleService userModuleService;
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
    void getByApiId() {
        //when
        User user = userModuleService.getByApiId(testUser.getApiId());

        //then
        Assertions.assertThat(testUser.getId()).isEqualTo(user.getId());
    }

    @Test
    void getById() {
        //when
        User user = userModuleService.getById(testUser.getId());

        //then
        Assertions.assertThat(testUser.getApiId()).isEqualTo(user.getApiId());
    }

    @Test
    void getByUsername() {
        //when
        User user = userModuleService.getByUsername(testUser.getUsername());

        //then
        Assertions.assertThat(testUser.getId()).isEqualTo(user.getId());
    }

    @Test
    void getByApiId_fail() {
        Assertions.assertThatThrownBy(() -> userModuleService.getByApiId(UUID.randomUUID().toString()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void getById_fail() {
        //when //then
        Assertions.assertThatThrownBy(() -> userModuleService.getById(999999999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void getByUsername_fail() {
        Assertions.assertThatThrownBy(() -> userModuleService.getByUsername("Test"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void getByEmail() {
        //when
        Optional<User> user = userModuleService.getByEmail(testUser.getEmail());

        //then
        Assertions.assertThat(user).isNotEmpty();
        Assertions.assertThat(testUser.getId()).isEqualTo(user.get().getId());
    }
}