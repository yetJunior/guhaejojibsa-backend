package mutsa.common.repository.payment;

import mutsa.common.domain.models.payment.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
}
