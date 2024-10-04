package org.dddjava.jig.domain.model.models.applications.frontends;

import org.dddjava.jig.domain.model.models.applications.services.ServiceMethods;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.*;

public record Entrypoint(List<EntrypointGroup> list, ServiceMethods serviceMethods) {

    public Entrypoint(JigTypes jigTypes, ServiceMethods serviceMethods) {
        this(jigTypes.list().stream()
                        .map(jigType -> EntrypointGroup.from(jigType))
                        .filter(entrypointGroup -> entrypointGroup.hasEntrypoint())
                        .toList(),
                serviceMethods);
    }

    public Map<String, String> mermaidMap() {
        var map = new HashMap<String, String>();

        for (EntrypointGroup entrypointGroup : list()) {
            var jigType = entrypointGroup.jigType();
            var mermaidText = externalApiMermaidText(entrypointGroup);
            map.put(jigType.fqn(), mermaidText);
        }

        return map;
    }

    private String externalApiMermaidText(EntrypointGroup entrypointGroup) {
        var externalApiMethods = entrypointGroup.handlerMethods().list();

        var apiMethodRelationText = new StringJoiner("\n");

        var serviceMethodMap = new HashMap<TypeIdentifier, List<JigMethod>>();
        var apiPointMmdIds = new HashSet<String>();

        externalApiMethods.forEach(apiMethod -> {
            // APIメソッドの名前と形
            var apiMethodMmdId = apiMethod.declaration().asSimpleText();
            var label = apiMethod.interfaceLabelText();
            apiMethodRelationText.add("    %s{{\"%s\"}}".formatted(apiMethodMmdId, label));

            // path -> apiMethod
            var description = apiMethod.interfacePointDescription();
            String apiPointMmdId = "__" + apiMethodMmdId;
            apiMethodRelationText.add("    %s>\"%s\"] -.-> %s".formatted(apiPointMmdId, description, apiMethodMmdId));
            apiPointMmdIds.add(apiPointMmdId);

            // APIメソッドからServiceへの関連
            apiMethod.usingMethods().methodDeclarations().list()
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
        var jigType = entrypointGroup.jigType();
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
