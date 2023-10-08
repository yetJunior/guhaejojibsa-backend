package mutsa.api.controller.payment;

import lombok.RequiredArgsConstructor;
import mutsa.api.dto.payment.ReceiptResponseDto;
import mutsa.api.service.payment.ReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles/{articleApiId}/order/{orderApiId}")
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping("/{receiptApiId}")
    public ResponseEntity<ReceiptResponseDto> getReceipt(
            @PathVariable("receiptApiId") String receiptApiId) {
        return ResponseEntity.ok(receiptService.getReceipt(receiptApiId));
    }
}
