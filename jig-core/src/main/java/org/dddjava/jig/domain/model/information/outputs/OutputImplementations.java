package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.data.types.JavaTypeDeclarationKind;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * 出力ポート／アダプタの実装群
 */
public record OutputImplementations(Collection<OutputImplementation> values) {

    public boolean empty() {
        return values.isEmpty();
    }

    public Gateways repositoryMethods() {
        return values.stream().map(OutputImplementation::gateway)
                .collect(collectingAndThen(toList(), Gateways::new));
    }

    // FIXME これのテストがない
    public static OutputImplementations from(JigTypes jigTypes, OutputAdapters outputAdapters) {
        return outputAdapters.stream()
                // interfaceのRepository(Spring Data JDBCなど)は実装クラスが存在しないため、自身をoutput portとして扱う
                .flatMap(outputAdapter -> outputPorts(outputAdapter, jigTypes)
                        .flatMap(outputPort -> outputPort.gatewayStream()
                                // 実装しているinvocationが
                                .flatMap(gateway -> outputAdapter.resolveInvocation(gateway).stream()
                                        .map(invocation -> new OutputImplementation(gateway, invocation, outputPort)))))
                .collect(collectingAndThen(toList(), outputImplementations ->
                        new OutputImplementations(outputImplementations.stream()
                                .collect(collectingAndThen(
                                        java.util.stream.Collectors.toMap(
                                                outputImplementation -> outputImplementation.outputPortGateway().jigMethodId().namespace()
                                                        + "#" + outputImplementation.outputPortGateway().name(),
                                                Function.identity(),
                                                (existing, ignored) -> existing,
                                                LinkedHashMap::new),
                                        map -> List.copyOf(map.values()))))));
    }

    private static Stream<OutputPort> outputPorts(OutputAdapter outputAdapter, JigTypes jigTypes) {
        if (outputAdapter.jigType().jigTypeHeader().javaTypeDeclarationKind() == JavaTypeDeclarationKind.INTERFACE) {
            return Stream.of(new OutputPort(outputAdapter.jigType()));
        }
        return outputAdapter.implementsPortStream(jigTypes);
    }

    public Stream<OutputImplementation> stream() {
        return values.stream();
    }
}
