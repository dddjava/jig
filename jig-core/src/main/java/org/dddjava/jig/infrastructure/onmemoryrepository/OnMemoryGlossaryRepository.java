package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.classes.type.JigTypeTerms;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.term.TermKind;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.javasources.comment.ClassComment;

import java.util.*;

@Repository
public class OnMemoryGlossaryRepository implements GlossaryRepository {

    private final Collection<Term> terms = new ArrayList<>();

    final Map<TypeIdentifier, ClassComment> map = new HashMap<>();

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
        // 現在本クラスは扱っていないが、フィールドおよびメソッドのコメントも含むようにする。
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
}
