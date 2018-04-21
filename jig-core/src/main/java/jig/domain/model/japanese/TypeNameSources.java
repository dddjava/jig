package jig.domain.model.japanese;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class TypeNameSources {

    List<Path> list;

    public TypeNameSources(List<Path> list) {
        this.list = list;
    }

    public TypeNames toTypeNames(Function<Path, Optional<TypeJapaneseName>> function) {
        List<TypeJapaneseName> names = new ArrayList<>();
        for (Path path : list) {
            function.apply(path).ifPresent(names::add);
        }
        return new TypeNames(names);
    }
}
