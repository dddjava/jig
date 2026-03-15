package sample.data.domain.order;

import sample.data.domain.customer.Customer;
import java.util.List;

/**
 * 受注
 */
public record Order(OrderId id, Customer customer, List<OrderItem> items) {
}
