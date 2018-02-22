package jig.domain.model.dependency;

import java.nio.file.Path;
import java.util.List;

public class SearchPaths {

    final List<Path> list;

    public SearchPaths(List<Path> list) {
        this.list = list;
    }
}
