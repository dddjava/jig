package sample.data.domain.customer;

/**
 * 顧客
 */
public class Customer {
    CustomerId id;
    String name;

    public Customer(CustomerId id, String name) {
        this.id = id;
        this.name = name;
    }
}
