package stub.infrastructure.datasource.fuga;

import org.springframework.stereotype.Repository;
import stub.domain.model.type.fuga.Fuga;
import stub.domain.model.type.fuga.FugaIdentifier;
import stub.domain.model.type.fuga.FugaRepository;

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
