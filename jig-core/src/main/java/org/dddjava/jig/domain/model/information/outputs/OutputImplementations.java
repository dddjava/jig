package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.types.JavaTypeDeclarationKind;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * 出力ポート／アダプタの実装群
 *
 * TODO: 一覧出力で使用するための橋渡し。不要にしたい。
 */
public record OutputImplementations(Collection<OutputImplementation> values) {

    public boolean empty() {
        return values.isEmpty();
    }

    public OutputPortOperations repositoryMethods() {
        return values.stream().map(OutputImplementation::outputPortOperation)
                .collect(collectingAndThen(toList(), OutputPortOperations::new));
    }

    // FIXME これのテストがない
    public static OutputImplementations from(JigTypes jigTypes, OutputAdapters outputAdapters) {
        return outputAdapters.stream()
                // interfaceのRepository(Spring Data JDBCなど)は実装クラスが存在しないため、自身をoutput portとして扱う
                .flatMap(outputAdapter -> outputPorts(outputAdapter, jigTypes)
                        .flatMap(outputPort -> outputPort.operationStream()
                                // 実装しているexecutionが
                                .flatMap(outputPortOperation -> outputAdapter.findExecution(outputPortOperation).stream()
                                        .map(outputAdapterExecution -> new OutputImplementation(outputPortOperation, outputAdapterExecution, outputPort)))))
                .collect(collectingAndThen(toList(), outputImplementations ->
                        new OutputImplementations(outputImplementations.stream()
                                .collect(collectingAndThen(
                                        java.util.stream.Collectors.toMap(
                                                outputImplementation -> outputImplementation.outputPortOperaionAsJigMethod().jigMethodId().namespace()
                                                        + "#" + outputImplementation.outputPortOperaionAsJigMethod().name(),
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
