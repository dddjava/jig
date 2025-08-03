package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.stream.Stream;

/**
 * 出力ポートとなるクラス
 */
public record OutputPort(JigType jigType) {

    public Stream<Gateway> gatewayStream() {
        return jigType().instanceJigMethodStream().map(Gateway::new);
    }
}
