package org.dddjava.jig.domain.model.information.method;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclarations;
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

    public boolean contains(MethodDeclaration methodDeclaration) {
        return list.stream()
                .anyMatch(item -> methodDeclaration.sameIdentifier(item));
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
