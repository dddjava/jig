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

    public Optional<Invocation> resolveInvocation(OutputPortOperation outputPortOperation) {
        return jigType.instanceJigMethodStream()
                .filter(jigMethod -> outputPortOperation.matches(jigMethod))
                .map(Invocation::new)
                .findAny();
    }
}
