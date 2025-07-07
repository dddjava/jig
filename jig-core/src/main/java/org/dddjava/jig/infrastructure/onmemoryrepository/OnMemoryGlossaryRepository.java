package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.members.methods.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.ArrayList;
import java.util.Collection;

@Repository
public class OnMemoryGlossaryRepository implements GlossaryRepository {

    private final Collection<Term> terms = new ArrayList<>();

    @Override
    public Term get(TypeId typeId) {
        TermId termId = fromTypeIdentifier(typeId);
        return terms.stream()
                .filter(term -> term.termKind() == TermKind.クラス)
                .filter(term -> term.id().equals(termId))
                .findAny()
                // 用語として事前登録されていなくても、IDがあるということは用語として存在することになるので、生成して返す。
                .orElseGet(() -> Term.simple(termId, typeId.asSimpleText(), TermKind.クラス));
    }

    @Override
    public Term get(PackageId packageId) {
        TermId termId = fromPackageIdentifier(packageId);
        return terms.stream()
                .filter(term -> term.termKind() == TermKind.パッケージ)
                .filter(term -> term.id().equals(termId))
                .findAny()
                // 用語として事前登録されていなくても、IDがあるということは用語として存在することになるので、生成して返す。
                .orElseGet(() -> Term.simple(termId, packageId.simpleName(), TermKind.パッケージ));
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
    public TermId fromPackageIdentifier(PackageId packageId) {
        return new TermId(packageId.asText());
    }

    @Override
    public TermId fromTypeIdentifier(TypeId typeId) {
        return new TermId(typeId.fullQualifiedName());
    }

    @Override
    public TermId fromMethodImplementationDeclarator(TypeId typeId, JavaMethodDeclarator methodImplementationDeclarator) {
        return new TermId(typeId.fullQualifiedName() + "#" + methodImplementationDeclarator.asText());
    }

    @Override
    public TermId fromFieldIdentifier(JigFieldId jigFieldId) {
        return new TermId(jigFieldId.fqn());
    }
}
