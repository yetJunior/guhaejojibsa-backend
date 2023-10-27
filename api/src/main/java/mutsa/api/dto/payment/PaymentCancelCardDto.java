package mutsa.api.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCancelCardDto {
    String issuerCode; // 이슈 코드
    String number; // 카드번호
    String installmentPlanMonths; // 할부 개월
    String isInterestFree;
    String approveNo; // 승인번호
    String useCardPoint; // 카드 포인트 사용 여부
    String cardType; // 카드 타입
    String ownerType; // 소유자 타입
    String acquireStatus; // 승인 상태
}
