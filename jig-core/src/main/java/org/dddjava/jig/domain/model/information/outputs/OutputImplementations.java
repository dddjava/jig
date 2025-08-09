package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 出力ポート／アダプタの実装群
 */
public record OutputImplementations(Collection<OutputImplementation> values) {

    public boolean empty() {
        return values.isEmpty();
    }

    public Gateways repositoryMethods() {
        return values.stream().map(OutputImplementation::outputPortGateway)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Gateways::new));
    }

    // FIXME これのテストがない
    public static OutputImplementations from(JigTypes jigTypes, OutputAdapters outputAdapters) {
        return outputAdapters.stream()
                // output adapterの実装しているoutput portのgatewayを
                .flatMap(outputAdapter -> outputAdapter.implementsPortStream(jigTypes)
                        .flatMap(outputPort -> outputPort.gatewayStream()
                                // 実装しているinvocationが
                                .flatMap(gateway -> outputAdapter.resolveInvocation(gateway).stream()
                                        .map(invocation -> new OutputImplementation(gateway.jigMethod(), invocation.jigMethod(), outputPort.jigType())))))
                .collect(Collectors.collectingAndThen(Collectors.toList(), OutputImplementations::new));
    }
}
