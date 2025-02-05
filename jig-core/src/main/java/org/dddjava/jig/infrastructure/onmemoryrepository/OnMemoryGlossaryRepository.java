package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.classes.method.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.data.classes.type.JigTypeTerms;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.term.TermKind;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class OnMemoryGlossaryRepository implements GlossaryRepository {

    private final Collection<Term> terms = new ArrayList<>();

    @Override
    public Term get(TypeIdentifier typeIdentifier) {
        return terms.stream()
                .filter(term -> term.termKind() == TermKind.クラス)
                .filter(term -> term.identifier().asText().equals(typeIdentifier.fullQualifiedName()))
                .findAny()
                .orElseGet(() -> Term.fromClass(typeIdentifier, typeIdentifier.asSimpleText()));
    }

    @Override
    public Term get(PackageIdentifier packageIdentifier) {
        return terms.stream()
                .filter(term -> term.termKind() == TermKind.パッケージ)
                .filter(term -> term.identifier().asText().equals(packageIdentifier.asText()))
                .findAny()
                .orElseGet(() -> Term.fromPackage(packageIdentifier, packageIdentifier.simpleName()));
    }

    @Override
    public JigTypeTerms collectJigTypeTerms(TypeIdentifier typeIdentifier) {
        // 型に紐づくTermを収集する。
        // 現在本クラスは扱っていないが、フィールドおよびメソッドのコメントも含むようにしたい。
        // そうしたらJigTypeでこれをもって、JigMethodはJigTypeから取得する際にここに入ってるコメントを付与して生成する形になる
        return new JigTypeTerms(List.of(get(typeIdentifier)));
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
    public Optional<Term> findMethodPossiblyMatches(MethodIdentifier methodIdentifier) {
        return terms.stream()
                .filter(term -> term.termKind() == TermKind.メソッド)
                .filter(term -> {
                    if (term.additionalInformation() instanceof JavaMethodDeclarator javaMethodDeclarator) {
                        return javaMethodDeclarator.possiblyMatches(methodIdentifier.methodSignature());
                    } else {
                        return false;
                    }
                })
                .findAny();
    }
}
