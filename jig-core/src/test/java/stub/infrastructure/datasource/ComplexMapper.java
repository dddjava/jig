package stub.infrastructure.datasource;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ComplexMapper {

    Object select_ognl();
}
