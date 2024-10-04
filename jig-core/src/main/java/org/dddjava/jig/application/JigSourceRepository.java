package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.term.Term;
import org.dddjava.jig.domain.model.parts.term.Terms;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;

public interface JigSourceRepository {

    void registerPackageComment(PackageComment packageComment);

    void registerTerm(Term term);

    void registerTextSourceModel(TextSourceModel textSourceModel);

    Terms terms();
}
