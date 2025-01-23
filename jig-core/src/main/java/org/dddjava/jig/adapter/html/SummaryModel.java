package org.dddjava.jig.adapter.html;

import org.dddjava.jig.domain.model.data.jigobject.class_.JigTypes;

import java.util.Map;

public class SummaryModel {
    private final JigTypes jigTypes;
    private final Map<String, Object> additionalMap;

    SummaryModel(JigTypes jigTypes) {
        this(jigTypes, Map.of());
    }

    SummaryModel(JigTypes jigTypes, Map<String, Object> additionalMap) {
        this.jigTypes = jigTypes;
        this.additionalMap = additionalMap;
    }

    public JigTypes jigTypes() {
        return jigTypes;
    }

    public boolean empty() {
        return jigTypes.empty();
    }

    public Map<String, Object> getAdditionalMap() {
        return additionalMap;
    }
}
