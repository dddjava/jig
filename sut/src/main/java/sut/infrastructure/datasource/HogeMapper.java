package sut.infrastructure.datasource;

import org.apache.ibatis.annotations.Mapper;
import sut.domain.model.hoge.Hoge;

@Mapper
public interface HogeMapper {

    Hoge findOne();

    void register(Hoge hoge);
}
