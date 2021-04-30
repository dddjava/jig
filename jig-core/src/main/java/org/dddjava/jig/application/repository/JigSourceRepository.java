package org.dddjava.jig.application.repository;

import org.dddjava.jig.domain.model.jigsource.analyzed.AliasRegisterResult;
import org.dddjava.jig.domain.model.jigsource.analyzed.TypeFacts;
import org.dddjava.jig.domain.model.parts.alias.MethodAlias;
import org.dddjava.jig.domain.model.parts.alias.PackageAlias;
import org.dddjava.jig.domain.model.parts.alias.TypeAlias;
import org.dddjava.jig.domain.model.parts.rdbaccess.Sqls;

public interface JigSourceRepository {

    void registerSqls(Sqls sqls);

    void registerTypeFact(TypeFacts typeFacts);

    void registerPackageAlias(PackageAlias packageAlias);

    AliasRegisterResult registerTypeAlias(TypeAlias typeAlias);

    void registerMethodAlias(MethodAlias methodAlias);

    TypeFacts allTypeFacts();

    Sqls sqls();
}
