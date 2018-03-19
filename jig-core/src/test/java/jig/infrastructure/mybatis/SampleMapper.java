package jig.infrastructure.mybatis;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SampleMapper {

    boolean simple(String key);

    boolean binding(String key);
}
