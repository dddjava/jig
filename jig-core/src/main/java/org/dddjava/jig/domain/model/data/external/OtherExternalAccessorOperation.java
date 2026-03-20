package org.dddjava.jig.domain.model.data.external;

import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethod;

import java.util.Collection;

/**
 * 外部アクセサ（その他）操作
 *
 * JIG読み取り範囲外のクラスを外部型とし、そのメソッドを呼び出しているメソッドのこと。
 *
 * @param externalMethodCalls 外部型のメソッド呼び出し。これをもたないものは外部アクセサ操作ではないので、必ず1件以上。
 */
public record OtherExternalAccessorOperation(
        TypeId accessorTypeId,
        JigMethod accessorJigMethod,
        Collection<MethodCall> externalMethodCalls
) {
    public OtherExternalAccessorOperation {
        if (externalMethodCalls.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    public String accessorMethodName() {
        return accessorJigMethod.name();
    }
}
