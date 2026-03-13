package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorsRepository;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 出力アダプタ
 */
public record OutputAdapter(JigType jigType, Collection<OutputPort> outputPorts, Collection<OutputAdapterExecution> outputAdapterExecutions) {

    public static OutputAdapter from(JigType jigType, JigTypes jigTypes, PersistenceAccessorsRepository persistenceAccessorsRepository) {
        var outputPorts = jigType.jigTypeHeader().interfaceTypeList()
                .stream()
                .flatMap(jigTypeReference -> jigTypes.resolveJigType(jigTypeReference.id()).stream())
                .map(OutputPort::new)
                .toList();

        var outputAdapterExecutions = jigType.instanceJigMethodStream()
                .map(jigMethod -> OutputAdapterExecution.from(jigMethod, jigTypes, persistenceAccessorsRepository))
                .toList();
        return new OutputAdapter(jigType, outputPorts, outputAdapterExecutions);
    }

    public Stream<OutputPort> implementsPortStream() {
        return outputPorts.stream();
    }

    /**
     * 操作に対する実行を取り出す
     *
     * ポートはアダプタに依存しないので起点がポートだと探すことになるが、これが必要な理由はいまいちわからない。
     */
    public Optional<OutputAdapterExecution> findExecution(OutputPortOperation outputPortOperation) {
        return outputAdapterExecutions.stream()
                .filter(outputAdapterExecution -> outputPortOperation.matches(outputAdapterExecution.jigMethod()))
                .findAny();
    }
}
