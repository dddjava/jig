package org.dddjava.jig.domain.model.information.outbound.springdata.ut;

import common.MyCrudRepository;

/**
 * 共通ライブラリの {@link MyCrudRepository} を継承するケース。
 * MyCrudRepository はテスト側で解析対象（buildJigTypes の引数）に含めないため、
 * 名前と型引数からのヒューリスティックで Spring Data Repository と推測されることを確認する。
 */
public interface CustomBaseRepository extends MyCrudRepository<Order, Long> {
}
