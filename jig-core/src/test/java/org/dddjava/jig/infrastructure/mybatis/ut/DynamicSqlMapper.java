package org.dddjava.jig.infrastructure.mybatis.ut;

import org.apache.ibatis.annotations.Mapper;

/**
 * {@code <bind>} や {@code <include>} を使った動的SQLでもテーブル名を抽出できることを確認する。
 */
@Mapper
public interface DynamicSqlMapper {

    boolean binding(String key);
}
