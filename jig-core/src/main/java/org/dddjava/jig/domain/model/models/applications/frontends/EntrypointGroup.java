package org.dddjava.jig.domain.model.models.applications.frontends;

import org.dddjava.jig.domain.model.models.applications.services.ServiceMethods;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.*;

/**
 * エントリーポイントメソッドのグループ。
 * グルーピング単位はクラス。
 *
 * - SpringMVCのControllerのRequestMapping
 * - SpringRabbitのRabbitListener
 */
public record EntrypointGroup
        (JigType jigType, HandlerMethods handlerMethods) {

    static EntrypointGroup from(JigType jigType) {
        return new EntrypointGroup(jigType, HandlerMethods.from(jigType));
    }

    public boolean hasEntrypoint() {
        return !handlerMethods().empty();
    }

    String mermaid(ServiceMethods serviceMethods) {

        var apiMethodRelationText = new StringJoiner("\n");

        var serviceMethodMap = new HashMap<TypeIdentifier, List<JigMethod>>();
        var apiPointMmdIds = new HashSet<String>();

        handlerMethods().list().forEach(entrypointMethod -> {
            // APIメソッドの名前と形
            var apiMethodMmdId = entrypointMethod.declaration().asSimpleText();
            var label = entrypointMethod.interfaceLabelText();
            apiMethodRelationText.add("    %s{{\"%s\"}}".formatted(apiMethodMmdId, label));

            // path -> apiMethod
            var description = entrypointMethod.interfacePointDescription();
            String apiPointMmdId = "__" + apiMethodMmdId;
            apiMethodRelationText.add("    %s>\"%s\"] -.-> %s".formatted(apiPointMmdId, description, apiMethodMmdId));
            apiPointMmdIds.add(apiPointMmdId);

            // APIメソッドからServiceへの関連
            entrypointMethod.usingMethods().methodDeclarations().list()
                    .stream()
                    .map(serviceMethods::find)
                    .flatMap(Optional::stream)
                    .forEach(usingJigMethod -> {
                        var key = usingJigMethod.declaration().declaringType();
                        serviceMethodMap.computeIfAbsent(key, k -> new ArrayList<>());
                        serviceMethodMap.get(key).add(usingJigMethod);

                        // apiMethod -> serviceMethod
                        apiMethodRelationText.add("    %s --> %s".formatted(apiMethodMmdId, usingJigMethod.declaration().asSimpleText()));
                    });
        });

        var mermaidText = new StringJoiner("\n");
        mermaidText.add("graph LR");
        // サービスメソッドの形を整える
        serviceMethodMap.forEach((key, values) -> {
            mermaidText.add("    subgraph %s".formatted(key.asSimpleText()));
            values.forEach(value -> {
                var methodMmdId = value.declaration().asSimpleText();
                mermaidText.add("    %s([\"%s\"])".formatted(methodMmdId, value.labelText()));
            });
            mermaidText.add("    end");
        });

        // api classでグルーピング
        var jigType = jigType();
        mermaidText.add("    subgraph %s[\"%s\"]".formatted(jigType.simpleName(), jigType.interfaceLabelText()));

        // classのRequestMappingのパスからメソッドのRequestMappingのパスにつなげる
        jigType.interfacePointDescription().ifPresent(point -> {
            mermaidText.add("    __>\"%s\"]".formatted(point));
            apiPointMmdIds.forEach(apiPointMmdId -> {
                mermaidText.add("    __ -.-> " + apiPointMmdId);
            });
        });

        mermaidText.add(apiMethodRelationText.toString());
        mermaidText.add("    end");

        return mermaidText.toString();
    }
}
