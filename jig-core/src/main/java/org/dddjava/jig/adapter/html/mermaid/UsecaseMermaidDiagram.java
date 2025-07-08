package org.dddjava.jig.adapter.html.mermaid;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

public record UsecaseMermaidDiagram(
        JigTypes contextJigTypes,
        MethodRelations methodRelations
) {

    public String textFor(JigMethod jigMethod) {
        var mermaidText = new StringJoiner("\n");
        mermaidText.add("graph LR");

        // 基点からの呼び出し全部 + 直近の呼び出し元
        var filteredRelations = methodRelations.filterFromRecursive(jigMethod.jigMethodId())
                .merge(methodRelations.filterTo(jigMethod.jigMethodId()));

        // 解決済み（Usecaseメソッドに含まれるもの）を識別するためのコレクション
        // filteredRelationsに問い合わせればいい気もする
        Set<JigMethodId> resolved = new HashSet<>();

        // メソッドのスタイル
        filteredRelations.toJigMethodIdStream().forEach(jigMethodId -> {
            // 自分は太字にする
            if (jigMethodId.equals(jigMethod.jigMethodId())) {
                resolved.add(jigMethodId);
                mermaidText.add(usecaseMermaidNodeText(jigMethod));
                mermaidText.add("style %s font-weight:bold".formatted(htmlIdText(jigMethod.jigMethodId())));
            } else {
                contextJigTypes.resolveJigMethod(jigMethodId)
                        .ifPresent(method -> {
                            resolved.add(jigMethodId);
                            if (method.remarkable()) {
                                // 出力対象のメソッドはusecase型＆クリックできるように
                                mermaidText.add(usecaseMermaidNodeText(method));
                                var htmlIdText = htmlIdText(method.jigMethodId());
                                mermaidText.add("click %s \"#%s\"".formatted(htmlIdText, htmlIdText));
                            } else {
                                // remarkableでないものは普通の。privateメソッドなど該当。　
                                mermaidText.add(normalMermaidNodeText(method));
                            }
                        });
            }
        });

        Set<TypeId> others = new HashSet<>();

        Function<JigMethodId, Optional<String>> converter = jigMethodId -> {
            // 解決済みのメソッドは出力済みなので、Mermaid上のIDだけでよい
            if (resolved.contains(jigMethodId)) {
                return Optional.of(htmlIdText(jigMethodId));
            }
            // 解決できなかったものは関心が薄いとして、メソッドではなくクラスとして解釈し
            var typeIdentifier = jigMethodId.tuple().declaringTypeId();
            if (typeIdentifier.packageId().equals(jigMethod.declaringType().packageId())) {
                // 暫定的に同じパッケージのもののみ出力する
                // Serviceの場合に出力したいのはControllerやRepositoryになるので、気が向いたらなんとかする
                others.add(typeIdentifier);
                return Optional.of(typeIdentifier.htmlIdText());
            } else {
                return Optional.empty();
            }
        };
        mermaidText.add(filteredRelations.mermaidEdgeText(converter));

        // JigMethodにならないものはクラスノードとして出力する
        others.forEach(typeIdentifier ->
                mermaidText.add("%s[%s]:::others".formatted(typeIdentifier.htmlIdText(), typeIdentifier.asSimpleText())));

        mermaidText.add("classDef others fill:#AAA,font-size:90%;");
        mermaidText.add("classDef lambda fill:#999,font-size:80%;");

        return mermaidText.toString();
    }

    private String normalMermaidNodeText(JigMethod jigMethod) {
        var jigMethodIdentifier = jigMethod.jigMethodId();
        var string = htmlIdText(jigMethodIdentifier);
        if (jigMethodIdentifier.isLambda()) {
            return "%s[\"%s\"]:::lambda".formatted(string, "(lambda)");
        }
        return "%s[\"%s\"]".formatted(string, jigMethod.labelText());
    }

    private String usecaseMermaidNodeText(JigMethod jigMethod) {
        return "%s([\"%s\"])".formatted(htmlIdText(jigMethod.jigMethodId()), jigMethod.labelTextOrLambda());
    }

    private static String htmlIdText(JigMethodId jigMethodId) {
        var tuple = jigMethodId.tuple();

        var typeText = tuple.declaringTypeId().packageAbbreviationText();
        var parameterText = tuple.parameterTypeIdentifiers().stream()
                .map(TypeId::packageAbbreviationText)
                .collect(Collectors.joining(", ", "(", ")"));
        return (typeText + '.' + tuple.name() + parameterText).replaceAll("[^a-zA-Z0-9]", "_");
    }
}
