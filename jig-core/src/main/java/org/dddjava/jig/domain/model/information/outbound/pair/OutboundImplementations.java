package org.dddjava.jig.domain.model.information.outbound.pair;

import org.dddjava.jig.domain.model.data.types.JavaTypeDeclarationKind;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapter;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;
import org.dddjava.jig.domain.model.information.outbound.OutboundPort;

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
public record OutboundImplementations(Collection<OutboundImplementation> values) {

    public boolean empty() {
        return values.isEmpty();
    }

    public OutboundImplementationPortMethods repositoryMethods() {
        return values.stream().map(OutboundImplementation::outboundPortOperation)
                .collect(collectingAndThen(toList(), OutboundImplementationPortMethods::new));
    }

    // FIXME これのテストがない
    public static OutboundImplementations from(OutboundAdapters outboundAdapters) {
        return outboundAdapters.stream()
                // interfaceのRepository(Spring Data JDBCなど)は実装クラスが存在しないため、自身をoutput portとして扱う
                .flatMap(outboundAdapter -> outboundPorts(outboundAdapter)
                        .flatMap(outboundPort -> outboundPort.operationStream()
                                // 実装しているexecutionが
                                .flatMap(outboundPortOperation -> outboundAdapter.findExecution(outboundPortOperation).stream()
                                        .map(outboundAdapterExecution -> new OutboundImplementation(outboundPortOperation, outboundAdapterExecution, outboundPort)))))
                .collect(collectingAndThen(toList(), outputImplementations ->
                        new OutboundImplementations(outputImplementations.stream()
                                .collect(collectingAndThen(
                                        java.util.stream.Collectors.toMap(
                                                outboundImplementation -> outboundImplementation.outboundPortOperaionAsJigMethod().jigMethodId().namespace()
                                                        + "#" + outboundImplementation.outboundPortOperaionAsJigMethod().name(),
                                                Function.identity(),
                                                (existing, ignored) -> existing,
                                                LinkedHashMap::new),
                                        map -> List.copyOf(map.values()))))));
    }

    private static Stream<OutboundPort> outboundPorts(OutboundAdapter outboundAdapter) {
        if (outboundAdapter.jigType().jigTypeHeader().javaTypeDeclarationKind() == JavaTypeDeclarationKind.INTERFACE) {
            return Stream.of(new OutboundPort(outboundAdapter.jigType()));
        }
        return outboundAdapter.implementsPortStream();
    }

    public Stream<OutboundImplementation> stream() {
        return values.stream();
    }
}
