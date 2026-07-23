package org.dddjava.jig.domain.model.information.outbound.springdata.ut;

import org.springframework.data.repository.CrudRepository;

public interface OrderWithItemsRepository extends CrudRepository<OrderWithItems, Long> {

    @Override
    OrderWithItems save(OrderWithItems entity);

    @Override
    OrderWithItems findById(Long id);

    @Override
    void deleteById(Long id);
}
