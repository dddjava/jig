package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodIdentifier;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * 呼び出しメソッド一覧
 */
public record CallerMethods(List<JigMethodIdentifier> list) {

    public boolean contains(JigMethodIdentifier jigMethodIdentifier) {
        return list.stream()
                .anyMatch(item -> item.equals(jigMethodIdentifier));
    }

    public int size() {
        return list.size();
    }

    public Collection<JigMethodIdentifier> jigMethodIdentifiers(Predicate<JigMethodIdentifier> predicate) {
        return list.stream().filter(predicate).toList();
    }

    public long typeCount() {
        return list.stream().map(JigMethodIdentifier::namespace).distinct().count();
    }
}
