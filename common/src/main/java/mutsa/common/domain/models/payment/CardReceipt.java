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
public class CardReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_receipt_id", nullable = false, unique = true)
    private Long cardReceiptId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "receipt_id")
    private Receipt receipt;

    @Column(unique = true, nullable = false)
    @Builder.Default
    private final String apiId = UUID.randomUUID().toString();

    @Column
    private String company; // 회사명

    @Column(nullable = false)
    private String number; // 카드번호

    @Column(nullable = false)
    private String installmentPlanMonths; // 할부 개월

    @Column
    private String isInterestFree;

    @Column(nullable = false)
    private String approveNo; // 승인번호

    @Column(nullable = false)
    private String useCardPoint; // 카드 포인트 사용 여부

    @Column(nullable = false)
    private String cardType; // 카드 타입

    @Column(nullable = false)
    private String ownerType; // 소유자 타입

    @Column(nullable = false)
    private String acquireStatus; // 승인 상태

    @Column
    private String receiptUrl; // 영수증 URL

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }
}
