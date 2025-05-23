package org.dddjava.jig.adapter.html;

import org.dddjava.jig.domain.model.information.module.JigPackages;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Map;

public record SummaryModel(JigTypes jigTypes, JigPackages jigPackages, Map<String, Object> additionalMap) {

    static SummaryModel of(JigTypes jigTypes, JigPackages jigPackages) {
        return new SummaryModel(jigTypes, jigPackages, Map.of());
    }

    public boolean empty() {
        return jigTypes.empty();
    }

    public SummaryModel withAdditionalMap(Map<String, Object> additionalMap) {
        return new SummaryModel(jigTypes, jigPackages, additionalMap);
    }
}
