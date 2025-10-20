package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 呼び出し元メソッド一覧
 */
public record CallerMethods(Set<JigMethodId> values) {

    public boolean contains(JigMethodId jigMethodId) {
        return values.stream()
                .anyMatch(item -> item.equals(jigMethodId));
    }

    public int size() {
        return values.size();
    }

    public Collection<JigMethodId> filter(Predicate<JigMethodId> predicate) {
        return values.stream().filter(predicate).toList();
    }

    public long typeCount() {
        return values.stream().map(JigMethodId::namespace).distinct().count();
    }
}
