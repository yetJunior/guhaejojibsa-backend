package mutsa.common.repository.payment;

import mutsa.common.domain.models.payment.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByApiId(String apiId);
}
