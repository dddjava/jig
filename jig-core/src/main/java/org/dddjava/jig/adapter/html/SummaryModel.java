package org.dddjava.jig.adapter.html;

import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethodFinder;

import java.util.Map;

public class SummaryModel {
    private final JigTypes jigTypes;
    /**
     * 補助に使用するJigType。メソッドの引数など、サマリ対象の外側にいるものの解決に使用する。
     */
    private final JigTypes supportJigTypes;

    // FIXME enumModelsの持ち方・・・
    EnumModels enumModels;
    Map<String, String> mermaidMap;

    SummaryModel(JigTypes supportJigTypes, JigTypes jigTypes, EnumModels enumModels) {
        this.supportJigTypes = supportJigTypes;
        this.jigTypes = jigTypes;
        this.enumModels = enumModels;
    }

    public JigTypes jigTypes() {
        return jigTypes;
    }

    public boolean empty() {
        return jigTypes.empty();
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
