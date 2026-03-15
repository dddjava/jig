package sample.data.infrastructure.order;

/**
 * 受注エンティティ（DB用）
 */
public record OrderEntity(String orderId, String customerName) {
}
