package stub.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SampleMapper {

    boolean binding(String key);
}
