package org.dddjava.jig.domain.model.information.outbound.springdata.ut;

import org.springframework.stereotype.Repository;

@Repository
public class OrderOutboundAdapter implements OrderOutboundPort {
    private final OrderRepository repository;

    public OrderOutboundAdapter(OrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order save(Order entity) {
        return repository.save(entity);
    }
}
