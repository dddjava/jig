package sample.data.infrastructure;

import sample.data.application.ProductRepository;
import sample.data.domain.product.Product;
import sample.data.domain.product.ProductId;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品データソース
 */
public class ProductDataSource implements ProductRepository {
    private final Map<ProductId, Product> catalog = new HashMap<>();

    public ProductDataSource() {
        // サンプルデータの初期化などはここでは行わず、リポジトリの役割に徹する
    }

    @Override
    public Product findBy(ProductId id) {
        return catalog.get(id);
    }
}
