package sample.data.domain.product;

/**
 * 商品
 */
public class Product {
    ProductId id;
    String name;
    Price price;

    public Product(ProductId id, String name, Price price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Price price() {
        return price;
    }
}
