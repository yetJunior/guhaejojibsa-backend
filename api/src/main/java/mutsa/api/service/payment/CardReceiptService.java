package mutsa.api.service.payment;

import lombok.RequiredArgsConstructor;
import mutsa.common.domain.models.payment.CardReceipt;
import mutsa.common.domain.models.payment.Receipt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardReceiptService {
    private final CardReceiptModuleService cardReceiptModuleService;

    public CardReceipt getByReceipt(Receipt receipt) {
        return cardReceiptModuleService.findByReceipt(receipt);
    }
}
