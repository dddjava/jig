package org.dddjava.jig.application.repository;

import org.dddjava.jig.domain.model.parts.class_.method.MethodComment;
import org.dddjava.jig.domain.model.parts.class_.type.ClassComment;
import org.dddjava.jig.domain.model.parts.package_.PackageComment;
import org.dddjava.jig.domain.model.parts.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.sources.jigfactory.AliasRegisterResult;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;

public interface JigSourceRepository {

    void registerSqls(Sqls sqls);

    void registerTypeFact(TypeFacts typeFacts);

    void registerPackageComment(PackageComment packageComment);

    AliasRegisterResult registerClassComment(ClassComment classComment);

    void registerMethodComment(MethodComment methodComment);

    TypeFacts allTypeFacts();

    Sqls sqls();
}
