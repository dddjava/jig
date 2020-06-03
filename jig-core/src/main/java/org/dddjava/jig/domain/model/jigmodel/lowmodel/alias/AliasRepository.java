package org.dddjava.jig.domain.model.jigmodel.lowmodel.alias;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

/**
 * 別名リポジトリ
 */
public interface AliasRepository {

    TypeAlias get(TypeIdentifier typeIdentifier);

    boolean exists(PackageIdentifier packageIdentifier);

    PackageAlias get(PackageIdentifier packageIdentifier);

    void register(TypeAlias typeAlias);

    void register(PackageAlias packageAlias);

    MethodAlias get(MethodIdentifier methodIdentifier);

    void register(MethodAlias methodAlias);
}
