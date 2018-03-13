package jig.infrastructure;

import jig.domain.model.list.kind.ModelKind;
import jig.domain.model.list.ModelType;
import jig.domain.model.list.ModelTypeRepository;
import jig.domain.model.list.ModelTypes;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class OnMemoryModelTypeRepository implements ModelTypeRepository {

    List<ModelType> list = new ArrayList<>();

    @Override
    public void register(ModelType modelType) {
        list.add(modelType);
    }

    @Override
    public ModelTypes find(ModelKind modelKind) {
        return new ModelTypes(list.stream().filter(modelKind::correct).collect(toList()));
    }
}
