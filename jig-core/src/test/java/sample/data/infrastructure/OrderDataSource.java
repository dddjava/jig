package sample.data.infrastructure;

import sample.data.application.OrderRepository;
import sample.data.domain.order.Order;
import sample.data.domain.order.OrderId;

import java.util.HashMap;
import java.util.Map;

/**
 * 受注データソース
 */
public class OrderDataSource implements OrderRepository {
    private final Map<OrderId, Order> db = new HashMap<>();

    @Override
    public void register(Order order) {
        db.put(order.id(), order);
    }

    @Override
    public Order findBy(OrderId id) {
        return db.get(id);
    }
}
