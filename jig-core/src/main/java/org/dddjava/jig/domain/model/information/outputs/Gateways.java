package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.UsingMethods;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * 出力ポートのゲートウェイ
 */
public record Gateways(Collection<JigMethod> values) {

    public Gateways filter(UsingMethods usingMethods) {
        return values.stream()
                .filter(method -> usingMethods.invokedMethodStream().anyMatch(invokedMethod -> invokedMethod.jigMethodIdIs(method.jigMethodId())))
                .collect(collectingAndThen(toList(), Gateways::new));
    }

    public List<JigMethod> list() {
        return values.stream()
                .sorted(Comparator.comparing(JigMethod::fqn))
                .toList();
    }
}
