package org.dddjava.jig.infrastructure.mybatis.ut;

import org.apache.ibatis.annotations.*;

/**
 * XMLベースとアノテーションベースのCRUDが同じテーブルに対して認識できることを確認する。
 */
@Mapper
public interface CrudMapper {

    void insert();

    void select();

    void update();

    void delete();

    @Insert("insert into crud_test(a) values('a')")
    void annotationInsert();

    @Select("select * from crud_test")
    void annotationSelect();

    @Update("update crud_test set a = 'b'")
    void annotationUpdate();

    @Delete("delete from crud_test")
    void annotationDelete();
}
