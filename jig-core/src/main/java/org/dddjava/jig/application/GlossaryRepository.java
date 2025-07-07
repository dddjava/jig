package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.members.methods.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * 別名リポジトリ
 */
public interface GlossaryRepository {

    Term get(TypeIdentifier typeIdentifier);

    Term get(PackageId packageId);

    void register(Term term);

    Glossary all();

    TermIdentifier fromPackageIdentifier(PackageId packageId);

    TermIdentifier fromTypeIdentifier(TypeIdentifier typeIdentifier);

    TermIdentifier fromMethodImplementationDeclarator(TypeIdentifier typeIdentifier, JavaMethodDeclarator methodImplementationDeclarator);

    TermIdentifier fromFieldIdentifier(JigFieldId from);
}
