package mutsa.common;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mutsa.common.domain.models.article.Article;
import mutsa.common.domain.models.order.Order;
import mutsa.common.domain.models.payment.PayType;
import mutsa.common.domain.models.payment.Payment;
import mutsa.common.domain.models.report.Report;
import mutsa.common.domain.models.review.Review;
import mutsa.common.domain.models.user.*;
import mutsa.common.repository.article.ArticleRepository;
import mutsa.common.repository.order.OrderRepository;
import mutsa.common.repository.payment.PaymentRepository;
import mutsa.common.repository.report.ReportRepository;
import mutsa.common.repository.review.ReviewRepository;
import mutsa.common.repository.user.AuthorityRepository;
import mutsa.common.repository.user.RoleRepository;
import mutsa.common.repository.user.UserRepository;
import mutsa.common.repository.user.UserRoleRepository;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class BootstrapDataLoader {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthorityRepository authorityRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ArticleRepository articleRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final PaymentRepository paymentRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public void createAdminUser() {
        createRoleAuthority();

        Map<String, Object> admin = new HashMap<>();
        admin.put("id", 1);
        admin.put("login", "admin");
        admin.put("password", "admin1234");
        admin.put("email", "mutsaproject@gmail.com");
        admin.put("image_url", "");
        admin.put("role", RoleStatus.ROLE_ADMIN);
        admin.put("nickname", "admin_nick");

        loadUser(admin);
    }

    /**
     * Authority 규칙
     * 1. {1}.{2}
     * 2. {1}에는 api의 도메인을 의미
     * 3. {2}에는 사용되는 형태에 따라 작성
     */
    private void createRoleAuthority() {
        /**
         * new authority saves
         */
        log.info("------------------------ User Authority ------------------------");
        Authority createAuthority = saveAuthority("user.create");
        Authority updateAuthority = saveAuthority("user.update");
        Authority deleteAuthority = saveAuthority("user.delete");
        Authority readAuthority = saveAuthority("user.read");

        log.info("------------------------ Article Authority ------------------------");
        Authority createArticle = saveAuthority("article.create");
        Authority updateArticle = saveAuthority("article.update");
        Authority deleteArticle = saveAuthority("article.delete");
        Authority readArticle = saveAuthority("article.read");

        log.info("------------------------ order Authority ------------------------");
        Authority createOrder = saveAuthority("order.create");
        Authority updateOrder = saveAuthority("order.update");
        Authority deleteOrder = saveAuthority("order.delete");
        Authority readOrder = saveAuthority("order.read");

        log.info("------------------------ review Authority ------------------------");
        Authority createReview = saveAuthority("review.create");
        Authority updateReview = saveAuthority("review.update");
        Authority deleteReview = saveAuthority("review.delete");
        Authority readReview = saveAuthority("review.read");


        log.info("------------------------ report Authority ------------------------");
        Authority createReport = saveAuthority("report.create");
        Authority updateReport = saveAuthority("report.update");
        Authority deleteReport = saveAuthority("report.delete");
        Authority readReport = saveAuthority("report.read");

        Role userRole = saveRole(RoleStatus.ROLE_USER);
        Role adminRole = saveRole(RoleStatus.ROLE_ADMIN);

        userRole.getAuthorities().clear();
        userRole.addAuthorities(createAuthority, updateAuthority, deleteAuthority, readAuthority,
                createArticle, updateArticle, readArticle, deleteArticle,
                createReport, updateReport, deleteReport, readReport,
                createOrder, updateOrder, deleteOrder, readOrder,
                createReview, updateReview, deleteReview, readReview);

        adminRole.getAuthorities().clear();
        adminRole.addAuthorities(createAuthority, updateAuthority, deleteAuthority, readAuthority,
                createArticle, updateArticle, readArticle, deleteArticle,
                createReport, updateReport, deleteReport, readReport,
                createOrder, updateOrder, deleteOrder, readOrder,
                createReview, updateReview, deleteReview, readReview);

        roleRepository.saveAll(Arrays.asList(userRole, adminRole));
    }

    private User loadUser(Map<String, Object> attributes) {
        String apiId = ((Integer) attributes.get("id")).toString();
        String login = (String) attributes.get("login");
        String email = (String) attributes.get("email");
        String imageUrl = (String) attributes.get("image_url");
        String password = (String) attributes.get("password");
        String nickname = (String) attributes.get("nickname");

        RoleStatus role = (RoleStatus) attributes.get("role");
        HashMap<String, Object> necessaryAttributes = createNecessaryAttributes(apiId, login,
                email, imageUrl);

        String username = login;
        Optional<User> userOptional = userRepository.findByUsername(username);
        return signUpOrUpdateUser(login, email, imageUrl, username, password, userOptional,
                necessaryAttributes, role,nickname);
    }

    private User signUpOrUpdateUser(String login, String email, String imageUrl, String username, String password,
                                    Optional<User> userOptional, Map<String, Object> necessaryAttributes, RoleStatus roleEnum,String nickname) {
        User user;
        //회원가입
        if (userOptional.isEmpty()) {
            Role role = roleRepository.findByValue(roleEnum).orElseThrow(() ->
                    new EntityNotFoundException(roleEnum + "에 해당하는 Role이 없습니다."));
            user = User.of(username, bCryptPasswordEncoder.encode(password), email, login, imageUrl, nickname);
            UserRole userRole = UserRole.of(user, role);

            userRepository.save(user);
            userRoleRepository.save(userRole);
            necessaryAttributes.put("create_flag", true);
        } else {
            user = userOptional.get();
            necessaryAttributes.put("create_flag", false);
        }
        return user;
    }

    private HashMap<String, Object> createNecessaryAttributes(String apiId, String login,
                                                              String email, String imageUrl) {
        HashMap<String, Object> necessaryAttributes = new HashMap<>();
        necessaryAttributes.put("id", apiId);
        necessaryAttributes.put("login", login);
        necessaryAttributes.put("email", email);
        necessaryAttributes.put("image_url", imageUrl);
        return necessaryAttributes;
    }

    private Authority saveAuthority(String name) {
        return authorityRepository.save(Authority.of(name));
    }

    private Role saveRole(RoleStatus roleStatus) {
        return roleRepository.save(Role.of(roleStatus));
    }

    public void createAricleOrder() {
        User user1 = createTestUser1();
        User user2 = createTestUser2();

        List<Article> articles = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            Article article = Article.builder()
                    .title("title-" + (i + 1))
                    .description("desc-" + (i + 1))
                    .user(i % 2 == 0 ? user1 : user2)
                    .price(i * 2000L)
                    .build();

            articles.add(article);
        }

        articles = articleRepository.saveAll(articles);

        List<Order> orders = new ArrayList<>();
        List<Payment> payments = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Order order = Order.of(articles.get(i), i % 2 == 0 ? user2 : user1);
            Payment payment = Payment.of(PayType.CARD, articles.get(i), order);
            payment.setOrder(order);
            orders.add(order);
            payments.add(payment);
        }
        orderRepository.saveAll(orders);
        paymentRepository.saveAll(payments);

        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            reviews.add(reviewRepository.save(Review.of(i % 2 == 0 ? user1 : user2, articles.get(i), "testContent" + (i + 1), (int) (Math.random() * 5 + 1))));
        }
        for (int i = 0; i < 11; i++) {
            reviews.add(reviewRepository.save(Review.of(user1, articles.get(0), "testContent" + (i + 1), (int) (Math.random() * 5 + 1))));
        }
        reviewRepository.saveAll(reviews);
    }

    public User createTestUser1() {
        Map<String, Object> testUser1 = new HashMap<>();
        testUser1.put("id", 2);
        testUser1.put("login", "qwer");
        testUser1.put("password", "qwer");
        testUser1.put("email", "qwer@gmail.com");
        testUser1.put("image_url", "");
        testUser1.put("role", RoleStatus.ROLE_USER);
        testUser1.put("nickname", "test-user1");

        return loadUser(testUser1);
    }

    public User createTestUser2() {
        Map<String, Object> testUser2 = new HashMap<>();
        testUser2.put("id", 3);
        testUser2.put("login", "asdf");
        testUser2.put("password", "asdf");
        testUser2.put("email", "asdf@gmail.com");
        testUser2.put("image_url", "");
        testUser2.put("role", RoleStatus.ROLE_USER);
        testUser2.put("nickname", "test-user2");

        return loadUser(testUser2);
    }

    public void createReport() {
        User reporter1 = User.of(
                "ReportControllerTestUser1",
                bCryptPasswordEncoder.encode("test"),
                "reporter1@gmail.com",
                null,
                null,
                "ReportControllerTestUser1-nick"
        );
        reporter1 = userRepository.save(reporter1);

        User reportedUser = User.of(
                "ReportControllerTestUser2",
                bCryptPasswordEncoder.encode("test"),
                "reportedUser@gmail.com",
                null,
                null,
                "ReportControllerTestUser2-nick"
        );
        reportedUser = userRepository.save(reportedUser);

        List<Report> reports = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Report report = Report.of(
                    (i % 2 == 0 ? reporter1 : reportedUser),
                    reportedUser, "Report-content-" + (i + 1)
            );
            reports.add(report);
        }

        reports = reportRepository.saveAll(reports);
    }

    public void clearRedisData() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.flushAll();
            return null;
        });
    }
}
