package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.services.ServiceAngle;
import org.dddjava.jig.domain.model.services.ServiceAngles;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

public class BooleanServiceTraceDiagram implements DotTextEditor<ServiceAngles> {

    private final JapaneseNameFinder japaneseNameFinder;
    private final MethodNodeLabelStyle methodNodeLabelStyle;

    public BooleanServiceTraceDiagram(JapaneseNameFinder japaneseNameFinder, MethodNodeLabelStyle methodNodeLabelStyle) {
        this.japaneseNameFinder = japaneseNameFinder;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
    }

    @Override
    public String edit(ServiceAngles model) {
        List<ServiceAngle> angles = model.returnsBooleanList();

        // メソッド間の関連
        RelationText relationText = new RelationText();
        for (ServiceAngle serviceAngle : angles) {
            for (MethodDeclaration methodDeclaration : serviceAngle.userMethods().list()) {
                relationText.add(methodDeclaration, serviceAngle.method());
            }
        }

        // booleanサービスメソッドの表示方法
        String labelText = angles.stream()
                .map(angle -> {
                    MethodDeclaration method = angle.method();
                    Node node = Node.of(method);
                    if (method.isLambda()) {
                        node.label("(lambda)").lambda();
                    } else {
                        // ラベルに和名をつける
                        node.label(japaneseNameLineOf(method) + methodNodeLabelStyle.apply(method, japaneseNameFinder));
                    }
                    return node.asText();
                }).collect(joining("\n"));

        // 使用メソッドのラベル
        String userMethodLabelText = angles.stream().flatMap(serviceAngle -> serviceAngle.userMethods().list().stream())
                .distinct()
                // booleanメソッドを除く
                .filter(userMethod -> angles.stream().noneMatch(serviceAngle -> serviceAngle.method().sameIdentifier(userMethod)))
                .map(userMethod ->
                        Node.of(userMethod)
                                .label(methodNodeLabelStyle.apply(userMethod, japaneseNameFinder))
                                .asText()
                ).collect(joining("\n"));

        // クラス名でグルーピングする
        String subgraphText = Stream.concat(angles.stream().map(ServiceAngle::method), angles.stream().flatMap(serviceAngle -> serviceAngle.userMethods().list().stream()))
                .collect(groupingBy(MethodDeclaration::declaringType))
                .entrySet().stream()
                .map(entry ->
                        "subgraph \"cluster_" + entry.getKey().fullQualifiedName() + "\""
                                + "{"
                                + "label=\"" + japaneseNameLineOf(entry.getKey()) + entry.getKey().asSimpleText() + "\";"
                                + entry.getValue().stream()
                                .map(MethodDeclaration::asFullNameText)
                                .map(text -> "\"" + text + "\";")
                                .collect(joining("\n"))
                                + "}")
                .collect(joining("\n"));
        // 凡例
        String legendText = new StringJoiner("\n", "subgraph cluster_legend {", "}")
                .add("label=凡例;")
                .add("メソッド;")
                .add(new Node("lambda").lambda().asText())
                .toString();

        String graphText = new StringJoiner("\n", "digraph JIG {", "}")
                .add("rankdir=LR;")
                .add("node [shape=box,style=filled,color=lightgoldenrod];")
                .add(relationText.asText())
                .add("/* labelText */")
                .add(labelText)
                .add("/* userMethodLabel */")
                .add(userMethodLabelText)
                .add("/* subgraphText */")
                .add(subgraphText)
                .add(legendText)
                .toString();

        return graphText;
    }

    private String japaneseNameLineOf(TypeIdentifier typeIdentifier) {
        String japaneseName = japaneseNameFinder.find(typeIdentifier).japaneseName().summarySentence();
        return japaneseName.isEmpty() ? "" : japaneseName + "\n";
    }

    private String japaneseNameLineOf(MethodDeclaration method) {
        String japaneseName = japaneseNameFinder.find(method.identifier()).japaneseName().summarySentence();
        return japaneseName.isEmpty() ? "" : japaneseName + "\n";
    }
}
