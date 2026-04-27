package org.dddjava.jig.domain.model.information.members;

import java.util.Collection;

public record JigFields(Collection<JigField> fields) {

    public boolean isEmpty() {
        return fields.isEmpty();
    }
}
