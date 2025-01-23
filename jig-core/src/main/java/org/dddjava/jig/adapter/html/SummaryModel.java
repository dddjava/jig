package org.dddjava.jig.adapter.html;

import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

import java.util.Map;

public class SummaryModel {
    private final JigTypes jigTypes;
    private final Map<String, Object> additionalMap;

    Map<String, String> mermaidMap;

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

    /**
     * htmlから使用
     */
    public Map<String, String> mermaidMap() {
        if (mermaidMap != null) return mermaidMap;
        return Map.of();
    }

    public Map<String, Object> getAdditionalMap() {
        return additionalMap;
    }
}
