package sample.data.infrastructure;

import sample.data.domain.order.OrderId;

/**
 * 受注エンティティ（DB用）
 */
public record OrderEntity(String orderId, String customerName) {
}
