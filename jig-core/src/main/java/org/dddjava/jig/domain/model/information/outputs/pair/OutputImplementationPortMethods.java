package org.dddjava.jig.domain.model.information.outputs.pair;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.UsingMethods;
import org.dddjava.jig.domain.model.information.outputs.OutputPortOperation;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public record OutputImplementationPortMethods(Collection<OutputPortOperation> values) {

    public OutputImplementationPortMethods filter(UsingMethods usingMethods) {
        return values.stream()
                .filter(outputPortOperation -> usingMethods.invokedMethodStream().anyMatch(invokedMethod -> invokedMethod.jigMethodIdIs(outputPortOperation.jigMethodId())))
                .collect(collectingAndThen(toList(), OutputImplementationPortMethods::new));
    }

    public List<JigMethod> list() {
        return values.stream()
                .sorted(Comparator.comparing(OutputPortOperation::jigMethodId))
                .map(OutputPortOperation::jigMethod)
                .toList();
    }
}
