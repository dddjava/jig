package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.UsingMethods;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * 出力ポートの操作
 */
public record OutputPortOperations(Collection<OutputPortOperation> values) {

    public OutputPortOperations filter(UsingMethods usingMethods) {
        return values.stream()
                .filter(outputPortOperation -> usingMethods.invokedMethodStream().anyMatch(invokedMethod -> invokedMethod.jigMethodIdIs(outputPortOperation.jigMethodId())))
                .collect(collectingAndThen(toList(), OutputPortOperations::new));
    }

    public List<JigMethod> list() {
        return values.stream()
                .sorted(Comparator.comparing(OutputPortOperation::jigMethodId))
                .map(OutputPortOperation::jigMethod)
                .toList();
    }
}
