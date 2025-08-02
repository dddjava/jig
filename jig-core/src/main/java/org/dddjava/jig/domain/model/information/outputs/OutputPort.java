package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.stream.Stream;

public record OutputPort(JigType jigType) {

    public Stream<Gateway> gatewayStream() {
        return jigType().instanceJigMethodStream().map(Gateway::new);
    }
}
