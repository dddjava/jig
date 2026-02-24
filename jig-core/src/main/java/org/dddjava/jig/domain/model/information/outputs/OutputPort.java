package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.stream.Stream;

/**
 * 出力ポートとなるクラス
 */
public record OutputPort(JigType jigType) {

    public Stream<OutputPortOperation> operationStream() {
        return jigType().instanceJigMethodStream().map(OutputPortOperation::new);
    }
}
