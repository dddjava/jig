package showcase.application;

import org.springframework.stereotype.Service;
import showcase.domain.customer.CustomerId;
import showcase.domain.order.Order;

import java.util.List;

/**
 * 注文サービス
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * 顧客の注文を一覧する
     */
    public List<Order> ordersOf(CustomerId customerId) {
        return orderRepository.findBy(customerId);
    }

    /**
     * 注文を登録する
     */
    public void register(Order order) {
        orderRepository.register(order);
    }
}
