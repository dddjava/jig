package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.members.methods.JavaMethodDeclarator;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.term.TermIdentifier;
import org.dddjava.jig.domain.model.data.term.TermKind;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Collection;

/**
 * 型およびメンバの用語を保持する
 */
public record JigTypeGlossary(Term term, Collection<Term> memberTerms) {

    public static JigTypeGlossary from(Glossary glossary, TypeIdentifier typeIdentifier) {
        return new JigTypeGlossary(glossary.typeTermOf(typeIdentifier),
                glossary.stream()
                        .filter(term -> term.termKind() == TermKind.メソッド || term.termKind() == TermKind.フィールド)
                        .filter(term -> term.relatesTo(typeIdentifier.fullQualifiedName()))
                        .toList());
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
}
