package showcase.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import showcase.application.OrderService;
import showcase.domain.order.Order;
import showcase.domain.customer.CustomerId;

import java.util.List;

/**
 * 注文の受付窓口
 */
@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 注文の一覧を表示する
     */
    @RequestMapping("/orders")
    public List<Order> list(CustomerId customerId) {
        return orderService.ordersOf(customerId);
    }

    /**
     * 注文を登録する
     */
    @RequestMapping("/orders/register")
    public void register(Order order) {
        orderService.register(order);
    }
}
