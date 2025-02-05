package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.classes.type.JigTypeTerms;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.javasources.comment.ClassComment;
import org.dddjava.jig.domain.model.sources.javasources.comment.PackageComment;

/**
 * 別名リポジトリ
 */
public interface GlossaryRepository {

    ClassComment get(TypeIdentifier typeIdentifier);

    boolean exists(PackageIdentifier packageIdentifier);

    PackageComment get(PackageIdentifier packageIdentifier);

    void register(ClassComment classComment);

    void register(PackageComment packageComment);

    JigTypeTerms collectJigTypeTerms(TypeIdentifier typeIdentifier);

    void register(Term term);
}
