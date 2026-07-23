package showcase.domain.order;

import showcase.domain.customer.CustomerId;

/**
 * 注文
 */
public class Order {

    private final OrderId orderId;
    private final CustomerId customerId;
    private final Quantity quantity;
    private final OrderStatus status;

    public Order(OrderId orderId, CustomerId customerId, Quantity quantity, OrderStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.quantity = quantity;
        this.status = status;
    }

    public OrderId orderId() {
        return orderId;
    }

    public Quantity quantity() {
        return quantity;
    }

    /**
     * 指定の顧客による注文か
     */
    public boolean orderedBy(CustomerId other) {
        return customerId.equals(other);
    }

    /**
     * 出荷できるか
     */
    public boolean shippable() {
        return status.shippable() && !quantity.isEmpty();
    }
}
