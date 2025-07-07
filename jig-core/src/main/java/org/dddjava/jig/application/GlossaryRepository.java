package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.members.methods.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeId;

/**
 * 別名リポジトリ
 */
public interface GlossaryRepository {

    Term get(TypeId typeId);

    Term get(PackageId packageId);

    void register(Term term);

    Glossary all();

    TermIdentifier fromPackageIdentifier(PackageId packageId);

    TermIdentifier fromTypeIdentifier(TypeId typeId);

    TermIdentifier fromMethodImplementationDeclarator(TypeId typeId, JavaMethodDeclarator methodImplementationDeclarator);

    TermIdentifier fromFieldIdentifier(JigFieldId from);
}
