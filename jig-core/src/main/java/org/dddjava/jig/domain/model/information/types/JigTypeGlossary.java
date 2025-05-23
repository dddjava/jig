package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldIdentifier;
import org.dddjava.jig.domain.model.data.members.methods.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermIdentifier;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Collection;

/**
 * 型およびメンバの用語を保持する
 */
public record JigTypeGlossary(Term term, Collection<Term> memberTerms) {

    public static JigTypeGlossary from(Glossary glossary, TypeIdentifier typeIdentifier) {
        TermIdentifier termIdentifier = new TermIdentifier(typeIdentifier.fullQualifiedName());
        Collection<Term> terms = glossary.findRelated(termIdentifier);

        Term typeTerm = terms.stream()
                .filter(term -> term.termKind() == TermKind.クラス)
                // termsにはネストクラスも含まれるため、完全一致に絞り込む
                .filter(term -> term.identifier().equals(termIdentifier))
                .findAny()
                // 用語として事前登録されていなくても、IDがあるということは用語として存在することになるので、生成して返す。
                .orElseGet(() -> Term.simple(termIdentifier, typeIdentifier.asSimpleName(), TermKind.クラス));

        return new JigTypeGlossary(typeTerm, terms);
    }

    public boolean markedCore() {
        return typeTerm().title().startsWith("*");
    }

    public Term typeTerm() {
        return term;
    }

    public Term getMethodTermPossiblyMatches(JigMethodIdentifier jigMethodIdentifier) {
        return memberTerms.stream()
                .filter(term -> term.termKind() == TermKind.メソッド)
                .filter(term -> {
                    if (term.additionalInformation() instanceof JavaMethodDeclarator javaMethodDeclarator) {
                        return javaMethodDeclarator.possiblyMatches(jigMethodIdentifier);
                    } else {
                        return false;
                    }
                })
                .findAny()
                .orElseGet(() -> new Term(new TermIdentifier(jigMethodIdentifier.value()), jigMethodIdentifier.name(), "", TermKind.メソッド));
    }

    public Term fieldTerm(JigFieldIdentifier id) {
        TermIdentifier termIdentifier = new TermIdentifier(id.value());
        return memberTerms.stream()
                .filter(term -> term.termKind() == TermKind.フィールド)
                .filter(term -> term.identifier().equals(termIdentifier))
                .findAny()
                .orElseGet(() -> Term.simple(termIdentifier, id.name(), TermKind.フィールド));
    }
}
