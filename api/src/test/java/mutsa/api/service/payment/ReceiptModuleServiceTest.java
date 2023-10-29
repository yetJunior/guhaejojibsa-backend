package mutsa.api.service.payment;

import mutsa.api.ApiApplication;
import mutsa.api.config.TestRedisConfiguration;
import mutsa.api.dto.payment.PaymentSuccessCardDto;
import mutsa.api.dto.payment.PaymentSuccessDto;
import mutsa.common.domain.models.payment.CardReceipt;
import mutsa.common.domain.models.payment.Receipt;
import mutsa.common.repository.payment.CardReceiptRepository;
import mutsa.common.repository.payment.ReceiptRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {ApiApplication.class, TestRedisConfiguration.class})
@ActiveProfiles("test")
@Transactional
public class ReceiptModuleServiceTest {

    @Autowired
    private ReceiptModuleService receiptModuleService;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private CardReceiptRepository cardReceiptRepository;

    @Test
    @DisplayName("API ID로 영수증 조회 - 성공 케이스")
    void getReceiptByApiId_SuccessTest() {
        // Given
        PaymentSuccessDto paymentSuccessDto = createPaymentSuccessDto();
        Receipt savedReceipt = createAndSaveReceipt(paymentSuccessDto);
        createAndSaveCardReceipt(savedReceipt);

        // When
        Receipt result = receiptModuleService.getReceiptByApiId(savedReceipt.getApiId());

        // Then
        assertNotNull(result);
        assertEquals(savedReceipt, result);
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
}
