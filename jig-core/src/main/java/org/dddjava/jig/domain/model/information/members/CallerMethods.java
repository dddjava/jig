package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodIdentifier;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 呼び出しメソッド一覧
 */
public record CallerMethods(Set<JigMethodIdentifier> methodIdentifiers) {

    public boolean contains(JigMethodIdentifier jigMethodIdentifier) {
        return methodIdentifiers.stream()
                .anyMatch(item -> item.equals(jigMethodIdentifier));
    }

    public int size() {
        return methodIdentifiers.size();
    }

    public Collection<JigMethodIdentifier> jigMethodIdentifiers(Predicate<JigMethodIdentifier> predicate) {
        return methodIdentifiers.stream().filter(predicate).toList();
    }

    public long typeCount() {
        return methodIdentifiers.stream().map(JigMethodIdentifier::namespace).distinct().count();
    }
}
