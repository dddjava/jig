package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.members.methods.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermIdentifier;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.ArrayList;
import java.util.Collection;

@Repository
public class OnMemoryGlossaryRepository implements GlossaryRepository {

    private final Collection<Term> terms = new ArrayList<>();

    @Override
    public Term get(TypeIdentifier typeIdentifier) {
        TermIdentifier termIdentifier = fromTypeIdentifier(typeIdentifier);
        return terms.stream()
                .filter(term -> term.termKind() == TermKind.クラス)
                .filter(term -> term.identifier().equals(termIdentifier))
                .findAny()
                // 用語として事前登録されていなくても、IDがあるということは用語として存在することになるので、生成して返す。
                .orElseGet(() -> Term.simple(termIdentifier, typeIdentifier.asSimpleText(), TermKind.クラス));
    }

    @Override
    public Term get(PackageId packageId) {
        TermIdentifier termIdentifier = fromPackageIdentifier(packageId);
        return terms.stream()
                .filter(term -> term.termKind() == TermKind.パッケージ)
                .filter(term -> term.identifier().equals(termIdentifier))
                .findAny()
                // 用語として事前登録されていなくても、IDがあるということは用語として存在することになるので、生成して返す。
                .orElseGet(() -> Term.simple(termIdentifier, packageId.simpleName(), TermKind.パッケージ));
    }

    @Override
    public void register(Term term) {
        terms.add(term);
    }

    @Override
    public Glossary all() {
        return new Glossary(terms);
    }

    @Override
    public TermIdentifier fromPackageIdentifier(PackageId packageId) {
        return new TermIdentifier(packageId.asText());
    }

    @Override
    public TermIdentifier fromTypeIdentifier(TypeIdentifier typeIdentifier) {
        return new TermIdentifier(typeIdentifier.fullQualifiedName());
    }

    @Override
    public TermIdentifier fromMethodImplementationDeclarator(TypeIdentifier typeIdentifier, JavaMethodDeclarator methodImplementationDeclarator) {
        return new TermIdentifier(typeIdentifier.fullQualifiedName() + "#" + methodImplementationDeclarator.asText());
    }

    @Override
    public TermIdentifier fromFieldIdentifier(JigFieldId jigFieldId) {
        return new TermIdentifier(jigFieldId.fqn());
    }
}
