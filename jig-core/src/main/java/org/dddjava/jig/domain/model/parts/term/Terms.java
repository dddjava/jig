package org.dddjava.jig.domain.model.parts.term;

import java.util.List;

public class Terms {
    List<Term> terms;

    public Terms(List<Term> terms) {
        this.terms = terms;
    }

    public List<Term> list() {
        return terms;
    }
}
