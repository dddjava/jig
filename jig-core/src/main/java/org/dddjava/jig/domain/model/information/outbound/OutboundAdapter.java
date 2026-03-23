package org.dddjava.jig.domain.model.information.outbound;

import org.dddjava.jig.domain.model.data.types.JavaTypeDeclarationKind;
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
public record OutboundAdapter(
        JigType jigType,
        Collection<OutboundPort> implementPorts,
        Collection<OutboundAdapterExecution> executions
) {

    public static OutboundAdapter from(JigType jigType, JigTypes jigTypes, ExternalAccessorRepositories accessorRepositories) {
        // アダプタ視点では実装しているインタフェースがJigTypesに含まれるのであれば区別なくポートとして扱う
        // 通常は1件を想定するが、2件以上あっても問題ない。
        // 0件はポート＝アダプタの実装だと考えられるが、ひとまず考慮しない。
        var outboundPorts = jigType.jigTypeHeader().interfaceTypeList()
                .stream()
                .flatMap(jigTypeReference -> jigTypes.resolveJigType(jigTypeReference.id()).stream())
                .map(OutboundPort::new)
                .toList();

        // メソッドがPortOperationの実装であればAdapterExecutionとなる
        var outboundAdapterExecutions = jigType.instanceJigMethodStream()
                // インタフェースの実装なのでpublicのみ（プライベートメソッドが多いクラスで無駄に検索しないように）
                .filter(jigMethod -> jigMethod.isPublic())
                .flatMap(jigMethod -> {
                    // 実装しているPortOperationを収集する
                    // 通常はないが、複数のインタフェースを同時に実装している場合もあるのでCollectionとする
                    Collection<OutboundPortOperation> outboundPortOperations = outboundPorts.stream()
                            .flatMap(outboundPort -> outboundPort.operationStream())
                            .filter(outboundPortOperation -> outboundPortOperation.matches(jigMethod))
                            .toList();
                    // なければAdapterExecutionではない
                    if (outboundPortOperations.isEmpty()) {
                        return Stream.empty();
                    }
                    return Stream.of(OutboundAdapterExecution.from(jigMethod, outboundPortOperations, jigTypes, accessorRepositories));
                })
                .toList();

        return new OutboundAdapter(jigType, outboundPorts, outboundAdapterExecutions);
    }

    public Stream<OutboundPort> outboundPortStream() {
        // 自身がインタフェースの場合は自身も加える
        // MEMO: というかimplementsPortStreamの時点で含まれていていいのでは？
        var jigType = jigType();
        if (jigType.jigTypeHeader().javaTypeDeclarationKind() == JavaTypeDeclarationKind.INTERFACE) {
            return Stream.concat(Stream.of(new OutboundPort(jigType)), implementsPortStream());
        }
        return implementsPortStream();
    }

    public Stream<OutboundPort> implementsPortStream() {
        return implementPorts.stream();
    }

    /**
     * 操作に対する実行を取り出す
     */
    public Optional<OutboundAdapterExecution> findExecution(OutboundPortOperation outboundPortOperation) {
        return executions.stream()
                .filter(outboundAdapterExecution -> outboundAdapterExecution
                        .implementOperations()
                        .stream().anyMatch(ops -> ops.equals(outboundPortOperation)))
                .findAny();
    }
}
