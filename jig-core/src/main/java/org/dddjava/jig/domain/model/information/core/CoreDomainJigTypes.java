package org.dddjava.jig.domain.model.information.core;

import org.dddjava.jig.domain.model.information.types.JigTypes;

/**
 * CoreDomainにフィルタリングされたJigTypes
 */
public record CoreDomainJigTypes(JigTypes jigTypes) {
    public boolean empty() {
        return jigTypes().empty();
    }
}
