package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.UsingMethods;
import org.dddjava.jig.domain.model.information.types.JigType;

/**
 * 出力ポート／アダプタの実装
 */
public record OutputImplementation(Gateway gateway, Invocation invocation, OutputPort outputPort) {

    public UsingMethods usingMethods() {
        return concreteMethod().usingMethods();
    }

    public JigMethod outputPortGateway() {
        return gateway.jigMethod();
    }

    public JigMethod concreteMethod() {
        return invocation.jigMethod();
    }

    public JigType interfaceJigType() {
        return outputPort.jigType();
    }
}
