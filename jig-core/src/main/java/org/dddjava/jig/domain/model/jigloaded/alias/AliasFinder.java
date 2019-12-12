package org.dddjava.jig.domain.model.jigloaded.alias;

import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * 別名発見機
 */
public interface AliasFinder {

    PackageAlias find(PackageIdentifier packageIdentifier);

    TypeAlias find(TypeIdentifier typeIdentifier);

    MethodAlias find(MethodIdentifier methodIdentifier);
}
