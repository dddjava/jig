package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 呼び出しメソッド一覧
 */
public record CallerMethods(Set<JigMethodId> methodIdentifiers) {

    public boolean contains(JigMethodId jigMethodId) {
        return methodIdentifiers.stream()
                .anyMatch(item -> item.equals(jigMethodId));
    }

    public int size() {
        return methodIdentifiers.size();
    }

    public Collection<JigMethodId> filter(Predicate<JigMethodId> predicate) {
        return methodIdentifiers.stream().filter(predicate).toList();
    }

    public long typeCount() {
        return methodIdentifiers.stream().map(JigMethodId::namespace).distinct().count();
    }
}
