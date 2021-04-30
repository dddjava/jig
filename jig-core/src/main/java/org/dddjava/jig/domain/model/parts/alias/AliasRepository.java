package org.dddjava.jig.domain.model.parts.alias;

import org.dddjava.jig.domain.model.parts.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.parts.declaration.type.TypeIdentifier;

/**
 * 別名リポジトリ
 */
public interface AliasRepository {

    TypeAlias get(TypeIdentifier typeIdentifier);

    boolean exists(PackageIdentifier packageIdentifier);

    PackageAlias get(PackageIdentifier packageIdentifier);

    void register(TypeAlias typeAlias);

    void register(PackageAlias packageAlias);
}
