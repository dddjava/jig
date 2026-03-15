package sample.data.infrastructure;

/**
 * 商品エンティティ（DB用）
 */
public record ProductEntity(String productId, String name, long price) {
}
