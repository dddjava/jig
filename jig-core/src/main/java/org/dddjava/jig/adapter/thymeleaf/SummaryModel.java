package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.mermaid.EntrypointMermaidDiagram;
import org.dddjava.jig.adapter.mermaid.UsecaseMermaidDiagram;
import org.dddjava.jig.application.CoreTypesAndRelations;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.knowledge.module.JigPackages;

import java.util.Map;

public record SummaryModel(JigTypes jigTypes, JigPackages jigPackages, Map<String, Object> additionalMap) {

    public static final String ENUM_MODEL_MAP_KEY = "enumModelMap";
    public static final String RELATIONSHIPS_KEY = "relationships";
    public static final String MERMAID_DIAGRAM_KEY = "mermaidDiagram";

    static SummaryModel of(JigTypes jigTypes, JigPackages jigPackages) {
        return new SummaryModel(jigTypes, jigPackages, Map.of());
    }

    static SummaryModel forEnumSummary(JigTypes categoryTypes, JigPackages packages, EnumModels enumModels) {
        return of(categoryTypes, packages)
                .withAdditionalMap(Map.of(ENUM_MODEL_MAP_KEY, enumModels.toMap()));
    }

    static SummaryModel withMermaidDiagram(JigTypes jigTypes, JigPackages packages, UsecaseMermaidDiagram usecaseMermaidDiagram) {
        return of(jigTypes, packages)
                .withAdditionalMap(Map.of(MERMAID_DIAGRAM_KEY, usecaseMermaidDiagram));
    }

    static SummaryModel withMermaidDiagram(JigTypes jigTypes, JigPackages packages, EntrypointMermaidDiagram entrypointMermaidDiagram) {
        return of(jigTypes, packages)
                .withAdditionalMap(Map.of(MERMAID_DIAGRAM_KEY, entrypointMermaidDiagram));
    }

    static SummaryModel forDomainSummary(JigTypes jigTypes, JigPackages packages, CoreTypesAndRelations coreTypesAndRelations, EnumModels enumModels) {
        return of(jigTypes, packages)
                .withAdditionalMap(Map.of(
                        RELATIONSHIPS_KEY, coreTypesAndRelations,
                        ENUM_MODEL_MAP_KEY, enumModels.toMap()
                ));
    }

    public boolean empty() {
        return jigTypes.empty();
    }

    private SummaryModel withAdditionalMap(Map<String, Object> additionalMap) {
        return new SummaryModel(jigTypes, jigPackages, additionalMap);
    }
}
