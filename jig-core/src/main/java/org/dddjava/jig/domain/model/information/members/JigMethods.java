package org.dddjava.jig.domain.model.information.members;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * メソッド一覧
 */
public record JigMethods(Collection<JigMethod> methods) {

    /**
     * 注目に値するメソッド一覧
     *
     * 「注目に値する」メソッドを可視性順で並べたもの。
     * 主に概要での出力に使用する。
     */
    public List<JigMethod> listRemarkable() {
        return stream()
                .filter(JigMethod::remarkable)
                .sorted(Comparator
                        .comparing(JigMethod::visibility)
                        .thenComparing(jigMethod -> jigMethod.jigMethodId().value()))
                .toList();
    }

    public List<JigMethod> list() {
        return stream()
                .sorted(Comparator
                        .comparing(JigMethod::visibility)
                        .thenComparing(jigMethod -> jigMethod.jigMethodId().value()))
                .toList();
    }

    public boolean empty() {
        return methods.isEmpty();
    }

    public JigMethods filterProgrammerDefined() {
        return new JigMethods(stream()
                .filter(jigMethod -> jigMethod.isProgrammerDefined())
                .toList());
    }

    public JigMethods excludeNotNoteworthyObjectMethod() {
        return new JigMethods(stream()
                .filter(jigMethod -> !jigMethod.isObjectMethod() || jigMethod.documented())
                .toList());
    }

    public Stream<JigMethod> stream() {
        return methods.stream();
    }

}
