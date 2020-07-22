package org.dddjava.jig.domain.model.jigmodel.lowmodel.alias;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

/**
 * 別名発見機
 */
public interface AliasFinder {

    PackageAlias find(PackageIdentifier packageIdentifier);

    TypeAlias find(TypeIdentifier typeIdentifier);

    MethodAlias find(MethodIdentifier methodIdentifier);

    default String methodText(MethodIdentifier identifier) {
        // 和名 or クラス名+メソッド名
        return find(identifier)
                .asTextOrDefault(identifier.declaringType().asSimpleText() + "\\n" + identifier.methodSignature().methodName());
    }
}
