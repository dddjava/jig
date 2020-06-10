package org.dddjava.jig.application.repository;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.MethodAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.PackageAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;

public interface JigSourceRepository {

    void registerSqls(Sqls sqls);

    void registerTypeFact(TypeFacts typeFacts);

    void registerPackageAlias(PackageAlias packageAlias);

    void registerTypeAlias(TypeAlias typeAlias);

    void registerMethodAlias(MethodAlias methodAlias);

    TypeFacts allTypeFacts();

    Sqls sqls();
}
