package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.data.members.instruction.InvokedMethod;
import org.dddjava.jig.domain.model.information.members.JigMethod;

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

    public RepositoryMethods filter(List<InvokedMethod> invokedMethods) {
        return list.stream()
                .filter(method -> invokedMethods.stream().anyMatch(invokedMethod -> invokedMethod.jigMethodIdentifierIs(method.jigMethodIdentifier())))
                .collect(collectingAndThen(toList(), RepositoryMethods::new));
    }

    public List<JigMethod> list() {
        return list;
    }
}
