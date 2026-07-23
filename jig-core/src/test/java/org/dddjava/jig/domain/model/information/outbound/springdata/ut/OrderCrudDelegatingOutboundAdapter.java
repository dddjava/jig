package org.dddjava.jig.domain.model.information.outbound.springdata.ut;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * リポジトリを CrudRepository 型の変数へ代入してから呼び出すケース。
 * 呼び出し先の静的型（CrudRepository）と実際の PersistenceAccessor の登録型（OrderRepository）が
 * 異なっていても解決できることを確認する。
 */
@Repository
public class OrderCrudDelegatingOutboundAdapter implements OrderCrudDelegatingOutboundPort {
    private final OrderRepository repository;

    public OrderCrudDelegatingOutboundAdapter(OrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order save(Order entity) {
        CrudRepository<Order, Long> crudRepository = repository;
        return crudRepository.save(entity);
    }
}
