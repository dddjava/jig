package org.dddjava.jig.domain.model.parts.alias;

import org.dddjava.jig.domain.model.parts.class_.type.ClassComment;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.package_.PackageComment;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;

/**
 * 別名発見機
 */
public interface AliasFinder {

    PackageComment find(PackageIdentifier packageIdentifier);

    ClassComment find(TypeIdentifier typeIdentifier);
}
