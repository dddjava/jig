package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;

/**
 * ハンドラ
 *
 * 外部からのリクエストを受け取る起点となるメソッドです。
 * 制限事項：RequestMappingをメタアノテーションとした独自アノテーションが付与されたメソッドは、ハンドラとして扱われません。
 */
public record EntrypointMethod(EntrypointType entrypointType, JigType jigType, JigMethod jigMethod) {

    public boolean anyMatch(CallerMethods callerMethods) {
        return callerMethods.contains(jigMethod.declaration());
    }

    public boolean isCall(JigMethodIdentifier jigMethodIdentifier) {
        return jigMethod.usingMethods().contains(jigMethodIdentifier);
    }

    public TypeIdentifier typeIdentifier() {
        return jigType.identifier();
    }

    public MethodDeclaration declaration() {
        return jigMethod.declaration();
    }

    public PackageIdentifier packageIdentifier() {
        return jigType.packageIdentifier();
    }
}
