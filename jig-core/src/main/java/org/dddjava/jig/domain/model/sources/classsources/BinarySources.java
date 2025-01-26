package org.dddjava.jig.domain.model.sources.classsources;

import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * バイナリソース一覧
 */
public class BinarySources {

    private final List<ClassSource> list;

    public BinarySources(List<ClassSource> list) {
        this.list = list;
    }

    public boolean nothing() {
        return list.isEmpty();
    }

    public List<String> classNames(Predicate<String> nameMatcher) {
        return list.stream()
                .map(classSource -> classSource.className())
                .filter(nameMatcher)
                .collect(toList());
    }

    public ClassSources classSources() {
        return new ClassSources(list);
    }
}
