package sample.data.infrastructure.product;

import sample.data.application.order.ProductRepository;
import sample.data.domain.product.Product;
import sample.data.domain.product.ProductId;

/**
 * 商品データソース
 */
public class ProductDataSource implements ProductRepository {
    ProductEntityAccessor productEntityAccessor;

    public ProductDataSource(ProductEntityAccessor productEntityAccessor) {
        this.productEntityAccessor = productEntityAccessor;
    }

    @Override
    public Product findBy(ProductId id) {
        // 構造の模擬に留めるため、直接nullを返す
        return null;
    }
}
