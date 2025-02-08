package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.classes.method.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.term.TermIdentifier;
import org.dddjava.jig.domain.model.data.term.TermKind;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.type.JigTypeTerms;
import org.dddjava.jig.infrastructure.javaparser.TermFactory;

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
                .orElseGet(() -> TermFactory.fromClass(termIdentifier, typeIdentifier.asSimpleText()));
    }

    @Override
    public Term get(PackageIdentifier packageIdentifier) {
        TermIdentifier termIdentifier = fromPackageIdentifier(packageIdentifier);
        return terms.stream()
                .filter(term -> term.termKind() == TermKind.パッケージ)
                .filter(term -> term.identifier().equals(termIdentifier))
                .findAny()
                // 用語として事前登録されていなくても、IDがあるということは用語として存在することになるので、生成して返す。
                .orElseGet(() -> TermFactory.fromPackage(termIdentifier, packageIdentifier.simpleName()));
    }

    @Override
    public JigTypeTerms collectJigTypeTerms(TypeIdentifier typeIdentifier) {
        return new JigTypeTerms(get(typeIdentifier),
                terms.stream()
                        .filter(term -> term.termKind() == TermKind.メソッド || term.termKind() == TermKind.フィールド)
                        .filter(term -> term.relatesTo(typeIdentifier.fullQualifiedName()))
                        .toList());
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
    public Term getMethodTermPossiblyMatches(MethodIdentifier methodIdentifier) {
        return terms.stream()
                .filter(term -> term.termKind() == TermKind.メソッド)
                .filter(term -> {
                    if (term.additionalInformation() instanceof JavaMethodDeclarator javaMethodDeclarator) {
                        return methodIdentifier.declaringType().equals(javaMethodDeclarator.typeIdentifier())
                                && javaMethodDeclarator.possiblyMatches(methodIdentifier.methodSignature());
                    } else {
                        return false;
                    }
                })
                .findAny()
                .orElseGet(() -> new Term(new TermIdentifier(methodIdentifier.asText()), methodIdentifier.methodSignature().methodName(), "", TermKind.メソッド));
    }

    @Override
    public TermIdentifier fromPackageIdentifier(PackageIdentifier packageIdentifier) {
        return new TermIdentifier(packageIdentifier.asText());
    }

    @Override
    public TermIdentifier fromTypeIdentifier(TypeIdentifier typeIdentifier) {
        return new TermIdentifier(typeIdentifier.fullQualifiedName());
    }

    @Override
    public TermIdentifier fromMethodIdentifier(MethodIdentifier methodIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TermIdentifier fromMethodImplementationDeclarator(TypeIdentifier typeIdentifier, JavaMethodDeclarator methodImplementationDeclarator) {
        return new TermIdentifier(typeIdentifier.fullQualifiedName() + "#" + methodImplementationDeclarator.asText());
    }
}
