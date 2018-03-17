package sut.infrastructure.datasource;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnnotationMapper {

    @Select("SELECT value from sut.piyo")
    List<String> select();

    @Insert("INSERT into sut.piyo(id, value) values(1, 'x')")
    void insert();

    @Update("UPDATE sut.piyo SET value = 'a'")
    void update();

    @Delete("DELETE FROM sut.piyo")
    void delete();
}
