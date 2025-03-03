package org.dddjava.jig.adapter.html.mermaid;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.inputs.EntrypointGroup;
import org.dddjava.jig.domain.model.information.inputs.Entrypoints;
import org.dddjava.jig.domain.model.information.inputs.HttpEndpoint;
import org.dddjava.jig.domain.model.information.inputs.QueueListener;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelation;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.*;
import java.util.stream.Collectors;

public record EntrypointMermaidDiagram(Entrypoints entrypoints, JigTypes contextJigTypes) {

    public String textFor(JigType jigType) {
        return entrypoints().list().stream()
                .filter(entrypointGroup -> entrypointGroup.jigType() == jigType)
                .findAny()
                .map(entrypointGroup -> mermaid(entrypointGroup, entrypoints().methodRelations(), contextJigTypes))
                .orElse("");
    }

    private static String mermaid(EntrypointGroup entrypointGroup, MethodRelations methodRelations, JigTypes jigTypes) {

        var entryPointText = new StringJoiner("\n");

        Map<TypeIdentifier, Set<JigMethod>> serviceMethodMap = new HashMap<>();
        Map<String, String> methodLabelMap = new HashMap<>();

        MethodRelations springComponentMethodRelations = MethodRelations.filterApplicationComponent(jigTypes, methodRelations).inlineLambda();

        var methodRelationSet = new HashSet<MethodRelation>();

        entrypointGroup.entrypointMethod().forEach(entrypointMethod -> {
            // APIメソッドの名前と形
            var apiMethodMmdId = htmlIdText(entrypointMethod.jigMethod().jigMethodIdentifier());
            String apiMethodLabel = entrypointMethod.jigMethod().labelText();

            var description = switch (entrypointMethod.entrypointType()) {
                case HTTP_API -> {
                    var httpEndpoint = HttpEndpoint.from(entrypointMethod);
                    apiMethodLabel = httpEndpoint.interfaceLabel();
                    yield "%s %s".formatted(httpEndpoint.method(), httpEndpoint.methodPath());
                }
                case QUEUE_LISTENER -> "queue: %s".formatted(QueueListener.from(entrypointMethod).queueName());
                default -> entrypointMethod.entrypointType().toString();
            };
            // apiMethod
            entryPointText.add("    %s{{\"%s\"}}".formatted(apiMethodMmdId, apiMethodLabel));
            // path -> apiMethod
            String apiPointMmdId = "__" + apiMethodMmdId;
            entryPointText.add("    %s>\"%s\"] -.-> %s".formatted(apiPointMmdId, description, apiMethodMmdId));

            // apiMethod -> others...
            var decraleMethodRelations = springComponentMethodRelations.filterFromRecursive(entrypointMethod.jigMethod().jigMethodIdentifier(),
                    // @Serviceのクラスについたら終了
                    jigMethodIdentifier -> jigTypes.isService(jigMethodIdentifier)
            );
            methodRelationSet.addAll(decraleMethodRelations.list());

            // Service表示＆リンクのための収集
            decraleMethodRelations.list()
                    .stream()
                    .map(MethodRelation::to)
                    .forEach(jigMethodIdentifier -> {
                        var declaringTypeIdentifier = jigMethodIdentifier.tuple().declaringTypeIdentifier();
                        if (jigTypes.isService(jigMethodIdentifier)) {
                            jigTypes.resolveJigMethod(jigMethodIdentifier)
                                    .ifPresent(jigMethod -> {
                                        serviceMethodMap.computeIfAbsent(declaringTypeIdentifier, k -> new HashSet<>());
                                        serviceMethodMap.get(declaringTypeIdentifier).add(jigMethod);
                                    });
                        } else {
                            // controllerと同じクラスのメソッドはメソッド名だけ
                            if (entrypointMethod.typeIdentifier().equals(declaringTypeIdentifier)) {
                                methodLabelMap.put(htmlIdText(jigMethodIdentifier), jigMethodIdentifier.name());
                            } else {
                                // 他はクラス名+メソッド名
                                methodLabelMap.put(htmlIdText(jigMethodIdentifier), declaringTypeIdentifier.asSimpleName() + '.' + jigMethodIdentifier.name());
                            }
                        }
                    });
        });

        var mermaidText = new StringJoiner("\n");
        mermaidText.add("graph LR");
        // pathとentrypoint method
        mermaidText.add(entryPointText.toString());
        // サービスメソッドをクラス単位にグループ化して名前解決＆クリックで遷移できるようにする
        serviceMethodMap.forEach((key, values) -> {
            mermaidText.add("    subgraph %s".formatted(key.asSimpleText()));
            values.forEach(jigMethod -> {
                var htmlIdText = htmlIdText(jigMethod.jigMethodIdentifier());
                mermaidText.add("    %s([\"%s\"])".formatted(htmlIdText, jigMethod.labelText()));
                mermaidText.add("    click %s \"./usecase.html#%s\"".formatted(htmlIdText, htmlIdText));
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
        return "%s --> %s".formatted(htmlIdText(methodRelation.from()), htmlIdText(methodRelation.to()));
    }

    private static String htmlIdText(JigMethodIdentifier jigMethodIdentifier) {
        var tuple = jigMethodIdentifier.tuple();

        var typeText = tuple.declaringTypeIdentifier().packageAbbreviationText();
        var parameterText = tuple.parameterTypeIdentifiers().stream()
                .map(TypeIdentifier::packageAbbreviationText)
                .collect(Collectors.joining(", ", "(", ")"));
        return (typeText + '.' + tuple.name() + parameterText).replaceAll("[^a-zA-Z0-9]", "_");
    }
}
