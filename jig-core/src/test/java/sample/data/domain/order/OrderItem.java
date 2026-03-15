package sample.data.domain.order;

import sample.data.domain.product.Product;
import sample.data.domain.product.Price;

/**
 * 受注明細
 */
public class OrderItem {
    Product product;
    Quantity quantity;

    public OrderItem(Product product, Quantity quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Price subtotal() {
        return product.price().multiply(quantity.value());
    }
}
