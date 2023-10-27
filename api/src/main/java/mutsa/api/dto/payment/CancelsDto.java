package mutsa.api.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelsDto {
    String cancelReason;        // 취소 사유
    String canceledAt;          // 취소 시간
    String cancelAmount;        // 취소 금액
    String taxFreeAmount;       // 면세 금액
    String taxExemptionAmount;  // 세금 면제 금액
    String refundableAmount;    // 환불 가능 금액
    String easyPayDiscountAmount;   // 간편 결제 할인 금액
    String transactionKey;      // 환급 키
    String receiptKey;          // 영수증 키
}
