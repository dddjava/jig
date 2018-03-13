package jig.domain.model.list;

import jig.domain.model.list.kind.ModelKind;

public interface ModelTypeRepository {

    void register(ModelType modelType);

    ModelTypes find(ModelKind modelKind);
}
