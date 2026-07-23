package org.dddjava.jig.infrastructure.mybatis.ut;

import org.apache.ibatis.annotations.Mapper;

/**
 * OGNL（{@code ${...}}）を使ったSQLは静的解析できないため、解析失敗として扱われることを確認する。
 */
@Mapper
public interface UnparseableMapper {

    Object selectOgnl();

    Object selectOgnlWhere();
}
