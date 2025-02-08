package org.dddjava.jig.adapter.html.mermaid;

import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.data.classes.method.JigMethodFinder;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.type.JigTypes;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

public record UsecaseMermaidDiagram(
        JigTypes contextJigTypes,
        MethodRelations methodRelations
) {

    public String textFor(JigMethod jigMethod) {
        JigMethodFinder jigMethodFinder = methodIdentifier -> contextJigTypes.resolveJigMethod(methodIdentifier);

        var mermaidText = new StringJoiner("\n");
        mermaidText.add("graph LR");

        // 基点からの呼び出し全部 + 直近の呼び出し元
        var filteredRelations = methodRelations.filterFromRecursive(jigMethod.declaration(), methodIdentifier -> false)
                .merge(methodRelations.filterTo(jigMethod.declaration()));

        Set<MethodIdentifier> resolved = new HashSet<>();

        // メソッドのスタイル
        filteredRelations.methodIdentifiers().forEach(methodIdentifier -> {
            // 自分は太字にする
            if (methodIdentifier.equals(jigMethod.declaration().identifier())) {
                resolved.add(methodIdentifier);
                mermaidText.add(usecaseMermaidNodeText(jigMethod));
                mermaidText.add("style %s font-weight:bold".formatted(jigMethod.htmlIdText()));
            } else {
                jigMethodFinder.find(methodIdentifier)
                        .ifPresent(method -> {
                            resolved.add(methodIdentifier);
                            if (method.remarkable()) {
                                // 出力対象のメソッドはusecase型＆クリックできるように
                                mermaidText.add(usecaseMermaidNodeText(method));
                                mermaidText.add("click %s \"#%s\"".formatted(method.htmlIdText(), method.htmlIdText()));
                            } else {
                                // remarkableでないものは普通の。privateメソッドなど該当。　
                                mermaidText.add(normalMermaidNodeText(method));
                            }
                        });
            }
        });

        Set<TypeIdentifier> others = new HashSet<>();

        Function<MethodDeclaration, Optional<String>> converter = methodDeclaration -> {
            if (resolved.contains(methodDeclaration.identifier())) {
                return Optional.of(methodDeclaration.htmlIdText());
            }
            // 解決できなかったものは関心が薄いとして、メソッドではなくクラスとして解釈し
            var typeIdentifier = methodDeclaration.declaringType();
            if (typeIdentifier.packageIdentifier().equals(jigMethod.declaration().declaringType().packageIdentifier())) {
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
        if (jigMethod.declaration().isLambda()) {
            return "%s[\"%s\"]:::lambda".formatted(jigMethod.htmlIdText(), "(lambda)");
        }
        return "%s[\"%s\"]".formatted(jigMethod.htmlIdText(), jigMethod.labelText());
    }

    private String usecaseMermaidNodeText(JigMethod jigMethod) {
        return "%s([\"%s\"])".formatted(jigMethod.htmlIdText(), jigMethod.labelTextOrLambda());
    }
}
