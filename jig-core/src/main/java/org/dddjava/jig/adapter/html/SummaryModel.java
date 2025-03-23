package org.dddjava.jig.adapter.html;

import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Map;

public record SummaryModel(JigTypes jigTypes, Map<String, Object> additionalMap) {

    public boolean empty() {
        return jigTypes.empty();
    }
}
