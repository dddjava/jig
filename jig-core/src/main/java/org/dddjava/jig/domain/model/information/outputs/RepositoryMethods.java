package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
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

    public RepositoryMethods filter(List<MethodCall> methodCalls) {
        return list.stream()
                .filter(method -> methodCalls.stream().anyMatch(invokedMethod -> invokedMethod.jigMethodIdIs(method.jigMethodIdentifier())))
                .collect(collectingAndThen(toList(), RepositoryMethods::new));
    }

    public List<JigMethod> list() {
        return list;
    }
}
