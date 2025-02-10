package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.term.Term;

import java.util.Collection;

public record JigTypeTerms(Term term, Collection<Term> memberTerms) {

    public boolean markedCore() {
        return typeTerm().title().startsWith("*");
    }

    public Term typeTerm() {
        return term;
    }
}
