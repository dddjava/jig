package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.services.ServiceAngle;
import org.dddjava.jig.domain.model.services.ServiceAngles;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

public class ServiceMethodCallDiagram implements DotTextEditor<ServiceAngles> {

    final JapaneseNameFinder japaneseNameFinder;
    final MethodNodeLabelStyle methodNodeLabelStyle;

    public ServiceMethodCallDiagram(JapaneseNameFinder japaneseNameFinder, MethodNodeLabelStyle methodNodeLabelStyle) {
        this.japaneseNameFinder = japaneseNameFinder;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
    }

    @Override
    public DotTexts edit(ServiceAngles serviceAngles) {
        if (serviceAngles.isEmpty()) {
            return new DotTexts(Collections.singletonList(DotText.empty()));
        }

        List<ServiceAngle> angles = serviceAngles.list();

        // メソッド間の関連
        RelationText relationText = new RelationText();
        for (ServiceAngle serviceAngle : angles) {
            for (MethodDeclaration methodDeclaration : serviceAngle.userServiceMethods().list()) {
                relationText.add(methodDeclaration, serviceAngle.method());
            }
        }

        // メソッドの表示方法
        String labelText = angles.stream()
                .map(angle -> {
                    MethodDeclaration method = angle.method();
                    Node node = Node.of(method);
                    if (method.isLambda()) {
                        node.label("(lambda)").lambda();
                    } else {
                        // ラベルに和名をつける
                        node.label(japaneseNameLineOf(method) + methodNodeLabelStyle.apply(method, japaneseNameFinder));

                        // 非publicは色なし
                        if (angle.isNotPublicMethod()) {
                            node.notPublicMethod();
                        }

                        // ハンドラを強調
                        if (angle.usingFromController()) {
                            node.handlerMethod();
                        }
                    }
                    return node.asText();
                }).collect(joining("\n"));

        // クラス名でグルーピングする
        String subgraphText = angles.stream()
                .collect(groupingBy(serviceAngle -> serviceAngle.method().declaringType()))
                .entrySet().stream()
                .map(entry ->
                        "subgraph \"cluster_" + entry.getKey().fullQualifiedName() + "\""
                                + "{"
                                + "label=\"" + japaneseNameLineOf(entry.getKey()) + entry.getKey().asSimpleText() + "\";"
                                + entry.getValue().stream()
                                .map(serviceAngle -> serviceAngle.method().asFullNameText())
                                .map(text -> "\"" + text + "\";")
                                .collect(joining("\n"))
                                + "}")
                .collect(joining("\n"));


        // 凡例
        String legendText = new StringJoiner("\n", "subgraph cluster_legend {", "}")
                .add("label=凡例;")
                .add(new Node("ハンドラメソッド").handlerMethod().asText())
                .add("通常のメソッド;")
                .add(new Node("非publicメソッド").notPublicMethod().asText())
                .add(new Node("lambda").lambda().asText())
                .toString();

        String graphText = new StringJoiner("\n", "digraph JIG {", "}")
                .add("rankdir=LR;")
                .add("node [shape=box,style=filled,color=lightgoldenrod];")
                .add(relationText.asText())
                .add(labelText)
                .add(subgraphText)
                .add(legendText)
                .toString();

        return new DotTexts(graphText);
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
