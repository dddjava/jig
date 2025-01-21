package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.data.classes.method.MethodRelation;
import org.dddjava.jig.domain.model.data.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.jigobject.class_.TypeCategory;

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
        if (jigType.typeCategory() == TypeCategory.RequestHandler) {
            return Optional.of(new EntrypointGroup(jigType, EntrypointKind.RequestHandler,
                    collectHandlerMethod(jigType).filter(EntrypointMethod::isRequestHandler).toList()));
        } else if (jigType.typeCategory() == TypeCategory.FrameworkComponent) {
            return Optional.of(new EntrypointGroup(jigType, EntrypointKind.RequestHandler,
                    collectHandlerMethod(jigType).filter(EntrypointMethod::isRabbitListener).toList()));
        }

        // not entrypoint
        return Optional.empty();
    }


    private static Stream<EntrypointMethod> collectHandlerMethod(JigType jigType) {
        return jigType.instanceMember().instanceMethods().stream()
                .map(jigMethod -> new EntrypointMethod(jigType, jigMethod));
    }

    String mermaid(MethodRelations methodRelations, JigTypes jigTypes) {

        var apiMethodRelationText = new StringJoiner("\n");

        Map<TypeIdentifier, Set<MethodIdentifier>> serviceMethodMap = new HashMap<>();
        Map<String, String> methodLabelMap = new HashMap<>();

        MethodRelations springComponentMethodRelations = jigTypes.filterSpringComponent(methodRelations).inlineLambda();

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
                    methodIdentifier -> jigTypes.isApplication(methodIdentifier)
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
                        if (jigTypes.isApplication(methodIdentifier)) {
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
