package jig.domain.model.japanese;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class PackageNameSources {

    List<Path> list;

    public PackageNameSources(List<Path> list) {
        this.list = list;
    }

    public PackageNames toPackageNames(Function<Path, Optional<PackageJapaneseName>> function) {
        List<PackageJapaneseName> names = new ArrayList<>();
        for (Path path : list) {
            function.apply(path).ifPresent(names::add);
        }
        return new PackageNames(names);
    }
}
