package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;

import java.util.List;

/**
 * 呼び出しメソッド一覧
 */
public class CallerMethods {
    List<MethodDeclaration> list;

    public CallerMethods(List<MethodDeclaration> list) {
        this.list = list;
    }

    public boolean contains(JigMethodIdentifier jigMethodIdentifier) {
        return list.stream()
                .anyMatch(item -> item.sameIdentifier(jigMethodIdentifier));
    }

    public int size() {
        return list.size();
    }

    public TypeIdentifiers toDeclareTypes() {
        return list.stream()
                .map(item -> item.declaringType())
                .sorted()
                .distinct()
                .collect(TypeIdentifiers.collector());
    }

    public MethodDeclarations methodDeclarations() {
        return new MethodDeclarations(list);
    }
}
