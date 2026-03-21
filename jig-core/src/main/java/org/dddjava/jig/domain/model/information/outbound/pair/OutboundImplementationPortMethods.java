package org.dddjava.jig.domain.model.information.outbound.pair;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.UsingMethods;
import org.dddjava.jig.domain.model.information.outbound.OutboundPortOperation;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public record OutboundImplementationPortMethods(Collection<OutboundPortOperation> values) {

    public OutboundImplementationPortMethods filter(UsingMethods usingMethods) {
        return values.stream()
                .filter(outboundPortOperation -> usingMethods.invokedMethodStream().anyMatch(invokedMethod -> invokedMethod.jigMethodIdIs(outboundPortOperation.jigMethodId())))
                .collect(collectingAndThen(toList(), OutboundImplementationPortMethods::new));
    }

    public List<JigMethod> list() {
        return values.stream()
                .sorted(Comparator.comparing(OutboundPortOperation::jigMethodId))
                .map(OutboundPortOperation::jigMethod)
                .toList();
    }
}
