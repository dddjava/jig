package sample.data.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import sample.data.application.order.OrderService;
import sample.data.domain.order.OrderItem;

import java.util.List;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @RequestMapping("/order")
    public List<OrderItem> order() {
        return orderService.getOrderItems(null);
    }
}
