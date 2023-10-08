package mutsa.api.dto.payment;

import lombok.Builder;
import lombok.Getter;
import mutsa.common.domain.models.payment.CardReceipt;

@Getter
@Builder
public class ReceiptCardResponseDto {
    String company; // 회사명
    String number; // 카드번호
    String installmentPlanMonths; // 할부 개월
    String isInterestFree;
    String approveNo; // 승인번호
    String useCardPoint; // 카드 포인트 사용 여부
    String cardType; // 카드 타입
    String ownerType; // 소유자 타입
    String acquireStatus; // 승인 상태
    String receiptUrl; // 영수증 URL

    public static ReceiptCardResponseDto of(CardReceipt cardReceipt) {
        return ReceiptCardResponseDto.builder()
                .company(cardReceipt.getCompany())
                .number(cardReceipt.getNumber())
                .installmentPlanMonths(cardReceipt.getInstallmentPlanMonths())
                .isInterestFree(cardReceipt.getIsInterestFree())
                .approveNo(cardReceipt.getApproveNo())
                .useCardPoint(cardReceipt.getUseCardPoint())
                .cardType(cardReceipt.getCardType())
                .ownerType(cardReceipt.getOwnerType())
                .acquireStatus(cardReceipt.getAcquireStatus())
                .receiptUrl(cardReceipt.getReceiptUrl())
                .build();
    }
}
