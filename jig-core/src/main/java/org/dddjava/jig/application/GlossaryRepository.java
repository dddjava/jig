package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.classes.type.JigTypeTerms;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.javasources.comment.ClassComment;

/**
 * 別名リポジトリ
 */
public interface GlossaryRepository {

    ClassComment get(TypeIdentifier typeIdentifier);

    Term get(PackageIdentifier packageIdentifier);

    void register(ClassComment classComment);

    JigTypeTerms collectJigTypeTerms(TypeIdentifier typeIdentifier);

    void register(Term term);
}
