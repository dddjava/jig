package sample.data.domain.order;

import sample.data.domain.customer.Customer;
import java.util.List;

/**
 * 受注
 */
public class Order {
    OrderId id;
    Customer customer;
    List<OrderItem> items;

    public Order(OrderId id, Customer customer, List<OrderItem> items) {
        this.id = id;
        this.customer = customer;
        this.items = items;
    }

    public List<OrderItem> items() {
        return items;
    }
}
