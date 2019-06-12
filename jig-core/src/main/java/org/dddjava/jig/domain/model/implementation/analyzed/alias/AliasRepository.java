package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;

/**
 * 別名リポジトリ
 */
public interface AliasRepository {

    Alias get(TypeIdentifier typeIdentifier);

    boolean exists(PackageIdentifier packageIdentifier);

    Alias get(PackageIdentifier packageIdentifier);

    void register(TypeAlias typeAlias);

    void register(PackageAlias packageAlias);

    MethodAlias get(MethodIdentifier methodIdentifier);

    void register(MethodAlias methodAlias);
}
