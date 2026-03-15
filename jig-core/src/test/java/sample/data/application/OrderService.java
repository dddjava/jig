package sample.data.application;

import sample.data.domain.customer.Customer;
import sample.data.domain.order.*;
import sample.data.domain.product.Product;
import sample.data.domain.product.ProductId;

import java.util.ArrayList;
import java.util.List;

/**
 * 受注サービス
 */
public class OrderService {
    OrderRepository orderRepository;
    ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    /**
     * 新規受注の登録
     */
    public void registerNewOrder(OrderId orderId, Customer customer, List<OrderItemRequest> itemRequests) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest request : itemRequests) {
            Product product = productRepository.findBy(request.productId());
            items.add(new OrderItem(product, request.quantity()));
        }

        Order order = new Order(orderId, customer, items);
        orderRepository.register(order);
    }

    /**
     * 受注明細の取得
     */
    public List<OrderItem> getOrderItems(OrderId orderId) {
        Order order = orderRepository.findBy(orderId);
        return order.items();
    }

    /**
     * 受注商品の追加リクエスト用record
     */
    public record OrderItemRequest(ProductId productId, Quantity quantity) {
    }
}
