package sample.data.infrastructure;

import org.springframework.data.repository.CrudRepository;

/**
 * 商品データアクセサ
 */
public interface ProductEntityAccessor extends CrudRepository<ProductEntity, String> {
}
