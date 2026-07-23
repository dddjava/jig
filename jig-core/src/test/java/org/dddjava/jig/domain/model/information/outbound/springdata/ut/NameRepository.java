package org.dddjava.jig.domain.model.information.outbound.springdata.ut;

import org.springframework.data.repository.CrudRepository;

/**
 * CRUDメソッドを一切オーバーライドしないパターン。
 * save/findById/deleteById は継承由来（{@code SpringDataBaseMethod}）で解決され、
 * カスタムメソッドは @Query なしでメソッド名から種別を推測する。
 */
public interface NameRepository extends CrudRepository<Order, Long> {

    Order findByName(String name);
}
