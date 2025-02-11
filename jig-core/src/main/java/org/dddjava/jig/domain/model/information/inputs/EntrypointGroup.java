package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelation;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.*;

/**
 * エントリーポイントメソッドのグループ。
 * グルーピング単位はクラス。
 *
 * - SpringMVCのControllerのRequestMapping
 * - SpringRabbitのRabbitListener
 */
public record EntrypointGroup(JigType jigType, List<EntrypointMethod> entrypointMethod) {
    public EntrypointGroup {
        if (entrypointMethod.isEmpty()) throw new IllegalArgumentException("entrypointMethod is empty");
    }

    static Optional<EntrypointGroup> from(EntrypointMethodDetector entrypointMethodDetector, JigType jigType) {
        var entrypointMethods = entrypointMethodDetector.collectMethod(jigType);
        if (!entrypointMethods.isEmpty()) {
            return Optional.of(new EntrypointGroup(jigType, entrypointMethods));
        }
        // not entrypoint
        return Optional.empty();
    }

    public String mermaid(MethodRelations methodRelations, JigTypes jigTypes) {

        var apiMethodRelationText = new StringJoiner("\n");

        Map<TypeIdentifier, Set<JigMethod>> serviceMethodMap = new HashMap<>();
        Map<String, String> methodLabelMap = new HashMap<>();

        MethodRelations springComponentMethodRelations = MethodRelations.filterSpringComponent(jigTypes, methodRelations).inlineLambda();

        entrypointMethod().forEach(entrypointMethod -> {
            // APIメソッドの名前と形
            var apiMethodMmdId = entrypointMethod.declaration().htmlIdText();
            String apiMethodLabel = entrypointMethod.jigMethod().labelText();

            var description = switch (entrypointMethod.entrypointType()) {
                case HTTP_API -> {
                    var httpEndpoint = HttpEndpoint.from(entrypointMethod);
                    apiMethodLabel = httpEndpoint.interfaceLabel();
                    yield "%s %s".formatted(httpEndpoint.method(), httpEndpoint.methodPath());
                }
                case QUEUE_LISTENER -> "queue: %s".formatted(QueueListener.from(entrypointMethod).queueName());
                default -> entrypointMethod.entrypointType().name();
            };
            // apiMethod
            apiMethodRelationText.add("    %s{{\"%s\"}}".formatted(apiMethodMmdId, apiMethodLabel));
            // path -> apiMethod
            String apiPointMmdId = "__" + apiMethodMmdId;
            apiMethodRelationText.add("    %s>\"%s\"] -.-> %s".formatted(apiPointMmdId, description, apiMethodMmdId));

            // apiMethod -> others...
            var decraleMethodRelations = springComponentMethodRelations.filterFromRecursive(entrypointMethod.declaration(),
                    // @Serviceのクラスについたら終了
                    jigMethodIdentifier -> jigTypes.isService(jigMethodIdentifier)
            );
            decraleMethodRelations.list()
                    .stream()
                    .map(methodRelation -> methodRelation.mermaidEdgeText())
                    .forEach(apiMethodRelationText::add);

            // Service表示＆リンクのための収集
            decraleMethodRelations.list()
                    .stream()
                    .map(MethodRelation::to)
                    .forEach(methodDeclaration -> {
                        if (jigTypes.isService(methodDeclaration)) {
                            jigTypes.resolveJigMethod(methodDeclaration.jigMethodIdentifier())
                                    .ifPresent(jigMethod -> {
                                        var key = methodDeclaration.declaringType();
                                        serviceMethodMap.computeIfAbsent(key, k -> new HashSet<>());
                                        serviceMethodMap.get(key).add(jigMethod);
                                    });
                        } else {
                            // controllerと同じクラスのメソッドはメソッド名だけ
                            if (entrypointMethod.typeIdentifier().equals(methodDeclaration.declaringType())) {
                                methodLabelMap.put(methodDeclaration.htmlIdText(), methodDeclaration.methodSignature().methodName());
                            } else {
                                // 他はクラス名+メソッド名
                                methodLabelMap.put(methodDeclaration.htmlIdText(), methodDeclaration.asSimpleText());
                            }
                        }
                    });
        });

        var mermaidText = new StringJoiner("\n");
        mermaidText.add("graph LR");
        // サービスメソッドをクラス単位にグループ化して名前解決＆クリックで遷移できるようにする
        serviceMethodMap.forEach((key, values) -> {
            mermaidText.add("    subgraph %s".formatted(key.asSimpleText()));
            values.forEach(jigMethod -> {
                mermaidText.add("    %s([\"%s\"])".formatted(jigMethod.htmlIdText(), jigMethod.labelText()));
                mermaidText.add("    click %s \"./usecase.html#%s\"".formatted(jigMethod.htmlIdText(), jigMethod.htmlIdText()));
            });
            mermaidText.add("    end");
        });
        // サービスメソッド以外のメソッド
        methodLabelMap.forEach((key, value) -> mermaidText.add("    %s[%s]".formatted(key, value)));

        mermaidText.add(apiMethodRelationText.toString());
        return mermaidText.toString();
    }
}
