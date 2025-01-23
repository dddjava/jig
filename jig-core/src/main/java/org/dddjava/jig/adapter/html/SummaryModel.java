package org.dddjava.jig.adapter.html;

import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryType;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryTypes;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethodFinder;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class SummaryModel {
    /**
     * 補助に使用するJigType。メソッドの引数など、サマリ対象の外側にいるものの解決に使用する。
     */
    private final JigTypes supportJigTypes;

    Map<PackageIdentifier, List<JigType>> map;
    // FIXME enumModelsの持ち方・・・
    EnumModels enumModels;
    Map<String, String> mermaidMap;

    private SummaryModel(JigTypes supportJigTypes, Map<PackageIdentifier, List<JigType>> map, EnumModels enumModels) {
        this.supportJigTypes = supportJigTypes;
        this.map = map;
        this.enumModels = enumModels;
    }

    public static SummaryModel from(JigTypes supportJigTypes, JigTypes jigTypes) {
        Map<PackageIdentifier, List<JigType>> map = jigTypes.stream()
                .collect(groupingBy(JigType::packageIdentifier));
        return new SummaryModel(supportJigTypes, map, new EnumModels(List.of()));
    }

    public static SummaryModel from(JigTypes supportJigTypes, CategoryTypes categoryTypes, EnumModels enumModels) {
        Map<PackageIdentifier, List<JigType>> map = categoryTypes.list().stream()
                .map(CategoryType::jigType)
                .collect(groupingBy(JigType::packageIdentifier));
        return new SummaryModel(supportJigTypes, map, enumModels);
    }

    public static SummaryModel from(JigTypes supportJigTypes, Entrypoint entrypoint) {
        Map<PackageIdentifier, List<JigType>> map = entrypoint.list().stream()
                .map(entrypointGroup -> entrypointGroup.jigType())
                .collect(groupingBy(JigType::packageIdentifier));

        var summaryModel = new SummaryModel(supportJigTypes, map, new EnumModels(List.of()));
        summaryModel.mermaidMap = entrypoint.mermaidMap(supportJigTypes);
        return summaryModel;
    }

    public Map<PackageIdentifier, List<JigType>> map() {
        return map;
    }

    public boolean empty() {
        return map.isEmpty();
    }

    public EnumModels enumModels() {
        return enumModels;
    }

    /**
     * htmlから使用
     */
    public Map<String, String> mermaidMap() {
        if (mermaidMap != null) return mermaidMap;
        return Map.of();
    }

    public String mermaidDiagram(JigMethod jigMethod) {
        var methodRelations = supportJigTypes.methodRelations().inlineLambda();
        JigMethodFinder jigMethodFinder = methodIdentifier -> supportJigTypes.resolveJigMethod(methodIdentifier);

        return supportJigTypes.resolveJigMethod(jigMethod.declaration().identifier())
                .map(m -> m.usecaseMermaidText(jigMethodFinder, methodRelations))
                .orElse("");
    }
}
