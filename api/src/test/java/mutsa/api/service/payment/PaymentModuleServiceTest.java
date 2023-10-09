package mutsa.api.service.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mutsa.api.ApiApplication;
import mutsa.api.config.TestRedisConfiguration;
import mutsa.api.config.payment.TossPaymentConfig;
import mutsa.api.dto.payment.PaymentDto;
import mutsa.api.dto.payment.PaymentSuccessCardDto;
import mutsa.api.dto.payment.PaymentSuccessDto;
import mutsa.api.util.SecurityUtil;
import mutsa.common.domain.models.article.Article;
import mutsa.common.domain.models.order.Order;
import mutsa.common.domain.models.payment.CardReceipt;
import mutsa.common.domain.models.payment.PayType;
import mutsa.common.domain.models.payment.Payment;
import mutsa.common.domain.models.payment.Receipt;
import mutsa.common.domain.models.user.User;
import mutsa.common.repository.article.ArticleRepository;
import mutsa.common.repository.order.OrderRepository;
import mutsa.common.repository.payment.CardReceiptRepository;
import mutsa.common.repository.payment.PaymentRepository;
import mutsa.common.repository.payment.ReceiptRepository;
import mutsa.common.repository.user.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.mockStatic;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(classes = {ApiApplication.class, TestRedisConfiguration.class})
@ActiveProfiles("test")
@Transactional
@Slf4j
class PaymentModuleServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PaymentModuleService paymentModuleService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ReceiptRepository receiptRepository;
    @Autowired
    private CardReceiptRepository cardReceiptRepository;

    private MockRestServiceServer mockServer;
    private static MockedStatic<SecurityUtil> securityUtilMockedStatic;
    private User buyer, seller;
    private Article article;

    @BeforeEach
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        setupSecurityUtilMock();
        initTestData();
    }

    private void setupSecurityUtilMock() {
        if (securityUtilMockedStatic == null) {
            securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        }
        securityUtilMockedStatic.when(SecurityUtil::getCurrentUsername).thenReturn("buyer1");
    }

    private void initTestData() {
        buyer = createAndSaveUser("buyer1", "buyerEmail@", "buyerOauthName");
        seller = createAndSaveUser("seller1", "sellerEmail@", "sellerOauthName");
        article = createAndSaveArticle("Sample Article", "Sample Article desc", 50000L, seller);
    }

    private User createAndSaveUser(String username, String email, String oauthName) {
        User user = User.of(username, "password", email, oauthName, null, username);
        return userRepository.save(user);
    }

    private Payment createAndSavePayment(Article article, Order order) {
        Payment payment = Payment.of(PayType.CARD, article, order);
        return paymentRepository.save(payment);
    }

    private Article createAndSaveArticle(String title, String description, Long price, User user) {
        Article newArticle = Article.builder()
                .title(title)
                .description(description)
                .user(user)
                .price(price)
                .build();
        return articleRepository.save(newArticle);
    }

    @AfterAll
    public static void afterAll() {
        securityUtilMockedStatic.close();
    }

    @DisplayName("결제 정보 확인 및 저장")
    @Test
    void getPaymentInfoAndSaveTest() {
        PaymentDto paymentDto = paymentModuleService.getPaymentInfoAndSave(article.getApiId());
        assertThat(paymentDto.getCustomerApiId()).isEqualTo(buyer.getApiId());
        assertThat(paymentDto.getAmount()).isEqualTo(article.getPrice());
    }

    @DisplayName("결제 성공")
    @Test
    void tossPaymentSuccessTest() {
        // Given
        Order savedOrder = createAndSaveOrder();
        log.info(savedOrder.getApiId());
        mockExternalPaymentApi(savedOrder.getApiId(), 50000L);
        createAndSavePayment(article, savedOrder);

        // When
        PaymentSuccessDto result = paymentModuleService.tossPaymentSuccess("somePaymentKey", savedOrder.getApiId(), 50000L);

        // Then
        assertThat(result).isNotNull();
        mockServer.verify();
    }

    @DisplayName("영수증 정보 저장")
    @Test
    void saveReceiptTest() {
        // Given
        Order savedOrder = createAndSaveOrder();
        Payment payment = createAndSavePayment(article, savedOrder);
        PaymentSuccessDto paymentSuccessDto = createMockedPaymentSuccessDto(savedOrder.getApiId(), article.getPrice());

        // When
        paymentModuleService.saveReceipt(paymentSuccessDto, payment);

        // Then
        Optional<Receipt> optionalReceipt = receiptRepository.findByPayment(payment);
        assertTrue(optionalReceipt.isPresent());

        Optional<CardReceipt> optionalCardReceipt = cardReceiptRepository.findByReceipt(optionalReceipt.get());
        assertTrue(optionalCardReceipt.isPresent());
    }

    private Order createAndSaveOrder() {
        Order order = Order.of(article, buyer);
        return orderRepository.save(order);
    }

    private void mockExternalPaymentApi(String orderId, Long amount) {
        PaymentSuccessDto expectedResponse = createMockedPaymentSuccessDto(orderId, amount);

        try {
            mockServer.expect(ExpectedCount.once(),
                            requestTo(TossPaymentConfig.URL + "somePaymentKey"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            new ObjectMapper().writeValueAsString(expectedResponse),
                            MediaType.APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("외부 결제 API 모킹 중 에러 발생", e);
        }
    }

    private PaymentSuccessDto createMockedPaymentSuccessDto(String orderId, Long amount) {
        PaymentSuccessDto dto = new PaymentSuccessDto();
        dto.setVersion("v1");
        dto.setPaymentKey("samplePaymentKey");
        dto.setOrderId(orderId);
        dto.setOrderName("Sample Order");
        dto.setCurrency("KRW");
        dto.setMethod("CARD");
        dto.setTotalAmount(amount.toString());
        dto.setBalanceAmount("0");
        dto.setSuppliedAmount("45000");
        dto.setVat("5000");
        dto.setStatus("APPROVED");
        dto.setRequestedAt(LocalDateTime.now().toString());
        dto.setApprovedAt(LocalDateTime.now().plusMinutes(1).toString());
        dto.setUseEscrow("N");
        dto.setCultureExpense("N");
        dto.setType("PAYMENT");
        dto.setCard(createMockedCardDto());
        return dto;
    }

    private PaymentSuccessCardDto createMockedCardDto() {
        PaymentSuccessCardDto cardDto = new PaymentSuccessCardDto();
        cardDto.setCompany("Visa");
        cardDto.setNumber("1234-5678-1234-5678");
        cardDto.setInstallmentPlanMonths("1");
        cardDto.setApproveNo("123456");
        cardDto.setUseCardPoint("N");
        cardDto.setCardType("CREDIT");
        cardDto.setOwnerType("PERSONAL");
        cardDto.setAcquireStatus("APPROVED");
        cardDto.setReceiptUrl("https://example.com/receipt");
        return cardDto;
    }
}
