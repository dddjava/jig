package sut.infrastructure.datasource;

import org.springframework.stereotype.Repository;
import sut.domain.model.fuga.Fuga;
import sut.domain.model.fuga.FugaIdentifier;
import sut.domain.model.fuga.FugaRepository;

@Repository
public class FugaDatasource implements FugaRepository {

    FugaMapper mapper;
    private final AnnotationMapper annotationMapper;

    public FugaDatasource(FugaMapper mapper, AnnotationMapper annotationMapper) {
        this.mapper = mapper;
        this.annotationMapper = annotationMapper;
    }

    @Override
    public Fuga get(FugaIdentifier identifier) {
        annotationMapper.insert();
        return mapper.get(identifier);
    }

    @Override
    public void register(Fuga fuga) {
        // no-op
    }
}
