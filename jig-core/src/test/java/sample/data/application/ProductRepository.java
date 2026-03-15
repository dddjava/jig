package sample.data.application;

import sample.data.domain.product.Product;
import sample.data.domain.product.ProductId;

/**
 * 商品リポジトリ
 */
public interface ProductRepository {
    Product findBy(ProductId id);
}
