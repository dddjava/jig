package org.dddjava.jig.domain.model.parts.alias;

import org.dddjava.jig.domain.model.parts.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.parts.declaration.type.TypeIdentifier;

/**
 * 別名発見機
 */
public interface AliasFinder {

    PackageAlias find(PackageIdentifier packageIdentifier);

    TypeAlias find(TypeIdentifier typeIdentifier);
}
