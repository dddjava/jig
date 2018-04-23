package jig.domain.model.japanese;

import java.nio.file.Path;
import java.util.List;

public class PackageNameSources {

    List<Path> list;

    public PackageNameSources(List<Path> list) {
        this.list = list;
    }

    public List<Path> list() {
        return list;
    }
}
