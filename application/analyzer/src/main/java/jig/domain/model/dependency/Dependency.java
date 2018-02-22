package jig.domain.model.dependency;

import java.util.ArrayList;
import java.util.List;

public class Dependency {

    final List<Model> models = new ArrayList<>();

    void add(Model model) {
        models.add(model);
    }

    public boolean empty() {
        return models.isEmpty();
    }

    public List<Model> list() {
        return models;
    }
}
