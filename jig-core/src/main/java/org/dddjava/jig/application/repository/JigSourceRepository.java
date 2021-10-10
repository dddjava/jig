package org.dddjava.jig.application.repository;

import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.term.Term;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;

public interface JigSourceRepository {

    void registerSqls(Sqls sqls);

    void registerTypeFact(TypeFacts typeFacts);

    void registerPackageComment(PackageComment packageComment);

    void registerClassComment(ClassComment classComment);

    void registerMethodComment(MethodComment methodComment);

    void registerTerm(Term term);

    TypeFacts allTypeFacts();

    Sqls sqls();
}
