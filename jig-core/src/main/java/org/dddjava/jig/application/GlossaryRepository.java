package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldIdentifier;
import org.dddjava.jig.domain.model.data.members.methods.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.term.TermIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * 別名リポジトリ
 */
public interface GlossaryRepository {

    Term get(TypeIdentifier typeIdentifier);

    Term get(PackageIdentifier packageIdentifier);

    void register(Term term);

    Glossary all();

    TermIdentifier fromPackageIdentifier(PackageIdentifier packageIdentifier);

    TermIdentifier fromTypeIdentifier(TypeIdentifier typeIdentifier);

    TermIdentifier fromMethodImplementationDeclarator(TypeIdentifier typeIdentifier, JavaMethodDeclarator methodImplementationDeclarator);

    TermIdentifier fromFieldIdentifier(JigFieldIdentifier from);
}
