package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.external.ExternalAccessorRepositories;
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

    public static OutputAdapter from(JigType jigType, JigTypes jigTypes, ExternalAccessorRepositories accessorRepositories) {
        // アダプタ視点では実装しているインタフェースがJigTypesに含まれるのであれば区別なくポートとして扱う
        // 通常は1件を想定するが、2件以上あっても問題ない。
        // 0件はポート＝アダプタの実装だと考えられるが、ひとまず考慮しない。
        var outputPorts = jigType.jigTypeHeader().interfaceTypeList()
                .stream()
                .flatMap(jigTypeReference -> jigTypes.resolveJigType(jigTypeReference.id()).stream())
                .map(OutputPort::new)
                .toList();

        // メソッドがPortOperationの実装であればAdapterExecutionとなる
        var outputAdapterExecutions = jigType.instanceJigMethodStream()
                // インタフェースの実装なのでpublicのみ（プライベートメソッドが多いクラスで無駄に検索しないように）
                .filter(jigMethod -> jigMethod.isPublic())
                .flatMap(jigMethod -> {
                    // 実装しているPortOperationを収集する
                    // 通常はないが、複数のインタフェースを同時に実装している場合もあるのでCollectionとする
                    Collection<OutputPortOperation> outputPortOperations = outputPorts.stream()
                            .flatMap(outputPort -> outputPort.operationStream())
                            .filter(outputPortOperation -> outputPortOperation.matches(jigMethod))
                            .toList();
                    // なければAdapterExecutionではない
                    if (outputPortOperations.isEmpty()) {
                        return Stream.empty();
                    }
                    return Stream.of(OutputAdapterExecution.from(jigMethod, outputPortOperations, jigTypes, accessorRepositories));
                })
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
                .filter(outputAdapterExecution -> outputAdapterExecution
                        .implementOperations()
                        .stream().anyMatch(ops -> ops.equals(outputPortOperation)))
                .findAny();
    }
}
