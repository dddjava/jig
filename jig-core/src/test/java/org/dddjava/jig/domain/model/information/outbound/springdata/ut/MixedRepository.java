package org.dddjava.jig.domain.model.information.outbound.springdata.ut;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RawSpringDataRepository;

/**
 * Spring Data と無関係なインタフェースを混ぜて継承しても認識できることを確認する。
 */
public interface MixedRepository extends RawSpringDataRepository, CrudRepository<Order, Long> {

    @Override
    Order save(Order entity);

    @Override
    Order findById(Long id);

    @Override
    void deleteById(Long id);
}
