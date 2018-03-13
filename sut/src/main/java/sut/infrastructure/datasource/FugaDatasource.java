package sut.infrastructure.datasource;

import org.springframework.stereotype.Repository;
import sut.domain.model.fuga.Fuga;
import sut.domain.model.fuga.FugaIdentifier;
import sut.domain.model.fuga.FugaRepository;

@Repository
public class FugaDatasource implements FugaRepository {

    FugaMapper mapper;
    private final PiyoMapper piyoMapper;

    public FugaDatasource(FugaMapper mapper, PiyoMapper piyoMapper) {
        this.mapper = mapper;
        this.piyoMapper = piyoMapper;
    }

    @Override
    public Fuga get(FugaIdentifier identifier) {
        piyoMapper.register();
        return mapper.get(identifier);
    }
}
