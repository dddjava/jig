package showcase.infrastructure;

import org.springframework.stereotype.Repository;
import showcase.application.OrderRepository;
import showcase.domain.customer.CustomerId;
import showcase.domain.order.Order;
import showcase.domain.order.OrderId;
import showcase.domain.order.Quantity;

import java.util.ArrayList;
import java.util.List;

/**
 * 注文の記録の実装
 */
@Repository
public class OrderDataSource implements OrderRepository {

    private final List<Order> orders = new ArrayList<>();

    @Override
    public List<Order> findBy(CustomerId customerId) {
        List<Order> results = new ArrayList<>();
        for (Order order : orders) {
            if (order.orderedBy(customerId)) results.add(order);
        }
        return results;
    }

    @Override
    public void register(Order order) {
        orders.add(order);
    }

    /**
     * 採番する
     */
    public OrderId nextId() {
        return new OrderId(orders.size() + 1);
    }

    Quantity totalQuantity() {
        Quantity total = new Quantity(0);
        for (Order order : orders) {
            total = total.add(order.quantity());
        }
        return total;
    }
}
