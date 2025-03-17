package org.dddjava.jig.domain.model.information.members;

import java.util.Collection;

public record JigFields(Collection<JigField> fields) {

    public boolean empty() {
        return fields.isEmpty();
    }
}
