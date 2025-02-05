package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.classes.type.JigTypeTerms;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * 別名リポジトリ
 */
public interface GlossaryRepository {

    Term get(TypeIdentifier typeIdentifier);

    Term get(PackageIdentifier packageIdentifier);

    JigTypeTerms collectJigTypeTerms(TypeIdentifier typeIdentifier);

    void register(Term term);

    Glossary all();
}
