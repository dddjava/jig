package sample.data.infrastructure;

import sample.data.application.OrderRepository;
import sample.data.domain.order.Order;
import sample.data.domain.order.OrderId;

/**
 * 受注データソース
 */
public class OrderDataSource implements OrderRepository {
    OrderEntityAccessor orderEntityAccessor;

    public OrderDataSource(OrderEntityAccessor orderEntityAccessor) {
        this.orderEntityAccessor = orderEntityAccessor;
    }

    @Override
    public void register(Order order) {
        OrderEntity entity = new OrderEntity(order.id().value(), "dummy");
        orderEntityAccessor.save(entity);
    }

    @Override
    public Order findBy(OrderId id) {
        OrderEntity entity = orderEntityAccessor.findByOrderId(id.value());
        // 本来はここからドメインモデルへ再構築するが、構造の模擬に留める
        return null;
    }
}
