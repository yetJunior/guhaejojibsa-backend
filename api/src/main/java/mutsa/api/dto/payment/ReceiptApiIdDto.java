package mutsa.api.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mutsa.common.domain.models.payment.Receipt;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptApiIdDto {
    private String receiptApiId;

    public static ReceiptApiIdDto of(Receipt receipt) {
        ReceiptApiIdDto dto = new ReceiptApiIdDto();
        dto.receiptApiId = receipt.getApiId();
        return dto;
    }
}
