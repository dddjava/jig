package jig.domain.model.japanese;

import java.nio.file.Path;
import java.util.List;

public class TypeNameSources {

    List<Path> list;

    public TypeNameSources(List<Path> list) {
        this.list = list;
    }

    public List<Path> list() {
        return list;
    }
}
