package showcase.domain.customer;

/**
 * 顧客
 */
public class Customer {

    private final CustomerId customerId;
    private final String name;

    public Customer(CustomerId customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }

    public CustomerId customerId() {
        return customerId;
    }

    /**
     * 表示名
     */
    public String displayName() {
        return name;
    }
}
