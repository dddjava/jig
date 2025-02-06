package org.dddjava.jig.domain.model.information.type;

import org.dddjava.jig.domain.model.data.classes.type.JigTypeDescription;
import org.dddjava.jig.domain.model.data.term.Term;

import java.util.Collection;
import java.util.Optional;

public record JigTypeTerms(Collection<Term> terms) {
    public JigTypeDescription jigTypeDescription() {
        return typeTerm()
                .map(term -> new JigTypeDescription(term.title(), term.description()))
                .orElseGet(() -> new JigTypeDescription("", ""));
    }

    public boolean markedCore() {
        return typeTerm()
                .filter(term -> term.title().startsWith("*"))
                .isPresent();
    }

    public Optional<Term> typeTerm() {
        // ちゃんとフィルタすべきだが今はTypeTermのみなのでこれで。
        return terms.stream().findFirst();
    }
}
