package stub.infrastructure.datasource.fuga;

import org.apache.ibatis.annotations.Mapper;
import stub.domain.model.type.fuga.Fuga;
import stub.domain.model.type.fuga.FugaIdentifier;

@Mapper
public interface FugaMapper {

    Fuga get(FugaIdentifier identifier);
}
