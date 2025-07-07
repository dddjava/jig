package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.members.methods.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;

/**
 * 型およびメンバの用語を保持する
 */
public record JigTypeGlossary(Term term, Collection<Term> memberTerms) {

    public static JigTypeGlossary from(Glossary glossary, TypeId typeId) {
        TermId termId = new TermId(typeId.fullQualifiedName());
        Collection<Term> terms = glossary.findRelated(termId);

        Term typeTerm = terms.stream()
                .filter(term -> term.termKind() == TermKind.クラス)
                // termsにはネストクラスも含まれるため、完全一致に絞り込む
                .filter(term -> term.id().equals(termId))
                .findAny()
                // 用語として事前登録されていなくても、IDがあるということは用語として存在することになるので、生成して返す。
                .orElseGet(() -> Term.simple(termId, typeId.asSimpleName(), TermKind.クラス));

        return new JigTypeGlossary(typeTerm, terms);
    }

    public boolean markedCore() {
        return typeTerm().title().startsWith("*");
    }

    public Term typeTerm() {
        return term;
    }

    public Term getMethodTermPossiblyMatches(JigMethodId jigMethodId) {
        return memberTerms.stream()
                .filter(term -> term.termKind() == TermKind.メソッド)
                .filter(term -> {
                    if (term.additionalInformation() instanceof JavaMethodDeclarator javaMethodDeclarator) {
                        return javaMethodDeclarator.possiblyMatches(jigMethodId);
                    } else {
                        return false;
                    }
                })
                .findAny()
                .orElseGet(() -> new Term(new TermId(jigMethodId.value()), jigMethodId.name(), "", TermKind.メソッド));
    }

    public Term fieldTerm(JigFieldId id) {
        TermId termId = new TermId(id.value());
        return memberTerms.stream()
                .filter(term -> term.termKind() == TermKind.フィールド)
                .filter(term -> term.id().equals(termId))
                .findAny()
                .orElseGet(() -> Term.simple(termId, id.name(), TermKind.フィールド));
    }
}
