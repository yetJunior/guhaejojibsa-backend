package mutsa.api.service.payment;

import lombok.RequiredArgsConstructor;
import mutsa.api.dto.payment.ReceiptApiIdDto;
import mutsa.api.dto.payment.ReceiptCardResponseDto;
import mutsa.api.dto.payment.ReceiptResponseDto;
import mutsa.common.domain.models.payment.CardReceipt;
import mutsa.common.domain.models.payment.Receipt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReceiptService {
    private final ReceiptModuleService receiptModuleService;
    private final CardReceiptService cardReceiptService;

    public ReceiptResponseDto getReceipt(String receiptApiId) {
        Receipt receipt = receiptModuleService.getReceiptByApiId(receiptApiId);
        CardReceipt cardReceipt = cardReceiptService.getByReceipt(receipt);
        ReceiptCardResponseDto receiptCardDto = ReceiptCardResponseDto.of(cardReceipt);
        ReceiptResponseDto receiptDto = ReceiptResponseDto.of(receipt);
        receiptDto.setCard(receiptCardDto);
        return receiptDto;
    }

    public ReceiptApiIdDto getReceiptApiIdByOrderApiId(String orderApiId) {
        return ReceiptApiIdDto.of(receiptModuleService.getReceiptByOrderApiId(orderApiId));
    }
}
