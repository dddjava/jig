package org.dddjava.jig.domain.model.sources.classsources;

import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * バイナリソース一覧
 */
public record ClassSources(List<ClassSource> list) {

    public boolean nothing() {
        return list.isEmpty();
    }

    public List<ClassSource> filterClassName(Predicate<String> nameMatcher) {
        return list.stream()
                .filter(classSource -> nameMatcher.test(classSource.className()))
                .collect(toList());
    }
}
