package org.dddjava.jig.domain.model.models.applications.backends;

import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * リポジトリインタフェースメソッド
 */
public class RepositoryMethods {

    List<JigMethod> list;

    RepositoryMethods(List<JigMethod> list) {
        this.list = list;
    }

    public String asSimpleText() {
        return list.stream().map(JigMethod::declaration)
                .collect(MethodDeclarations.collector())
                .asSimpleText();
    }

    public RepositoryMethods filter(MethodDeclarations methodDeclarations) {
        return list.stream()
                .filter(method -> methodDeclarations.contains(method.declaration()))
                .collect(collectingAndThen(toList(), RepositoryMethods::new));
    }

    public List<JigMethod> list() {
        return list;
    }
}
