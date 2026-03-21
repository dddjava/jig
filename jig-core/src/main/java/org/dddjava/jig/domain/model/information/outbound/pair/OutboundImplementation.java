package org.dddjava.jig.domain.model.information.outbound.pair;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapterExecution;
import org.dddjava.jig.domain.model.information.outbound.OutboundPort;
import org.dddjava.jig.domain.model.information.outbound.OutboundPortOperation;
import org.dddjava.jig.domain.model.information.types.JigType;

/**
 * 出力ポート／アダプタの実装
 */
public record OutboundImplementation(OutboundPortOperation outboundPortOperation,
                                     OutboundAdapterExecution outboundAdapterExecution, OutboundPort outboundPort) {

    public JigMethod outboundPortOperaionAsJigMethod() {
        return outboundPortOperation.jigMethod();
    }

    public JigMethod concreteMethod() {
        return outboundAdapterExecution.jigMethod();
    }

    public JigType interfaceJigType() {
        return outboundPort.jigType();
    }
}
