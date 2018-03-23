package stub.infrastructure.datasource;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SampleMapper {

    boolean binding(String key);
}
