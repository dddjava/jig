package sample.data.domain.order;

import sample.data.domain.product.Product;
import sample.data.domain.product.Price;

/**
 * 受注明細
 */
public record OrderItem(Product product, Quantity quantity) {
    public Price subtotal() {
        return product.price().multiply(quantity.value());
    }
}
