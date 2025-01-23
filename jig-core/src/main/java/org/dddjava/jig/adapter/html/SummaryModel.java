package org.dddjava.jig.adapter.html;

import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethodFinder;

import java.util.List;
import java.util.Map;

public class SummaryModel {
    /**
     * 補助に使用するJigType。メソッドの引数など、サマリ対象の外側にいるものの解決に使用する。
     */
    private final JigTypes supportJigTypes;

    Map<PackageIdentifier, List<JigType>> map;
    // FIXME enumModelsの持ち方・・・
    EnumModels enumModels;
    Map<String, String> mermaidMap;

    SummaryModel(JigTypes supportJigTypes, Map<PackageIdentifier, List<JigType>> map, EnumModels enumModels) {
        this.supportJigTypes = supportJigTypes;
        this.map = map;
        this.enumModels = enumModels;
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
