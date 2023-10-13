package mutsa.api.service.payment;

import lombok.RequiredArgsConstructor;
import mutsa.common.domain.models.order.Order;
import mutsa.common.domain.models.payment.Payment;
import mutsa.common.domain.models.payment.Receipt;
import mutsa.common.exception.BusinessException;
import mutsa.common.exception.ErrorCode;
import mutsa.common.repository.order.OrderRepository;
import mutsa.common.repository.payment.PaymentRepository;
import mutsa.common.repository.payment.ReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReceiptModuleService {
    private final ReceiptRepository receiptRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public Receipt getReceiptByApiId(String receiptApiId) {
        return receiptRepository.findByApiId(receiptApiId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEIPT_NOT_FOUND));
    }

    public Receipt getReceiptByOrderApiId(String orderApiId) {
        Order order = orderRepository.findByApiId(orderApiId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return receiptRepository.findByPayment(payment)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEIPT_NOT_FOUND));
    }
}
