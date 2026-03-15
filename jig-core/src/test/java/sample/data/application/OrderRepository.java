package sample.data.application;

import sample.data.domain.order.Order;
import sample.data.domain.order.OrderId;

/**
 * 受注リポジトリ
 */
public interface OrderRepository {
    void register(Order order);
    Order findBy(OrderId id);
}
