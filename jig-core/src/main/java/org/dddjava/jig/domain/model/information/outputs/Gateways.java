package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.UsingMethods;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * 出力ポートのゲートウェイ
 */
public record Gateways(List<JigMethod> list) {

    public Gateways filter(UsingMethods usingMethods) {
        return list.stream()
                .filter(method -> usingMethods.invokedMethodStream().anyMatch(invokedMethod -> invokedMethod.jigMethodIdIs(method.jigMethodId())))
                .collect(collectingAndThen(toList(), Gateways::new));
    }
}
