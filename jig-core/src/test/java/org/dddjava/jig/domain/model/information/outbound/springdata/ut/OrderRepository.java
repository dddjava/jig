package org.dddjava.jig.domain.model.information.outbound.springdata.ut;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * CRUDメソッドを明示的にオーバーライドし、カスタムメソッドは @Query で示す典型パターン。
 */
public interface OrderRepository extends CrudRepository<Order, Long> {

    @Override
    Order save(Order entity);

    @Override
    Order findById(Long id);

    @Override
    void deleteById(Long id);

    @Query("update orders set id = :id where id = :id")
    void updateById(Long id);

    // 前後の空白・コメントが混じっていてもクエリ種別を判定できることを確認する
    @Query("   /* leading comment */\n\tupdate orders set id = :id where id = :id")
    void updateByIdWithComment(Long id);
}
