package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.data.classes.type.JigType;
import org.dddjava.jig.domain.model.data.classes.type.JigTypes;
import org.dddjava.jig.domain.model.data.classes.type.TypeCategory;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelation;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;

import java.util.*;
import java.util.stream.Stream;

/**
 * エントリーポイントメソッドのグループ。
 * グルーピング単位はクラス。
 *
 * - SpringMVCのControllerのRequestMapping
 * - SpringRabbitのRabbitListener
 */
public record EntrypointGroup
        (JigType jigType, EntrypointKind entrypointKind, List<EntrypointMethod> entrypointMethod) {
    public EntrypointGroup {
        if (entrypointMethod.isEmpty()) throw new IllegalArgumentException("entrypointMethod is empty");
    }

    enum EntrypointKind {
        RequestHandler,
        Others
    }

    static Optional<EntrypointGroup> from(JigType jigType) {
        List<EntrypointMethod> entrypointMethods = collectHandlerMethod(jigType)
                .filter(entrypointMethod -> {
                    if (jigType.typeCategory() == TypeCategory.InputAdapter) {
                        return entrypointMethod.isRequestHandler();
                    } else if (jigType.typeCategory() == TypeCategory.BoundaryComponent) {
                        return entrypointMethod.isRabbitListener();
                    }
                    return false;
                })
                .toList();
        if (!entrypointMethods.isEmpty()) {
            return Optional.of(new EntrypointGroup(jigType, EntrypointKind.RequestHandler, entrypointMethods));
        }

        // not entrypoint
        return Optional.empty();
    }


    private static Stream<EntrypointMethod> collectHandlerMethod(JigType jigType) {
        return jigType.instanceMember().instanceMethods().stream()
                .map(jigMethod -> new EntrypointMethod(jigType, jigMethod));
    }

    public String mermaid(MethodRelations methodRelations, JigTypes jigTypes) {

        var apiMethodRelationText = new StringJoiner("\n");

        Map<TypeIdentifier, Set<MethodIdentifier>> serviceMethodMap = new HashMap<>();
        Map<String, String> methodLabelMap = new HashMap<>();

        MethodRelations springComponentMethodRelations = MethodRelations.filterSpringComponent(jigTypes, methodRelations).inlineLambda();

        entrypointMethod().forEach(entrypointMethod -> {
            // APIメソッドの名前と形
            var apiMethodMmdId = entrypointMethod.declaration().htmlIdText();
            var label = entrypointMethod.interfaceLabelText();
            apiMethodRelationText.add("    %s{{\"%s\"}}".formatted(apiMethodMmdId, label));

            // path -> apiMethod
            var description = entrypointMethod.interfacePointDescription();
            String apiPointMmdId = "__" + apiMethodMmdId;
            apiMethodRelationText.add("    %s>\"%s\"] -.-> %s".formatted(apiPointMmdId, description, apiMethodMmdId));

            // apiMethod -> others...
            var decraleMethodRelations = springComponentMethodRelations.filterFromRecursive(entrypointMethod.declaration(),
                    // @Serviceのクラスについたら終了
                    methodIdentifier -> jigTypes.isService(methodIdentifier)
            );
            decraleMethodRelations.list()
                    .stream()
                    .map(methodRelation -> methodRelation.mermaidEdgeText())
                    .forEach(apiMethodRelationText::add);

            // Service表示＆リンクのための収集
            decraleMethodRelations.list()
                    .stream()
                    .map(MethodRelation::to)
                    .map(MethodDeclaration::identifier)
                    .forEach(methodIdentifier -> {
                        if (jigTypes.isService(methodIdentifier)) {
                            var key = methodIdentifier.declaringType();
                            serviceMethodMap.computeIfAbsent(key, k -> new HashSet<>());
                            serviceMethodMap.get(key).add(methodIdentifier);
                        } else {
                            // controllerと同じクラスのメソッドはメソッド名だけ
                            if (entrypointMethod.typeIdentifier().equals(methodIdentifier.declaringType())) {
                                methodLabelMap.put(methodIdentifier.htmlIdText(), methodIdentifier.methodSignature().methodName());
                            } else {
                                // 他はクラス名+メソッド名
                                methodLabelMap.put(methodIdentifier.htmlIdText(), methodIdentifier.asSimpleText());
                            }
                        }
                    });
        });

        var mermaidText = new StringJoiner("\n");
        mermaidText.add("graph LR");
        // サービスメソッドをクラス単位にグループ化して名前解決＆クリックで遷移できるようにする
        serviceMethodMap.forEach((key, values) -> {
            mermaidText.add("    subgraph %s".formatted(key.asSimpleText()));
            values.forEach(value -> jigTypes.resolveJigMethod(value)
                    .ifPresent(jigMethod -> {
                        mermaidText.add("    %s([\"%s\"])".formatted(value.htmlIdText(), jigMethod.labelText()));
                        mermaidText.add("    click %s \"./usecase.html#%s\"".formatted(value.htmlIdText(), value.htmlIdText()));
                    }));
            mermaidText.add("    end");
        });
        // サービスメソッド以外のメソッド
        methodLabelMap.forEach((key, value) -> mermaidText.add("    %s[%s]".formatted(key, value)));

        mermaidText.add(apiMethodRelationText.toString());
        return mermaidText.toString();
    }

    public boolean isRequestHandler() {
        return entrypointKind == EntrypointKind.RequestHandler;
    }
}
