package org.dddjava.jig.adapter.html;

import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Map;

public record SummaryModel(JigTypes jigTypes, Map<String, Object> additionalMap) {

    static SummaryModel of(JigTypes jigTypes) {
        return new SummaryModel(jigTypes, Map.of());
    }

    public boolean empty() {
        return jigTypes.empty();
    }

    public SummaryModel withAdditionalMap(Map<String, Object> additionalMap) {
        return new SummaryModel(jigTypes, additionalMap);
    }
}
