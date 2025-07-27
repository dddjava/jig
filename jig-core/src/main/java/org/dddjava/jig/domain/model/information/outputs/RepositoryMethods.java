package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.UsingMethods;

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

    public RepositoryMethods filter(UsingMethods usingMethods) {
        return list.stream()
                .filter(method -> usingMethods.invokedMethodStream().anyMatch(invokedMethod -> invokedMethod.jigMethodIdIs(method.jigMethodId())))
                .collect(collectingAndThen(toList(), RepositoryMethods::new));
    }

    public List<JigMethod> list() {
        return list;
    }
}
