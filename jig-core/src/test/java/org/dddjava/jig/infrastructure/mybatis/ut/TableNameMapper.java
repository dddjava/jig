package org.dddjava.jig.infrastructure.mybatis.ut;

import org.apache.ibatis.annotations.Mapper;

/**
 * SQLの書式のばらつき（タブ区切り・非ASCII識別子・シーケンス関数・結合）から
 * テーブル名を抽出できることを確認する。
 */
@Mapper
public interface TableNameMapper {

    void tabInsert();

    void tabSelect();

    void tabUpdate();

    void tabDelete();

    String japanese();

    int sequencePostgresql();

    void joinSelect();

    void leftJoinSelect();
}
