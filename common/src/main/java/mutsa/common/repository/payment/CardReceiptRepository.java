package mutsa.common.repository.payment;

import mutsa.common.domain.models.payment.CardReceipt;
import mutsa.common.domain.models.payment.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardReceiptRepository extends JpaRepository<CardReceipt, Long> {
    Optional<CardReceipt> findByReceipt(Receipt receipt);
}
