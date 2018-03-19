package jig.infrastructure.mybatis;

import org.apache.ibatis.annotations.*;

@Mapper
public interface CanonicalMapper {

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

    @Select("select * from あのスキーマ.このテーブル")
    void japanese();

    @Select("SQLじゃない何かが入っているとか")
    void illegal();
}
