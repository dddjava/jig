package sut.infrastructure.datasource;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PiyoMapper {

    @Insert("INSERT INTO piyo() VALUES ()")
    void register();
}
