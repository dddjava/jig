package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModels;
import org.dddjava.jig.domain.model.parts.classes.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.term.Term;
import org.dddjava.jig.domain.model.parts.term.Terms;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;

public interface JigSourceRepository {

    void registerSqls(Sqls sqls);

    void registerPackageComment(PackageComment packageComment);

    void registerTerm(Term term);

    void registerTextSourceModel(TextSourceModel textSourceModel);

    Sqls sqls();

    Terms terms();

    EnumModels enumModels();
}
