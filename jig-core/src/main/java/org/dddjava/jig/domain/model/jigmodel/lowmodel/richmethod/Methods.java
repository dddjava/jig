package org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * メソッド一覧
 */
public class Methods {
    List<Method> list;

    public Methods(List<Method> list) {
        this.list = list;
    }

    public MethodDeclarations declarations() {
        return list.stream().map(Method::declaration).collect(MethodDeclarations.collector());
    }

    public List<Method> list() {
        list.sort(Comparator.comparing(method -> method.declaration().asFullNameText()));
        return list;
    }

    public Method get(MethodDeclaration methodDeclaration) {
        for (Method method : list) {
            if (method.declaration().sameIdentifier(methodDeclaration)) {
                return method;
            }
        }
        throw new NoSuchElementException(methodDeclaration.asFullNameText());
    }

    public boolean empty() {
        return list.isEmpty();
    }
}
