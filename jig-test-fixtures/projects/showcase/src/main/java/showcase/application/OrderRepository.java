package showcase.application;

import showcase.domain.customer.CustomerId;
import showcase.domain.order.Order;

import java.util.List;

/**
 * 注文の記録
 */
public interface OrderRepository {

    /**
     * 顧客の注文を取得する
     */
    List<Order> findBy(CustomerId customerId);

    /**
     * 注文を記録する
     */
    void register(Order order);
}
