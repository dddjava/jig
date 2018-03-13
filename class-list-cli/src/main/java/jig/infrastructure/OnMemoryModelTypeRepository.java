package jig.infrastructure;

import jig.domain.model.list.ModelType;
import jig.domain.model.list.ModelTypeRepository;
import jig.domain.model.list.ModelTypes;
import jig.domain.model.list.kind.ModelKind;

import java.util.ArrayList;
import java.util.List;

public class OnMemoryModelTypeRepository implements ModelTypeRepository {

    List<ModelType> list = new ArrayList<>();

    @Override
    public void register(ModelType modelType) {
        list.add(modelType);
    }

    @Override
    public ModelTypes find(ModelKind modelKind) {
        return null;
    }
}
