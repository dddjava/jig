package org.dddjava.jig.application.repository;

import org.dddjava.jig.domain.model.jigsource.jigfactory.AliasRegisterResult;
import org.dddjava.jig.domain.model.jigsource.jigfactory.TypeFacts;
import org.dddjava.jig.domain.model.parts.class_.method.MethodComment;
import org.dddjava.jig.domain.model.parts.class_.type.ClassComment;
import org.dddjava.jig.domain.model.parts.package_.PackageComment;
import org.dddjava.jig.domain.model.parts.rdbaccess.Sqls;

public interface JigSourceRepository {

    void registerSqls(Sqls sqls);

    void registerTypeFact(TypeFacts typeFacts);

    void registerPackageComment(PackageComment packageComment);

    AliasRegisterResult registerClassComment(ClassComment classComment);

    void registerMethodComment(MethodComment methodComment);

    TypeFacts allTypeFacts();

    Sqls sqls();
}
