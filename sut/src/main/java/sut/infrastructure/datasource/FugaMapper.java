package sut.infrastructure.datasource;

import org.apache.ibatis.annotations.Mapper;
import sut.domain.model.fuga.Fuga;
import sut.domain.model.fuga.FugaIdentifier;

@Mapper
public interface FugaMapper {


    Fuga get(FugaIdentifier identifier);
}
