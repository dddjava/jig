package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.members.instruction.InvokedMethod;
import org.dddjava.jig.domain.model.information.members.JigMethod;

import java.util.List;
import java.util.stream.Collectors;

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
        return list.stream()
                .map(JigMethod::nameAndArgumentSimpleText)
                .collect(Collectors.joining(", ", "[", "]"));
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
