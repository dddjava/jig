package sample.data.infrastructure;

import org.springframework.data.repository.CrudRepository;

/**
 * 受注データアクセサ
 */
public interface OrderEntityAccessor extends CrudRepository<OrderEntity, String> {
    OrderEntity findByOrderId(String orderId);
}
