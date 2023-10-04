package mutsa.api.service.review;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import mutsa.api.ApiApplication;
import mutsa.api.config.TestRedisConfiguration;
import mutsa.api.dto.review.ReviewRequestDto;
import mutsa.api.dto.review.ReviewResponseDto;
import mutsa.api.util.SecurityUtil;
import mutsa.common.domain.models.article.Article;
import mutsa.common.domain.models.order.OrderStatus;
import mutsa.common.domain.models.review.Review;
import mutsa.common.domain.models.user.User;
import mutsa.common.domain.models.order.Order;
import mutsa.common.repository.article.ArticleRepository;
import mutsa.common.repository.order.OrderRepository;
import mutsa.common.repository.review.ReviewRepository;
import mutsa.common.repository.user.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ApiApplication.class, TestRedisConfiguration.class})
@ActiveProfiles("test")
@Transactional
@Slf4j
public class ReviewServiceTest {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private RedisTemplate<String, User> userRedisTemplate;

    private User reviewer1, reviewer2, reviewer3, reviewer4;
    private Article article;
    private Order order;
    private static MockedStatic<SecurityUtil> securityUtilMockedStatic;

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
        reviewer1 = User.of("user1", "password", "email1@", "oauthName1", "", "user1");
        reviewer1 = userRepository.save(reviewer1);

        reviewer2 = User.of("user2", "password", "email2@", "oauthName2", "", "user2");
        reviewer2 = userRepository.save(reviewer2);

        reviewer3 = User.of("user3", "password", "email3@", "oauthName3", "", "user3");
        reviewer3 = userRepository.save(reviewer3);

        reviewer4 = User.of("user4", "password", "email4@", "oauthName4", "", "user4");
        reviewer4 = userRepository.save(reviewer4);

        User seller = User.of("seller", "password", "sellerEmail@", "sellerOauthName", "", "seller");
        seller = userRepository.save(seller);

        article = Article.builder()
                .title("Pre Article 1")
                .description("Pre Article 1 desc")
                .user(seller)
                .build();
        article = articleRepository.save(article);

        order = Order.of(article, reviewer1);
        order.setOrderStatus(OrderStatus.END);
        order = orderRepository.save(order);
    }

    @AfterEach
    public void tearDown() {
        // Redis 데이터 삭제
        Objects.requireNonNull(userRedisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    @DisplayName("후기 생성 서비스 테스트")
    @Test
    void createReview() {
        // given
        ReviewRequestDto requestDto = new ReviewRequestDto();
        requestDto.setContent("test Review");
        requestDto.setPoint(5);

        // when
        ReviewResponseDto responseDto
                = reviewService.createReview(article.getApiId(), order.getApiId(),
                reviewer1.getUsername(), requestDto);

        // then
        assertThat(responseDto.getUsername()).isEqualTo(reviewer1.getUsername());
    }

    @DisplayName("후기 단일 조회 서비스 테스트")
    @Test
    void findReview() {
        // given
        Review review = Review.of(reviewer1, article, "test Review", 5);
        review = reviewRepository.save(review);

        // when
        ReviewResponseDto responseDto = reviewService.getReview(review.getApiId());

        // then
        assertThat(responseDto.getContent()).isEqualTo(review.getContent());
        assertThat(responseDto.getPoint()).isEqualTo(review.getPoint());
        assertThat(responseDto.getUsername()).isEqualTo(reviewer1.getUsername());
    }

    @DisplayName("후기 전체 조회 서비스 테스트")
    @Test
    void findAllReview() {
        // given
        reviewRepository.save(Review.of(reviewer1, article, "content1", 1));
        reviewRepository.save(Review.of(reviewer2, article, "content2", 2));
        reviewRepository.save(Review.of(reviewer3, article, "content3", 3));
        reviewRepository.save(Review.of(reviewer4, article, "content4", 4));

        // when
        Page<ReviewResponseDto> allReviews = reviewService.findAllReview(article.getApiId(), 1, 20, "descByDate");

        // then
        log.info(allReviews.getContent().toString());
        assertThat(allReviews.getTotalPages()).isEqualTo(1);
        assertThat(allReviews.getTotalElements()).isEqualTo(4);
    }

    @DisplayName("후기 수정 서비스 테스트")
    @Test
    void updateReview() {
        // given
        Review review = Review.of(reviewer1, article, "test Review", 5);

        ReviewRequestDto updateDto = new ReviewRequestDto();
        updateDto.setContent("reviewUpdate test");
        updateDto.setPoint(3);

        // when
        ReviewResponseDto responseDto = reviewService.updateReview(review.getApiId(),
                reviewer1.getUsername(), updateDto);

        // then
        assertThat(updateDto.getContent()).isEqualTo(responseDto.getContent());
        assertThat(updateDto.getPoint()).isEqualTo(responseDto.getPoint());
    }

    @DisplayName("후기 삭제 서비스 테스트")
    @Test
    void deleteReview() {
        // given
        Review review = reviewRepository.save(Review.of(reviewer1, article, "content1", 1));
        entityManager.flush();
        entityManager.clear();

        // when
        when(SecurityUtil.getCurrentUsername()).thenReturn(reviewer1.getUsername());
        reviewService.deleteReview(review.getApiId(), reviewer1.getUsername());
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Review> deletedReview = reviewRepository.findByApiId(review.getApiId());
        assertThat(deletedReview.isPresent()).isFalse();
    }
}

