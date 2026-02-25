package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 出力アダプタとなるクラス
 */
public record OutputAdapter(JigType jigType) {

    public Stream<OutputPort> implementsPortStream(JigTypes contextJigTypes) {
        return jigType().jigTypeHeader().interfaceTypeList()
                .stream()
                .flatMap(jigTypeReference -> contextJigTypes.resolveJigType(jigTypeReference.id()).stream())
                .map(OutputPort::new);
    }

    /**
     * 操作に対する実行を取り出す
     *
     * ポートはアダプタに依存しないので起点がポートだと探すことになるが、これが必要な理由はいまいちわからない。
     */
    public Optional<OutputAdapterExecution> findExecution(OutputPortOperation outputPortOperation) {
        return jigType.instanceJigMethodStream()
                .filter(jigMethod -> outputPortOperation.matches(jigMethod))
                .map(OutputAdapterExecution::new)
                .findAny();
    }
}
