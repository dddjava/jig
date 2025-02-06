package org.dddjava.jig.domain.model.information.type;

import org.dddjava.jig.adapter.html.dialect.JigTypeDescription;
import org.dddjava.jig.domain.model.data.term.Term;

import java.util.Collection;

public record JigTypeTerms(Term term, Collection<Term> memberTerms) {
    public JigTypeDescription jigTypeDescription() {
        return new JigTypeDescription(term.title(), term.description());
    }

    public boolean markedCore() {
        return typeTerm().title().startsWith("*");
    }

    public Term typeTerm() {
        return term;
    }
}
