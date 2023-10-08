package mutsa.common.domain.models.payment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id", nullable = false, unique = true)
    private Long receiptId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(unique = true, nullable = false)
    @Builder.Default
    private final String apiId = UUID.randomUUID().toString();

    @Column
    private String mid;             // 가맹점 ID. 고유 식별자 (사업자 등록 필요)

    @Column(nullable = false)
    private String version;         // API 버전

    @Column(nullable = false)
    private String paymentKey;      // 결제 키. 결제 식별자

    @Column(nullable = false)
    private String orderId;         // 주문 ID. Toss API 호출 키

    @Column(nullable = false)
    private String orderName;       // 주문 이름

    @Column(nullable = false)
    private String currency;        // 화폐 단위

    @Column(nullable = false)
    private String method;          // 결제 방식

    @Column(nullable = false)
    private String totalAmount;     // 총 결제 금액

    @Column(nullable = false)
    private String balanceAmount;   // 잔액. 결제 후 남은 금액

    @Column(nullable = false)
    private String suppliedAmount;  // 공급 가격. 부가세를 제외한 실제 상품/서비스의 가격

    @Column(nullable = false)
    private String vat;             // 부가세

    @Column(nullable = false)
    private String status;          // 결제 상태

    @Column(nullable = false)
    private String requestedAt;     // 결제 요청 시각

    @Column(nullable = false)
    private String approvedAt;      // 결제 승인 시각

    @Column(nullable = false)
    private String useEscrow;       // 에스크로 사용 여부

    @Column(nullable = false)
    private String cultureExpense;  // 문화비 지출 여부

    @Column(nullable = false)
    private String type;            // 결제 유형
}
