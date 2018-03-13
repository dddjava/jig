package jig.domain.model.list;

public interface ModelTypeRepository {

    void register(ModelType modelType);

    ModelTypes find(ModelKind modelKind);
}
