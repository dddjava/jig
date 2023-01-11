package org.dddjava.jig.domain.model.sources.file.binary;

import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * バイナリソース一覧
 */
public class BinarySources {

    List<BinarySource> list;

    public BinarySources(List<BinarySource> list) {
        this.list = list;
    }

    public BinarySource toBinarySource() {
        return list.stream().reduce(new BinarySource(), BinarySource::merge);
    }

    public List<BinarySource> list() {
        return list;
    }

    public boolean nothing() {
        return list.isEmpty();
    }

    public List<String> classNames(Predicate<String> nameMatcher) {
        return list.stream()
                .flatMap(binarySource -> binarySource.classSources().list().stream())
                .map(classSource -> classSource.className())
                .filter(nameMatcher)
                .collect(toList());
    }
}
