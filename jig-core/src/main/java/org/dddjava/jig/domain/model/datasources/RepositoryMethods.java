package org.dddjava.jig.domain.model.datasources;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.analyzed.unit.method.Method;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * リポジトリインタフェースメソッド
 */
public class RepositoryMethods {

    List<Method> list;

    RepositoryMethods(List<Method> list) {
        this.list = list;
    }

    public String asSimpleText() {
        return list.stream().map(Method::declaration)
                .collect(MethodDeclarations.collector())
                .asSimpleText();
    }

    public RepositoryMethods filter(MethodDeclarations methodDeclarations) {
        return list.stream()
                .filter(method -> methodDeclarations.contains(method.declaration()))
                .collect(collectingAndThen(toList(), RepositoryMethods::new));
    }

    public List<Method> list() {
        return list;
    }
}
