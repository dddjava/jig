package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.members.JigMethod;

public record Gateway(JigMethod jigMethod) {

    public boolean matches(JigMethod invocationJigMethod) {
        var invocationJigMethodId = invocationJigMethod.jigMethodId();
        var jigMethodId = jigMethod.jigMethodId();

        // メソッド名と引数の型が一致すればマッチ
        // TODO ジェネリクス対応できてなさそう
        return jigMethodId.name().equals(invocationJigMethod.name())
                && jigMethodId.tuple().parameterTypeNameList().equals(invocationJigMethodId.tuple().parameterTypeNameList());
    }
}
