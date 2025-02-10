package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.classes.method.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.term.TermIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.types.JigTypeTerms;

/**
 * 別名リポジトリ
 */
public interface GlossaryRepository {

    Term get(TypeIdentifier typeIdentifier);

    Term get(PackageIdentifier packageIdentifier);

    JigTypeTerms collectJigTypeTerms(TypeIdentifier typeIdentifier);

    void register(Term term);

    Glossary all();

    Term getMethodTermPossiblyMatches(MethodIdentifier methodIdentifier);

    TermIdentifier fromPackageIdentifier(PackageIdentifier packageIdentifier);

    TermIdentifier fromTypeIdentifier(TypeIdentifier typeIdentifier);

    TermIdentifier fromMethodIdentifier(MethodIdentifier methodIdentifier);

    TermIdentifier fromMethodImplementationDeclarator(TypeIdentifier typeIdentifier, JavaMethodDeclarator methodImplementationDeclarator);
}
