package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.UsingMethods;
import org.dddjava.jig.domain.model.information.types.JigType;

/**
 * 出力ポート／アダプタの実装
 */
public record OutputImplementation(JigMethod outputPortGateway, JigMethod concreteMethod, JigType interfaceJigType) {

    public UsingMethods usingMethods() {
        return concreteMethod().usingMethods();
    }
}
