package sut.infrastructure.datasource;

import org.springframework.stereotype.Repository;
import sut.domain.model.fuga.Fuga;
import sut.domain.model.fuga.FugaIdentifier;
import sut.domain.model.fuga.FugaRepository;

@Repository
public class FugaDatasource implements FugaRepository {

    FugaMapper mapper;

    public FugaDatasource(FugaMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Fuga get(FugaIdentifier identifier) {
        return mapper.get(identifier);
    }
}
