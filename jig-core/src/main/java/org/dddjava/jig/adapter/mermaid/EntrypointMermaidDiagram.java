package org.dddjava.jig.adapter.mermaid;

import org.dddjava.jig.adapter.thymeleaf.HtmlSupport;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.inputs.InputAdapter;
import org.dddjava.jig.domain.model.information.inputs.InputAdapters;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelation;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.*;

public record EntrypointMermaidDiagram(InputAdapters inputAdapters, JigTypes contextJigTypes) {

    public String textFor(JigType jigType) {
        return inputAdapters().groups().stream()
                .filter(inputAdapter -> inputAdapter.jigType() == jigType)
                .findAny()
                .map(inputAdapter -> mermaid(inputAdapter, inputAdapters().methodRelations(), contextJigTypes))
                .orElse("");
    }

    private static String mermaid(InputAdapter inputAdapter, MethodRelations methodRelations, JigTypes jigTypes) {
        var entrypointMermaidText = new StringJoiner("\n");

        Map<TypeId, Set<JigMethod>> serviceMethodMap = new HashMap<>();
        Map<String, String> methodLabelMap = new HashMap<>();

        MethodRelations springComponentMethodRelations = methodRelations.filterApplicationComponent(jigTypes).inlineLambda();

        var methodRelationSet = new HashSet<MethodRelation>();

        inputAdapter.entrypoints().forEach(entrypoint -> {
            var entrypointMmdId = MermaidSupport.mermaidIdText(entrypoint.jigMethod().jigMethodId());
            // エントリーポイント
            entrypointMermaidText.add("    %s{{\"%s\"}}".formatted(entrypointMmdId, entrypoint.methodLabelText()));
            // エントリーポイントに繋がるパス
            entrypointMermaidText.add("    %s>\"%s\"] -.-> %s".formatted("__" + entrypointMmdId, entrypoint.pathText(), entrypointMmdId));

            // apiMethod -> others...
            var decraleMethodRelations = springComponentMethodRelations.filterFromRecursive(entrypoint.jigMethod().jigMethodId(),
                    // @Serviceのクラスについたら終了
                    jigMethodId -> jigTypes.isService(jigMethodId)
            );
            methodRelationSet.addAll(decraleMethodRelations.list());

            // Service表示＆リンクのための収集
            decraleMethodRelations.list()
                    .stream()
                    .map(MethodRelation::to)
                    .forEach(jigMethodId -> {
                        var declaringTypeId = jigMethodId.tuple().declaringTypeId();
                        if (jigTypes.isService(jigMethodId)) {
                            jigTypes.resolveJigMethod(jigMethodId)
                                    .ifPresent(jigMethod -> {
                                        serviceMethodMap.computeIfAbsent(declaringTypeId, k -> new HashSet<>());
                                        serviceMethodMap.get(declaringTypeId).add(jigMethod);
                                    });
                        } else {
                            // controllerと同じクラスのメソッドはメソッド名だけ
                            if (entrypoint.typeId().equals(declaringTypeId)) {
                                methodLabelMap.put(MermaidSupport.mermaidIdText(jigMethodId), jigMethodId.name());
                            } else {
                                // 他はクラス名+メソッド名
                                methodLabelMap.put(MermaidSupport.mermaidIdText(jigMethodId), declaringTypeId.asSimpleName() + '.' + jigMethodId.name());
                            }
                        }
                    });
        });

        var mermaidText = new StringJoiner("\n");
        mermaidText.add("graph LR");
        // pathとentrypoint method
        mermaidText.add(entrypointMermaidText.toString());
        // サービスメソッドをクラス単位にグループ化して名前解決＆クリックで遷移できるようにする
        serviceMethodMap.forEach((key, values) -> {
            mermaidText.add("    subgraph %s".formatted(key.asSimpleText()));
            values.forEach(jigMethod -> {
                var methodId = MermaidSupport.mermaidIdText(jigMethod.jigMethodId());
                // JigMethodIdをリンク先となるHTMLに書き出しているIDと同じルールで変換する
                var linkTargetId = HtmlSupport.htmlMethodIdText(jigMethod.jigMethodId());
                mermaidText.add("    %s([\"%s\"])".formatted(methodId, jigMethod.labelText()));
                mermaidText.add("    click %s \"./usecase.html#%s\"".formatted(methodId, linkTargetId));
            });
            mermaidText.add("    end");
        });
        // サービスメソッド以外のメソッド
        methodLabelMap.forEach((key, value) -> mermaidText.add("    %s[%s]".formatted(key, value)));

        // 関連線
        methodRelationSet.stream()
                .map(methodRelation -> methodRelationEdgeText(methodRelation))
                .forEach(mermaidText::add);

        return mermaidText.toString();
    }

    private static String methodRelationEdgeText(MethodRelation methodRelation) {
        return "%s --> %s".formatted(MermaidSupport.mermaidIdText(methodRelation.from()), MermaidSupport.mermaidIdText(methodRelation.to()));
    }
}
