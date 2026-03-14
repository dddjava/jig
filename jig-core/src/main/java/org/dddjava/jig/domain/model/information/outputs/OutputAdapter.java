package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorsRepository;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 出力アダプタ
 *
 * @param implementPorts どのポートに対するアダプタなのかを示す
 * @param executions     このアダプタに実装されている操作
 */
public record OutputAdapter(
        JigType jigType,
        Collection<OutputPort> implementPorts,
        Collection<OutputAdapterExecution> executions
) {

    public static OutputAdapter from(JigType jigType, JigTypes jigTypes, PersistenceAccessorsRepository persistenceAccessorsRepository) {
        var outputPorts = jigType.jigTypeHeader().interfaceTypeList()
                .stream()
                .flatMap(jigTypeReference -> jigTypes.resolveJigType(jigTypeReference.id()).stream())
                .map(OutputPort::new)
                .toList();

        var outputAdapterExecutions = outputPorts.stream()
                .flatMap(outputPort -> outputPort.operationStream()
                        .flatMap(operation -> jigType.instanceJigMethodStream()
                                .filter(operation::matches)
                                .map(jigMethod -> OutputAdapterExecution.from(operation, jigMethod, jigTypes, persistenceAccessorsRepository))))
                .toList();
        return new OutputAdapter(jigType, outputPorts, outputAdapterExecutions);
    }

    public Stream<OutputPort> implementsPortStream() {
        return implementPorts.stream();
    }

    /**
     * 操作に対する実行を取り出す
     */
    public Optional<OutputAdapterExecution> findExecution(OutputPortOperation outputPortOperation) {
        return executions.stream()
                .filter(outputAdapterExecution -> outputAdapterExecution.outputPortOperation().equals(outputPortOperation))
                .findAny();
    }
}
