package mutsa.common.repository.payment;

import mutsa.common.domain.models.order.Order;
import mutsa.common.domain.models.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderKey(String orderKey);
    Optional<Payment> findByOrder(Order order);
}
