package mutsa.api.service.payment;

import lombok.RequiredArgsConstructor;
import mutsa.common.domain.models.payment.CardReceipt;
import mutsa.common.domain.models.payment.Receipt;
import mutsa.common.exception.BusinessException;
import mutsa.common.exception.ErrorCode;
import mutsa.common.repository.payment.CardReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CardReceiptModuleService {
    private final CardReceiptRepository cardReceiptRepository;

    public CardReceipt findByReceipt(Receipt receipt) {
        return cardReceiptRepository.findByReceipt(receipt)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_RECEIPT_NOT_FOUND));
    }
}
