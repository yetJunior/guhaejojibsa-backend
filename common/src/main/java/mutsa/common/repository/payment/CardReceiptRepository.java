package mutsa.common.repository.payment;

import mutsa.common.domain.models.payment.CardReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardReceiptRepository extends JpaRepository<CardReceipt, Long> {
}
