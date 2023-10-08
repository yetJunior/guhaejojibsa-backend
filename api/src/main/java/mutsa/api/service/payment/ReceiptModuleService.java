package mutsa.api.service.payment;

import lombok.RequiredArgsConstructor;
import mutsa.common.domain.models.payment.Receipt;
import mutsa.common.exception.BusinessException;
import mutsa.common.exception.ErrorCode;
import mutsa.common.repository.payment.ReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReceiptModuleService {
    private final ReceiptRepository receiptRepository;

    public Receipt getReceiptByApiId(String receiptApiId) {
        return receiptRepository.findByApiId(receiptApiId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEIPT_NOT_FOUND));
    }
}
