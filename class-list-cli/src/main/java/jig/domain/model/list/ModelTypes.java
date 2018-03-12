package jig.domain.model.list;

import java.util.List;

public class ModelTypes {

    private final List<ModelType> list;

    public ModelTypes(List<ModelType> list) {
        this.list = list;
    }

    public List<ModelType> list() {
        return list;
    }
}
