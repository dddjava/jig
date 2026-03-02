package stub.infrastructure.datasource.trace;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TraceMapper {
    boolean binding(String key);
}
