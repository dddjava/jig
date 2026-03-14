package org.dddjava.jig.domain.model.information.outputs.pair;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.outputs.OutputAdapterExecution;
import org.dddjava.jig.domain.model.information.outputs.OutputPort;
import org.dddjava.jig.domain.model.information.outputs.OutputPortOperation;
import org.dddjava.jig.domain.model.information.types.JigType;

/**
 * 出力ポート／アダプタの実装
 */
public record OutputImplementation(OutputPortOperation outputPortOperation,
                                   OutputAdapterExecution outputAdapterExecution, OutputPort outputPort) {

    public JigMethod outputPortOperaionAsJigMethod() {
        return outputPortOperation.jigMethod();
    }

    public JigMethod concreteMethod() {
        return outputAdapterExecution.jigMethod();
    }

    public JigType interfaceJigType() {
        return outputPort.jigType();
    }
}
