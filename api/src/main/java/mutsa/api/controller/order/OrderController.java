package mutsa.api.controller.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mutsa.api.dto.CustomPage;
import mutsa.api.dto.order.*;
import mutsa.api.service.order.OrderService;
import mutsa.api.util.SecurityUtil;
import mutsa.common.dto.order.OrderResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    /**
     * @param articleApiId
     * @param orderApiId
     * @return 주문 단건 조회
     */
    @GetMapping("articles/{articleApiId}/order/{orderApiId}")
    public ResponseEntity<OrderDetailResponseDto> getDetailOrder(
            @PathVariable("articleApiId") String articleApiId,
            @PathVariable("orderApiId") String orderApiId) {
        OrderDetailResponseDto dto = orderService.findDetailOrder(articleApiId, orderApiId, SecurityUtil.getCurrentUsername());
        return ResponseEntity.ok(dto);
    }

    /**
     * @param articleApiId article apiID
     * @return 게시글의 주문 모두 조회(판매자만 가능)
     */
    @GetMapping("articles/{articleApiId}/order")
    public ResponseEntity<CustomPage<OrderResponseDto>> getAllOrder(
            @PathVariable("articleApiId") String articleApiId,
            @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestParam(name = "orderStatus", required = false) String orderStatus,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "limit", defaultValue = "20") Integer limit) {
        CustomPage<OrderResponseDto> dtos = orderService.findAllOrder(articleApiId, sortOrder, orderStatus, page, limit, SecurityUtil.getCurrentUsername());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("order/sell")
    public ResponseEntity<OrderResponseListDto> getOrderBySellerFilter(
            @RequestParam(name = "orderStatus", required = false) String orderStatus,
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "limit", defaultValue = "20") Integer limit) {
        OrderFilterDto orderFilterDto = new OrderFilterDto(orderStatus, searchText, sortOrder,"SELLER", page, limit);
        return ResponseEntity.ok(orderService.getOrderPage(orderFilterDto, SecurityUtil.getCurrentUsername()));
    }

    @GetMapping("order/consume")
    public ResponseEntity<OrderResponseListDto> getOrderByConsumerFilter(
            @RequestParam(name = "orderStatus", required = false) String orderStatus,
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "limit", defaultValue = "20") Integer limit) {
        OrderFilterDto orderFilterDto = new OrderFilterDto(orderStatus, searchText, sortOrder,"CONSUMER", page, limit);
        return ResponseEntity.ok(orderService.getOrderPage(orderFilterDto, SecurityUtil.getCurrentUsername()));
    }


    /**
     * @param articleApiId
     * @return 주문 생성
     */
    @PostMapping("articles/{articleApiId}/order")
    public ResponseEntity<OrderDetailResponseDto> saveOrder(
            @PathVariable("articleApiId") String articleApiId) {
        OrderDetailResponseDto dto = orderService.saveOrder(articleApiId, SecurityUtil.getCurrentUsername());
        return ResponseEntity.ok(dto);
    }

    /**
     * @param articleApiId
     * @param orderApiId
     * @param orderStatusRequestDto
     * @return 주문 수정
     */
    @PutMapping("articles/{articleApiId}/order/{orderApiId}")
    public ResponseEntity<OrderDetailResponseDto> updateOrderStatus(
            @PathVariable("articleApiId") String articleApiId,
            @PathVariable("orderApiId") String orderApiId,
            @RequestBody OrderStatusRequestDto orderStatusRequestDto) {
        OrderDetailResponseDto dto = orderService.updateOrderStatus(articleApiId, orderApiId, orderStatusRequestDto, SecurityUtil.getCurrentUsername());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("articles/{articleApiId}/order/{orderApiId}")
    public ResponseEntity<String> deleteOrder(
            @PathVariable("articleApiId") String articleApiId,
            @PathVariable("orderApiId") String orderApiId) {
        orderService.deleteOrder(articleApiId, orderApiId, SecurityUtil.getCurrentUsername());
        return ResponseEntity.ok("삭제 완료");
    }

}
