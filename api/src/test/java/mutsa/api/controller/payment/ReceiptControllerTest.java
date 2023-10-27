package mutsa.api.controller.payment;

import mutsa.api.ApiApplication;
import mutsa.api.config.TestRedisConfiguration;
import mutsa.api.dto.payment.PaymentSuccessCardDto;
import mutsa.api.dto.payment.PaymentSuccessDto;
import mutsa.api.dto.payment.ReceiptResponseDto;
import mutsa.api.service.payment.ReceiptService;
import mutsa.api.util.SecurityUtil;
import mutsa.common.domain.models.article.Article;
import mutsa.common.domain.models.payment.CardReceipt;
import mutsa.common.domain.models.payment.PayType;
import mutsa.common.domain.models.payment.Payment;
import mutsa.common.domain.models.order.Order;
import mutsa.common.domain.models.payment.Receipt;
import mutsa.common.domain.models.user.User;
import mutsa.common.repository.article.ArticleRepository;
import mutsa.common.repository.order.OrderRepository;
import mutsa.common.repository.payment.CardReceiptRepository;
import mutsa.common.repository.payment.PaymentRepository;
import mutsa.common.repository.payment.ReceiptRepository;
import mutsa.common.repository.user.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {ApiApplication.class, TestRedisConfiguration.class})
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public class ReceiptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReceiptService receiptService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private CardReceiptRepository cardReceiptRepository;

    private static MockedStatic<SecurityUtil> securityUtilMockedStatic;

    private User consumer;
    private Article article;
    private Payment payment;
    private Order order;
    private Receipt receipt;
    private PaymentSuccessDto paymentSuccessDto;

    @BeforeAll
    public static void beforeAll() {
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
    }

    @AfterAll
    public static void afterAll() {
        securityUtilMockedStatic.close();
    }

    @BeforeEach
    public void setup() {
        initializeEntities();
        setupMocks();
    }

    private void initializeEntities() {
        consumer = User.of("user2", "password", "email2@", "oauthName2", null, "user2");
        consumer = userRepository.save(consumer);

        article = Article.builder()
                .title("Pre Article 1")
                .description("Pre Article 1 desc")
                .user(consumer)
                .price(129000L)
                .build();
        article = articleRepository.save(article);

        order = orderRepository.save(Order.of(article, consumer));
        payment = Payment.of(PayType.CARD, article, order);
        payment = paymentRepository.save(payment);

        paymentSuccessDto = createPaymentSuccessDto();
        receipt = createAndSaveReceipt(paymentSuccessDto);
        createAndSaveCardReceipt(receipt);
    }

    private void setupMocks() {
        when(SecurityUtil.getCurrentUsername()).thenReturn(consumer.getUsername());

        ReceiptResponseDto mockResponseDto = ReceiptResponseDto.builder().build();
        when(receiptService.getReceipt(anyString())).thenReturn(mockResponseDto);
    }

    @DisplayName("영수증 조회")
    @Test
    void getReceipt() throws Exception {
        mockMvc.perform(get("/api/articles/" + article.getApiId() + "/order/" + order.getApiId() + "/" + receipt.getReceiptId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON)
                );
    }

    private Receipt createAndSaveReceipt(PaymentSuccessDto dto) {
        Receipt receipt = Receipt.builder()
                .mid(dto.getMId())
                .version(dto.getVersion())
                .paymentKey(dto.getPaymentKey())
                .orderId(dto.getOrderId())
                .orderName(dto.getOrderName())
                .currency(dto.getCurrency())
                .method(dto.getMethod())
                .totalAmount(dto.getTotalAmount())
                .balanceAmount(dto.getBalanceAmount())
                .suppliedAmount(dto.getSuppliedAmount())
                .vat(dto.getVat())
                .status(dto.getStatus())
                .requestedAt(dto.getRequestedAt())
                .approvedAt(dto.getApprovedAt())
                .useEscrow(dto.getUseEscrow())
                .cultureExpense(dto.getCultureExpense())
                .type(dto.getType())
                .build();
        return receiptRepository.save(receipt);
    }

    private PaymentSuccessDto createPaymentSuccessDto() {
        PaymentSuccessDto dto = new PaymentSuccessDto();
        dto.setVersion("v1");
        dto.setPaymentKey("samplePaymentKey");
        dto.setOrderId("Test Order");
        dto.setOrderName("Sample Order");
        dto.setCurrency("KRW");
        dto.setMethod("CARD");
        dto.setTotalAmount("10000");
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

    private void createAndSaveCardReceipt(Receipt receipt) {
        CardReceipt cardReceipt = CardReceipt.builder()
                .receipt(receipt)
                .company("TestCompany")
                .number("1234-5678-1234-5678")
                .installmentPlanMonths("1")
                .approveNo("123456")
                .useCardPoint("N")
                .cardType("CREDIT")
                .ownerType("PERSONAL")
                .acquireStatus("APPROVED")
                .receiptUrl("https://example.com/receipt")
                .build();
        cardReceiptRepository.save(cardReceipt);
    }
}
