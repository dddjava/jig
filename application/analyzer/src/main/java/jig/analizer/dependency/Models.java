package jig.analizer.dependency;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

public class Models {

    Set<Model> models = new HashSet<>();

    public void register(Model model) {
        models.add(model);
    }

    public boolean notExists(FullQualifiedName name) {
        return models.stream().noneMatch(model -> model.matches(name));
    }

    public Model get(FullQualifiedName name) {
        return models.stream()
                .filter(model -> model.matches(name))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    public String format(ModelFormatter formatter) {
        return models.stream()
                .filter(Model::hasDependency)
                .map(formatter::format)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
